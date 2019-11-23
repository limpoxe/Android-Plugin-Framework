#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_example_plugintest_hellojni_HelloJni_calculate(
        JNIEnv* env,
        jclass /* this */,
        jint x,
        jint y) {
    std::string hello = "plugintest Hello from C++, method : calculate";
    int ret = x + y;
    __android_log_print(ANDROID_LOG_INFO, "HelloJni", "%s", hello.c_str());
    return ret;
}
