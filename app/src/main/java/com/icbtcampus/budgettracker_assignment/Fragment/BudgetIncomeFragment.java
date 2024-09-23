package com.icbtcampus.budgettracker_assignment.Fragment;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.BUDGET;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.icbtcampus.budgettracker_assignment.Activity.LoginActivity;
import com.icbtcampus.budgettracker_assignment.Activity.NoInternetActivity;
import com.icbtcampus.budgettracker_assignment.Adapter.SetBudgetCategoriesAdapter;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.Category;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.ApiKeyUtil;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BudgetIncomeFragment extends Fragment implements SetBudgetCategoriesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ArrayList<Category> incomeCategories;
    private SetBudgetCategoriesAdapter setBudgetCategoriesAdapter;
    private ProgressBar progressBar;
    private SessionHandler sessionHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_fragment_income, container, false);

        recyclerView = view.findViewById(R.id.incomeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeCategories = new ArrayList<>();

        setBudgetCategoriesAdapter = new SetBudgetCategoriesAdapter(getContext(), incomeCategories, this);
        recyclerView.setAdapter(setBudgetCategoriesAdapter);

        progressBar = view.findViewById(R.id.progressBar);

        // Initialize SessionHandler
        sessionHandler = new SessionHandler(getContext());
        // Check if the user is logged in
        if (!sessionHandler.isLoggedIn()) {
            // Start the login activity
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            requireActivity().finish();  // Close the current activity
            return null;  // Exit early since user is not logged in
        }

        // Fetch income categories
        fetchIncomeCategories();

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();

        // Check if the user is logged in
        if (!sessionHandler.isLoggedIn()) {
            // Start the login activity
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            getActivity().finish();  // Close the current activity
            return; // Exit the method to prevent further execution
        }

        // Check for internet connection
        if (!NetworkUtil.isConnected(requireContext())) {
            Intent intent = new Intent(getActivity(), NoInternetActivity.class);
            startActivity(intent);
            getActivity().finish();  // Close the current activity
        } else {
            // Refresh the data when the fragment is resumed
            fetchIncomeCategories();
        }
    }

    @Override
    public void onItemClick(Category category) {
        showSetAmountDialog(category);
    }

    private void showSetAmountDialog(final Category category) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_set_amount, null);

        // Get the views from the inflated layout
        EditText input = dialogView.findViewById(R.id.content);
        TextView categoryName = dialogView.findViewById(R.id.categoryname);
        ImageButton closeButton = dialogView.findViewById(R.id.bt_close);
        AppCompatButton saveButton = dialogView.findViewById(R.id.save);

        // Set the category name with the custom message
        categoryName.setText("Monthly " + category.getCategoryName());

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Auto-focus the EditText and show the keyboard
        input.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Close button listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountString = input.getText().toString();
                if (!amountString.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountString);
                        if (amount > 0) {
                            // Update the category or perform any necessary action with the new amount
                            category.setBudgetAmount(amount);
                            setBudgetCategoriesAdapter.notifyDataSetChanged();
                            sendUpdatedBudgetAmount(category, amount);
//                            Toast.makeText(getContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Amount cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show the dialog
        dialog.show();
    }



    private void fetchIncomeCategories() {
        if (!sessionHandler.isLoggedIn()) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = sessionHandler.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(getContext());

        String url = BUDGET + "?type=income&email=" + email + "&api_key=" + apiKey;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressBar.setVisibility(View.GONE);

                            Log.d("IncomeFragment", "Response: " + response.toString());

                            if (response.optString("status").equals("success") && response.has("categories")) {
                                JSONArray categories = response.getJSONArray("categories");
                                incomeCategories.clear();
                                for (int i = 0; i < categories.length(); i++) {
                                    JSONObject category = categories.getJSONObject(i);

                                    // Get category_id, category_name, and budget_amount from the response
                                    int categoryId = category.getInt("category_id");
                                    String categoryName = category.getString("category_name");
                                    String budgetAmountString = category.getString("budget_amount");
                                    double budgetAmount = Double.parseDouble(budgetAmountString);

                                    // Create the Category object with categoryId, categoryName, and budgetAmount
                                    incomeCategories.add(new Category(categoryId, categoryName, budgetAmount, true)); // Pass `true` for income
                                }
                                setBudgetCategoriesAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getContext(), "No categories found or error in response", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        String errorMessage;
                        if (error.networkResponse != null) {
                            errorMessage = "Error code: " + error.networkResponse.statusCode;
                        } else {
                            errorMessage = "Network error: " + error.getMessage();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("ACCESS_KEY", ACCESS_KEY);
                // API_KEY is included in the URL query parameters
                return headers;
            }
        };

        queue.add(request);
    }



    private void sendUpdatedBudgetAmount(final Category category, final double newAmount) {

        String url = BUDGET;

        // Create JSON Object to send in POST request
        JSONObject postData = new JSONObject();
        try {
            postData.put("email", sessionHandler.getUserDetails().getEmail());
            postData.put("api_key", ApiKeyUtil.getDecodedApiKey(getContext()));
            postData.put("category_id", category.getCategoryId());
            postData.put("budget_amount", newAmount);
            postData.put("type", "income");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send POST request using Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(getContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update budget: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage;
                        if (error.networkResponse != null) {
                            errorMessage = "Error code: " + error.networkResponse.statusCode;
                        } else {
                            errorMessage = "Network error: " + error.getMessage();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("ACCESS_KEY", ACCESS_KEY);
                return headers;
            }
        };

        // Add request to queue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }

}
