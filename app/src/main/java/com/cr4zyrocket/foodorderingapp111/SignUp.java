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
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {
    private static final String TAG = "SignUp";
    private Spinner spinner;
    MaterialEditText edtPhone,edtName,edtPassword;
    Button btnSignUp;
    DatabaseReference table_user;
    Dialog dialogVerify;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        setTitle(getString(R.string.t_signup));

        spinner=findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        spinner.setBackgroundColor(getResources().getColor(android.R.color.white));
        spinner.setSelection(200);

        edtPhone=findViewById(R.id.edtPhone);
        edtName=findViewById(R.id.edtName);
        edtPassword=findViewById(R.id.edtPassword);
        btnSignUp=findViewById(R.id.btnSignUp);

        //Init Firebase
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        table_user=database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())){
                    dialog=new ProgressDialog(SignUp.this);
                    dialog.setMessage("Please waiting...");
                    dialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot) {
                            String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                            String number = Objects.requireNonNull(edtPhone.getText()).toString();
                            if(number.isEmpty() || number.length() < 10){
                                dialog.dismiss();
                                edtPhone.setError(getString(R.string.e));
                                edtPhone.requestFocus();
                                return;
                            }
                            if(snapshot.child(edtPhone.getText().toString()).exists()){
                                dialog.dismiss();
                                edtPhone.setError(getString(R.string.par));
                                edtPhone.requestFocus();
                                return;
                            }
                            String phoneNumber="+" + code + number;
                            if (Objects.requireNonNull(edtName.getText()).toString().trim().length()>0 && Objects.requireNonNull(edtPassword.getText()).toString().trim().length()>7 && Common.isValidPassword(edtPassword.getText().toString().trim())){
                                //verify phone number
                                PhoneAuthProvider.verifyPhoneNumber(
                                        PhoneAuthOptions.newBuilder()
                                                .setActivity(SignUp.this)
                                                .setPhoneNumber(phoneNumber)
                                                .setTimeout(60L,TimeUnit.SECONDS)
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
                                                        Toast.makeText(SignUp.this, "Failed !", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "onVerificationFailed:"+e.getLocalizedMessage());
                                                    }

                                                    @Override
                                                    public void onCodeSent(@NonNull final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                        super.onCodeSent(s, forceResendingToken);
                                                        dialogVerify = new Dialog(SignUp.this);
                                                        dialogVerify.setContentView(R.layout.verify_popup);

                                                        final EditText etVerifyCode = dialogVerify.findViewById(R.id.etVerifyCode);
                                                        Button btnVerifyCode = dialogVerify.findViewById(R.id.btnVerifyOTP);
                                                        btnVerifyCode.setOnClickListener(v1 -> {
                                                            dialog.dismiss();
                                                            String verificationCode = etVerifyCode.getText().toString();
                                                            if(s.isEmpty()) return;
                                                            //create a credential
                                                            dialogVerify.dismiss();
                                                            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(s,verificationCode);
                                                            signInUser(credential);
                                                        });
                                                        dialogVerify.show();
                                                    }
                                                })
                                                .build()
                                );
                            }else {
                                dialog.dismiss();
                                if (Objects.requireNonNull(edtName.getText()).toString().trim().length()==0){
                                    edtName.setError(getString(R.string.ee));
                                }
                                if (Objects.requireNonNull(edtPassword.getText()).toString().trim().length()<8){
                                    edtPassword.setError(getString(R.string.pme));
                                }
                                else if (!Common.isValidPassword(edtPassword.getText().toString().trim())){
                                    edtPassword.setError(getString(R.string.eea));
                                }
                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    Toast.makeText(SignUp.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    private void signInUser(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        User user=new User(Objects.requireNonNull(edtName.getText()).toString(), Objects.requireNonNull(edtPassword.getText()).toString(), Objects.requireNonNull(edtPhone.getText()).toString(),"");
                        table_user.child(edtPhone.getText().toString()).setValue(user);
                        Toast.makeText(SignUp.this, getString(R.string.sus), Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(SignUp.this, getString(R.string.wc), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete:"+ Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    }
                });
    }
}