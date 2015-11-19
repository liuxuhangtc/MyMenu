package com.android.tmall;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tmall.util.VolleyUtil;
import com.android.volley.toolbox.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 宝贝详细信息 Activity
 */
public class ProductDetailActivity extends AppCompatActivity {

    @InjectView(R.id.productId)
    TextView productId;
    @InjectView(R.id.productName)
    TextView productName;
    @InjectView(R.id.productPrice)
    TextView productPrice;
    @InjectView(R.id.productImgPath)
    ImageView productImgPath;
    @InjectView(R.id.productRemark)
    TextView productRemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ButterKnife.inject(this);

        // 获得 intent 中附加信息
        Intent intent = this.getIntent();
        int id = intent.getIntExtra("id", 0);
        String name = intent.getStringExtra("name");
        double price = intent.getDoubleExtra("price", 0d);
        String imgPath = intent.getStringExtra("imgPath");
        String remark = intent.getStringExtra("remark");

        // Log.v(TmallUtil.TAG, String.format("%d,%s,%.02f,%s,%s",id, name, price, imgPath, remark));

        // 设置应用栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 设置应用栏中的标题(宝贝名)
        CollapsingToolbarLayout collapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(name);

        // 填充数据到指定控件
        productId.setText(String.format("%d", id));
        productName.setText(name);
        productPrice.setText(String.format("%.02f", price));
        productRemark.setText(remark);

        ImageLoader imageLoader =
                VolleyUtil.getInstance(this).getImageLoader();
        ImageLoader.ImageListener listener =
                ImageLoader.getImageListener(
                        productImgPath, R.drawable.logo, R.mipmap.ic_launcher);
        imageLoader.get(imgPath, listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();  // 返回退出当前 Activity
                break;
        }

        return true;
    }
}
