package com.cr4zyrocket.foodorderingapp111.ViewHolder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cr4zyrocket.foodorderingapp111.Model.Order;
import com.cr4zyrocket.foodorderingapp111.R;

import java.util.List;

class OrderDetailViewHolder extends RecyclerView.ViewHolder{
    public TextView tvOrderFoodName,tvOrderFoodQuantity,tvOrderFoodPrice,tvOrderDiscount;
    public OrderDetailViewHolder(@NonNull View itemView) {
        super(itemView);
        tvOrderFoodName=itemView.findViewById(R.id.tvOrderFoodName);
        tvOrderFoodQuantity=itemView.findViewById(R.id.tvOrderFoodQuantity);
        tvOrderFoodPrice=itemView.findViewById(R.id.tvOrderFoodPrice);
        tvOrderDiscount=itemView.findViewById(R.id.tvOrderDiscount);
    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailViewHolder>{
    List<Order> orders;

    public OrderDetailAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_layout,parent,false);
        return new OrderDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        Order order=orders.get(position);
        String fn=holder.itemView.getContext().getString(R.string.fn)+order.getFoodName();
        String fq=holder.itemView.getContext().getString(R.string.fq)+order.getQuantity();
        String fp=holder.itemView.getContext().getString(R.string.fp)+order.getPrice();
        String fd=holder.itemView.getContext().getString(R.string.fd)+order.getDiscount();
        holder.tvOrderFoodName.setText(fn);
        holder.tvOrderFoodQuantity.setText(fq);
        holder.tvOrderFoodPrice.setText(fp);
        holder.tvOrderDiscount.setText(fd);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
