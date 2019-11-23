#include <jni.h>
#include <string>

//在C++代码中调用C的库文件，需加上extern "C"，用来告诉编译器：这是一个用C写的库文件，请用C的方式链接它们
extern "C" {
#include "id.h"
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_pluginmain_CxxTest_stringFromJNI(
        JNIEnv* env,
        jclass /* this */) {

    whoami();

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
