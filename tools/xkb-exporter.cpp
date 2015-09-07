/*
 * compile: g++ -std=c++11 xkb-exporter.cpp -o xkb-exporter -lX11 -lxkbfile -ltinyxml
 */
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <unistd.h>
#include <sys/stat.h>

#include <string>
#include <iomanip>
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <map>
#include <regex>

#include <X11/XKBlib.h>
#include <X11/extensions/XKBrules.h>

#include <tinyxml.h>

#define DEFAULT_EXPORT_DIR "export"

using namespace std;

typedef struct Unicode {
    string name;
    int value;
} Unicode;

typedef struct Keysym {
    string name;
    int value;
    Unicode unicode;
} Keysym;

typedef struct Keycode {
    int value;
    vector<Keysym> keysyms;
} Keycode;

typedef struct Variant {
    string name;
    string description;
} Variant;

typedef struct Layout {
    string name;
    string description;
    vector<Variant> variants;
} Layout;

typedef struct LayoutInfo {
    string layout;
    string variant;
} LayoutInfo;

typedef enum State {
    NONE,
    MODELS,
    LAYOUTS,
    VARIANTS,
    OPTIONS
} State;

static string executableName;
static int deviceId = XkbUseCoreKbd;
static string layout;
static string variant;
static string exportDir = DEFAULT_EXPORT_DIR;
static bool listKeycodes = false;
static bool listLayouts = false;
static bool listKeysyms = false;
static bool exportOne = false;
static bool exportAll = false;
static bool print = false;

static Display* display;

static int execProcess(string command, string& out) {
    FILE* outFd;

    outFd = popen(command.c_str(), "r");
    if(!outFd) {
        return -1;
    }

    char buffer[512];
    while(fgets(buffer, sizeof(buffer), outFd) != NULL) {
        out.append(buffer);
    }

    return pclose(outFd);
}

static bool setLayout(const LayoutInfo& layoutInfo) {
    string cmd = "setxkbmap -layout " + layoutInfo.layout;

    if (!layoutInfo.variant.empty()) {
        cmd.append(" -variant " + variant);
    }

    string out;
    int ret = execProcess(cmd, out);
    if (ret != 0) {
        cerr <<"cannot set keyboard layout (layout: " <<layoutInfo.layout <<", " <<layoutInfo.variant <<")'\n";
        return false;
    }

    return true;
}

static vector<Keycode> getKeycodeList() {
    vector<Keycode> keycodes;

    XkbDescPtr keyboardMap = XkbGetMap(display, XkbAllClientInfoMask, deviceId);
    for(KeyCode k = keyboardMap->min_key_code; k < keyboardMap->max_key_code; k++) {
        Keycode keycode;
        keycode.value = k;

        //retrieve all keysyms associated with group 0 at all possible shift levels
        unsigned int group = 0;
        unsigned char keysymShiftLevelMax = XkbKeyGroupWidth(keyboardMap, k, group);
        int keysymShiftLevel;
        for (keysymShiftLevel = 0; keysymShiftLevel < keysymShiftLevelMax; keysymShiftLevel++) {
            Keysym keysym;
            keysym.value = XkbKeySymEntry(keyboardMap, k, keysymShiftLevel, group);
            keycode.keysyms.push_back(keysym);
        }

        keycodes.push_back(keycode);
    }

    return keycodes;
}

static map<string, Layout> getLayoutList() {
    map<string, Layout> layouts;

    string filepath = "/usr/share/X11/xkb/rules/xorg.xml";
    TiXmlDocument document(filepath);
    if (!document.LoadFile()) {
        cerr <<"cannot load file '" <<filepath <<"'\n";
    }
    else {
        TiXmlElement* rootNode = document.FirstChildElement("xkbConfigRegistry");
        if (rootNode) {
            TiXmlElement* layoutListNode = rootNode->FirstChildElement("layoutList");
            if (layoutListNode) {
                for (TiXmlElement* layoutNode = layoutListNode->FirstChildElement("layout"); layoutNode; layoutNode = layoutNode->NextSiblingElement("layout")) {
                    TiXmlElement* configItemNode = layoutNode->FirstChildElement("configItem");
                    if (configItemNode) {
                        Layout layout;

                        TiXmlElement* nameNode = configItemNode->FirstChildElement("name");
                        if (nameNode) {
                            layout.name = nameNode->GetText();
                        }

                        TiXmlElement* descriptionNode = configItemNode->FirstChildElement("description");
                        if (descriptionNode) {
                            layout.description = descriptionNode->GetText();
                        }

                        TiXmlElement* variantListNode = layoutNode->FirstChildElement("variantList");
                        if (variantListNode) {
                            for (TiXmlElement* variantNode = variantListNode->FirstChildElement("variant"); variantNode; variantNode = variantNode->NextSiblingElement("variant")) {
                                TiXmlElement* configItemNode = variantNode->FirstChildElement("configItem");
                                if (configItemNode) {
                                    Variant variant;

                                    TiXmlElement* nameNode = configItemNode->FirstChildElement("name");
                                    if (nameNode) {
                                        variant.name = nameNode->GetText();
                                    }

                                    TiXmlElement* descriptionNode = configItemNode->FirstChildElement("description");
                                    if (descriptionNode) {
                                        variant.description = descriptionNode->GetText();
                                    }

                                    layout.variants.push_back(variant);
                                }
                            }
                        }

                        layouts[layout.name] = layout;
                    }
                }
            }
        }
    }

    return layouts;
}

