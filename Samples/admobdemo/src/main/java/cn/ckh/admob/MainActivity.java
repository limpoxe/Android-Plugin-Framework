package cn.ckh.admob;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //onClick in xml
    public void show(View view) {
        GADManager.getInstance(this).show();
    }

    //onClick in xml
    public void loadad(View v){
        //init
        GADManager.getInstance(this);
    }
}
