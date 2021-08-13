package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Model.CountryData;
import com.cr4zyrocket.foodorderingapp111.Model.FacebookUser;
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddInfoFacebookLogin extends AppCompatActivity {
    DatabaseReference facebookUsers;
    private static final String TAG = "AddInfoFacebookLogin";
    private Spinner spinner;
    MaterialEditText edtPhone;
    Button btnConfirm,btnCancel;
    String number;
    Dialog dialogVerify;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info_facebook_login);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_fbinfo));

        spinner=findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        spinner.setBackgroundColor(getResources().getColor(android.R.color.white));
        spinner.setSelection(200);

        edtPhone=findViewById(R.id.edtPhoneFacebookLogin);
        btnConfirm=findViewById(R.id.btnConfirmAddInfoFB);
        btnCancel=findViewById(R.id.btnCancelAddInfoFB);

        //Init Firebase
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        facebookUsers=database.getReference("FacebookUser");

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){
                    dialog=new ProgressDialog(AddInfoFacebookLogin.this);
                    dialog.setMessage("Please waiting...");
                    dialog.show();

                    facebookUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot) {
                            String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                            number = Objects.requireNonNull(edtPhone.getText()).toString();
                            if(number.isEmpty() || number.length() < 10){
                                dialog.dismiss();
                                edtPhone.setError(getString(R.string.a1));
                                edtPhone.requestFocus();
                                return;
                            }
                            final String phoneNumber="+" + code + number;
                            //verify phone number
                            PhoneAuthProvider.verifyPhoneNumber(
                                    PhoneAuthOptions.newBuilder()
                                            .setActivity(AddInfoFacebookLogin.this)
                                            .setPhoneNumber(phoneNumber)
                                            .setTimeout(60L, TimeUnit.SECONDS)
                                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                @Override
                                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                                    dialog.dismiss();
                                                    dialogVerify.dismiss();
                                                    signInUser(phoneAuthCredential);
                                                }

                                                @Override
                                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                                    dialog.dismiss();
                                                    dialogVerify.dismiss();
                                                    Toast.makeText(AddInfoFacebookLogin.this, "Failed !", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onVerificationFailed:"+e.getLocalizedMessage());
                                                }

                                                @Override
                                                public void onCodeSent(@NonNull final String s, @NonNull final PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                    super.onCodeSent(s, forceResendingToken);
                                                    dialogVerify = new Dialog(AddInfoFacebookLogin.this);
                                                    dialogVerify.setContentView(R.layout.verify_popup);
                                                    final EditText etVerifyCode = dialogVerify.findViewById(R.id.etVerifyCode);
                                                    Button btnVerifyCode = dialogVerify.findViewById(R.id.btnVerifyOTP);
                                                    btnVerifyCode.setOnClickListener(v1 -> {
                                                        dialog.dismiss();
                                                        String verificationCode = etVerifyCode.getText().toString();
                                                        if(s.isEmpty()) return;
                                                        PhoneAuthCredential credential =
                                                                PhoneAuthProvider.getCredential(s,verificationCode);
                                                        signInUser(credential);
                                                        dialogVerify.dismiss();
                                                    });
                                                    dialogVerify.show();
                                                }
                                            })
                                            .build()
                            );
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    Toast.makeText(AddInfoFacebookLogin.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                finish();
            }
        });
    }
    private void signInUser(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Common.currentFacebookUser.setPhone(number);
                            FacebookUser facebookUser=new FacebookUser(
                                    Profile.getCurrentProfile().getId(),
                                    Common.currentFacebookUser.getName(),
                                    number,"",Common.currentFacebookUser.getImageURI());
                            facebookUsers.child(Profile.getCurrentProfile().getId()).setValue(facebookUser);
                            Toast.makeText(AddInfoFacebookLogin.this, getString(R.string.a2), Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(AddInfoFacebookLogin.this,Home.class);
                            startActivity(intent);
                        }else {
                            dialog.dismiss();
                            Toast.makeText(AddInfoFacebookLogin.this, getString(R.string.a3), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete:"+ Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        }
                    }
                });
    }
}