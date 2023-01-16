#include <jni.h>
#include <string>
#include <termios.h>
#include <fcntl.h>
#include <unistd.h>
#include "include/SerialPort.h"
#include "include/SerialPortLog.h"

static speed_t getBaudRate(jint baud_rate) {
    switch (baud_rate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_zktony_serialport_core_SerialPort_open(JNIEnv *env, jobject thiz, jstring path,
                                                jint baudRate, jint stopBits, jint dataBits,
                                                jint parity, jint flowCon, jint flags) {
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

/* Check arguments */
    speed = getBaudRate(baudRate);
    if (speed == -1) {
        LOGE("Invalid baudRate");
        return nullptr;
    }

/* Opening device */
    const char *path_utf = env->GetStringUTFChars(path, nullptr);
    LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
    fd = open(path_utf, O_RDWR | flags);
    LOGD("open() fd = %d", fd);
    env->ReleaseStringUTFChars(path, path_utf);
    if (fd == -1) {
        LOGE("不能打开串口：%s", path_utf);
        return nullptr;
    }

/* Configure device */
    struct termios cfg{};
    LOGD("Configuring serial port");
    if (tcgetattr(fd, &cfg)) {
        LOGE("读取终端的配置失败");
        close(fd);
        return nullptr;
    }

    cfmakeraw(&cfg);
    cfsetispeed(&cfg, speed);
    cfsetospeed(&cfg, speed);

    cfg.c_cflag &= ~CSIZE;
    switch (dataBits) {
        case 5:
            cfg.c_cflag |= CS5;    //使用5位数据位
            break;
        case 6:
            cfg.c_cflag |= CS6;    //使用6位数据位
            break;
        case 7:
            cfg.c_cflag |= CS7;    //使用7位数据位
            break;
        case 8:
            cfg.c_cflag |= CS8;    //使用8位数据位
            break;
        default:
            cfg.c_cflag |= CS8;
            break;
    }

    switch (parity) {
        case 0:
            cfg.c_cflag &= ~PARENB;    //无奇偶校验
            break;
        case 1:
            cfg.c_cflag |= (PARODD | PARENB);   //奇校验
            break;
        case 2:
            cfg.c_iflag &= ~(IGNPAR | PARMRK); // 偶校验
            cfg.c_iflag |= INPCK;
            cfg.c_cflag |= PARENB;
            cfg.c_cflag &= ~PARODD;
            break;
        default:
            cfg.c_cflag &= ~PARENB;
            break;
    }

    switch (stopBits) {
        case 1:
            cfg.c_cflag &= ~CSTOPB;    //1位停止位
            break;
        case 2:
            cfg.c_cflag |= CSTOPB;    //2位停止位
            break;
        default:
            break;
    }

    // hardware flow control
    switch (flowCon) {
        case 0:
            cfg.c_cflag &= ~CRTSCTS;    //不使用流控
            break;
        case 1:
            cfg.c_cflag |= CRTSCTS;    //硬件流控
            break;
        case 2:
            cfg.c_cflag |= IXON | IXOFF | IXANY;    //软件流控
            break;
        default:
            cfg.c_cflag &= ~CRTSCTS;
            break;
    }


    if (tcsetattr(fd, TCSANOW, &cfg)) {
        LOGE("写入终端的配置失败");
        close(fd);
        return nullptr;
    }

/* Create a corresponding file descriptor */
    jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
    jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
    jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
    mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
    env->SetIntField(mFileDescriptor, descriptorID, (jint) fd);

    return mFileDescriptor;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zktony_serialport_core_SerialPort_close(JNIEnv *env, jobject thiz) {
    jclass SerialPortClass = env->GetObjectClass(thiz);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "fd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(thiz, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}