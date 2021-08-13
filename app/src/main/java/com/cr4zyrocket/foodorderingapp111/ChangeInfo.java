package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Model.Request;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ChangeInfo extends AppCompatActivity {
    private static final int USER_ADDRESS_ACTIVITY_CODE=112;
    Button btnChangeAddress,btnChange,btnCancelChange;
    TextView tvUserAddress;
    EditText edtUserName;
    String apartmentNumber,provinceCity,countyDistrict,wardCommune;
    FirebaseDatabase database;
    DatabaseReference requests;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==USER_ADDRESS_ACTIVITY_CODE){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                apartmentNumber=data.getStringExtra("ApartmentNumber");
                provinceCity=data.getStringExtra("ProvinceCity");
                countyDistrict=data.getStringExtra("CountyDistrict");
                wardCommune=data.getStringExtra("WardCommune");

                tvUserAddress.setText(apartmentNumber+", "+wardCommune+", "+countyDistrict+", "+provinceCity);
            }
        }

    }
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
        setContentView(R.layout.activity_change_info);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_changeinfo));

        btnChangeAddress=findViewById(R.id.btnChangeAddress);
        btnChange=findViewById(R.id.btnChangeInfo);
        btnCancelChange=findViewById(R.id.btnCancelChange);
        tvUserAddress=findViewById(R.id.tvUserAddress);
        edtUserName=findViewById(R.id.edtUserName);
        if (Common.getCurrentUserName()!=null)
            edtUserName.setText(Common.getCurrentUserName());

        if (!Common.systemUserLogin){
            edtUserName.setEnabled(false);
        }
        //Firebase
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        btnChange.setOnClickListener(v -> {
            String ref="";
            if (apartmentNumber==null||wardCommune==null||countyDistrict==null||provinceCity==null){
                Toast.makeText(this, getString(R.string.f), Toast.LENGTH_SHORT).show();
            } else {
                if (Common.systemUserLogin){
                    ref="User";
                }
                else{
                    ref="FacebookUser";
                }

                final android.app.AlertDialog waitingDialog=new SpotsDialog(ChangeInfo.this);
                waitingDialog.show();
                Map<String, Object> addressUpdate = new HashMap<>();
                addressUpdate.put("address", tvUserAddress.getText().toString());
                Map<String, Object> userNameUpdate = new HashMap<>();
                userNameUpdate.put("name", edtUserName.getText().toString());
                DatabaseReference users = FirebaseDatabase.getInstance().getReference(ref);
                users.child(Common.getCurrentUserID())
                        .updateChildren(addressUpdate)
                        .addOnCompleteListener(task -> {
                            waitingDialog.dismiss();
                            if (Common.systemUserLogin)
                                Common.currentUser.setAddress(tvUserAddress.getText().toString());
                            else
                                Common.currentFacebookUser.setAddress(tvUserAddress.getText().toString());
                        })
                        .addOnFailureListener(e -> {
                            waitingDialog.dismiss();
                            Toast.makeText(ChangeInfo.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                        });
                if (Common.systemUserLogin) {
                    users.child(Common.getCurrentUserID())
                            .updateChildren(userNameUpdate)
                            .addOnCompleteListener(task -> {
                                waitingDialog.dismiss();
                                Common.currentUser.setName(edtUserName.getText().toString());
                            })
                            .addOnFailureListener(e -> {
                                waitingDialog.dismiss();
                                Toast.makeText(ChangeInfo.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            });
                }
                Toast.makeText(ChangeInfo.this, getString(R.string.c3), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChangeInfo.this,Home.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        btnCancelChange.setOnClickListener(v -> {
            startActivity(new Intent(ChangeInfo.this, Home.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        btnChangeAddress.setOnClickListener(v -> {
            startActivityForResult(new Intent(ChangeInfo.this, UserAddress.class), USER_ADDRESS_ACTIVITY_CODE);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
}