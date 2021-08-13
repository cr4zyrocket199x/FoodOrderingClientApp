package com.cr4zyrocket.foodorderingapp111;
import com.cr4zyrocket.foodorderingapp111.Model.FacebookUser;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cr4zyrocket.foodorderingapp111.Common.Common;
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FacebookLogin";
    CallbackManager callbackManager;
    LoginButton btnFacebookLogin;
    Button btnSignIn,btnSignUp;
    TextView txtSlogan;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference facebookUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Food ordering app");

        mAuth=FirebaseAuth.getInstance();

        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUp);
        txtSlogan=findViewById(R.id.txtSlogan);

        //Init Firebase database
        database=FirebaseDatabase.getInstance();

        // Initialize Facebook Login button
        callbackManager=CallbackManager.Factory.create();
        btnFacebookLogin=findViewById(R.id.btnFacebookLogin);
        btnFacebookLogin.setPermissions("email", "public_profile");
        btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        //Slogan
        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtSlogan.setTypeface(typeface);

        //Init Paper
        Paper.init(this);

        btnSignIn.setOnClickListener(v -> {
            Intent intentSignIn=new Intent(MainActivity.this,SignIn.class);
            startActivity(intentSignIn);
        });
        btnSignUp.setOnClickListener(v -> {
            Intent intentSignUp=new Intent(MainActivity.this,SignUp.class);
            startActivity(intentSignUp);
        });

        //Check remember
        String phone=Paper.book().read(Common.USER_KEY);
        String pwd=Paper.book().read(Common.PWD_KEY);
        if (phone!=null && pwd!=null){
            if (!phone.isEmpty() && !pwd.isEmpty())
                login(phone,pwd);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
//        if (AccessToken.getCurrentAccessToken()!=null){
//            Intent intent = new Intent(this, Home.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (AccessToken.getCurrentAccessToken()!=null){
//            Intent intent = new Intent(this, Home.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            Intent intent = new Intent(this, Home.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            startActivity(intent);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void login(final String phone, final String pwd) {
        Common.systemUserLogin=true;
        //Init Firebase
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=database.getReference("User");
        if (Common.isConnectedToInternet(getBaseContext())){
            final ProgressDialog dialog=new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please waiting...");
            dialog.show();
            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Check if user does not exist in database
                    if(snapshot.child(phone).exists()){
                        //Get User Information
                        dialog.dismiss();
                        User user=snapshot.child(phone).getValue(User.class);
                        assert user != null;
                        user.setPhone(phone);
                        if (user.getPassword().equals(pwd)){
                            Intent homeIntent=new Intent(MainActivity.this,Home.class);
                            Common.currentUser=user;
                            startActivity(homeIntent);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this, getString(R.string.wp)+"", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, getString(R.string.unid)+"", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            Toast.makeText(MainActivity.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        updateUI(user,token);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateUI(final FirebaseUser user, AccessToken token) {
        //UserInfo userInfo= (UserInfo) user.getProviderData();
        Common.systemUserLogin=false;
        Common.currentFacebookUser=new FacebookUser(Profile.getCurrentProfile().getId(),user.getDisplayName(),user.getPhoneNumber(),"","");
        String photoUrl = user.getPhotoUrl()+"/picture?height=1000&width=1000&access_token="+token.getToken();
        Common.currentFacebookUser.setImageURI(photoUrl);
        facebookUsers=database.getReference("FacebookUser");
        facebookUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(Profile.getCurrentProfile().getId()).exists()){
                    Intent homeIntent=new Intent(MainActivity.this,AddInfoFacebookLogin.class);
                    startActivity(homeIntent);
                }else {
                    Common.currentFacebookUser.setPhone(snapshot.child(Profile.getCurrentProfile().getId()).child("phone").getValue(String.class));
                    Intent homeIntent=new Intent(MainActivity.this,Home.class);
                    startActivity(homeIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}