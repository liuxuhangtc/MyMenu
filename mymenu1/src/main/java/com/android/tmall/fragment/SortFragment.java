package com.android.tmall.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tmall.ProductDetailActivity;
import com.android.tmall.R;
import com.android.tmall.pojo.Product;
import com.android.tmall.util.TmallUtil;
import com.android.tmall.util.VolleyUtil;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SortFragment extends Fragment {
    private List<Product> productList;
    private RecyclerView.Adapter adapter;

    public SortFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_product_list, container, false);
        setupRecyclerView(recyclerView);
        return recyclerView;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        productList = new ArrayList<>();
        loadProductListData();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ProductListRecyclerViewAdapter(getActivity(), productList);
        recyclerView.setAdapter(adapter);
    }

    // 访问网络加载所有的宝贝
    private void loadProductListData() {
        JsonArrayRequest request = new JsonArrayRequest(
                VolleyUtil.getAbsoluteUrl("ProductServletJson"),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray ja) {
                        try {
                            Log.v(TmallUtil.TAG, ja.toString());
                            if (ja.length() > 0) productList.clear();
                            // 解析 JSON Data
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                Product product = new Product();
                                product.setId(jo.getInt("id"));
                                product.setName(jo.getString("name"));
                                product.setPrice(jo.getDouble("price"));
                                product.setRemark(jo.getString("remark"));
                                product.setImgPath(VolleyUtil.BASE_URL + jo.getString("imgPath"));
                                productList.add(product);
                            }
                            // 数据发生改变,Adapter 通知 View 更新UI
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        TmallUtil.toast(getActivity(), R.string.net_error);
                    }
                }
        );

        VolleyUtil.getInstance(getActivity()).addToRequestQueue(request);
    }

    // 宝贝列表适配器类
    public static class ProductListRecyclerViewAdapter
            extends RecyclerView.Adapter<ProductListRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<Product> products;

        public ProductListRecyclerViewAdapter(Context context, List<Product> products) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground,
                    mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            this.products = products;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.product_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Product product = products.get(position);
            holder.tvName.setText(product.getName());
            holder.tvPrice.setText(String.format("%.02f", product.getPrice()));

            ImageLoader imageLoader =
                    VolleyUtil.getInstance(holder.imgPath.getContext()).
                            getImageLoader();
            ImageLoader.ImageListener listener =
                    ImageLoader.getImageListener(
                            holder.imgPath, R.drawable.logo, R.mipmap.ic_launcher);
            imageLoader.get(product.getImgPath(), listener);

            // 点击 RecyclerView 选项的事件
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TmallUtil.TAG, String.format("%d,%s,%.02f,%s,%s", product.getId(), product.getName(), product.getPrice(), product.getImgPath(), product.getRemark()));

                    // 启动宝贝详细信息 Activity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("id", product.getId());
                    intent.putExtra("name", product.getName());
                    intent.putExtra("price", product.getPrice());
                    intent.putExtra("imgPath", product.getImgPath());
                    intent.putExtra("remark", product.getRemark());

                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView imgPath;
            public final TextView tvName;
            public final TextView tvPrice;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                imgPath = (ImageView) view.findViewById(R.id.imgPath);
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            }
        }
    }
}
