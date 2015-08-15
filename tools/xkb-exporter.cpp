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

#include <X11/XKBlib.h>
#include <X11/extensions/XKBrules.h>

using namespace std;

typedef struct Variant {
    string name;
    string description;
} Variant;

typedef struct Layout {
    string name;
    string description;
    vector<Variant> variants;
} Layout;

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

void replaceAll(string& s, const string& search, const string& replace) {
    for(size_t pos = 0; ; pos += replace.length()) {
        pos = s.find( search, pos );
        if( pos == string::npos ) {
            break;
        }

        s.erase( pos, search.length() );
        s.insert( pos, replace );
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

bool printKeycodeList() {
    cout <<"#<keycode>,<keysym_level_1>,<keysym_level_2>,<keysym_level_3>,<keysym_level_4>,...\n";

    //get keyboard map
    XkbDescPtr keyboardMap = XkbGetMap(display, XkbAllClientInfoMask, XkbUseCoreKbd);

    //iterate over possible keycodes
    for(KeyCode keycode = keyboardMap->min_key_code; keycode < keyboardMap->max_key_code; keycode++) {
        cout <<dec <<(int)keycode;

        //retrieve all keysyms associated with group 0 at all possible shift levels
        unsigned int group = 0;
        unsigned char keysymShiftLevelMax = XkbKeyGroupWidth(keyboardMap, keycode, group);
        int keysymShiftLevel;
        for (keysymShiftLevel = 0; keysymShiftLevel < keysymShiftLevelMax; keysymShiftLevel++) {
            KeySym keysym = XkbKeySymEntry(keyboardMap, keycode, keysymShiftLevel, group);
            cout <<",0x" <<setfill('0') <<setw(4) <<hex <<(int)keysym;
        }

        cout <<"\n";
    }

    return true;
}

bool printLayoutList() {
    //open file
    string filepath = "/usr/share/X11/xkb/rules/base.lst";
    std::ifstream fs(filepath.c_str());

    map<string, Layout> layouts;

    State state = NONE;

    //iterate over lines
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

    string delemiter = ",";

    cout <<"#<layout>" + delemiter + "<layout_description>" + delemiter + "<variant_1>" + delemiter + "<variant_description_1>" + delemiter + "<variant_2>" + delemiter + "<variant_description_2>...\n";

    for (std::map<string, Layout>::iterator it = layouts.begin(); it != layouts.end(); it++) {
        Layout& layout = it->second;
        vector<Variant> variants = layout.variants;
        replaceAll(layout.name, delemiter, "\\" + delemiter);
        replaceAll(layout.description, delemiter, "\\" + delemiter);
        cout <<layout.name <<delemiter <<layout.description;
        if (!variants.empty()) {
            cout <<delemiter;
            for (unsigned int i = 0; i < variants.size(); i++) {
                Variant variant = variants[i];
                replaceAll(variant.name, delemiter, "\\" + delemiter);
                replaceAll(variant.description, delemiter, "\\" + delemiter);
                cout <<variant.name <<delemiter <<variant.description <<((i < variants.size() - 1) ? delemiter : "");
            }
        }
        cout <<"\n";
    }

    return true;
}

bool printKeysymList() {
    cout <"TODO\n";
}

bool printLayout() {
    XkbRF_VarDefsRec vd;
    char *tmp;

    XkbRF_GetNamesProp(display, &tmp, &vd);

    cout <<"layout: " <<vd.layout <<"\n";
    cout <<"variant: " <<vd.variant <<"\n";

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
        if (!printLayout()) {
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
