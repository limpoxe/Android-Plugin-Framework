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

//替换后的方法名称和签名
static const char *hackedLogMethod = "onLog";
static const char *hackedLogSignature = "(IILjava/lang/String;Ljava/lang/String;)I";
//替换后的方法名称所在的类
static const char *hackedLogClassName = "com/example/pluginmain/LogProxy";

static jclass hackedLogClass;
static jmethodID hackedLogMethodId;

static int hacked_println_native(JNIEnv* env, jclass clazz, jint bufID, jint priority,
		jobject tag, jobject msg) {

	int result = env->CallStaticIntMethod(hackedLogClass, hackedLogMethodId, bufID, priority, tag,
										  msg);
	if (result == 0) {
		//仍然打印日志
		int prio = priority;
		const char *nativeTag = env->GetStringUTFChars((jstring)tag, 0);
		const char *nativeMsg = env->GetStringUTFChars((jstring)msg, 0);
		__android_log_print(prio, nativeTag, "%s:%s", "By Log4a", nativeMsg);
		env->ReleaseStringUTFChars((jstring)tag, nativeTag);
		env->ReleaseStringUTFChars((jstring)msg, nativeMsg);

	} else {
		//表示日志被拦截了 啥也不做
	}
	return result;
}

static JNINativeMethod gMethods[] = {
		{ "println_native", "(IILjava/lang/String;Ljava/lang/String;)I",
				(void*) hacked_println_native },
};


static int replaceNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz = env->FindClass(className);

	if (clazz == NULL)
	{
		//LOGE("Can't find XXX");
		return JNI_FALSE;
	}

	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
	{
		return JNI_FALSE;
	}

	if (hackedLogClass == NULL || hackedLogMethodId == NULL)
	{
		jclass newClazz = env->FindClass(hackedLogClassName);
		if (newClazz != NULL)
		{
			hackedLogClass = (jclass) env->NewGlobalRef(newClazz);
			hackedLogMethodId = env->GetStaticMethodID(hackedLogClass, hackedLogMethod, hackedLogSignature);
		}
	}

	return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
	{
		//LOGE("ERROR: GetEnv failed\n");
		goto bail;
	}
	assert(env != NULL);

	//正常情况下，应该是将自己的java端方法和自己的native端方法建立映射关系
	//但是这里是将别人的java端方法和自己的native端方法建立了映射关系
	if (!replaceNativeMethods(env, "android/util/Log", gMethods,
							   sizeof(gMethods) / sizeof(gMethods[0])))
	{
		//LOGE("ERROR: native registration failed\n");
		goto bail;
	}

	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	bail:
	return result;
}



