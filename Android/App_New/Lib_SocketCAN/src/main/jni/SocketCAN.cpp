#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cstdint>
#include <unistd.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include "include/SocketCAN.h"
#include "include/SocketCANLog.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_socketcan_SocketCAN_open(JNIEnv *env, jobject thiz, jstring channel) {
    int fd;
    struct ifreq ifr;
    struct sockaddr_can addr;

    /* open socket */
    if ((fd = socket(PF_CAN, SOCK_RAW, CAN_RAW)) < 0) {
        return -1;
    }

    const char *str = env->GetStringUTFChars(channel, 0);
    strcpy(ifr.ifr_name, str);
    ioctl(fd, SIOCGIFINDEX, &ifr);

    memset(&addr, 0, sizeof(addr));
    addr.can_family = AF_CAN;
    addr.can_ifindex = ifr.ifr_ifindex;

    if (bind(fd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
        return -2;
    }
    LOGD("open() fd = %d", fd);
    return fd;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_socketcan_SocketCAN_close(JNIEnv *env, jobject thiz, jint fd) {
    LOGD("close() fd = %d", fd);
    return close(fd);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_socketcan_SocketCAN_write(JNIEnv *env, jobject thiz, jint fd, jlong id, jlong eff,
                                          jlong rtr,
                                          jint len, jintArray data) {
    struct can_frame frame;
    frame.can_id = (eff << 31) | (rtr << 30) | id;
    frame.can_dlc = len;
    jint *pdata = env->GetIntArrayElements(data, 0);
    for (uint8_t i = 0; i < len; i++) {
        frame.data[i] = pdata[i] & 0xFF;
    }
    int ret = write(fd, &frame, sizeof(struct can_frame));
    env->ReleaseIntArrayElements(data, pdata, 0);
    return ret;
}


extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_zktony_socketcan_SocketCAN_read(JNIEnv *env, jobject thiz, jint fd) {
    jlongArray ret;
    ret = env->NewLongArray(12);
    struct can_frame frame;
    read(fd, &frame, sizeof(struct can_frame));
    int64_t data[12];
    data[0] = frame.can_id & 0x1FFFFFFF;
    data[1] = (frame.can_id >> 31) & 0x1;
    data[2] = (frame.can_id >> 30) & 0x1;
    data[3] = frame.can_dlc;
    for (uint8_t i = 0; i < frame.can_dlc; i++) {
        data[i + 4] = frame.data[i];
    }
    env->SetLongArrayRegion(ret, 0, 12, data);
    return ret;
}