package com.probzip.probzip;

import java.util.HashMap;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;


public class HomeActivity extends Activity {

    //Variable Declarations

    SessionManager session;
    private Double sourceLatitude;
    private Double sourceLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private Button callCustomerButton;

    private Button checkMap;

    private Button orderFinishedButton;

    private TextView userNumber;

    private Button callManagerButton;

    private Button sosMessage;

    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(getApplicationContext());
        userNumber = (TextView) findViewById(R.id.user_number);

        session.checkLogin();

        HashMap<String, String> user = session.getUserDetails();

        String number = user.get(SessionManager.KEY_SECRET);

        userNumber.setText(number);

        //Location updates
        GPSTracker gps = new GPSTracker(HomeActivity.this);


        //Call Customer

        callCustomerButton = (Button) findViewById(R.id.call_customer_button);

        callCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Put phone number of customer
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:7417485915"));
                startActivity(callIntent);

            }
        });

        //Launch map

        checkMap = (Button) findViewById(R.id.map_check_button);

        checkMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get current location

                GPSTracker gps = new GPSTracker(HomeActivity.this);

                destinationLatitude = 28.5872637;
                destinationLongitude = 77.3593832;

                if(gps.canGetLocation()){

                    sourceLatitude = gps.getLatitude();
                    sourceLongitude = gps.getLongitude();

                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);

                }
                else{
                    gps.showSettingsAlert();
                }
            }
        });

        //Order finished

        orderFinishedButton = (Button) findViewById(R.id.order_finished_button);
        orderFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gps = new GPSTracker(HomeActivity.this);
                Double latitude = gps.getLatitude();
                Double longitude = gps.getLongitude();
                deliveryConfirmAlert(latitude, longitude);
            }
        });


        //Call Manager

        callManagerButton = (Button) findViewById(R.id.call_manager_button);
        callManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Put phone number of manager
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:7895161705"));
                startActivity(callIntent);

            }
        });

        //SOS Message

        sosMessage =(Button) findViewById(R.id.sos_message_button);
        sosMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Put phone number of manager
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("7895161705", null, "FUCK YOU", null, null);

            }
        });

        //Sign Out

        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signOutAlert();

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signOutAlert(){


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Sign Out");

        alertDialog.setMessage("Are you sure you want sign out?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                session.logoutUser();
                finish();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    public void deliveryConfirmAlert(final double latitude,final double longitude){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Order Finished?");

        alertDialog.setMessage("Have you finished order delivery/pick-up?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String display = "lat : " + latitude + "\r\n" + "long : " + longitude;

                Toast.makeText(getApplicationContext(), display, Toast.LENGTH_LONG).show();

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

    }


}
