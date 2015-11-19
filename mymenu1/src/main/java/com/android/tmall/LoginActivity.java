package com.android.tmall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.tmall.util.MyJsonObjectRequest;
import com.android.tmall.util.TmallUtil;
import com.android.tmall.util.VolleyUtil;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

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

    @InjectView(R.id.txtUsername)
    EditText txtUsername;
    @InjectView(R.id.txtPassword)
    EditText txtPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
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
