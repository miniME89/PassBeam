#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>

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

string executableName;

bool listKeycodes = false;
bool listLayouts = false;
bool listKeysyms = false;
bool print = false;

Display* display;

vector<Keycode> getKeycodeList() {
    vector<Keycode> keycodes;

    XkbDescPtr keyboardMap = XkbGetMap(display, XkbAllClientInfoMask, XkbUseCoreKbd);
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

map<string, Layout> getLayoutList() {
    //open file
    string filepath = "/usr/share/X11/xkb/rules/base.lst";
    std::ifstream fs(filepath.c_str());

    map<string, Layout> layouts;

    //iterate over lines
    State state = NONE;
    string line;
    while (getline(fs, line)) {
        if (line.empty()) {
            continue;
        }

        if (line[0] == '!') {
            if (line.find("! model") == 0)
                state = MODELS;
            else if (line.find("! layout") == 0)
                state = LAYOUTS;
            else if (line.find("! variant") == 0)
                state = VARIANTS;
            else if (line.find("! option") == 0)
                state = OPTIONS;
            else
                state = NONE;

            continue;
        }

        if (state == LAYOUTS) {
            string layoutName;
            string layoutDescription;

            std::istringstream ss(line);
            ss >>layoutName >>skipws;
            getline(ss, layoutDescription);

            layoutDescription.erase(0, layoutDescription.find_first_not_of(" "));

            Layout& layout = layouts[layoutName];
            layout.name = layoutName;
            layout.description = layoutDescription;
        }
        else if (state == VARIANTS) {
            string variantName;
            string layoutName;
            string variantDescription;

            std::istringstream ss(line);
            ss >>variantName >>layoutName;
            getline(ss, variantDescription);

            layoutName.erase(layoutName.size() - 1);
            variantDescription.erase(0, variantDescription.find_first_not_of(" "));

            Variant variant;
            variant.name = variantName;
            variant.description = variantDescription;

            Layout& layout = layouts[layoutName];
            layout.variants.push_back(variant);
        }
    }

    fs.close();

    return layouts;
}

vector<Keysym> getKeysymList() {
    //open file
    string filepath = "/usr/include/X11/keysymdef.h";
    std::ifstream fs(filepath.c_str());

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
            }
            else {
                keysym.unicode.value = -1;
            }

            keysyms.push_back(keysym);
        }
    }

    fs.close();

    return keysyms;
}

LayoutInfo getLayoutInfo() {
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

void parseArguments(int argc, char** argv) {
    static struct option options[] =
    {
        {"list-keycodes", no_argument, 0, 'k'},
        {"list-layouts", no_argument, 0, 'l'},
        {"list-keysyms", no_argument, 0, 's'},
        {"print", no_argument, 0, 'p'},
        {0, 0, 0, 0}
    };

    executableName = argv[0];

    int opt;
    while ((opt = getopt_long(argc, argv, "", options, NULL)) != -1) {
        switch(opt) {
            case 'k':
                listKeycodes = true;
            break;
            case 'l':
                listLayouts = true;
            break;
            case 's':
                listKeysyms = true;
            break;
            case 'p':
                print = true;
            break;
        }
    }
}

void printUsage() {
    cout <<"usage: " <<executableName <<" [options]\n\n";
    cout <<"options:\n";
    cout <<"     --list-keycodes         list all keycodes and their keysyms\n";
    cout <<"     --list-layouts          list all keyboard layouts and their variants\n";
    cout <<"     --list-keysyms          list all keysyms, their values and unicodes\n";
    cout <<"     --print                 print current layout\n";
}

bool printKeycodeList() {
    map<string, Layout> layouts = getLayoutList();
    LayoutInfo layoutInfo = getLayoutInfo();

    map<string, Layout>::iterator it = layouts.find(layoutInfo.layout);
    if (it == layouts.end()) {
        cout <<"current keyboard layout is unknown\n";

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
            cout <<"current keyboard layout variant is unknown\n";

            return false;
        }
    }

    vector<Keycode> keycodes = getKeycodeList();

    //keycodes
    cout <<"<keycodes layoutName=\"" + layout.name + "\" layoutDescription=\"" + layout.description + "\" variantName=\"" + variant.name + "\" variantDescription=\"" + variant.description + "\">\n";

    //iterate over keycodes
    for(unsigned int i = 0; i < keycodes.size(); i++) {
        Keycode keycode = keycodes[i];

        cout <<string(2, ' ') <<"<keycode value=\"" <<dec <<(int)keycode.value <<"\">\n";

        //iterate over keysyms
        vector<Keysym> keysyms = keycode.keysyms;
        for(unsigned int j = 0; j < keysyms.size(); j++) {
            Keysym keysym = keysyms[j];
            cout <<string(4, ' ') <<"<keysym value=\"" <<"0x" <<setfill('0') <<hex <<(int)keysym.value <<"\"/>\n";
        }

        cout <<string(2, ' ') <<"</keycode>\n";
    }

    cout <<"</keycodes>\n";

    return true;
}

