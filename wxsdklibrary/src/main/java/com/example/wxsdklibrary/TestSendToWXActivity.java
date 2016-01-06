package com.example.wxsdklibrary;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.pluginmain.wxapi.WXEntryActivity;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class TestSendToWXActivity extends Activity {

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, WXEntryActivity.APP_ID, false);
		api.registerApp(WXEntryActivity.APP_ID);

		Button btn = new Button(this);
		btn.setText("点击发送信息到微信");
		setContentView(btn);

		btn.setOnClickListener(new View.OnClickListener() {

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
