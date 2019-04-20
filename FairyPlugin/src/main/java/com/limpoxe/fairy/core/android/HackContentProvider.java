package com.limpoxe.fairy.core.android;

import android.content.ContentProvider;

import com.limpoxe.fairy.util.RefInvoker;

public class HackContentProvider {

    private static final String Field_mAuthority = "mAuthority";

    private Object instance;

    public HackContentProvider(ContentProvider instance) {
        this.instance = instance;
    }

    public String getAuthority() {
        return (String)RefInvoker.getField(instance, ContentProvider.class, Field_mAuthority);
    }

}
