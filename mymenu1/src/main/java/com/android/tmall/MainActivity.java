package com.android.tmall;

import android.app.AlertDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tmall.fragment.DisFragment;
import com.android.tmall.fragment.HomeFragment;
import com.android.tmall.fragment.SortFragment;
import com.android.tmall.util.TmallUtil;
import com.android.tmall.util.VolleyUtil;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity (主活动界面)
 *
 * 1.注册时可添加自己的图片
 *   1.1 t_user 增加一列 imgPath
 *   1.2 注册时拍照/相册选择一张上传
 *   1.3 导航视图中显示自定义图片
 * 2.分页数据
 *   LoadMore (下拉刷新加载更多)
 * 3.MainActivity 中菜单 refresh 事件
 *   3.1 根据 某某 排序
 *   3.2 搜索 功能
 * 4.订单
 * 5.使用轮询推送 折扣/新宝贝
 */
public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 抽屉布局
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // 工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 应用栏
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // 导航视图
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        if (navView != null) {
            setDrawerContent(navView);
        }

        // 滑动分页
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setViewPage(viewPager);
        }

        // fab 按钮
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar 类似于 Toast
                Snackbar.make(view, "Snackbar for 分页", Snackbar.LENGTH_SHORT).
                        setAction(R.string.app_name, null).show();
            }
        });

        // 选项卡布局 关联 滑动分页
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    // 初始化导航视图
    private void setDrawerContent(NavigationView navView) {
        // -- 左侧抽屉导航视图 头部 ----------------------------
        // 设置当前登录的用户名
        View view = View.inflate(this,R.layout.nav_header,navView);
        TextView tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        String username = TmallUtil.getUsername(this);
        tvUsername.setText(username);

        // 设置当前登录的用户图片
        ImageView imgUsername = (ImageView) navView.findViewById(R.id.imgUsername);
        // 可用 ImageLoader 加载自定义的用户图片
        // 用户表中新增一列 img_path 存储用户图片 (替换硬代码)
        String imgPath;
        if (username.length() > 0 && "admin".equals(username)) {
            imgPath = "";
        }
        // 来自选项存储的图片路径 (实际项目中此行替换以上四行代码)
        imgPath = TmallUtil.getImgPath(this);

        ImageLoader imageLoader =
                VolleyUtil.getInstance(this).getImageLoader();
        ImageLoader.ImageListener listener =
                ImageLoader.getImageListener(
                        imgUsername, R.mipmap.ic_launcher, R.drawable.logo);
        imageLoader.get(VolleyUtil.BASE_URL + imgPath, listener);

        // -- 左侧抽屉导航视图 菜单 ------------------------------
        // 导航视图中的菜单选中事件
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    // 匹配菜单的选项 (其它选项的处理 省略)
//                    case R.id.action_about:
//                        aboutDialog();
//                        break;
//                }
                // 选择后自动关闭左侧抽屉
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    // 关于对话框
    private void aboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.action_about);
        builder.setMessage(R.string.message_about);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 点击左上角图标,打开抽屉
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_logout:
                // 注销当前用户
                TmallUtil.savePreferences(this, 0, "", "","");
                finish();
                break;
        }
        return true;
    }

    // 初始化滑动分页
    private void setViewPage(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(),
                getString(R.string.product_list));
        adapter.addFragment(new DisFragment(),getString(R.string.community));
        adapter.addFragment(new SortFragment(),getString(R.string.sort));
        viewPager.setAdapter(adapter);
    }

    // Fragment 分页适配器
    static class Adapter extends FragmentPagerAdapter {
        // Fragment 集合
        private final List<Fragment> fragments = new ArrayList<>();
        // Fragment Title 集合
        private final List<String> fragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        // 添加 Fragment 和 Fragment Title
        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
