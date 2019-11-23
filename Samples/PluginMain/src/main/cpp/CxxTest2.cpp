#include <android/log.h>
#include <assert.h>
#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>
#include <utime.h>


static int println_native(JNIEnv *env, jclass clazz, jint bufID, jint priority,
                                 jobject tag, jobject msg) {
    int prio = priority;
    const char *nativeTag = env->GetStringUTFChars((jstring) tag, 0);
    const char *nativeMsg = env->GetStringUTFChars((jstring) msg, 0);
    __android_log_buf_print(bufID, prio, nativeTag, "%s : %s", "By CxxTest println_native", nativeMsg);
    env->ReleaseStringUTFChars((jstring) tag, nativeTag);
    env->ReleaseStringUTFChars((jstring) msg, nativeMsg);
    return 0;
}


// class -> java method name -> java method signatures -> native method ptr
static const char *gJavaClassFullName = "com/example/pluginmain/CxxTest";
static JNINativeMethod gMethods[] = {
        {"println", "(IILjava/lang/String;Ljava/lang/String;)I", (void *) println_native},
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz = env->FindClass(className);

    if (clazz == NULL) {
        //LOGE("Can't find XXX");
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        //LOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (!registerNativeMethods(env, gJavaClassFullName, gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0]))) {
        //LOGE("ERROR: native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    bail:
    return result;
}



