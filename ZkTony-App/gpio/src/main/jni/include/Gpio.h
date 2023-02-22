/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class com_zktony_Gpio */

#ifndef ZKTONY_GPIO_H
#define ZKTONY_GPIO_H
#ifdef __cplusplus
extern "C" {
#endif

int readData(const char *filePath);

int writeData(const char *data, int count, const char *filePath);

JNIEXPORT jint JNICALL
Java_com_zktony_gpio_Gpio_nativeWriteGpio(JNIEnv *env, jobject thiz, jstring path, jstring value);

JNIEXPORT jint JNICALL
Java_com_zktony_gpio_Gpio_nativeReadGpio(JNIEnv *env, jobject thiz, jstring path);

#ifdef __cplusplus
}
#endif
#endif




