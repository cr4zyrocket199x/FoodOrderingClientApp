package com.cr4zyrocket.foodorderingapp111.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cr4zyrocket.foodorderingapp111.Model.FacebookUser;
import com.cr4zyrocket.foodorderingapp111.Model.Food;
import com.cr4zyrocket.foodorderingapp111.Model.Request;
import com.cr4zyrocket.foodorderingapp111.Model.User;
import com.cr4zyrocket.foodorderingapp111.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    public static String currentLanguage="vi";
    public static boolean systemUserLogin=true;
    public static String currentPhonePlace="";

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    public static String getCurrentUserID(){
        if (!systemUserLogin){
            if (currentFacebookUser!=null)
                return currentFacebookUser.getFacebookID();
            else return "null";
        }else {
            if (currentUser!=null)
                return currentUser.getPhone();
            else return "null";
        }
    }

    public static String getCurrentUserPhone(){
        if (!systemUserLogin){
            if (currentFacebookUser!=null)
                return currentFacebookUser.getPhone();
            else return "null";
        }else {
            if (currentUser!=null)
                return currentUser.getPhone();
            else return "null";
        }
    }
    public static String getCurrentUserName(){
        if (!systemUserLogin){
            if (currentFacebookUser!=null)
                return currentFacebookUser.getName();
            else return "null";
        }else {
            if (currentUser!=null)
                return currentUser.getName();
            else return "null";
        }
    }
    public static FacebookUser currentFacebookUser;
    public static User currentUser;
    public static Request currentRequest;
    public static String convertCodeToStatus(String status){
        switch (status) {
            case "0":
                return "Placed";
            case "1":
                return "Processing";
            case "2":
                return "On my way";
            case "3":
                return "Shipped";
            default:
                return "Cancel";
        }
    }
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
            if (info!=null){
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static final String DELETE="Delete";
    public static final String USER_KEY="User";
    public static final String PWD_KEY="Password";
}