static vector<Keysym> getKeysymList() {
    //open file
    string filepath = "/usr/include/X11/keysymdef.h";
    ifstream fs(filepath.c_str());

    vector<Keysym> keysyms;

    regex regex("#define XK_(\\w+)\\s+(0x[0-9a-fA-F]*)(\\s*\\/\\*\\s*U\\+([0-9a-fA-F]{4})\\s+(.+?)\\s*\\*\\/)?");

    //iterate over lines
    string line;
    while (getline(fs, line)) {
        if (line.empty()) {
            continue;
        }

        smatch match;
        if(regex_search(line, match, regex)) {
            Keysym keysym;

            keysym.name = match[1];
            sscanf(string(match[2]).c_str(), "%x", &keysym.value);

            if (!string(match[4]).empty() && !string(match[5]).empty()) {
                keysym.unicode.name = match[5];
                sscanf(string(match[4]).c_str(), "%x", &keysym.unicode.value);
            } else {
                keysym.unicode.value = -1;
            }

            keysyms.push_back(keysym);
        }
    }

    fs.close();

    return keysyms;
}

static LayoutInfo getCurrentLayoutInfo() {
    LayoutInfo layoutInfo;
    XkbRF_VarDefsRec vd;
    char *tmp;

    XkbRF_GetNamesProp(display, &tmp, &vd);

    if (vd.layout != NULL) {
        layoutInfo.layout = vd.layout;
    }

    if (vd.variant != NULL) {
        layoutInfo.variant = vd.variant;
    }

    size_t pos;
    pos = layoutInfo.layout.find(',');
    if (pos != string::npos) {
        layoutInfo.layout.erase(pos);
    }

    pos = layoutInfo.variant.find(',');
    if (pos != string::npos) {
        layoutInfo.variant.erase(pos);
    }

    return layoutInfo;
}

static bool getKeycodeListXml(TiXmlDocument& document) {
    map<string, Layout> layouts = getLayoutList();
    LayoutInfo layoutInfo = getCurrentLayoutInfo();

    map<string, Layout>::iterator it = layouts.find(layoutInfo.layout);
    if (it == layouts.end()) {
        cerr <<"cannot find keyboard layout '" <<layoutInfo.layout <<"'\n";
        return false;
    }

    Layout layout = it->second;
    Variant variant;
    if (!layoutInfo.variant.empty()) {
        vector<Variant>& variants = layout.variants;
        for(unsigned int i = 0; i < variants.size(); i++) {
            Variant& v = variants[i];
            if (layoutInfo.variant == v.name) {
                variant = v;
            }
        }

        if (variant.name.empty()) {
            cerr <<"cannot find keyboard layout variant '" <<layoutInfo.variant <<"'\n";
            return false;
        }
    }

    vector<Keycode> keycodes = getKeycodeList();

    //keycodes
    TiXmlElement* keycodesNode = new TiXmlElement("keycodes");
    keycodesNode->SetAttribute("layoutName", layout.name);
    keycodesNode->SetAttribute("layoutDescription", layout.description);
    keycodesNode->SetAttribute("variantName", variant.name);
    keycodesNode->SetAttribute("variantDescription", variant.description);
    document.LinkEndChild(keycodesNode);

    //iterate over keycodes
    for(unsigned int i = 0; i < keycodes.size(); i++) {
        Keycode keycode = keycodes[i];

        TiXmlElement* keycodeNode = new TiXmlElement("keycodes");
        keycodeNode->SetAttribute("value", keycode.value);
        keycodesNode->LinkEndChild(keycodeNode);

        //iterate over keysyms
        vector<Keysym> keysyms = keycode.keysyms;
        for(unsigned int j = 0; j < keysyms.size(); j++) {
            Keysym keysym = keysyms[j];

            TiXmlElement* keysymNode = new TiXmlElement("keycodes");
            keysymNode->SetAttribute("value", keysym.value);
            keycodeNode->LinkEndChild(keysymNode);
        }
    }

    return true;
}

