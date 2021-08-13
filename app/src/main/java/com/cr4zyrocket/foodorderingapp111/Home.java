package com.cr4zyrocket.foodorderingapp111;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Model.Banner;
import com.cr4zyrocket.foodorderingapp111.Model.Category;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.MenuViewHolder;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.paperdb.Paper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ImageView ivProfile;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference category;
    TextView tvFullName;
    RecyclerView recyclerMenu;
    DrawerLayout drawerLayout;
    SwipeRefreshLayout srlHome;
    CounterFab fab;
    LayoutAnimationController controller;
    SliderLayout sliderLayout;
    HashMap<String,String> image_list;
    String phone = Common.getCurrentUserPhone();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);

        //Init Firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category").child(Common.currentLanguage);
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();
        adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder holder, int position, @NonNull Category model) {
                holder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        holder.imageView.setImageResource(R.drawable.image_notfound);
                    }
                });
                holder.setItemClickListener((view, position1, isLongClick) -> {
                    //Get CategoryID and send to new Activity
                    Intent foodListIntent=new Intent(Home.this, FoodList.class);
                    foodListIntent.putExtra("CategoryID",adapter.getRef(position1).getKey());
                    startActivity(foodListIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(view);
            }
        };
        Paper.init(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent cartIntent=new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        fab.setCount(new Database(this).getCountCart());
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView =findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //View
        srlHome=findViewById(R.id.srlHome);
        srlHome.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        srlHome.setOnRefreshListener(() -> {
            if (Common.isConnectedToInternet(getBaseContext())){
                loadMenu();
            }else {
                Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
            }
        });
        //Default, load for first item
        srlHome.post(() -> {
            if (Common.isConnectedToInternet(getBaseContext())){
                loadMenu();
            }else {
                Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
            }
        });

        //Set Name for user
        View headerView=navigationView.getHeaderView(0);
        tvFullName=headerView.findViewById(R.id.txtFullName);
        ivProfile=headerView.findViewById(R.id.ivMenuIcon);

        //Load menu
        recyclerMenu=findViewById(R.id.recycler_menu);
        controller= AnimationUtils.loadLayoutAnimation(recyclerMenu.getContext(),R.anim.layout_fall_down);
        recyclerMenu.setLayoutAnimation(controller);
        recyclerMenu.setLayoutManager(new GridLayoutManager(this,2));
        recyclerMenu.startAnimation(controller.getAnimation());

        //Set information
        tvFullName.setText(Common.getCurrentUserName());
        if (!Common.systemUserLogin){
            navigationView.getMenu().findItem(R.id.nav_changePassword).setVisible(false);
            Picasso.with(getBaseContext()).load(Common.currentFacebookUser.getImageURI()).into(ivProfile);
        }else {
            navigationView.getMenu().findItem(R.id.nav_changePassword).setVisible(true);
            ivProfile.setImageResource(R.drawable.logo_eat_it);
        }

        //Setup Slider
        setUpSlider();


    }
    private void setUpSlider() {
        sliderLayout=findViewById(R.id.sliderHome);
        image_list=new HashMap<>();
        final DatabaseReference banners=database.getReference("Banner").child(Common.currentLanguage);
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Banner banner=dataSnapshot.getValue(Banner.class);
                    assert banner != null;
                    image_list.put(banner.getName()+"_"+banner.getId(),banner.getImage());
                }
                for (String key:image_list.keySet()){
                    String[] keySplit=key.split("_");
                    String foodName=keySplit[0];
                    final String foodID=keySplit[1];
                    //Create slider
                    final TextSliderView textSliderView=new TextSliderView(getBaseContext());
                    textSliderView
                            .description(foodName)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(slider -> {
                                Intent intent=new Intent(Home.this,FoodDetail.class);
                                intent.putExtras(textSliderView.getBundle());
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            });
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodID",foodID);
                    sliderLayout.addSlider(textSliderView);

                    //Remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(4000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromFacebook();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvFullName.setText(Common.getCurrentUserName());
        adapter.startListening();
        recyclerMenu.startAnimation(controller.getAnimation());
        fab.setCount(new Database(this).getCountCart());
        sliderLayout.startAutoCycle();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.refreshMenu){
            loadMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMenu(){

        adapter.startListening();
        recyclerMenu.setAdapter(adapter);
        srlHome.setRefreshing(false);

        Objects.requireNonNull(recyclerMenu.getAdapter()).notifyDataSetChanged();
        recyclerMenu.scheduleLayoutAnimation();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.nav_myLocation){
            startActivity(new Intent(Home.this,MyLocation.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(id==R.id.nav_cart){
            Intent cartIntent=new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        else if(id==R.id.nav_orders){
            Intent orderIntent=new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        else if(id==R.id.nav_changePassword){
            showChangePasswordDialog();
        }
        else if(id==R.id.nav_changeInfo){
            showChangeInfoDialog();
        }
        else if(id==R.id.nav_changeLanguage){
            showChangeLanguageDialog();
        }
        else if(id==R.id.nav_log_out){
            //Delete Remember User
            Paper.book().destroy();

            disconnectFromFacebook();

            Intent signInIntent=new Intent(Home.this,MainActivity.class);
            signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signInIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }else if (id==R.id.nav_favoriteFood){
            Intent favouriteIntent=new Intent(Home.this,Favourite.class);
            startActivity(favouriteIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangeLanguageDialog() {
        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_changePwd=inflater.inflate(R.layout.change_language_layout,null);
        final RadioButton rbVietnamese=layout_changePwd.findViewById(R.id.rbVi);
        final RadioButton rbEnglish=layout_changePwd.findViewById(R.id.rbEn);
        rbVietnamese.setChecked(true);
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(this)
                .setTitle(R.string.change_language)
                .setMessage(R.string.select_language)
                .setView(layout_changePwd)
                .setPositiveButton(R.string.change, (dialog, which) -> {
                    final android.app.AlertDialog waitingDialog=new SpotsDialog(Home.this);
                    waitingDialog.show();
                    if (rbVietnamese.isChecked()){
                        changeLanguage("vi");
                        Common.currentLanguage="vi";
                    }else {
                        changeLanguage("en");
                        Common.currentLanguage="en";
                    }
                    waitingDialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        alertDialog.create();
        alertDialog.show();
    }

    private void showChangeInfoDialog() {
        Intent changeInfoIntent=new Intent(Home.this,ChangeInfo.class);
        startActivity(changeInfoIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void disconnectFromFacebook() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }

    public void changeLanguage(String language){
        Locale locale=new Locale(language);
        Configuration configuration= new Configuration();
        configuration.locale=locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        Intent intent=new Intent(Home.this,Home.class);
        startActivity(intent);
    }
    private void showChangePasswordDialog() {
        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_changePwd=inflater.inflate(R.layout.change_password_layout,null);
        final MaterialEditText etCurPass=layout_changePwd.findViewById(R.id.etCurrentPassword);
        final MaterialEditText etNewPass=layout_changePwd.findViewById(R.id.etNewPassword);
        final MaterialEditText etNewPassRepeat=layout_changePwd.findViewById(R.id.etNewPasswordRepeat);
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(this)
                .setTitle(R.string.change_pass)
                .setMessage(R.string.fill_info)
                .setView(layout_changePwd)
                .setPositiveButton(R.string.change, (dialog, which) -> {
                    final android.app.AlertDialog waitingDialog=new SpotsDialog(Home.this);
                    waitingDialog.show();
                    if (Objects.requireNonNull(etCurPass.getText()).toString().length()==0 || Objects.requireNonNull(etNewPass.getText()).toString().length()==0 || Objects.requireNonNull(etNewPassRepeat.getText()).toString().length()==0){
                        Toast.makeText(Home.this, getString(R.string.f), Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                    else {
                        if (etCurPass.getText().toString().equals(Common.currentUser.getPassword())){
                            if (etNewPass.getText().toString().equals(etNewPassRepeat.getText().toString())){
                                if (etNewPass.getText().toString().length()>=8) {
                                    if (Common.isValidPassword(etNewPass.getText().toString().trim())) {
                                        Map<String, Object> passwordUpdate = new HashMap<>();
                                        passwordUpdate.put("password", etNewPass.getText().toString());
                                        DatabaseReference users = FirebaseDatabase.getInstance().getReference("User");
                                        users.child(Common.currentUser.getPhone())
                                                .updateChildren(passwordUpdate)
                                                .addOnCompleteListener(task -> {
                                                    waitingDialog.dismiss();
                                                    Common.currentUser.setPassword(etNewPass.getText().toString());
                                                    Toast.makeText(Home.this, getString(R.string.passwaschange), Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    waitingDialog.dismiss();
                                                    Toast.makeText(Home.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                    else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, getString(R.string.eea), Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    waitingDialog.dismiss();
                                    Toast.makeText(Home.this, getString(R.string.pme), Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, getString(R.string.npns), Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            waitingDialog.dismiss();
                            Toast.makeText(Home.this, getString(R.string.wcp), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        alertDialog.create();
        alertDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        sliderLayout.stopAutoCycle();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }
}