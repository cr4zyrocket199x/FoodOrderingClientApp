package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;
import java.util.Objects;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    MaterialEditText edtPhone,edtPassword;
    Button btnSignIn,btnForgotPassword;
    CheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.t_signin);

        edtPhone=findViewById(R.id.edtPhone);
        edtPassword=findViewById(R.id.edtPassword);
        btnSignIn=findViewById(R.id.btnSignIn);
        cbRemember=findViewById(R.id.cbRemember);
        btnForgotPassword=findViewById(R.id.btnForgotPassword);

        Common.currentLanguage=Locale.getDefault().getLanguage();
        btnForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this,FPassInputPhone.class));
        });

        //Init Firebase
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.systemUserLogin=true;
                if (Common.isConnectedToInternet(getBaseContext())){
                    if(Objects.requireNonNull(edtPhone.getText()).toString().trim().length()>0 && Objects.requireNonNull(edtPassword.getText()).toString().trim().length()>0){
                        if (cbRemember.isChecked()){
                            Paper.book().write(Common.USER_KEY, Objects.requireNonNull(edtPhone.getText()).toString());
                            Paper.book().write(Common.PWD_KEY, Objects.requireNonNull(edtPassword.getText()).toString());
                        }
                        final ProgressDialog dialog=new ProgressDialog(SignIn.this);
                        dialog.setMessage("Please waiting...");
                        dialog.show();
                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.child(Objects.requireNonNull(edtPhone.getText()).toString()).exists()){
                                    //Get User Information
                                    dialog.dismiss();
                                    User user=snapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                    assert user != null;
                                    user.setPhone(edtPhone.getText().toString());
                                    if (user.getPassword().equals(Objects.requireNonNull(edtPassword.getText()).toString())){
                                        Intent homeIntent=new Intent(SignIn.this,Home.class);
                                        Common.currentUser=user;
                                        startActivity(homeIntent);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        finish();
                                        table_user.removeEventListener(this);
                                    }else{
                                        Toast.makeText(SignIn.this, getString(R.string.wp), Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    dialog.dismiss();
                                    Toast.makeText(SignIn.this, getString(R.string.unid), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else {
                        Toast.makeText(SignIn.this, getString(R.string.fillalll), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignIn.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}