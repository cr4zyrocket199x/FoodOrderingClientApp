package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Interface.ItemClickListener;
import com.cr4zyrocket.foodorderingapp111.Model.Category;
import com.cr4zyrocket.foodorderingapp111.Model.Order;
import com.cr4zyrocket.foodorderingapp111.Model.Request;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.MenuViewHolder;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class OrderStatus extends AppCompatActivity {

    String phone=Common.getCurrentUserPhone();
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests_databases;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

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
        setContentView(R.layout.activity_order_status);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.or_status);

        //Firebase
        database=FirebaseDatabase.getInstance();
        requests_databases =database.getReference("Requests");
        recyclerView=findViewById(R.id.recyclerListOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent().getStringExtra("userPhone")==null){
            loadOrderList(phone);
        }else {
            loadOrderList(getIntent().getStringExtra("userPhone"));
        }
    }

    private void loadOrderList(String phone) {
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(requests_databases.orderByChild("phone").equalTo(phone), Request.class)
                .build();
        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Request model) {
                String orID= getString(R.string.orID)+adapter.getRef(position).getKey();
                String orS= getString(R.string.orS)+Common.convertCodeToStatus(model.getStatus());
                String orA=getString(R.string.orA)+model.getAddress();
                String orP=getString(R.string.orP)+model.getPhone();
                String orC=getString(R.string.orC)+model.getComment();
                holder.tvOrderID.setText(orID);
                holder.tvOrderStatus.setText(orS);
                holder.tvOrderAddress.setText(orA);
                holder.tvOrderPhone.setText(orP);
                holder.tvOrderComment.setText(orC);

                Common.currentRequest=model;
                holder.setItemClickListener((view, position1, isLongClick) -> {
                    if (isLongClick){
                        Intent orderDetailIntent=new Intent(OrderStatus.this,OrderDetail.class);
                        orderDetailIntent.putExtra("OrderID",adapter.getRef(position1).getKey());
                        startActivity(orderDetailIntent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}