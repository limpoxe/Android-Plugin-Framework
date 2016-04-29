package com.example.pluginmain;

/**
 * 可以用来拦截当前进程所有log。
 *
 * 这个jni仅仅是为了测试日志拦截的可行性。和插件框架无关。
 */
public class LogProxy {

	static {
		try {
			System.loadLibrary("log4a");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	/**
	 *
	 * @param bufID
	 * @param priority
	 * @param tag
	 * @param msg
	 * @return  返回非零值表示拦截日志，日志完全交给onLog函数处理
	 *          返回0则继续打印日志
	 */
	static int onLog(int bufID, int priority, String tag, String msg) {
		//此处万不可再次调用android.util.Log的方法，会变成死循环
		//do your things.
		return 0;
	}
}
