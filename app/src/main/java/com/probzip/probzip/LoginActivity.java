package com.probzip.probzip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.DialogInterface;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class LoginActivity extends Activity {

    public final static String EXTRA_MESSAGE = "com.probzip.probzip.MESSAGE";

    private Button loginButton;
    private EditText mobileNumber, password;
    private TextView textView;

    // Session Manager Class
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Session Manager
        session = new SessionManager(getApplicationContext());
        loginButton = (Button) findViewById(R.id.login_button);

    }

    public void checkCredentials(final String mobileNumber, String password) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://probzip.com/delivery_boy_login/";

        url += "?mobile_number=" + mobileNumber + "&password=" + password;

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        session.createLoginSession(mobileNumber);
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showAlertDialog("Invalid username or password");
                    }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void handleLogin(View view) {

        mobileNumber = (EditText) findViewById(R.id.mobile_number);
        password = (EditText) findViewById(R.id.password);
        textView = (TextView) findViewById(R.id.text);

        if(mobileNumber.getText().length() != 10 ){
            showAlertDialog("Enter valid mobile number");
            return;
        }

        checkCredentials(mobileNumber.getText().toString(), password.getText().toString());

        // Remove on check-credentials
        /*
        if(mobileNumber.getText().toString().equals("0123456789") && password.getText().toString().equals("password")){
            session.createLoginSession(mobileNumber.getText().toString());
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            showAlertDialog("Cannot log in");
        }
        */

    }

    public void showAlertDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Login failed");

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

    @Override
    public void onBackPressed(){
        finish();
    }
}

