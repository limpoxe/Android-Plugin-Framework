package com.example.wxsdklibrary;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.pluginmain.wxapi.WXEntryActivity;
import com.example.plugintestbase.ILoginService;
import com.example.plugintestbase.LoginVO;
import com.plugin.core.annotation.PluginContainer;
import com.plugin.util.PackageNameUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

@PluginContainer
public class TestSendToWXActivity extends Activity {

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context fakeContext = PackageNameUtil.fakeContext(this);
		api = WXAPIFactory.createWXAPI(fakeContext, WXEntryActivity.APP_ID, false);
		api.registerApp(WXEntryActivity.APP_ID);

		setContentView(R.layout.send_activity);


		ILoginService login = (ILoginService) getSystemService("login_service");
		if (login != null) {
			LoginVO vo = login.login("admin", "123456");
			Toast.makeText(this, vo.getUsername() + ":" + vo.getPassword(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "ILoginService == null", Toast.LENGTH_SHORT).show();
		}

		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WXTextObject textObj = new WXTextObject();
				textObj.text = "呵呵更健康";

				WXMediaMessage msg = new WXMediaMessage();
				msg.mediaObject = textObj;
				msg.title = "Will be ignored";
				msg.description = "呵呵更健康";

				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = "呵呵更健康" + String.valueOf(System.currentTimeMillis());
				req.message = msg;
				req.scene = SendMessageToWX.Req.WXSceneSession;//发送给某人

				api.sendReq(req);
			}
		});
	}

}
