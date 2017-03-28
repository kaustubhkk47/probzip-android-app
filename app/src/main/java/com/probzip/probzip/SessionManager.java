package com.probzip.probzip;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "Probzip";

    // All Shared Preferences Keys

    private static final String IS_LOGIN = "IsloggedIn";

    // User name
    public static final String KEY_SECRET = "secret";

    //All order details
    public static final String ORDER_TYPE = "type";
    public static final String CUSTOMER_NAME = "customername";
    public static final String ADDRESS = "address";
    public static final String CUSTOMER_PHONE = "phone";
    public static final String CUSTOMER_LAT = "custlat";
    public static final String CUSTOMER_LONG = "custlong";
    public static final String ORDER_ID = "orderid";

    //Own co-ordinates
    public static final String OWN_LAT = "ownlat";
    public static final String OWN_LONG = "ownlong";

    //All statuses
    public static final String ACKNOWLEDGED_STATUS = "acknowledged";
    public static final String REACHED_STATUS = "reached";
    public static final String COMPLETED_STATUS = "completed";
    public static final String ORDER_PENDING_STATUS = "pending";

    public static final String TRANSMISSION_STATUS = "transmission";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String secret){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_SECRET, secret);

        //Update order details
        editor.putString(ORDER_TYPE, "None");
        editor.putString(CUSTOMER_NAME, "None");
        editor.putString(ADDRESS, "None");
        editor.putString(CUSTOMER_PHONE, "");
        editor.putFloat(CUSTOMER_LAT, 0);
        editor.putFloat(CUSTOMER_LONG, 0);
        editor.putString(ORDER_ID, "0");

        editor.putBoolean(ORDER_PENDING_STATUS, false);
        editor.putBoolean(REACHED_STATUS, false);
        editor.putBoolean(COMPLETED_STATUS, false);
        editor.putBoolean(ACKNOWLEDGED_STATUS, false);

        editor.putBoolean(TRANSMISSION_STATUS, false);

        // commit changes
        editor.commit();
    }

    public void updateCurrentOrder(String ordertype, String custname, String address,
                                   String custphone, double latitude, double longitude,
                                   String orderid){

        editor.putString(ORDER_TYPE, ordertype);
        editor.putString(CUSTOMER_NAME, custname);
        editor.putString(ADDRESS, address);
        editor.putString(CUSTOMER_PHONE, custphone);
        editor.putFloat(CUSTOMER_LAT, (float) latitude);
        editor.putFloat(CUSTOMER_LONG, (float) longitude);



        editor.putString(ORDER_ID,orderid);
        editor.putBoolean(ORDER_PENDING_STATUS, true);
        editor.putBoolean(REACHED_STATUS, false);
        editor.putBoolean(COMPLETED_STATUS, false);
        editor.putBoolean(ACKNOWLEDGED_STATUS, false);


        editor.commit();
    }

    public void checkLogin(){
        // Check login status

        if(!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            _context.startActivity(i);

        }
    }

    public void createAcknowledgement(){
        editor.putBoolean(ACKNOWLEDGED_STATUS, true);
        editor.commit();
    }


    public boolean isAcknowledged(){
        return pref.getBoolean(ACKNOWLEDGED_STATUS, false);
    }

    public boolean isReached(){
        return pref.getBoolean(REACHED_STATUS, false);
    }

    public void onReached(){
        editor.putBoolean(REACHED_STATUS, true);
        editor.commit();
    }

    public void refreshOrder(){
        editor.putString(ORDER_TYPE, "None");
        editor.putString(CUSTOMER_NAME, "None");
        editor.putString(ADDRESS, "None");
        editor.putString(CUSTOMER_PHONE, "");
        editor.putFloat(CUSTOMER_LAT, 0);
        editor.putFloat(CUSTOMER_LONG, 0);
        editor.putString(ORDER_ID, "None");

        editor.putBoolean(ORDER_PENDING_STATUS, false);
        editor.putBoolean(REACHED_STATUS, false);
        editor.putBoolean(COMPLETED_STATUS, false);
        editor.putBoolean(ACKNOWLEDGED_STATUS, false);

        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_SECRET, pref.getString(KEY_SECRET, "None"));
        user.put(ORDER_TYPE, pref.getString(ORDER_TYPE, "None"));
        user.put(CUSTOMER_NAME, pref.getString(CUSTOMER_NAME, "None"));
        user.put(ADDRESS, pref.getString(ADDRESS, "None"));
        user.put(CUSTOMER_PHONE, pref.getString(CUSTOMER_PHONE, "None"));
        user.put(ORDER_ID, pref.getString(ORDER_ID, "None"));
        // return user
        return user;
    }

    public HashMap<String, Float> getCustLatlong(){
        HashMap<String,Float> user = new HashMap<String,Float>();
        user.put(CUSTOMER_LAT, pref.getFloat(CUSTOMER_LAT, -1));
        user.put(CUSTOMER_LONG, pref.getFloat(CUSTOMER_LONG, -1));
        return user;
    }

    public HashMap<String, Float> getOwnLatlong(){
        HashMap<String,Float> user = new HashMap<String,Float>();
        user.put(OWN_LAT, pref.getFloat(OWN_LAT, -1));
        user.put(OWN_LONG, pref.getFloat(OWN_LONG, -1));
        return user;
    }

    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);

    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isPending(){
        return pref.getBoolean(ORDER_PENDING_STATUS, false);
    }

    public void updateOwnLocation(double latitude, double longitude){
        float lat1 = (float) latitude;
        float long1 = (float) longitude;

        editor.putFloat(OWN_LAT, lat1);
        editor.putFloat(OWN_LONG, long1);

        editor.commit();
    }

    public void startLocationService(){
        editor.putBoolean(TRANSMISSION_STATUS, true);
        editor.commit();
    }

    public void stopLocationService(){
        editor.putBoolean(TRANSMISSION_STATUS, false);
        editor.commit();
    }

    public boolean isTransmitting(){
        return pref.getBoolean(TRANSMISSION_STATUS, true);
    }

}