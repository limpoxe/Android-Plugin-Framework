package com.example.plugintest.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class PluginDbTables {

	public static final String AUTHORITY = "com.example.plugintest.provider";

	public static final class PluginFirstTable implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/pluginfirst");

		public static final String MY_FIRST_PLUGIN_NAME = "my_first_plugin_name";

		public static final String CREATED_DATE = "created_date";//记录创建时间

		public static final String MODIFIED_DATE = "modified_date";//记录修改时间

		public static final String DEFAULT_SORT_ORDER = "modified_date DESC";//默认排序方式

	}

}