package com.example.pluginmain;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;
import android.util.Log;

import com.limpoxe.fairy.core.FairyGlobal;
import com.limpoxe.fairy.core.android.HackApplication;
import com.limpoxe.fairy.core.android.HackLoadedApk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Debug {

    public static boolean trackHuaweiReceivers() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return false;
        }
        boolean maybeLeak = false;
        Object mLoadedApk = new HackApplication(FairyGlobal.getHostApplication()).getLoadedApk();
        if (mLoadedApk != null) {
            Object object = new HackLoadedApk(mLoadedApk).getReceivers();
            if (object != null && object instanceof ArrayMap) {
                ArrayMap arrayMap = (ArrayMap) object;
                Set entrySet = arrayMap.entrySet();
                Iterator entrySetIterator = entrySet.iterator();
                JSONObject jsonObject = new JSONObject();
                while(entrySetIterator.hasNext()) {
                    Object entry = entrySetIterator.next();
                    if (entry instanceof Map.Entry) {
                        String key1 = ((Map.Entry)entry).getKey().getClass().getName();
                        Object value = ((Map.Entry)entry).getValue();
                        if (value instanceof ArrayMap) {
                            Iterator valueIterator = ((ArrayMap)value).entrySet().iterator();
                            while (valueIterator.hasNext()) {
                                Object valueEntry = valueIterator.next();
                                if (valueEntry instanceof Map.Entry) {
                                    String key2 = ((Map.Entry)valueEntry).getKey().getClass().getName();
                                    String key = key1 + "->" + key2;
                                    int count = jsonObject.optInt(key);
                                    count++;
                                    try {
                                        jsonObject.put(key, count);
                                    } catch (JSONException e) {
                                    }
                                    if (count >=10) {
                                        maybeLeak = true;
                                    }
                                }
                            }
                        }
                    }
                }
                Log.e("Debug_track", jsonObject.toString());
            }
        }
        return maybeLeak;
    }

}
