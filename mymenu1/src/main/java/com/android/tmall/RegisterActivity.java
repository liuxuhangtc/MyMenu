package com.android.tmall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tmall.uploadimage.ImageTools;
import com.android.tmall.uploadimage.ImageUri;
import com.android.tmall.uploadimage.LoadDialog;
import com.android.tmall.uploadimage.UploadService;
import com.android.tmall.util.MyJsonObjectRequest;
import com.android.tmall.util.TmallUtil;
import com.android.tmall.util.VolleyUtil;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 注册 Activity
 */
public class RegisterActivity extends Activity {
    @InjectView(R.id.txtUsername)
    EditText txtUsername;
    @InjectView(R.id.txtPassword)
    EditText txtPassword;
    @InjectView(R.id.txtRePassword)
    EditText txtRePassword;
    @InjectView(R.id.txtEmail)
    EditText txtEmail;
    @InjectView(R.id.tvProtocol)
    TextView tvProtocol;
    @InjectView(R.id.imgLogoView)
    ImageView imageView;

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int SCALE = 7;// 照片缩小比例
    private String uploadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.inject(this);

        String protocol = "<font color=" + "\"" + "#AAAAAA" + "\">" + "点击上面的"
                + "\"" + "注册" + "\"" + "按钮,即表示你同意" + "</font>" + "<u>"
                + "<font color=" + "\"" + "#576B95" + "\">" + "《CNM菜谱软件许可及服务协议》"
                + "</font>" + "</u>";

        tvProtocol.setText(Html.fromHtml(protocol));
    }

    //拍照上传头像
    @OnClick(R.id.imgLogo)
    public void imglogoClick(){
        // 显示相片操作(0 拍照 / 1 选择相片)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("图片来源:");
        builder.setNegativeButton("取消", null);
        builder.setItems(new String[]{"拍照", "相册"},
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TAKE_PHOTO:
                                Intent openCameraIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri imageUri = Uri.fromFile(new File(Environment
                                        .getExternalStorageDirectory(), "image.jpg"));
                                // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(openCameraIntent, TAKE_PHOTO);
                                break;

                            case CHOOSE_PHOTO:
                                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                openAlbumIntent.setType("image/*");
                                startActivityForResult(openAlbumIntent, CHOOSE_PHOTO);
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    // 将保存在本地的图片取出并缩小后显示在界面上
                    Bitmap bitmap = BitmapFactory.decodeFile(Environment
                            .getExternalStorageDirectory() + "/image.jpg");
                    Bitmap newBitmap = ImageTools.zoomBitmap(bitmap, bitmap.getWidth()
                            / SCALE, bitmap.getHeight() / SCALE);
                    // 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                    bitmap.recycle();

                    // 将处理过的图片显示在界面上，并保存到本地
                    imageView.setImageBitmap(newBitmap);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
                    String filename = sdf.format(new Date());

                    ImageTools.savePhotoToSDCard(newBitmap, Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .getAbsolutePath()
                            + "/Camera", "IMG_" + filename);
                    uploadFile = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .getAbsolutePath()
                            + "/Camera/" + "IMG_" + filename + ".png";
                    break;

                case CHOOSE_PHOTO:
                    ContentResolver resolver = getContentResolver();
                    // 照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {
                        // 使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver,
                                originalUri);
                        if (photo != null) {
                            // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            Bitmap smallBitmap = ImageTools.zoomBitmap(photo, photo.getWidth()
                                    / SCALE, photo.getHeight() / SCALE);
                            // 释放原始图片占用的内存，防止out of memory异常发生
                            photo.recycle();

                            imageView.setImageBitmap(smallBitmap);
                            uploadFile = ImageUri.getImageAbsolutePath(this, originalUri);
                        }
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "读取文件,出错啦", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    @OnClick(R.id.btn_register)
    public void registerClick() {
        String password = txtPassword.getText().toString();
        String rePassword = txtRePassword.getText().toString();
        if (!password.equals(rePassword)) {
            TmallUtil.toast(this, R.string.password_error);
            return;
        }
        if (TextUtils.isEmpty(uploadFile)) {
            uploadFile="images/logo.png";
        } else {
            // 上传
            // 上传图片
            String requestUrl = AndroidClient.BASE_URL + "UploadServlet";
            File file = new File(uploadFile);
            LoadDialog loadDialog = new LoadDialog(this);
            // (回调处理方法,上传图片类,指定上传图片类中的方法,请求服务器的URL路径,本地需上传的文件)
            loadDialog.execute
                    (new LoadDialog.Callback() {
                        @Override
                        public void getResult(Object obj) {
                            // do nothing
                        }
                    }, UploadService.class, "postUseUrlConnection", requestUrl, file);

            // 表单数据
            Map<String, String> params = new HashMap<>();
            params.put("username", txtUsername.getText().toString());
            params.put("password", password);
            params.put("email", txtEmail.getText().toString());
            params.put("photopath", "images" + uploadFile.substring(uploadFile.lastIndexOf("/"), uploadFile.length()));

            // 自定义Json对象请求类(请求方式,URL,表单参数,响应成功后的处理类,响应错误后的处理类)
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    VolleyUtil.getAbsoluteUrl("ReigisterServletJson"),
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jo) {
                            try {
                                if ("success".equals(jo.getString("flag"))) {
                                    // 注册成功后跳转至登录
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                } else if ("error".equals(jo.getString("flag"))) {
                                    // 注册失败
                                    TmallUtil.toast(getApplicationContext(), R.string.register_error);
                                } else if ("exist".equals(jo.getString("flag"))) {
                                    // 用户名已存在
                                    TmallUtil.toast(getApplicationContext(), R.string.register_exist);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            TmallUtil.toast(getApplicationContext(), R.string.net_error);
                        }
                    }
            );
            VolleyUtil.getInstance(this).addToRequestQueue(request, "register_req");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        VolleyUtil.getInstance(this).cancelRequests("register_req");
    }
}
