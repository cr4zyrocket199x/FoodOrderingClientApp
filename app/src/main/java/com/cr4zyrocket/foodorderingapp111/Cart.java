package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Model.Order;
import com.cr4zyrocket.foodorderingapp111.Model.Request;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.CartAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Cart extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    public TextView tvTotal;
    Button btnOrderPlace;
    int total=0;
    List<Order> cart=new ArrayList<>();
    CartAdapter adapter;

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
        setContentView(R.layout.activity_cart);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.t_cart));


        //Init
        recyclerView=findViewById(R.id.recyclerListCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        tvTotal=findViewById(R.id.tvTotal);
        btnOrderPlace=findViewById(R.id.btnPlaceOrder);

        loadListFoodOrder();
        btnOrderPlace.setOnClickListener(v -> {
            if (total==0)
                Toast.makeText(Cart.this, getString(R.string.c1), Toast.LENGTH_LONG).show();
            else {
                Intent intentOrderInfo = new Intent(Cart.this, OrderInfo.class);
                intentOrderInfo.putExtra("Total",tvTotal.getText().toString());
                startActivity(intentOrderInfo);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void loadListFoodOrder() {
        cart=new Database(getBaseContext()).getCarts();
        adapter=new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);
        //Calculate total price
        total=0;
        for (Order order:cart){
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        }
        Locale locale=new Locale("vi","VN");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        tvTotal.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int order) {
        cart.remove(order);
        new Database(this).cleanCart();
        for (Order item:cart){
            new Database(this).addToCart(item);
        }
        adapter.notifyDataSetChanged();
        loadListFoodOrder();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Cart.this,Home.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}