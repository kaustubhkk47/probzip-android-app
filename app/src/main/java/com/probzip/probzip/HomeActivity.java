package com.probzip.probzip;

import java.util.HashMap;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class HomeActivity extends Activity {

    //Variable Declarations

    SessionManager session;


    private TextView orderType;
    private TextView CustomerName;
    private TextView addressDisplay;
    private TextView customerPhone;

    private Button callCustomerButton;

    private Button checkMap;

    private Button acceptOrderButton;

    private Button orderReachedButton;

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
        session.checkLogin();


        if(session.isTransmitting() == false){
            startLocationTransmitter();
        }

        //Location updates


        //Call Customer

        callCustomerButton = (Button) findViewById(R.id.call_customer_button);

        callCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.isPending() == true) {
                    HashMap<String, String> user = session.getUserDetails();
                    String number = user.get(SessionManager.CUSTOMER_PHONE);
                    number = "tel:" + number;
                    // Put phone number of customer
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(number));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "No pending orders", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Launch map

        checkMap = (Button) findViewById(R.id.map_check_button);

        checkMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get current location

                HashMap<String, Float> user1 = session.getOwnLatlong();

                double sourceLatitude = (double) user1.get(SessionManager.OWN_LAT);
                double sourceLongitude = (double) user1.get(SessionManager.OWN_LONG);

                if (session.isPending() == false){
                    Toast.makeText(getApplicationContext(), "No pending orders", Toast.LENGTH_SHORT).show();
                } else if(sourceLatitude != -1 && sourceLongitude != -1){

                    HashMap<String, Float> user2 = session.getCustLatlong();

                    double destinationLatitude = (double) user2.get(SessionManager.CUSTOMER_LAT);
                    double destinationLongitude = (double) user2.get(SessionManager.CUSTOMER_LONG);


                    if(destinationLatitude != -1 && destinationLongitude != -1) {
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }

                }

            }
        });

        //Accept order

        acceptOrderButton = (Button) findViewById(R.id.acknowledge_order_button);
        acceptOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(session.isPending() == false) {
                    Toast.makeText(getApplicationContext(), "No pending orders", Toast.LENGTH_SHORT).show();
                } else if(session.isAcknowledged() == true) {
                    Toast.makeText(getApplicationContext(), "Already acknowledged", Toast.LENGTH_SHORT).show();
                } else if(session.isReached() == true){
                    Toast.makeText(getApplicationContext(), "Already acknowledged", Toast.LENGTH_SHORT).show();
                }else {
                    OrderConfirmAlert("Accept order", "Do you want to confirm order?",
                            "accept");
                }
            }

        });

        //Order reached

        orderReachedButton = (Button) findViewById(R.id.order_reached_button);
        orderReachedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(session.isPending() ==  false){
                    Toast.makeText(getApplicationContext(), "No pending orders", Toast.LENGTH_SHORT).show();
                } else if (session.isReached() == true){
                    Toast.makeText(getApplicationContext(), "Already reached", Toast.LENGTH_SHORT).show();
                } else if(session.isAcknowledged() == false) {
                    Toast.makeText(getApplicationContext(), "Acknowledge order", Toast.LENGTH_SHORT).show();
                } else {
                        OrderConfirmAlert("Reached?", "Have you picked up reached order location?",
                                "reach");
                    }
                }
        });

        //Order finished

        orderFinishedButton = (Button) findViewById(R.id.order_finished_button);
        orderFinishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(session.isPending() ==  false){
                    Toast.makeText(getApplicationContext(), "No pending orders", Toast.LENGTH_SHORT).show();
                } else if(session.isAcknowledged() == false) {
                    Toast.makeText(getApplicationContext(), "Acknowledge order", Toast.LENGTH_SHORT).show();
                } else if (session.isReached() == false){
                        Toast.makeText(getApplicationContext(), "Press reached button", Toast.LENGTH_SHORT).show();
                } else {
                    OrderConfirmAlert("Order completed?", "Have you completed the order?",
                            "complete");
                }
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
                HashMap<String, String> user = session.getUserDetails();
                final String number = user.get(SessionManager.KEY_SECRET);
                String message = "I have a problem! Please call me on my number " + number;
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("7895161705", null, message, null, null);

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
    protected void onStart(){
        super.onStart();
        if(session.isPending() == false) {
            toServer("check");
        }
        updateView();
    }


    public void OrderConfirmAlert(String title, String message,final String status){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(title);

        alertDialog.setMessage(message);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                toServer(status);

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

    }


    //Location transmission functions
    public void startLocationTransmitter(){
        session.startLocationService();
        Intent intent = new Intent(this, LocationTransmitter.class);
        startService(intent);
    }

    public void stopLocationTransmitter(){
        session.stopLocationService();
        Intent intent = new Intent(this, LocationTransmitter.class);
        stopService(intent);
    }

    //Update order

    public void toServer(final String status) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        HashMap<String, String> user = session.getUserDetails();
        final String number = user.get(SessionManager.KEY_SECRET);
        final String orderId = user.get(SessionManager.ORDER_ID);

        String url = "http://probzip.webfactional.com/delivery_boy/api/v1/";

        HashMap<String, Float> user1 = session.getOwnLatlong();
        final String latitude = user1.get(SessionManager.OWN_LAT).toString();
        final String longitude = user1.get(SessionManager.OWN_LONG).toString();


        url += "?status=" + status + "&secret=" + number + "&latitude=" + latitude +
                "&longitude=" + longitude + "&order_id=" + orderId;

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if(status.equals("accept")){
                            if(getResponse(response).equals("ok")) {
                                session.createAcknowledgement();
                                Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
                            } else {
                                showAlertDialog("Please press " + status + " button again");
                            }
                        }

                        else if(status.equals("complete")){
                            if(getResponse(response).equals("ok")) {
                                session.refreshOrder();
                                updateView();
                                Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_LONG).show();
                                toServer("check");
                            } else {
                                showAlertDialog("Please press " + status + " button again");

                            }
                        }

                        else if(status.equals("reach")){
                            if(getResponse(response).equals("ok")) {
                                session.onReached();
                                Toast.makeText(getApplicationContext(), "Reached", Toast.LENGTH_LONG).show();
                            } else {
                                showAlertDialog("Please press " + status + " button again");
                            }
                        }

                        else if(status.equals("check")){
                            if (getResponse(response).equals("ok")){
                                JSONObject orderdetails = response;
                                updateOrderDetails(orderdetails);
                            }
                            else if(getResponse(response).equals("error")){
                                //showAlertDialog("Could not update customer details");
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(status.equals("check")){

                    updateView();

                } else {
                    showAlertDialog("Please press " + status + " button again error");

                    Toast.makeText(getApplicationContext(), "?status=" + status + "&secret=" + number + "&latitude=" + latitude +
                            "&longitude=" + longitude + "&order_id=" + orderId, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public String getResponse(JSONObject response){
        String result = "error";

        try {
            result = response.getString("result");
        } catch (JSONException e){

        }
        return result;
    }

    public void updateOrderDetails(JSONObject orderdetails){


        showAlertDialog("New order received");

        String ordertype = "None";
        String custname = "None";
        String address = "None";
        String custphone = "";
        double latitude = 0.0;
        double longitude = 0.0;
        String orderid = "0";

        try {
            ordertype = orderdetails.getString("type");
            custname = orderdetails.getString("name");
            address = orderdetails.getString("address");
            custphone = orderdetails.getString("phone");
            orderid = orderdetails.getString("order_id");

            String lat1 = orderdetails.getString("lat");
            String long1 = orderdetails.getString("long");
            if(lat1.equals("null") == false && long1.equals("null") == false){
                latitude = Double.parseDouble(lat1);
                longitude = Double.parseDouble(long1);
            }
        } catch (JSONException e){
        }

        session.updateCurrentOrder(ordertype, custname, address, custphone, latitude, longitude, orderid);

        updateView();
    }

    public void updateView(){


        HashMap<String, String> user = session.getUserDetails();

        orderType = (TextView) findViewById(R.id.order_type);
        CustomerName = (TextView) findViewById(R.id.display_name);
        addressDisplay = (TextView) findViewById(R.id.address);
        customerPhone = (TextView) findViewById(R.id.customer_number);
        userNumber = (TextView) findViewById(R.id.user_number);

        String ordertype = user.get(SessionManager.ORDER_TYPE);
        String custname = user.get(SessionManager.CUSTOMER_NAME);
        String address = user.get(SessionManager.ADDRESS);
        String custphone = user.get(SessionManager.CUSTOMER_PHONE);
        String number = user.get(SessionManager.KEY_SECRET);

        orderType.setText(ordertype);
        CustomerName.setText(custname);
        addressDisplay.setText(address);
        customerPhone.setText(custphone);
        userNumber.setText(number);
    }

    //Logout functions
    public void notifyLogout(){
        RequestQueue queue = Volley.newRequestQueue(this);
        HashMap<String, String> user = session.getUserDetails();
        String number = user.get(SessionManager.KEY_SECRET);

        String url = "http://probzip.webfactional.com/delivery_boy/api/v1/";

        url += "?status=logout" +"&secret=" + number;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(getResponse(response).equals("ok")) {
                            stopLocationTransmitter();
                            session.logoutUser();
                            finish();
                        } else {
                            showAlertDialog("Please try again");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                showAlertDialog("Please try again");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void signOutAlert(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Sign Out");

        alertDialog.setMessage("Are you sure you want sign out?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                notifyLogout();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    // General alert dialog
    public void showAlertDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Alert");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton(alertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

}
