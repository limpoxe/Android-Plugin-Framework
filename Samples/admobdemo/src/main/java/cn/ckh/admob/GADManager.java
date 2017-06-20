package cn.ckh.admob;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by ckh on 2017/6/19.
 */

public class GADManager {
    private Context context;
    private static GADManager sManager;
    private InterstitialAd mInterstitialAd;

    private GADManager(Context context) {
        this.context = context;
        initAD();
    }

    public static synchronized GADManager getInstance(Context context){
        if(sManager==null){
            sManager = new GADManager(context);
        }
        return sManager;
    }
    private void initAD(){
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                Toast.makeText(context, "广告加载完成, 点击show按钮显示广告", Toast.LENGTH_LONG).show();

                Log.e("tag","onAdLoaded");
            }
        });
        requestNewInterstitial();
    }
    private void requestNewInterstitial() {
        Log.e("tag",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.1");
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("DB4F002BB4E2845D7F70010FB82DE372")
                .build();
        Log.e("tag",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.2");
        mInterstitialAd.loadAd(adRequest);
    }

    public void show(){
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }else{
            Toast.makeText(context, "广告还没有加载完毕, 请等待", Toast.LENGTH_LONG).show();
            Log.e("tag","广告还没有加载完毕");
        }
    }


}