static bool getLayoutListXml(TiXmlDocument& document) {
    map<string, Layout> layouts = getLayoutList();

    //layouts
    TiXmlElement* layoutsNode = new TiXmlElement("layouts");
    document.LinkEndChild(layoutsNode);

    //iterate over layouts
    for (map<string, Layout>::iterator it = layouts.begin(); it != layouts.end(); it++) {
        Layout& layout = it->second;

        //layout
        TiXmlElement* layoutNode = new TiXmlElement("layout");
        layoutsNode->LinkEndChild(layoutNode);

        TiXmlElement* nameNode = new TiXmlElement("name");
        nameNode->LinkEndChild(new TiXmlText(layout.name));
        layoutNode->LinkEndChild(nameNode);

        TiXmlElement* descriptionNode = new TiXmlElement("description");
        descriptionNode->LinkEndChild(new TiXmlText(layout.description));
        layoutNode->LinkEndChild(descriptionNode);

        //iterate over variants
        vector<Variant> variants = layout.variants;
        TiXmlElement* variantsNode = new TiXmlElement("variants");
        layoutNode->LinkEndChild(variantsNode);
        for (unsigned int i = 0; i < variants.size(); i++) {
            Variant variant = variants[i];

            TiXmlElement* variantNode = new TiXmlElement("variant");
            variantsNode->LinkEndChild(variantNode);

            TiXmlElement* nameNode = new TiXmlElement("name");
            nameNode->LinkEndChild(new TiXmlText(variant.name));
            variantNode->LinkEndChild(nameNode);

            TiXmlElement* descriptionNode = new TiXmlElement("description");
            descriptionNode->LinkEndChild(new TiXmlText(variant.description));
            variantNode->LinkEndChild(descriptionNode);
        }
    }

    return true;
}

static bool getKeysymListXml(TiXmlDocument& document) {
    vector<Keysym> keysyms = getKeysymList();

    //keysyms
    TiXmlElement* keysymsNode = new TiXmlElement("keysyms");
    document.LinkEndChild(keysymsNode);

    //iterate over keysyms
    for (unsigned int i = 0; i < keysyms.size(); i++) {
        Keysym keysym = keysyms[i];

        TiXmlElement* keysymNode = new TiXmlElement("keysym");
        keysymNode->SetAttribute("value", keysym.value);
        keysymNode->SetAttribute("name", keysym.name);
        keysymsNode->LinkEndChild(keysymNode);

        if (keysym.unicode.value > -1) {
            TiXmlElement* unicodeNode = new TiXmlElement("unicode");
            unicodeNode->SetAttribute("value", keysym.unicode.value);
            unicodeNode->SetAttribute("name", keysym.unicode.name);
            keysymNode->LinkEndChild(unicodeNode);
        }
    }

    return true;
}

static bool printKeycodeList() {
    TiXmlDocument document;
    if (!getKeycodeListXml(document)) {
        return false;
    }

    TiXmlPrinter printer;
    printer.SetIndent("  ");
    document.Accept(&printer);

    cout <<printer.CStr();

    return true;
}

static bool printLayoutList() {
    TiXmlDocument document;
    if (!getLayoutListXml(document)) {
        return false;
    }

    TiXmlPrinter printer;
    printer.SetIndent("  ");
    document.Accept(&printer);

    cout <<printer.CStr();

    return true;
}

static bool printKeysymList() {
    TiXmlDocument document;
    if (!getKeysymListXml(document)) {
        return false;
    }

    TiXmlPrinter printer;
    printer.SetIndent("  ");
    document.Accept(&printer);

    cout <<printer.CStr();

    return true;
}

static bool printCurrentLayoutInfo() {
    LayoutInfo layoutInfo = getCurrentLayoutInfo();

    cout <<"layout: " <<layoutInfo.layout <<"\n";
    cout <<"variant: " <<layoutInfo.variant <<"\n";

    return true;
}

