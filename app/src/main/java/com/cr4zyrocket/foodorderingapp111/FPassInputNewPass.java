package com.cr4zyrocket.foodorderingapp111;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class FPassInputNewPass extends AppCompatActivity {

    Button btnFPUpdatePass;
    EditText edtNewPass,edtNewPassRepeat;
    String FPPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpass_input_new_pass);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.t_newpass);

        btnFPUpdatePass=findViewById(R.id.btnFPUpdatePass);
        edtNewPass=findViewById(R.id.edtNewPass);
        edtNewPassRepeat=findViewById(R.id.edtNewPassRepeat);
        if (getIntent()!=null){
            FPPhone=getIntent().getStringExtra("FPPhone");
        }

        btnFPUpdatePass.setOnClickListener(v -> {
            final android.app.AlertDialog waitingDialog=new SpotsDialog(FPassInputNewPass.this);
            waitingDialog.show();
            if (edtNewPass.getText().toString().equals(edtNewPassRepeat.getText().toString())){
                if (edtNewPass.getText().length()>=8) {
                    Map<String, Object> passwordUpdate = new HashMap<>();
                    passwordUpdate.put("password", edtNewPass.getText().toString());
                    DatabaseReference users = FirebaseDatabase.getInstance().getReference("User");
                    users.child(FPPhone)
                            .updateChildren(passwordUpdate)
                            .addOnCompleteListener(task -> {
                                waitingDialog.dismiss();
                                Toast.makeText(FPassInputNewPass.this, getString(R.string.np1)+"", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(FPassInputNewPass.this,SignIn.class));
                            })
                            .addOnFailureListener(e -> {
                                waitingDialog.dismiss();
                                Toast.makeText(FPassInputNewPass.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            });
                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(FPassInputNewPass.this, "New password must be at least 8 characters !", Toast.LENGTH_SHORT).show();
                }
            }else {
                waitingDialog.dismiss();
                Toast.makeText(FPassInputNewPass.this, "New password not same !", Toast.LENGTH_SHORT).show();
            }
        });
    }
}