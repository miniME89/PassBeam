/*
 * compile: g++ -std=c++11 virtual-keyboard.cpp -o virtual-keyboard -lpthread -lX11 -lXi
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <getopt.h>

#include <thread>

#include <X11/extensions/XInput.h>
#include <X11/Xutil.h>
#include <X11/Xlocale.h>

#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>

#include <linux/uhid.h>
#include <linux/input.h>

#include <iostream>
#include <string>
#include <iomanip>
#include <thread>
#include <chrono>
#include <mutex>
#include <queue>

#define KEY_RELEASE 0
#define KEY_PRESS 1
#define KEY_KEEPING_PRESSED 2

#define DEFAULT_UHID_DEVICE_NAME "virtual-keyboard"
#define DEFAULT_UHID_FILEPATH "/dev/uhid"
#define DEFAULT_INTERFACE_PORT 4242
#define MAX_INPUT_QUEUE_SIZE 10

using namespace std;
using namespace std::chrono;

static string executableFilename;
static string uhidFilepath = DEFAULT_UHID_FILEPATH;
static int interfacePort = DEFAULT_INTERFACE_PORT;
static bool verbose = false;

static Display* display = NULL;
static int uhidFd = -1;
static int serverFd = -1;
static int clientFd = -1;

static mutex inputQueueMutex;
static queue<string> inputQueue;

static unsigned char deviceDescription[] = {
    0x05, 0x01, 0x09, 0x06, 0xa1,
    0x01, 0x05, 0x07, 0x19, 0xe0,
    0x29, 0xe7, 0x15, 0x00, 0x25,
    0x01, 0x75, 0x01, 0x95, 0x08,
    0x81, 0x02, 0x95, 0x01, 0x75,
    0x08, 0x81, 0x01, 0x95, 0x05,
    0x75, 0x01, 0x05, 0x08, 0x19,
    0x01, 0x29, 0x05, 0x91, 0x02,
    0x95, 0x01, 0x75, 0x03, 0x91,
    0x01, 0x95, 0x06, 0x75, 0x08,
    0x15, 0x00, 0x26, 0xff, 0x00,
    0x05, 0x07, 0x19, 0x00, 0x2a,
    0xff, 0x00, 0x81, 0x00, 0xc0
};

static bool uhidWrite(const struct uhid_event* event) {
    ssize_t size = sizeof(*event);
    ssize_t ret = write(uhidFd, event, size);
    if (ret < 0) {
        cerr <<"cannot write to uhid: " <<strerror(errno) <<"\n";
        return false;
    } else if (ret != size) {
        cerr <<"wrong size written to uhid device file: "<<ret <<" != " <<size <<"\n";
        return false;
    }

    return true;
}

static bool uhidCreateKeyboard() {
    struct uhid_event event;
    memset(&event, 0, sizeof(event));

    event.type = UHID_CREATE;
    strcpy((char*)event.u.create.name, DEFAULT_UHID_DEVICE_NAME);
    event.u.create.rd_data = deviceDescription;
    event.u.create.rd_size = sizeof(deviceDescription);
    event.u.create.bus = BUS_USB;
    event.u.create.vendor = 0x15d9;
    event.u.create.product = 0x0a37;
    event.u.create.version = 0;
    event.u.create.country = 0;

    if (!uhidWrite(&event)) {
        return false;
    }

    return true;
}

static void keyboardThreadWorker() {
    if (verbose) {
        cout <<"started keyboard thread\n";
    }

    //open uhid device file
    uhidFd = open(uhidFilepath.c_str(), O_RDWR | O_CLOEXEC);
    if (uhidFd < 0) {
        cerr <<"cannot open uhid device file '" <<uhidFilepath <<"': " <<strerror(errno) <<"\n";
        exit(EXIT_FAILURE);
    }

    //create a new USB HID keyboard device
    if (!uhidCreateKeyboard()) {
        cerr <<"cannot create USB HID keyboard device\n";
        exit(EXIT_FAILURE);
    }

    this_thread::sleep_for(milliseconds(500));

    //set locale so XLookupString will return correct results
    if (setlocale(LC_ALL,"") == NULL) {
        cerr <<"cannot set default locale\n";
        exit(EXIT_FAILURE);
    }

    //open X display
    display = XOpenDisplay(NULL);
    if(display == NULL) {
        cerr <<"cannot open display\n";
        exit(EXIT_FAILURE);
    }

    //verify existence of XInputExtension
    int op;
    int ev;
    int err;
    if (!XQueryExtension(display, "XInputExtension", &op, &ev, &err)) {
        cerr <<"cannot find XInputExtension\n";
        exit(EXIT_FAILURE);
    }

    //find the virtual keyboard device
    int devicesCount;
    XDeviceInfo* deviceInfos = XListInputDevices(display, &devicesCount);
    XDeviceInfo* deviceInfo = NULL;
    for(int i = 0; i < devicesCount; i++) {
        if (strcmp(deviceInfos[i].name, DEFAULT_UHID_DEVICE_NAME) == 0) {
            deviceInfo = &deviceInfos[i];
        }
    }

    if(deviceInfo == NULL) {
        cerr <<"cannot find device info for '" <<DEFAULT_UHID_DEVICE_NAME <<"'\n";
        exit(EXIT_FAILURE);
    }

    //open the found device
    XDevice* device = XOpenDevice(display, deviceInfo->id);
    if (!device) {
        cerr <<"cannot open device '" <<deviceInfo->name <<"'\n";
        exit(EXIT_FAILURE);
    }

    //register for X events
    XEventClass eventClasses[2];
    int eventTypes[2] = {-1};

    DeviceKeyPress(device, eventTypes[0], eventClasses[0]);
    DeviceKeyRelease(device, eventTypes[1], eventClasses[1]);

    unsigned long screen = DefaultScreen(display);
    Window rootWindow = RootWindow(display, screen);
    XSelectExtensionEvent(display, rootWindow, eventClasses, 2);

    //process X events
    while(true) {
        XEvent event;
        XNextEvent(display, &event);

        //key press
        if (event.type == eventTypes[0]) {
            XDeviceKeyEvent* deviceKeyEvent = (XDeviceKeyEvent*)&event;
            XKeyEvent keyEvent;
            keyEvent.display = deviceKeyEvent->display;
            keyEvent.state = deviceKeyEvent->state;
            keyEvent.keycode = deviceKeyEvent->keycode;

            char str[256+1];
            KeySym keysym;
            int strLen = XLookupString(&keyEvent, str, 256, &keysym, NULL);


            if (verbose) {
                cout <<"received key press event [keycode: " <<keyEvent.keycode <<", " <<"keysym: " <<keysym <<", " <<"XLookupString: " <<str <<" (" <<strLen <<" bytes)" <<"]\n";
            }

            if (strLen > 0) {
                lock_guard<std::mutex> lock(inputQueueMutex);
                inputQueue.push(str);
                if (inputQueue.size() > MAX_INPUT_QUEUE_SIZE) {
                    inputQueue.pop();
                }
            }
        }
    }
}

static void interfaceThreadWorker() {
    if (verbose) {
        cout <<"started interface thread\n";
    }

    serverFd = socket (AF_INET, SOCK_STREAM, 0);
    if (serverFd < 0) {
        cerr <<"cannot create socket: " <<strerror(errno) <<"\n";
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(interfacePort);

    if(bind(serverFd, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        cerr <<"cannot bind: " <<strerror(errno) <<"\n";
        exit(EXIT_FAILURE);
    }

    listen(serverFd, 5);

    while(true) {
        struct sockaddr_in clientAddr;
        socklen_t clientAddrLen = sizeof(clientAddr);
        memset(&clientAddr, 0, clientAddrLen);

        clientFd = accept(serverFd, (struct sockaddr*)&clientAddr, &clientAddrLen);
        if (clientFd < 0) {
            cerr <<"cannot accept: " <<strerror(errno) <<"\n";
            continue;
        }

        if (verbose) {
            cout <<"accepted connection\n";
        }

        bool run = true;
        while(run) {
            struct uhid_event event;
            memset(&event, 0, sizeof(struct uhid_event));
            event.type = UHID_INPUT;

            //read HID keyboard event from socket
            while(event.u.input.size < 16 && run) {
                if (verbose) {
                    cout <<"read...";
                }

                int c = 0;
                int num = read(clientFd, &c, 1);

                if (verbose) {
                    cout <<setfill('0') <<setw(2) <<hex <<c <<" (" <<num <<" " <<" bytes)\n";
                }

                if (num == 0) {
                    run = false;
                    continue;
                }

                event.u.input.data[event.u.input.size] = c;
                event.u.input.size++;
            }

            if (run) {
                {
                    lock_guard<std::mutex> lock(inputQueueMutex);
                    inputQueue = queue<string>();
                }

                //trigger key press event
                if (!uhidWrite(&event)) {
                    run = false;
                    continue;
                }

                //trigger key release event
                memset(&event, 0, sizeof(struct uhid_event));
                event.type = UHID_INPUT;
                event.u.input.size = 8;

                if (!uhidWrite(&event)) {
                    run = false;
                    continue;
                }

                this_thread::sleep_for(milliseconds(20));

                string str;
                {
                    lock_guard<std::mutex> lock(inputQueueMutex);
                    while (!inputQueue.empty()) {
                        str += inputQueue.front();
                        inputQueue.pop();
                    }
                }

                char data[1024];
                int size;

                if (str.size() > 0) {
                    strcpy(data, str.c_str());
                    size = str.size();
                }
                else {
                    data[0] = '\0';
                    size = 1;
                }

                if (verbose) {
                    cout <<"write " <<data <<" (" <<size <<" " <<" bytes)\n";
                }

                write(clientFd, data, size);
            }
        }

        if (verbose) {
            cout <<"close connection\n";
        }

        close(clientFd);
    }
}

static void printUsage() {
    cout <<"usage: " <<executableFilename <<" [options]\n\n";
    cout <<"     --uhid-file, -u         filepath of the uhid device file [Default: " <<DEFAULT_UHID_FILEPATH <<"]\n";
    cout <<"     --port, -p              port of the TCP interface [Default: " <<DEFAULT_INTERFACE_PORT <<"]\n";
    cout <<"     --verbose, -v           verbose\n";
}

static void parseArguments(int argc, char** argv) {
    static struct option options[] = {
        {"uhid-file", required_argument, 0, 'u'},
        {"port", required_argument, 0, 'p'},
        {"verbose", required_argument, 0, 'v'},
        {0, 0, 0, 0}
    };

    executableFilename = argv[0];

    int opt;
    while ((opt = getopt_long(argc, argv, "u:p:v", options, NULL)) != -1) {
        switch(opt) {
        case 'u':
            uhidFilepath = optarg;
            break;
        case 'p':
            interfacePort = strtol(optarg, NULL, 10);
            break;
        case 'v':
            verbose = true;
            break;
        default:
            printUsage();
            exit(EXIT_SUCCESS);
        }
    }
}

int main(int argc, char** argv) {
    parseArguments(argc, argv);

    thread keyboardThread(keyboardThreadWorker);
    thread interfaceThread(interfaceThreadWorker);

    keyboardThread.join();
    interfaceThread.join();

    return EXIT_SUCCESS;
}
