package com.icbtcampus.budgettracker_assignment.Activity;

import static com.icbtcampus.budgettracker_assignment.constant.ADD_RECORDS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.Utils.ApiKeyUtil;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;
import com.icbtcampus.budgettracker_assignment.constant;
import com.icbtcampus.budgettracker_assignment.databinding.ActivityAddIncomeBinding;
import com.icbtcampus.budgettracker_assignment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private SessionHandler session;

    private Spinner spinnerExpenseCategory;
    private Spinner spinnerAccountType;
    private ArrayAdapter<String> expenseCategoryAdapter;
    private ArrayAdapter<String> accountTypeAdapter;
    private List<String> expenseCategories = new ArrayList<>();
    private List<String> accountTypes = new ArrayList<>();
    private List<JSONObject> categoriesList = new ArrayList<>();
    private List<JSONObject> accountsList = new ArrayList<>();
    private ProgressBar loader;
    private EditText amountEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize SessionHandler
        session = new SessionHandler(this);

        User user = session.getUserDetails();
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> onBackPressed());

        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(AddExpenseActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activity
        }

        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        spinnerAccountType = findViewById(R.id.spinnerAccountType);
        loader = findViewById(R.id.loader);
        amountEditText = findViewById(R.id.etAmount);
        descriptionEditText = findViewById(R.id.etDescription);

        // Initialize Spinners
        expenseCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategories);
        expenseCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(expenseCategoryAdapter);

        accountTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountTypes);
        accountTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(accountTypeAdapter);

        fetchCategoriesAndAccounts();
        findViewById(R.id.btnUpdate).setOnClickListener(view -> submitData());

    }
    private void submitData() {
        onSubmit(findViewById(R.id.btnUpdate));
    }

    private void fetchCategoriesAndAccounts() {
        loader.setVisibility(View.VISIBLE);

        if (!session.isLoggedIn()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
            return;
        }

        User user = session.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(this);

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Base URL
        String baseUrl = ADD_RECORDS + "?type=expense&email=" + email + "&api_key=" + apiKey;

        // Append query parameters to the URL
        Uri.Builder uriBuilder = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("category_type", "expense");

        String url = uriBuilder.toString();

        // Create a StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONArray categoriesArray = jsonResponse.getJSONArray("categories");
                            JSONArray accountsArray = jsonResponse.getJSONArray("accounts");

                            expenseCategories.clear();
                            accountTypes.clear();
                            categoriesList.clear();
                            accountsList.clear();

                            for (int i = 0; i < categoriesArray.length(); i++) {
                                JSONObject category = categoriesArray.getJSONObject(i);
                                expenseCategories.add(category.getString("category_name"));
                                categoriesList.add(category);
                            }

                            for (int i = 0; i < accountsArray.length(); i++) {
                                JSONObject account = accountsArray.getJSONObject(i);
                                String accountName = account.getString("account_name");
                                String accountBalance = account.getString("balance");
                                accountTypes.add(accountName + " - " + accountBalance);
                                accountsList.add(account);
                            }

                            // Notify adapters about data changes
                            expenseCategoryAdapter.notifyDataSetChanged();
                            accountTypeAdapter.notifyDataSetChanged();
                        } else {
                            // Handle error
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_LONG).show();
                    }
                    loader.setVisibility(View.GONE);
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
                    loader.setVisibility(View.GONE);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Add the Access Key to the headers
                headers.put("Access-Key", constant.ACCESS_KEY);
                return headers;
            }
        };

        // Add request to the RequestQueue
        queue.add(stringRequest);
    }

    private int getSelectedCategoryId() {
        String selectedCategory = spinnerExpenseCategory.getSelectedItem().toString();
        for (JSONObject category : categoriesList) {
            try {
                if (category.getString("category_name").equals(selectedCategory)) {
                    return category.getInt("category_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1; // Or handle appropriately
    }

    private int getSelectedAccountId() {
        String selectedAccount = spinnerAccountType.getSelectedItem().toString();
        for (JSONObject account : accountsList) {
            try {
                if ((account.getString("account_name") + " - " + account.getString("balance")).equals(selectedAccount)) {
                    return account.getInt("account_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1; // Or handle appropriately
    }

    private void sendPostRequest(String email, String apiKey, int accountId, int categoryId, double amount, String description) {
        String url = ADD_RECORDS;

        RequestQueue queue = Volley.newRequestQueue(this);

        // Create JSON object for the request body
        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("api_key", apiKey);
            postData.put("account_id", accountId);
            postData.put("category_id", categoryId);
            postData.put("amount", amount);
            postData.put("transaction_type", "expense");
            postData.put("account_type", getAccountType(accountId));
            postData.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON data", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a StringRequest for POST method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        // Clear the EditText fields
                        amountEditText.setText("");
                        descriptionEditText.setText("");

                        // Fetch categories and accounts again
                        fetchCategoriesAndAccounts();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing server response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public byte[] getBody() {
                return postData.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Access-Key", constant.ACCESS_KEY);
                return headers;
            }
        };

        queue.add(stringRequest);
    }

    private String getAccountType(int accountId) {
        // Implement this method to return account type based on accountId
        for (JSONObject account : accountsList) {
            try {
                if (account.getInt("account_id") == accountId) {
                    return account.getString("account_type");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ""; // Or handle appropriately
    }

    // Method to be called on form submission
    public void onSubmit(View view) {
        User user = session.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(this); // Replace with actual API key
        int accountId = getSelectedAccountId();
        int categoryId = getSelectedCategoryId();
        double amount;
        String description;

        try {
            amount = Double.parseDouble(amountEditText.getText().toString());
            description = descriptionEditText.getText().toString();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_LONG).show();
            return;
        }

        if (accountId == -1 || categoryId == -1) {
            Toast.makeText(this, "Please select a valid account and category", Toast.LENGTH_LONG).show();
            return;
        }

        sendPostRequest(email, apiKey, accountId, categoryId, amount, description);
    }
}

