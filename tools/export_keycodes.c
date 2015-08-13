#include <stdio.h>
#include <X11/XKBlib.h>

int main()
{
    Display *display;
    XkbDescPtr keyboardMap;
    KeyCode keycode;

    //open X11 display
    display = XOpenDisplay(NULL);
    if(display == NULL) {
        printf("error: could not open display\n");

        return 1;
    }

    printf("#<keycode>,<keysym_1>, <keysym_2>, <keysym_3>, <keysym_4>, ...\n");
    printf("#where keysym_1: key\n");
    printf("#      keysym_2: shift + key\n");
    printf("#      keysym_3: altGr + key\n");
    printf("#      keysym_4: shift + altGr + key\n");

    //get keyboard map
    keyboardMap = XkbGetMap(display, XkbAllClientInfoMask, XkbUseCoreKbd);

    //iterate over possible keycodes
    keycode = keyboardMap->min_key_code;
    while (keycode < keyboardMap->max_key_code) {
        int keysymShiftLevel;

        printf("%d,",(int)keycode);

        //retrieve all keysyms associated with group 0 at all possible shift levels
        unsigned int group = 0;
        unsigned char keysymShiftLevelMax = XkbKeyGroupWidth(keyboardMap, keycode, group);
        for (keysymShiftLevel = 0; keysymShiftLevel < keysymShiftLevelMax; keysymShiftLevel++) {
            if (keysymShiftLevel > 0) {
                printf(",");
            }

            KeySym keysym = XkbKeySymEntry(keyboardMap, keycode, keysymShiftLevel, group);
            printf("0x%04x", (int)keysym);
        }

        printf("\n");

        keycode++;
    }

    //close X11 display
    XCloseDisplay(display);

    return 0;
}

