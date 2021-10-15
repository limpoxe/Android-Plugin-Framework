package com.example.pluginhelloworld;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.FileNotFoundException;

public class HelloFileProvider extends FileProvider {
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
        throws FileNotFoundException {
        //插件中需要对外部app提供的intent，都需要通过宿主来桥接
        //本例中插件提供了一个contentprovider给外系统app：PackageInstaller调用。
        //所以插件中的这个contentprovider是被宿主桥接过来的
        //桥接时框架会给uri增加一个固定的前缀，这里需要将前缀移除掉，还原到原本期望的uri
        Uri realUri = Uri.parse(uri.toString().replace("unsafe.proxy.", ""));
        return super.openFile(realUri, mode);
    }
}