static void printUsage() {
    cout <<"usage: " <<executableName <<" [options]\n\n";
    cout <<"options:\n";
    cout <<"     --device <id>           define the input device [Default: core input device]\n";
    cout <<"     --layout <name>         set the layout\n";
    cout <<"     --variant <name>        set the layout variant\n";
    cout <<"     --list-keycodes         list all keycodes and their keysyms\n";
    cout <<"     --list-layouts          list all keyboard layouts and their variants\n";
    cout <<"     --list-keysyms          list all keysyms, their values and unicodes\n";
    cout <<"     --export                export keycodes of the current keyboard layout\n";
    cout <<"     --export-all            export keycodes of all keyboard layouts\n";
    cout <<"     --export-dir            define the export directory [Default: " <<DEFAULT_EXPORT_DIR <<"]\n";
    cout <<"     --print                 print current layout\n";
}

static void parseArguments(int argc, char** argv) {
    static struct option options[] = {
        {"device", required_argument, 0, 'c'},
        {"layout", required_argument, 0, 'l'},
        {"variant", required_argument, 0, 'v'},
        {"list-keycodes", no_argument, 0, 'k'},
        {"list-layouts", no_argument, 0, 'i'},
        {"list-keysyms", no_argument, 0, 's'},
        {"export", no_argument, 0, 'e'},
        {"export-all", no_argument, 0, 'a'},
        {"export-dir", required_argument, 0, 'd'},
        {"print", no_argument, 0, 'p'},
        {0, 0, 0, 0}
    };

    executableName = argv[0];

    int opt;
    while ((opt = getopt_long(argc, argv, "", options, NULL)) != -1) {
        switch(opt) {
        case 'c':
            deviceId = strtol(optarg, NULL, 10);
            break;
        case 'l':
            layout = optarg;
            break;
        case 'v':
            variant = optarg;
            break;
        case 'k':
            listKeycodes = true;
            break;
        case 'i':
            listLayouts = true;
            break;
        case 's':
            listKeysyms = true;
            break;
        case 'e':
            exportOne = true;
            break;
        case 'a':
            exportAll = true;
            break;
        case 'd':
            exportDir = optarg;
            break;
        case 'p':
            print = true;
            break;
        }
    }
}

static bool exportLayout(LayoutInfo layoutInfo) {
    string cmd = "mkdir -p " + exportDir;
    int ret = system(cmd.c_str());
    if (ret != 0) {
        cout <<"cannot create export directory '" <<exportDir <<"'\n";
        return false;
    }

    if (!setLayout(layoutInfo)) {
        return false;
    }

    TiXmlDocument document;
    if (!getKeycodeListXml(document)) {
        return false;
    }

    string filename = exportDir  + "/" + layoutInfo.layout;
    if (!layoutInfo.variant.empty()) {
        filename += "-" + layoutInfo.variant;
    }

    if (!document.SaveFile(filename)) {
        cerr <<"cannot export to file '" <<filename <<"'\n";
        return false;
    }

    return true;
}

static bool exportCurrentLayout() {
    LayoutInfo layoutInfo = getCurrentLayoutInfo();
    return exportLayout(layoutInfo);
}

static bool exportAllLayouts() {
    map<string, Layout> layouts = getLayoutList();

    //iterate over layouts
    for (map<string, Layout>::iterator it = layouts.begin(); it != layouts.end(); it++) {
        Layout layout = it->second;

        LayoutInfo layoutInfo;
        layoutInfo.layout = layout.name;
        layoutInfo.variant = "";

        exportLayout(layoutInfo);

        //iterate over variants
        vector<Variant> variants = layout.variants;
        for (unsigned int i = 0; i < variants.size(); i++) {
            Variant variant = variants[i];

            layoutInfo.variant = variant.name;
            exportLayout(layoutInfo);
        }
    }

    return true;
}

int main(int argc, char** argv) {
    parseArguments(argc, argv);

    display = XOpenDisplay(NULL);
    if(display == NULL) {
        cerr <<"cannot open display\n";
        return EXIT_FAILURE;
    }

    if (!layout.empty()) {
        LayoutInfo layoutInfo;
        layoutInfo.layout = layout;
        layoutInfo.variant = variant;
        if (!setLayout(layoutInfo)) {
            exit(1);
        }
    }

    if (listKeycodes) {
        if (!printKeycodeList()) {
            exit(1);
        }
    } else if (listLayouts) {
        if (!printLayoutList()) {
            exit(1);
        }
    } else if (listKeysyms) {
        if (!printKeysymList()) {
            exit(1);
        }
    } else if (exportOne) {
        if (!exportCurrentLayout()) {
            exit(1);
        }
    } else if (exportAll) {
        if (!exportAllLayouts()) {
            exit(1);
        }
    } else if (print) {
        if (!printCurrentLayoutInfo()) {
            exit(1);
        }
    } else if (layout.empty()) {
        printUsage();
    }

    XCloseDisplay(display);

    return EXIT_SUCCESS;
}
