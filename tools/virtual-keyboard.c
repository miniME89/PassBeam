#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <linux/uhid.h>

#define UHID_FILEPATH "/dev/uhid"
#define UHID_DEVICE_NAME "virtual-uhid-device"

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

static int uhidWrite(int fd, const struct uhid_event* event) {
    size_t size = sizeof(*event);
    ssize_t ret = write(fd, event, size);
    if (ret < 0) {
        fprintf(stderr, "cannot write to uhid: %m\n");
        return -errno;
    } else if (ret != sizeof(*event)) {
        fprintf(stderr, "wrong size written to uhid: %ld != %lu\n", ret, size);
        return -EFAULT;
    } else {
        return 0;
    }
}

static int uhidCreate(int fd) {
    struct uhid_event event;
    memset(&event, 0, sizeof(event));

    event.type = UHID_CREATE;
    strcpy((char*)event.u.create.name, UHID_DEVICE_NAME);
    event.u.create.rd_data = deviceDescription;
    event.u.create.rd_size = sizeof(deviceDescription);
    event.u.create.bus = BUS_USB;
    event.u.create.vendor = 0x15d9;
    event.u.create.product = 0x0a37;
    event.u.create.version = 0;
    event.u.create.country = 0;

    return uhidWrite(fd, &event);
}

static int uhidDestroy(int fd) {
    struct uhid_event ev;
    memset(&ev, 0, sizeof(ev));

    ev.type = UHID_DESTROY;

    return uhidWrite(fd, &ev);
}

int main(int argc, char** argv) {
    int fd = open(UHID_FILEPATH, O_RDWR | O_CLOEXEC);
    if (fd < 0) {
        fprintf(stderr, "cannot open uhid %s: %m\n", UHID_FILEPATH);

        return EXIT_FAILURE;
    }

    int ret = uhidCreate(fd);
    if (ret) {
        close(fd);

        return EXIT_FAILURE;
    }

    char* line = NULL;
    size_t size;
    struct uhid_event event;
    while (getline(&line, &size, stdin) > 0) {
        memset(&event, 0, sizeof(struct uhid_event));
        event.type = UHID_INPUT;

        int i = 0;
        char* split = strtok(line, " ");
        while(split != NULL) {
            int value = strtol(split, NULL, 16);

            event.u.input.size++;
            event.u.input.data[i] = value;

            split = strtok(NULL, " ");
            i++;
        }

        uhidWrite(fd, &event);

        memset(&event, 0, sizeof(struct uhid_event));
        event.type = UHID_INPUT;
        event.u.input.size = 8;
        uhidWrite(fd, &event);
    }

    uhidDestroy(fd);

    return EXIT_SUCCESS;
}
