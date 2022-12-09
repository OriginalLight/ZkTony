#include <jni.h>
#include <string>
#include <termios.h>
#include <fcntl.h>
#include <unistd.h>

#include "include/Gpio.h"
#include "include/GpioLog.h"

int readData(const char *filePath) {
    int fd;
    int value;
    char valueStr[32];
    char *end;
    fd = open(filePath, O_RDONLY);
    if (fd < 0) {
        return -2;
    }
    memset(valueStr, 0, sizeof(valueStr));
    read(fd, (void *) valueStr, sizeof(valueStr) - 1);
    value = strtol(valueStr, &end, 0);
    if (end == valueStr) {
        close(fd);
        return -3;
    }
    close(fd);
    return value;
}

int writeData(const char *data, int count, const char *filePath) {
    int fd;
    fd = open(filePath, O_RDWR);
    if (fd < 0) {
        return -2;
    }
    write(fd, data, count);
    close(fd);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_gpio_Gpio_nativeWriteGpio(JNIEnv *env, jobject thiz, jstring path, jstring value) {
    const char *pathStr;
    const char *valueStr;
    pathStr = env->GetStringUTFChars(path, nullptr);
    valueStr = env->GetStringUTFChars(value, nullptr);
    int ret = writeData(valueStr, 1, pathStr);
    env->ReleaseStringUTFChars(path, pathStr);
    env->ReleaseStringUTFChars(value, valueStr);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_gpio_Gpio_nativeReadGpio(JNIEnv *env, jobject thiz, jstring path) {
    const char *pathStr;
    pathStr = env->GetStringUTFChars(path, nullptr);
    int ret = readData(pathStr);
    env->ReleaseStringUTFChars(path, pathStr);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_gpio_GpioSw_nativeWriteGpio(JNIEnv *env, jobject thiz, jstring path, jstring value) {
    const char *pathStr;
    const char *valueStr;
    pathStr = env->GetStringUTFChars(path, nullptr);
    valueStr = env->GetStringUTFChars(value, nullptr);
    int ret = writeData(valueStr, 1, pathStr);
    env->ReleaseStringUTFChars(path, pathStr);
    env->ReleaseStringUTFChars(value, valueStr);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zktony_gpio_GpioSw_nativeReadGpio(JNIEnv *env, jobject thiz, jstring path) {
    const char *pathStr;
    pathStr = env->GetStringUTFChars(path, nullptr);
    int ret = readData(pathStr);
    env->ReleaseStringUTFChars(path, pathStr);
    return ret;
}