package com.cr4zyrocket.foodorderingapp111;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Model.CountryData;
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class FPassInputPhone extends AppCompatActivity {
    private static final String TAG = "FPassInputPhone";
    Button btnFPNext;
    Spinner spinnerFPCountries;
    EditText edtFPPhone;
    DatabaseReference table_user;
    FirebaseDatabase database;
    ProgressDialog dialog;
    Dialog dialogVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpass_input_phone);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.t_accphone);

        btnFPNext=findViewById(R.id.btnFPNext);
        spinnerFPCountries=findViewById(R.id.spinnerFPCountries);
        edtFPPhone=findViewById(R.id.edtFPPhone);
        spinnerFPCountries.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        spinnerFPCountries.setBackgroundColor(getResources().getColor(android.R.color.white));
        spinnerFPCountries.setSelection(200);

        //Init Firebase
        database=FirebaseDatabase.getInstance();
        table_user=database.getReference("User");

        btnFPNext.setOnClickListener(v -> {
            if (Common.isConnectedToInternet(getBaseContext())){
                dialog=new ProgressDialog(FPassInputPhone.this);
                dialog.setMessage("Please waiting...");
                dialog.show();
                table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                    final String code = CountryData.countryAreaCodes[spinnerFPCountries.getSelectedItemPosition()];
                    final String number = Objects.requireNonNull(edtFPPhone.getText()).toString();
                    final String phoneNumber="+" + code + number;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(Objects.requireNonNull(edtFPPhone.getText()).toString()).exists()){
                            //verify phone number
                            PhoneAuthProvider.verifyPhoneNumber(
                                    PhoneAuthOptions.newBuilder()
                                            .setActivity(FPassInputPhone.this)
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
                                                    Toast.makeText(FPassInputPhone.this, "Failed !", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "onVerificationFailed:"+e.getLocalizedMessage());
                                                }

                                                @Override
                                                public void onCodeSent(@NonNull final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                    super.onCodeSent(s, forceResendingToken);
                                                    dialogVerify = new Dialog(FPassInputPhone.this);
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
                        }else{
                            dialog.dismiss();
                            Toast.makeText(FPassInputPhone.this, getString(R.string.ip1)+"", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else {
                Toast.makeText(FPassInputPhone.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void signInUser(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Intent intent=new Intent(FPassInputPhone.this,FPassInputNewPass.class);
                        intent.putExtra("FPPhone",edtFPPhone.getText().toString());
                        startActivity(intent);
                    }else {
                        dialog.dismiss();
                        Toast.makeText(FPassInputPhone.this, "Wrong code !", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete:"+ Objects.requireNonNull(task.getException()).getLocalizedMessage());
                    }
                });
    }
}