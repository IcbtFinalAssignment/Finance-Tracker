package com.icbtcampus.budgettracker_assignment.Activity;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.RESET_PWD;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.ApiKeyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
public class ChangePasswordActivity extends AppCompatActivity {

    private SessionHandler sessionHandler;
    private EditText etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sessionHandler = new SessionHandler(this);

        // Redirect to login if the user is not logged in
        if (!sessionHandler.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Initialize views
        etNewPassword = findViewById(R.id.etChangePassword);
        etConfirmPassword = findViewById(R.id.etChangePasswordConfirm);
        btnChangePassword = findViewById(R.id.btnLogin);  // Assuming button is btnLogin

        // Set click listener for changing the password
        btnChangePassword.setOnClickListener(v -> changepwd());
    }

    public void changepwd() {
        User user = sessionHandler.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(this);

        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate password fields
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill out both password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            etNewPassword.setText("");
            etConfirmPassword.setText("");
            return;
        }
        // Show loader
        displayLoader();

        // Send API request to change the password
        String url = RESET_PWD;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Dismiss the loader
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("message");

                    if (status == 0) {
                        Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_LONG).show();
                        sessionHandler.logoutUser();  // Log out after password change
                        Intent i = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ChangePasswordActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "";

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    errorMessage = "Network timeout or no connection!";
                } else if (error instanceof AuthFailureError) {
                    errorMessage = "Authentication failure!";
                } else if (error instanceof ServerError) {
                    errorMessage = "Server error!";
                } else if (error instanceof NetworkError) {
                    errorMessage = "Network error!";
                } else if (error instanceof ParseError) {
                    errorMessage = "Parse error!";
                }

                if (error.networkResponse != null) {
                    errorMessage += " Status Code: " + error.networkResponse.statusCode;
                } else {
                    errorMessage += " Error: " + error.getMessage();
                }

                Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                error.printStackTrace();  // Print stack trace for debugging in logs
            }

        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    // Create a JSON object for the request body
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("method", "change_password");
                    jsonBody.put("email", email);
                    jsonBody.put("api_key", apiKey);
                    jsonBody.put("password", newPassword);
                    return jsonBody.toString().getBytes("UTF-8");  // Convert to bytes
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;  // Return null in case of a JSON error
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;  // Handle the UnsupportedEncodingException
                }
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Add headers (access key)
                Map<String, String> headers = new HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);
                headers.put("Content-Type", "application/json");  // Specify content type
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }
    private void displayLoader() {
        pDialog = new ProgressDialog(ChangePasswordActivity.this);
        pDialog.setMessage("Loading.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

}
