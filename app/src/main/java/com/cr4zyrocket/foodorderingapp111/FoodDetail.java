package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Model.Food;
import com.cr4zyrocket.foodorderingapp111.Model.Order;
import com.cr4zyrocket.foodorderingapp111.Model.Rating;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.CommentAdapter;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

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

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    RecyclerView rvComment;
    RecyclerView.LayoutManager layoutManager;
    CommentAdapter adapter;
    List<Rating> ratingList;
    TextView tvFoodName,tvFoodPrice,tvFoodDescription;
    ImageView ivFoodImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fabCart,fabRating;
    ElegantNumberButton numberButton;
    String foodID="",categoryID;
    FirebaseDatabase database;
    DatabaseReference food,ratingData;
    Food currentFood;
    RatingBar ratingBar;
    float sum,count;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        setContentView(R.layout.activity_food_detail);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_fooddetail));

        //Get Intent
        if (getIntent()!=null){
            categoryID=getIntent().getStringExtra("CategoryID");
        }

        //Firebase
        database=FirebaseDatabase.getInstance();
        food=database.getReference("Food").child(Common.currentLanguage);
        ratingData=database.getReference("Rating");

        //Init view
        rvComment=findViewById(R.id.rvListComment);
        rvComment.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        rvComment.setLayoutManager(layoutManager);
        numberButton=findViewById(R.id.nbCount);
        fabCart=findViewById(R.id.btnCart);
        fabRating=findViewById(R.id.fabRating);
        tvFoodDescription=findViewById(R.id.tvFoodDescription);
        tvFoodName=findViewById(R.id.tvFoodName);
        tvFoodPrice=findViewById(R.id.tvFoodPrice);
        ivFoodImage=findViewById(R.id.iv_Food);
        ratingBar=findViewById(R.id.ratingBar);
        collapsingToolbarLayout=findViewById(R.id.ctlFoodDetail);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get FoodID from Intent
        if(getIntent()!=null){
            foodID=getIntent().getStringExtra("FoodID");
            if (foodID!=null && foodID.length()>0){
                if (Common.isConnectedToInternet(getBaseContext())) {
                    getFoodDetail(foodID);
                    getRatingFood(foodID);
                    showAllComment(foodID);
                }else {
                    Toast.makeText(this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        }

        fabCart.setOnClickListener(v -> {
            new Database(getBaseContext()).addToCart(
                    new Order(
                            foodID,
                            currentFood.getName(),
                            currentFood.getImage(),
                            numberButton.getNumber(),
                            currentFood.getPrice(),
                            currentFood.getDiscount()));
            Toast.makeText(FoodDetail.this, getString(R.string.f1), Toast.LENGTH_SHORT).show();
        });

        fabRating.setOnClickListener(v -> showRatingDialog());
    }
    private void showAllComment(String foodID){
        ratingData.orderByChild("foodID").equalTo(foodID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ratingList=new ArrayList<>();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ratingList.add(dataSnapshot.getValue(Rating.class));
                }
                adapter=new CommentAdapter(ratingList,getBaseContext());
                rvComment.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getRatingFood(String foodID) {
        com.google.firebase.database.Query foodRating=ratingData.orderByChild("foodID").equalTo(foodID);
        foodRating.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Rating item=dataSnapshot.getValue(Rating.class);
                    assert item != null;
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count!=0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRatingDialog(){
        new AppRatingDialog.Builder()
                .setPositiveButtonText(R.string.f2)
                .setNegativeButtonText(R.string.cancel)
                .setNoteDescriptions(Arrays.asList(getString(R.string.f3),getString(R.string.f4),getString(R.string.f5),getString(R.string.f6),getString(R.string.f7)))
                .setDefaultRating(5)
                .setTitle(R.string.f8)
                .setDescription(R.string.f9)
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorAccent)
                .setHint(R.string.f10)
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(this)
                .show();
    }

    private void getFoodDetail(String foodID) {
        food.child(foodID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentFood=snapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(ivFoodImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        ivFoodImage.setImageResource(R.drawable.image_notfound);
                    }
                });
                Locale locale=new Locale("vi","VN");
                NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                tvFoodPrice.setText(fmt.format(Integer.parseInt(currentFood.getPrice())));
                tvFoodDescription.setText(currentFood.getDescription());
                tvFoodName.setText(currentFood.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {
        final String userID=Common.getCurrentUserID();
        final String userName=Common.getCurrentUserName();
        //Get rating and upload to firebase
        final Rating rating=new Rating(userID,userName,foodID,String.valueOf(i),s,userID+"_"+foodID);
        ratingData.orderByChild("userID_foodID").equalTo(userID+"_"+foodID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ratingData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ratingData.child(String.valueOf(System.currentTimeMillis())).setValue(rating);
                Toast.makeText(FoodDetail.this, getString(R.string.f11), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}