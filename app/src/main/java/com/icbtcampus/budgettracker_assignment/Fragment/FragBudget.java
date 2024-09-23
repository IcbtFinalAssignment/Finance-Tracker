package com.icbtcampus.budgettracker_assignment.Fragment;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.BUDGET;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.icbtcampus.budgettracker_assignment.Activity.LoginActivity;
import com.icbtcampus.budgettracker_assignment.Activity.NoInternetActivity;
import com.icbtcampus.budgettracker_assignment.Adapter.BudgetFragCategoriesAdapter;
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
import java.util.List;
import java.util.Map;

public class FragBudget extends Fragment {
    private SessionHandler sessionHandler;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BudgetFragCategoriesAdapter budgetCategoriesAdapter;
    private List<Category> allCategories = new ArrayList<>(); // Combined list

    public FragBudget() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        budgetCategoriesAdapter = new BudgetFragCategoriesAdapter(allCategories);
        recyclerView.setAdapter(budgetCategoriesAdapter);

        // Initialize SessionHandler first to check login status
        sessionHandler = new SessionHandler(getContext());

        // Check if the user is logged in
        if (!sessionHandler.isLoggedIn()) {
            // Start the login activity
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            requireActivity().finish();  // Close the current activity
            return null;  // Exit early since user is not logged in
        }

        // Initialize data fetch
        fetchCategories();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchCategories();
            }
        });

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
            fetchCategories();
        }
    }
    private void fetchCategories() {
        if (!sessionHandler.isLoggedIn()) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear the existing list before fetching new data
        allCategories.clear();

        User user = sessionHandler.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(getContext());

        String urlIncome = BUDGET + "?type=income&email=" + email + "&api_key=" + apiKey;
        String urlExpense = BUDGET + "?type=expense&email=" + email + "&api_key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Fetch both income and expense categories
        fetchCategoriesFromUrl(urlIncome, true, queue);
        fetchCategoriesFromUrl(urlExpense, false, queue);
    }

    private void fetchCategoriesFromUrl(String url, boolean isIncome, RequestQueue queue) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressBar.setVisibility(View.GONE);

                            Log.d("FragBudget", "Response: " + response.toString());

                            if (response.optString("status").equals("success") && response.has("categories")) {
                                JSONArray categories = response.getJSONArray("categories");
                                for (int i = 0; i < categories.length(); i++) {
                                    JSONObject category = categories.getJSONObject(i);

                                    int categoryId = category.getInt("category_id");
                                    String categoryName = category.getString("category_name");
                                    String budgetAmountString = category.getString("budget_amount");
                                    double budgetAmount = Double.parseDouble(budgetAmountString);

                                    // Add category to the combined list
                                    allCategories.add(new Category(categoryId, categoryName, budgetAmount, isIncome));
                                }

                                // Sort categories after both income and expense categories are added
                                sortCategories();

                                budgetCategoriesAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getContext(), "No categories found or error in response", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                        } finally {
                            // Stop refreshing animation when data is loaded
                            swipeRefreshLayout.setRefreshing(false);
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
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation on error
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

    private void sortCategories() {
        // Custom comparator to sort categories: income first, then expense
        allCategories.sort((cat1, cat2) -> Boolean.compare(cat2.isIncome(), cat1.isIncome()));
    }
}
