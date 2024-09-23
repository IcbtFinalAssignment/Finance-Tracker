package com.icbtcampus.budgettracker_assignment.Activity;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.LOGIN_URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.icbtcampus.budgettracker_assignment.Handler.MySingleton;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMPTY = "";
    private TextInputEditText etemail;
    private TextInputEditText etPassword;
    private String email;
    private String password;
    private ProgressDialog pDialog;
    private boolean backPressedOnce = false;
    private Handler backPressHandler;

    private SessionHandler session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            loadDashboard();
        }
        setContentView(R.layout.activity_login);

        etemail = findViewById(R.id.etLoginemail);
        etPassword = findViewById(R.id.etLoginPassword);

        TextView register = findViewById(R.id.btnLoginRegister);
        Button login = findViewById(R.id.btnLogin);
        // Initialize back press handler
        backPressHandler = new Handler(Looper.getMainLooper());

        // Launch Registration screen when Register Button is clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the data entered in the text inputs
                email = etemail.getText().toString().toLowerCase().trim();
                password = etPassword.getText().toString().trim();
                if (validateInputs()) {
                    login();
                }
            }
        });

        // Set forgot password functionality
        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle forgot password
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);            }
        });
        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(LoginActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activity
        }
    }

    /**
     * Launch Dashboard Activity on Successful Login
     */
    private void loadDashboard() {
        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Display Progress bar while Logging in
     */
    private void displayLoader() {
        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void login() {
        displayLoader();
        JSONObject request = new JSONObject();
        try {
            // Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_PASSWORD, password);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST, LOGIN_URL, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        try {
                            // Check if user got logged in successfully
                            if (response.getInt(KEY_STATUS) == 0) {
                                session.loginUser(email, response.getString(KEY_FULL_NAME));
                                // Get the API key from response
                                String ApiKey = response.getString("api_key");
                                storeApiKey(ApiKey);
                                loadDashboard();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        response.getString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                // Display error message whenever an error occurs
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Access-Key", ACCESS_KEY);  // Set the access key header
                return headers;
            }
        };

        // Access the RequestQueue through  singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    /**
     * Validates inputs and shows error if any
     * @return boolean
     */
    private boolean validateInputs() {
        if (KEY_EMPTY.equals(email)) {
            etemail.setError("Email cannot be empty");
            etemail.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(password)) {
            etPassword.setError("Password cannot be empty");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void storeApiKey(String apiKey) {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store the API key
        editor.putString("api_key", apiKey);
        editor.apply();  // Commit changes asynchronously
    }
    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();  // Exit the app
            return;
        }

        // Show a toast message asking the user to press back again
        this.backPressedOnce = true;
        Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();

        // Reset the flag after 2 seconds
        backPressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressedOnce = false;
            }
        }, 2000);
    }
}
