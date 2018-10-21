package com.limpoxe.fairy.core.localservice;

import android.net.Uri;
import android.os.Bundle;

public interface PluginDataProvider {
    public Bundle handleRequest(Uri uri, Bundle extras);
}
