package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Interface.ItemClickListener;
import com.cr4zyrocket.foodorderingapp111.Model.Category;
import com.cr4zyrocket.foodorderingapp111.Model.FavouriteFood;
import com.cr4zyrocket.foodorderingapp111.Model.Food;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.FoodViewHolder;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.MenuViewHolder;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class FoodList extends AppCompatActivity {
    boolean soft=false;
    ShareActionProvider miShareAction;
    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView recyclerFood;
    RecyclerView.LayoutManager layoutManager;
    String categoryID="",userID=Common.getCurrentUserID();
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBarFood;
    Database localDB;
    ShareDialog shareDialog;
    SwipeRefreshLayout srlFoodList;
    LayoutAnimationController controller;
    Bitmap myBitmap;
    Intent shareIntent;

    //Facebook share
    CallbackManager callbackManager;

    //Create target from picasso
    Target target=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from Bitmap
            SharePhoto photo=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

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
        setContentView(R.layout.activity_food_list);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_fl));

        //Get Intent
        if (getIntent()!=null){
            categoryID=getIntent().getStringExtra("CategoryID");
        }

        //Init facebook
        callbackManager=CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);

        //Init Firebase
        database=FirebaseDatabase.getInstance();
        foodList=database.getReference("Food").child(Common.currentLanguage);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("categoryID").equalTo(categoryID), Food.class)
                .build();
        adapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                final FavouriteFood favouriteFood=new FavouriteFood();
                favouriteFood.setFoodID(adapter.getRef(position).getKey());
                favouriteFood.setUserID(userID);
                favouriteFood.setFoodName(model.getName());
                favouriteFood.setFoodImage(model.getImage());
                favouriteFood.setFoodDescription(model.getDescription());
                favouriteFood.setFoodPrice(model.getPrice());
                favouriteFood.setFoodDiscount(model.getDiscount());
                favouriteFood.setCategoryID(model.getCategoryID());
                holder.tvFoodName.setText(model.getName());
                Locale locale=new Locale("vi","VN");
                NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
                holder.tvFoodPrice.setText(fmt.format(Integer.parseInt(model.getPrice())));
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.ivFoodImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        holder.ivFoodImage.setImageResource(R.drawable.image_notfound);
                    }
                });

                //Click to share
                holder.ivShare.setOnClickListener(v -> {
//                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                    Uri screenshotUri = Uri.parse(model.getImage());
//                    sharingIntent.setType("image/*");
//                    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
//                    startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                    Picasso.with(getBaseContext()).load(model.getImage()).into(target);
                });

                //Add Favorite
                if (localDB.isFavorite(favouriteFood)){
                    holder.ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                }

                //Click to add or remove favorite
                holder.ivFavorite.setOnClickListener(v -> {
                    if (!localDB.isFavorite(favouriteFood)){
                        localDB.addToFavorite(favouriteFood);
                        holder.ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Toast.makeText(FoodList.this, model.getName()+getString(R.string.fl1)+"", Toast.LENGTH_SHORT).show();
                    }else {
                        localDB.removeFromFavorite(favouriteFood);
                        holder.ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                        Toast.makeText(FoodList.this, model.getName()+getString(R.string.fl2)+"", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.setItemClickListener((view, position1, isLongClick) -> {
                    //Start new Activity
                    Intent foodDetailIntent=new Intent(FoodList.this,FoodDetail.class);
                    foodDetailIntent.putExtra("FoodID",adapter.getRef(position1).getKey());
                    startActivity(foodDetailIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);
            }

        };

        //Local DB
        localDB=new Database(this);
        
        //Load food
        recyclerFood=findViewById(R.id.recyclerFood);
        recyclerFood.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        controller= AnimationUtils.loadLayoutAnimation(recyclerFood.getContext(),R.anim.layout_from_right);
        recyclerFood.setLayoutAnimation(controller);
        recyclerFood.setLayoutManager(layoutManager);
        recyclerFood.startAnimation(controller.getAnimation());

        //View
        srlFoodList=findViewById(R.id.srlFoodList);
        srlFoodList.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        srlFoodList.setOnRefreshListener(() -> {
            if (categoryID!=null&&categoryID.length()>0){
                if (Common.isConnectedToInternet(getBaseContext())) {
                    srlFoodList.setRefreshing(true);
                    loadFoodList();
                }else {
                    Toast.makeText(FoodList.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        srlFoodList.post(() -> {
            if (categoryID!=null&&categoryID.length()>0){
                if (Common.isConnectedToInternet(getBaseContext())) {
                    srlFoodList.setRefreshing(true);
                    loadFoodList();
                }else {
                    Toast.makeText(FoodList.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Search
        materialSearchBarFood=findViewById(R.id.searchBarFood);
        materialSearchBarFood.setHint(getString(R.string.fl3));
        loadSuggest();
        materialSearchBarFood.setLastSuggestions(suggestList);
        materialSearchBarFood.setCardViewElevation(10);
        materialSearchBarFood.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggests=new ArrayList<>();
                for (String search:suggestList){
                    if (search.toLowerCase().contains(s.toString().toLowerCase())) {
                        suggests.add(search);
                    }
                }
                materialSearchBarFood.setLastSuggestions(suggests);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBarFood.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When searchBar is close
                //Restore original adapter
                if (!enabled)
                    recyclerFood.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearchFood(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearchFood(CharSequence food){
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("name").equalTo(food.toString()), Food.class)
                .build();
        searchAdapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                holder.tvFoodName.setText(model.getName());
                Locale locale = new Locale("vi", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                holder.tvFoodPrice.setText(fmt.format(Integer.parseInt(model.getPrice())));
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.ivFoodImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        holder.ivFoodImage.setImageResource(R.drawable.image_notfound);
                    }
                });
                holder.setItemClickListener((view, position12, isLongClick) -> {
                    //Start new Activity
                    Intent foodDetailIntent = new Intent(FoodList.this, FoodDetail.class);
                    foodDetailIntent.putExtra("FoodID", searchAdapter.getRef(position12).getKey());
                    foodDetailIntent.putExtra("CategoryID", categoryID);
                    startActivity(foodDetailIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);
            }
        };
        searchAdapter.startListening();
        recyclerFood.setAdapter(searchAdapter);
    }
    private void loadSuggest() {
        foodList.orderByChild("categoryID").equalTo(categoryID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Food item=snapshot1.getValue(Food.class);
                    assert item != null;
                    suggestList.add(item.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerFood.startAnimation(controller.getAnimation());
    }

    @Override
    protected void onStart() {
        //Toast.makeText(this, "haha", Toast.LENGTH_SHORT).show();
        super.onStart();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.food_list_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.menu_soft_price) {
//            if (!soft){
//                item.setIcon(R.drawable.soft_up_icon);
//            }else {
//                item.setIcon(R.drawable.soft_down_icon);
//            }
//            soft=!soft;
////            Query query1 = foodList.orderByChild("CategoryID").equalTo(categoryID);
////            Query query2 = query1.orderByChild("Price");
////            FirebaseRecyclerOptions<Food> option_price_down = new FirebaseRecyclerOptions.Builder<Food>()
////                    .setQuery(query2, Food.class)
////                    .build();
////            adapter.updateOptions(option_price_down);
////            adapter.notifyDataSetChanged();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void loadFoodList() {
        adapter.startListening();
        recyclerFood.setAdapter(adapter);
        srlFoodList.setRefreshing(false);

        recyclerFood.scheduleLayoutAnimation();
    }
}