#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_example_pluginhelloworld_HelloJni_calculate(
        JNIEnv* env,
        jclass /* this */,
        jint x,
        jint y) {
    int ret = x + y;
    __android_log_print(ANDROID_LOG_INFO, "HelloJni", "pluginhelloworld %i + %i = %i", x, y, ret);
    return ret;
}
