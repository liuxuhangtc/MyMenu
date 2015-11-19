package com.android.tmall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.android.tmall.util.TmallUtil;

/**
 * 开屏 Activity
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 用指定的布局文件填充开屏视图
        View view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);

        // 渐变动画
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1500);
        view.startAnimation(animation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 休眠的目的是让渐变动画完成
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Class<?> cls = null;
                if (TmallUtil.isLogined(getApplicationContext())) {
                    cls = MainActivity.class; // 已成功登录过
                } else {
                    cls = LoginActivity.class; // 第一次登录
                }

                // 启动指定的 Activity
                startActivity(new Intent(SplashActivity.this, cls));
                finish();  // 销毁当前 Activity
            }
        }).start();
    }
}