bool printLayoutList() {
    map<string, Layout> layouts = getLayoutList();

    cout <<"<layouts>\n";

    for (std::map<string, Layout>::iterator it = layouts.begin(); it != layouts.end(); it++) {
        Layout& layout = it->second;
        vector<Variant> variants = layout.variants;

        //layout
        cout <<string(2, ' ') <<"<layout>\n";
        cout <<string(4, ' ') <<"<name>" <<layout.name <<"</name>\n";
        cout <<string(4, ' ') <<"<description>" <<layout.description <<"</description>\n";

        //variants
        if (!variants.empty()) {
            cout <<string(4, ' ') <<"<variants>\n";
            for (unsigned int i = 0; i < variants.size(); i++) {
                Variant variant = variants[i];
                cout <<string(6, ' ') <<"<variant>\n";
                cout <<string(8, ' ') <<"<name>" <<variant.name <<"</name>\n";
                cout <<string(8, ' ') <<"<description>" <<variant.description <<"</description>\n";
                cout <<string(6, ' ') <<"</variant>\n";
            }
            cout <<string(4, ' ') <<"</variants>\n";
        }

        cout <<string(2, ' ') <<"</layout>\n";
    }

    cout <<"</layouts>\n";

    return true;
}

bool printKeysymList() {
    vector<Keysym> keysyms = getKeysymList();

    cout <<"<keysyms>\n";

    for (unsigned int i = 0; i < keysyms.size(); i++) {
        Keysym keysym = keysyms[i];
        cout <<string(2, ' ') <<"<keysym value=\"0x" <<hex <<keysym.value <<"\" name=\"" <<keysym.name <<"\">\n";

        if (keysym.unicode.value > -1) {
            cout <<string(4, ' ') <<"<unicode value=\"0x" <<setfill('0') <<setw(4) <<hex <<keysym.unicode.value <<"\" name=\"" <<keysym.unicode.name <<"\"/>\n";
        }

        cout <<string(2, ' ') <<"</keysym>\n";
    }

    cout <<"<keysyms>\n";

    return true;
}

bool printLayoutInfo() {
    LayoutInfo layoutInfo = getLayoutInfo();

    cout <<"layout: " <<layoutInfo.layout <<"\n";
    cout <<"variant: " <<layoutInfo.variant <<"\n";

    return true;
}

int main(int argc, char** argv)
{
    parseArguments(argc, argv);

    //open X11 display
    display = XOpenDisplay(NULL);
    if(display == NULL) {
        printf("error: could not open display\n");

        return 1;
    }

    if (listKeycodes) {
        if (!printKeycodeList()) {
            exit(1);
        }
    }
    else if (listLayouts) {
        if (!printLayoutList()) {
            exit(1);
        }
    }
    else if (listKeysyms) {
        if (!printKeysymList()) {
            exit(1);
        }
    }
    else if (print) {
        if (!printLayoutInfo()) {
            exit(1);
        }
    }
    else {
        printUsage();
    }

    //close X11 display
    XCloseDisplay(display);

    return 0;
}
