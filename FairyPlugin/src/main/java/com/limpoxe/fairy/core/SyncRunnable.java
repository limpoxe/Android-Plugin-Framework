package com.limpoxe.fairy.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.limpoxe.fairy.util.LogUtil;

public class SyncRunnable implements Runnable {
    private final Runner mTarget;
    private Object mResult;
    private boolean mComplete;
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private SyncRunnable(Runner target) {
        mTarget = target;
    }

    @Override
    public void run() {
        try {
            mResult = mTarget.run();
        } catch (Exception e) {
            LogUtil.printException("Exception kill", e);
            LogUtil.e("Kill", "发生了无法处理的异常，杀掉当前进程: " + Process.myPid());
            Process.killProcess(Process.myPid());
        }
        synchronized (this) {
            mComplete = true;
            notifyAll();
        }
    }

    private Object waitForComplete() {
        synchronized (this) {
            while (!mComplete) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            return mResult;
        }
    }

    /**
     * 如果在主线程中，则直接调用runner， 如果不在主线程中，则转到主线程中调用，并等待主线程返回
     * @param runner
     */
    public static <T> T runOnMainSync(Runner<T> runner) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return runner.run();
        } else {
            SyncRunnable sr = new SyncRunnable(runner);
            sMainHandler.post(sr);
            return (T)sr.waitForComplete();
        }
    }

}