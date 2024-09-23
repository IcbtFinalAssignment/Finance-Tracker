package com.icbtcampus.budgettracker_assignment.Activity;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.RESET_PWD;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.icbtcampus.budgettracker_assignment.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etOtp, etNewPassword, etConfirmPassword;
    private Button btnAction;
    private RequestQueue requestQueue;
    private TextView txtemail,tvOtp,tvNewPassword,tvConfirmPassword;
    private ProgressDialog pDialog;

    // Class-level variable to hold the decoded API key
    private String decodedApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        txtemail = findViewById(R.id.email);
        tvOtp = findViewById(R.id.tvOtp);
        tvNewPassword = findViewById(R.id.tvNewPassword);
        tvConfirmPassword = findViewById(R.id.tvConfirmPassword);

        etEmail = findViewById(R.id.etLoginemail);
        etOtp = findViewById(R.id.etotp);
        etNewPassword = findViewById(R.id.etForgotPassword);
        etConfirmPassword = findViewById(R.id.etForgotPasswordConfirm);
        btnAction = findViewById(R.id.btn);

        requestQueue = Volley.newRequestQueue(this);

        // Initial state
        etOtp.setVisibility(View.GONE);
        etNewPassword.setVisibility(View.GONE);
        etConfirmPassword.setVisibility(View.GONE);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAction.getText().equals("Get OTP")) {
                    sendOtp();
                } else if (btnAction.getText().equals("Verify OTP")) {
                    verifyOtp();
                } else if (btnAction.getText().equals("Change Password")) {
                    changePassword();
                }
            }
        });
        LinearLayout myLinearLayout = findViewById(R.id.loginhere);
        myLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendOtp() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("method", "forgot_password");
            requestBody.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Show loader
        displayLoader();

        String url = RESET_PWD;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 0) {
                                // Dismiss the loader
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                Toast.makeText(ForgotPasswordActivity.this, "OTP sent to your email.", Toast.LENGTH_SHORT).show();
                                etEmail.setVisibility(View.GONE);
                                txtemail.setVisibility(View.GONE);
                                etOtp.setVisibility(View.VISIBLE);
                                tvOtp.setVisibility(View.VISIBLE);
                                btnAction.setText("Verify OTP");
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordActivity.this, "Request failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("method", "otp_validate");
            requestBody.put("email", email);
            requestBody.put("otp", otp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Show loader
        displayLoader();

        String url = RESET_PWD;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 0) {
                                // Dismiss the loader
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                String apiKey = response.getString("api_key");
                                Toast.makeText(ForgotPasswordActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                // Decode API key 3 times
                                decodedApiKey = decodeApiKey(apiKey); // Store it in the class variable
                                etOtp.setVisibility(View.GONE);
                                tvOtp.setVisibility(View.GONE);
                                tvNewPassword.setVisibility(View.VISIBLE);
                                tvConfirmPassword.setVisibility(View.VISIBLE);
                                etNewPassword.setVisibility(View.VISIBLE);
                                etConfirmPassword.setVisibility(View.VISIBLE);
                                btnAction.setText("Change Password");
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordActivity.this, "Request failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the retained decodedApiKey
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("method", "change_password");
            requestBody.put("email", etEmail.getText().toString().trim());
            requestBody.put("api_key", decodedApiKey); // Use the retained decoded API key
            requestBody.put("password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Show loader
        displayLoader();

        String url = RESET_PWD;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 0) {
                                // Dismiss the loader
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                Toast.makeText(ForgotPasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ForgotPasswordActivity.this, "Request failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private String decodeApiKey(String apiKey) {
        String decodedKey = apiKey;
        for (int i = 0; i < 3; i++) {
            // Decode the string from Base64
            byte[] decodedBytes = Base64.decode(decodedKey, Base64.DEFAULT);
            decodedKey = new String(decodedBytes);
        }
        return decodedKey; // Return the final decoded key
    }
    private void displayLoader() {
        pDialog = new ProgressDialog(ForgotPasswordActivity.this);
        pDialog.setMessage("Loading.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}
