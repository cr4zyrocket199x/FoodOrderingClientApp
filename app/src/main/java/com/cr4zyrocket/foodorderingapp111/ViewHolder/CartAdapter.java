package com.cr4zyrocket.foodorderingapp111.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.cr4zyrocket.foodorderingapp111.Cart;
import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Interface.ItemClickListener;
import com.cr4zyrocket.foodorderingapp111.Model.Order;
import com.cr4zyrocket.foodorderingapp111.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener {

    public ImageView ivCartFoodImage;
    public TextView tvCartFoodName,tvCartFoodPrice;
    public ElegantNumberButton nbCountItemCart;

    private ItemClickListener itemClickListener;

    public void setTvCartFoodName(TextView tvCartFoodName) {
        this.tvCartFoodName = tvCartFoodName;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        tvCartFoodName=itemView.findViewById(R.id.tvCartFoodName);
        tvCartFoodPrice=itemView.findViewById(R.id.tvCartFoodPrice);
        nbCountItemCart=itemView.findViewById(R.id.nbCountItemCart);
        ivCartFoodImage=itemView.findViewById(R.id.ivCartFoodImage);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAbsoluteAdapterPosition(), Common.DELETE);
    }
}
public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private final List<Order> orderList;
    private final Cart cart;

    public CartAdapter(List<Order> orderList, Cart cart) {
        this.orderList = orderList;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(cart);
        View itemView=inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder holder, final int position) {
        Locale locale=new Locale("vi","VN");
        final NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        int price=(Integer.parseInt(orderList.get(position).getPrice()));
        holder.tvCartFoodPrice.setText(fmt.format(price));
        Picasso.with(cart.getBaseContext()).load(orderList.get(position).getImage()).into(holder.ivCartFoodImage);
        holder.nbCountItemCart.setNumber(orderList.get(position).getQuantity());
        holder.nbCountItemCart.setOnValueChangeListener((view, oldValue, newValue) -> {
            Order order=orderList.get(position);
            order.setQuantity(String.valueOf(newValue));
            new Database(cart).updateCart(order);
            //Calculate total price
            int total=0;
            List<Order> orders=new Database(cart).getCarts();
            for (Order order1:orders){
                total+=(Integer.parseInt(order1.getPrice()))*(Integer.parseInt(order1.getQuantity()));
            }
            Locale locale1 =new Locale("vi","VN");
            NumberFormat fmt1 =NumberFormat.getCurrencyInstance(locale1);
            cart.tvTotal.setText(fmt1.format(total));
        });
        holder.tvCartFoodName.setText(orderList.get(position).getFoodName());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
