package com.icbtcampus.budgettracker_assignment.Fragment;
import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.GET_RECORDS;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.icbtcampus.budgettracker_assignment.Activity.LoginActivity;
import com.icbtcampus.budgettracker_assignment.Activity.NoInternetActivity;
import com.icbtcampus.budgettracker_assignment.Adapter.TransactionAdapter;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.Transaction;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.ApiKeyUtil;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragIncome extends Fragment {

    TextView txtMonthName, txtNoFound, txtTotalIncome;
    ImageView ivPrevious, ivNext;
    RecyclerView rvIncomelist;
    int cMonth;

    TransactionAdapter transactionAdapter;
    List<Transaction> transactionList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    double sarvaloIncome = 0;
    SwipeRefreshLayout swipeRefreshIncone;
    RequestQueue requestQueue;
    private SessionHandler sessionHandler;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_income, container, false);

        findView(rootView);
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
        setupRecyclerView();
        txtTotalIncome.setText("Loading...");

        requestQueue = Volley.newRequestQueue(requireContext());

        cMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        getTotalBalance(cMonth);

        swipeRefreshIncone.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            swipeRefreshIncone.setRefreshing(false);
            getTotalBalance(cMonth);
        }, 1000));

        Log.e("cMonth--)", "" + cMonth);
        ivNext.setOnClickListener(view -> {
            cMonth = cMonth + 1;
            if (cMonth > 12) {
                cMonth = 1;
            }
            txtMonthName.setText(getMonthName(cMonth));
            getTotalBalance(cMonth);
        });

        ivPrevious.setOnClickListener(view -> {
            cMonth = cMonth - 1;
            if (cMonth < 1) {
                cMonth = 12;
            }
            txtMonthName.setText(getMonthName(cMonth));
            getTotalBalance(cMonth);
        });

        return rootView;
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
            getTotalBalance(cMonth);
        }
    }
    private void setupRecyclerView() {
        rvIncomelist.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(transactionList);
        rvIncomelist.setAdapter(transactionAdapter);
    }
    public void getTotalBalance(int month) {
        if (!sessionHandler.isLoggedIn()) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = sessionHandler.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(getContext());
        String url = GET_RECORDS + "?email=" + email + "&api_key=" + apiKey + "&transaction_type=income&month=" + month;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray transactions = response.getJSONArray("transactions");
                            transactionList.clear();
                            sarvaloIncome = 0;

                            if (transactions.length() > 0) {
                                for (int i = 0; i < transactions.length(); i++) {
                                    JSONObject transaction = transactions.getJSONObject(i);
                                    int transactionId = transaction.getInt("transaction_id");
                                    int categoryId = transaction.getInt("category_id");
                                    double amount = transaction.getDouble("amount");
                                    String transactionType = transaction.getString("transaction_type");
                                    int accountId = transaction.getInt("account_id");
                                    String description = transaction.getString("description");
                                    String transactionDate = transaction.getString("transaction_date");
                                    String categoryName = transaction.getString("category_name");
                                    Transaction trans = new Transaction(transactionId, categoryId, amount, transactionType, accountId, description, transactionDate,categoryName);
                                    transactionList.add(trans);

                                    sarvaloIncome += amount;
                                }
                                // Notify the adapter that data has changed
                                transactionAdapter.notifyDataSetChanged();
                                // Update the total income text view
                                txtTotalIncome.setText(String.format("%,d", (int) sarvaloIncome));
                                // Hide the "No Transaction found" message
                                txtNoFound.setVisibility(View.GONE);
                            } else {
                                // Handle the case where no transactions are found
                                transactionList.clear(); // Clear the list
                                transactionAdapter.notifyDataSetChanged(); // Notify the adapter
                                txtTotalIncome.setText("0");
                                txtNoFound.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Handle error response
                            transactionList.clear(); // Clear the list
                            transactionAdapter.notifyDataSetChanged(); // Notify the adapter
                            txtTotalIncome.setText("0");
                            txtNoFound.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e("Volley Error", e.toString());
                        transactionList.clear(); // Clear the list
                        transactionAdapter.notifyDataSetChanged(); // Notify the adapter
                        txtTotalIncome.setText("0");
                        txtNoFound.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    transactionList.clear(); // Clear the list
                    transactionAdapter.notifyDataSetChanged(); // Notify the adapter
                    txtTotalIncome.setText("0");
                    txtNoFound.setVisibility(View.VISIBLE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }






    public String getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        return month_date.format(cal.getTime());
    }

    public String getMonthName(int month) {
        switch (month) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return null;
        }
    }

    private void findView(View rootView) {
        txtTotalIncome = rootView.findViewById(R.id.txtTotalIncome);
        txtMonthName = rootView.findViewById(R.id.txtMonthName);
        ivPrevious = rootView.findViewById(R.id.ivPrevious);
        ivNext = rootView.findViewById(R.id.ivNext);
        txtNoFound = rootView.findViewById(R.id.txtNoFound);
        rvIncomelist = rootView.findViewById(R.id.rvIncomelist);
        swipeRefreshIncone = rootView.findViewById(R.id.swipeRefreshIncone);


        txtMonthName.setText(getCurrentMonth());
    }
}
