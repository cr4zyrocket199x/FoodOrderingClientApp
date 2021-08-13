package com.cr4zyrocket.foodorderingapp111;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.OrderDetailAdapter;

import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class OrderDetail extends AppCompatActivity {

    TextView tvOrderID,tvOrderPhone,tvOrderAddress,tvOrderTotal,tvOrderComment;
    String order_id_value,phone=Common.getCurrentUserPhone();
    RecyclerView rvListFood;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/res_font.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_order_detail);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.or);

        tvOrderID=findViewById(R.id.tvOrderID);
        tvOrderPhone=findViewById(R.id.tvOrderPhone);
        tvOrderAddress=findViewById(R.id.tvOrderAddress);
        tvOrderTotal=findViewById(R.id.tvOrderTotal);
        tvOrderComment=findViewById(R.id.tvOrderComment);
        rvListFood=findViewById(R.id.rvListFood);
        layoutManager=new LinearLayoutManager(this);
        rvListFood.setLayoutManager(layoutManager);

        if (getIntent()!=null){
            order_id_value=getIntent().getStringExtra("OrderID");
        }
        String orID=getString(R.string.orID)+order_id_value;
        String orP= getString(R.string.orP)+phone;
        String orA=getString(R.string.orA)+Common.currentRequest.getAddress();
        String orT=getString(R.string.orT)+Common.currentRequest.getTotal();
        String orC=getString(R.string.orC)+Common.currentRequest.getComment();
        tvOrderID.setText(orID);
        tvOrderPhone.setText(orP);
        tvOrderAddress.setText(orA);
        tvOrderTotal.setText(orT);
        tvOrderComment.setText(orC);

        OrderDetailAdapter adapter=new OrderDetailAdapter(Common.currentRequest.getFoodOrdered());
        adapter.notifyDataSetChanged();
        rvListFood.setAdapter(adapter);
    }
}