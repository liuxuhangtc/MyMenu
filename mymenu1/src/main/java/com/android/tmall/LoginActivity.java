package com.android.tmall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.tmall.util.MyJsonObjectRequest;
import com.android.tmall.util.QQUtil;
import com.android.tmall.util.TmallUtil;
import com.android.tmall.util.VolleyUtil;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 登录 Activity
 */
public class LoginActivity extends Activity {
    // 自己的 APP_ID 替换 222222
    public static String mAppid = "222222";
    // 腾讯对象
    public static Tencent mTencent;

    @InjectView(R.id.txtUsername)
    EditText txtUsername;
    @InjectView(R.id.txtPassword)
    EditText txtPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (mTencent == null) {
            // 根据 APP_ID和上下文 创建腾讯对象实例
            mTencent = Tencent.createInstance(mAppid, this);
        }

        ButterKnife.inject(this);
    }
    ///QQ登录
    @OnClick(R.id.login_qq)
    public void qqLogin(View view) {
        // QQ登录按钮动画
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.shake);
        onClickLogin();
        view.startAnimation(shake);
    }

    private void onClickLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", loginListener);
        }
    }

    // 登录监听事件
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
        }
    };

    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            if (null == response) {
                QQUtil.showResultDialog(LoginActivity.this, "返回为空", "登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                QQUtil.showResultDialog(LoginActivity.this, "返回为空", "登录失败");
                return;
            }
            //QQUtil.showResultDialog(LoginActivity.this, response.toString(), "登录成功");
            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
            Intent qqLogin=getIntent();
            //  qqLogin.setData();
            setResult(MainActivity.RESULT_OK, qqLogin);
            LoginActivity.this.finish();
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            QQUtil.toastMessage(LoginActivity.this, "onError: " + e.errorDetail);
            QQUtil.dismissDialog();
        }

        @Override
        public void onCancel() {
            QQUtil.toastMessage(LoginActivity.this, "onCancel: ");
            QQUtil.dismissDialog();
        }
    }

    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.btn_login)
    public void loginClick() {
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        // 表单数据
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        // 自定义Json对象请求类(请求方式,URL,表单参数,响应成功后的处理类,响应错误后的处理类)
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.POST,
                VolleyUtil.getAbsoluteUrl("LoginServletJson"),
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getInt("id") == 0) {
                                TmallUtil.toast(getApplicationContext(), R.string.user_invalid);
                            } else {
                                // 1.保存已登录成功的用户到选项存储
                                // (注意事项:名值对 - 名一定要在 JSON 对象中存在,否则无反应或异常)
                                TmallUtil.savePreferences(getApplicationContext(),
                                        jsonObject.getInt("id"),
                                        jsonObject.getString("username"),
                                        jsonObject.getString("email"),
                                        jsonObject.getString("photopath"));
                                // 2.跳转至 MainActivity
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                // 3.销毁当前 Activity
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }
        );
        // 把 Json请求对象 加入到 Volley 的请求队列 (默认的TAG)
        VolleyUtil.getInstance(this).addToRequestQueue(request);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 从 Volley 的请求队列中移除默认的TAG
        VolleyUtil.getInstance(this).cancelRequests(VolleyUtil.TAG);
    }

    @OnClick(R.id.btn_register)
    public void registerClick() {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
