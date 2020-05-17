package jp.yuriwolflotslove.MY_CHECK_MATE;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        presentLogo();
        overridePendingTransition(0, R.anim.activity_fadeout);
    }

    private void presentLogo() {
        final SplashActivity splashActivity = this;
        new Thread() {
            public void run() {
                synchronized (splashActivity) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                    } finally {
                        Intent intent = new Intent();
                        intent.setClass(splashActivity, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }.start();
    }

}
