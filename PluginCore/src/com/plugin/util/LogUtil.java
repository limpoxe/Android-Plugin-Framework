package com.plugin.util;

import android.util.Log;

public class LogUtil {
	
	private static final boolean isDebug = true;
	
    public static void d(Object... msg) {
        if (isDebug) {
            StringBuilder str = new StringBuilder();

            if (msg != null) {
                for (Object obj : msg) {
                    str.append("★").append(obj);
                }
                if (str.length() > 0) {
                    str.deleteCharAt(0);
                }
            } else {
                str.append("null");
            }
            try {
                StackTraceElement[] sts = Thread.currentThread().getStackTrace();
                StackTraceElement st = null;
                String tag = null;
                if (sts != null && sts.length > 3) {
                    st = sts[3];
                    if (st != null) {
                        String fileName = st.getFileName();
                        tag = (fileName == null) ? "Unkown" : fileName.replace(".java", "");
                        str.insert(0, "【" + tag + "." + st.getMethodName() + "() line " + st.getLineNumber() + "】\n>>>[")
                                .append("]");
                    }
                }

                tag =  (tag==null)?"Plugin":("Plugin_" + tag);
                // use logcat log
                while (str.length() > 0) {
                    Log.v(tag, str.substring(0, Math.min(2000, str.length())).toString());
                    str.delete(0, 2000);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void printStackTrace() {
        if (isDebug) {
            try {
                StackTraceElement[] sts = Thread.currentThread().getStackTrace();
                for (StackTraceElement stackTraceElement : sts) {
                    Log.e("Log_trace", stackTraceElement.toString());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void printException(String msg, Throwable e) {
        if (isDebug) {
            Log.e("Log_trace", msg, e);
        }
    }
}
