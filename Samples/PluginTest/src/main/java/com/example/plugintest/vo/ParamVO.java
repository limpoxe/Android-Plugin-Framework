package com.example.plugintest.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cailiming on 15/9/22.
 */
public class ParamVO implements Parcelable {

    public String name;

    public ParamVO() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
    }

    public static final Parcelable.Creator<ParamVO> CREATOR = new Parcelable.Creator<ParamVO>() {
        public ParamVO createFromParcel(Parcel in) {
            return new ParamVO(in);
        }

        public ParamVO[] newArray(int size) {
            return new ParamVO[size];
        }
    };

    private ParamVO(Parcel in) {
        name = in.readString();
    }
}
