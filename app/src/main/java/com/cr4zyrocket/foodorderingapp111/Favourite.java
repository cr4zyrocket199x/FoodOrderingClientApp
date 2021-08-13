package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.FavouriteAdapter;

import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Favourite extends AppCompatActivity {
    FavouriteAdapter adapter;
    RecyclerView recyclerFavFood;
    RecyclerView.LayoutManager layoutManager;
    LayoutAnimationController controller;
    String userID=Common.getCurrentUserID();

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
        setContentView(R.layout.activity_favourite);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_ff));


        //Load food
        recyclerFavFood=findViewById(R.id.recyclerFavouriteFood);
        recyclerFavFood.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        controller= AnimationUtils.loadLayoutAnimation(recyclerFavFood.getContext(),R.anim.layout_from_right);
        recyclerFavFood.setLayoutAnimation(controller);
        recyclerFavFood.setLayoutManager(layoutManager);
        recyclerFavFood.startAnimation(controller.getAnimation());
        loadFavourites();
    }

    private void loadFavourites() {
        adapter=new FavouriteAdapter(new Database(this).getAllFavourites(userID),getBaseContext());
        recyclerFavFood.setAdapter(adapter);
    }

}