package com.probzip.probzip;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4  .app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Maddy on 09-09-2015.
 */
public class LocationTransmitter extends Service implements com.google.android.gms.location.LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener{

    SessionManager session;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private NotificationManager mNotificationManager;

    public static final int NOTIFICATION_ID = 1;

    BroadcastReceiver receiver = new BroadcastReceiver();

    @Override
    public Binder onBind(Intent intent){
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        session = new SessionManager(getApplicationContext());

        addLocationListener();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mGoogleApiClient.connect();

        receiver.SetAlarm(this);
    }

    private void addLocationListener(){
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000 * 30);
        startLocationUpdates();

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            session.updateOwnLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            sendLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());        }
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if(mLastLocation != null) {
            session.updateOwnLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            sendLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    }

    public void sendLocation(final double latitude, final double longitude){

        RequestQueue queue = Volley.newRequestQueue(this);
        HashMap<String, String> user = session.getUserDetails();
        String number = user.get(SessionManager.KEY_SECRET);

        String url = "http://probzip.com/delivery_boy/api/v1/";

        url += "?status=location" + "&secret=" + number + "&latitude=" + latitude + "&longitude=" + longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (session.isPending() == false && getResponse(response).equals("ok")) {
                            launchNotification();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Couldn't transmit", Toast.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }


    public void launchNotification(){

        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), 0);

        Notification.Builder mBuilder = new Notification.Builder(
                this)
                .setContentTitle("NEW ORDER")
                .setSmallIcon(R.drawable.example_picture)
                .setContentText("Please Check");

        mBuilder.setContentIntent(contentIntent);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mBuilder.setVibrate(new long[]{0000, 3000, 1000, 3000, 1000});

        Notification notification = mBuilder.getNotification();

        notification.flags = notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(NOTIFICATION_ID, notification);


    }

    public String getResponse(JSONObject response){
        String result = "error";

        try {
            result = response.getString("result");
        } catch (JSONException e){

        }
        return result;
    }

    @Override
    public boolean stopService (Intent service){
        receiver.CancelAlarm(this);
        return true;
    }
}
