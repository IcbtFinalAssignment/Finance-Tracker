package com.icbtcampus.budgettracker_assignment.Fragment;

import static com.icbtcampus.budgettracker_assignment.constant.ACCESS_KEY;
import static com.icbtcampus.budgettracker_assignment.constant.GET_RECORDS;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

public class FragHistory extends Fragment {

    RecyclerView rvTranscation;
    TransactionAdapter transactionAdapter;
    List<Transaction> transactionList = new ArrayList<>();
    RequestQueue requestQueue;
    private SessionHandler sessionHandler;

    LinearLayout reports;
    TextView txtTotalAmount;
    TextView txtCashAmount; // Add for cash balance
    TextView txtBankAmount; // Add for bank balance
    SwipeRefreshLayout swipeRefreshTrans;
    private Uri pdfUri;


    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_transcation, container, false);

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

        // Initialize views
        rvTranscation = rootView.findViewById(R.id.rvTranscation);
        reports = rootView.findViewById(R.id.reports);
        txtTotalAmount = rootView.findViewById(R.id.txtTotalAmount);
        txtCashAmount = rootView.findViewById(R.id.txtCashAmount); // Initialize cash TextView
        txtBankAmount = rootView.findViewById(R.id.txtBankAmount); // Initialize bank TextView
        swipeRefreshTrans = rootView.findViewById(R.id.swipeRefreshTran);

        // Initialize RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Set RecyclerView layout manager
        rvTranscation.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Initialize the adapter
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTranscation.setAdapter(transactionAdapter);

        // Swipe to refresh functionality
        swipeRefreshTrans.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                swipeRefreshTrans.setRefreshing(false);
                getTotalTransactions();  // Reload data on refresh
            }, 1000);
        });

        // Fetch total balance and setup RecyclerView
        getTotalTransactions();

        // Set click listener for reports to open date picker
        reports.setOnClickListener(v -> openDateRangePicker());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if the user is logged in
        if (!sessionHandler.isLoggedIn()) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();

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
            getTotalTransactions();
        }
    }


    public void getTotalTransactions() {
        if (!sessionHandler.isLoggedIn()) {
            // Start the login activity
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);

            // Optionally, finish the current activity if you don't want the user to come back to it
            getActivity().finish();
            return;
        }


        User user = sessionHandler.getUserDetails();
        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(getContext());
        String url = GET_RECORDS + "?email=" + email + "&api_key=" + apiKey;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray transactions = response.getJSONArray("transactions");
                            transactionList.clear();  // Clear existing transactions

                            double totalBalance = 0.0;

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

                                    // Create a transaction object and add to the list
                                    Transaction trans = new Transaction(transactionId, categoryId, amount, transactionType, accountId, description, transactionDate, categoryName);
                                    transactionList.add(trans);

                                    // Adjust totalBalance based on transaction type
                                    if ("income".equals(transactionType)) {
                                        totalBalance += amount;  // Add for income
                                    } else if ("expense".equals(transactionType)) {
                                        totalBalance -= amount;  // Subtract for expense
                                    }
                                }

                                // Notify the adapter that data has changed
                                transactionAdapter.notifyDataSetChanged();

                                // Update the total amount text view with formatted balance
                                txtTotalAmount.setText(String.format("%,.2f", totalBalance));
                            } else {
                                handleEmptyTransactions();
                            }

                            // Extract balances for cash and bank accounts
                            JSONArray balances = response.getJSONArray("balances");
                            double cashBalance = 0.0;
                            double bankBalance = 0.0;

                            for (int i = 0; i < balances.length(); i++) {
                                JSONObject account = balances.getJSONObject(i);
                                String accountType = account.getString("account_type");
                                double balance = account.getDouble("balance");

                                if ("cash".equalsIgnoreCase(accountType)) {
                                    cashBalance = balance;  // Set cash balance
                                } else if ("bank".equalsIgnoreCase(accountType)) {
                                    bankBalance = balance;  // Set bank balance
                                }
                            }

                            // Update the respective TextViews with formatted balances
                            txtCashAmount.setText(String.format("%,.2f", cashBalance));
                            txtBankAmount.setText(String.format("%,.2f", bankBalance));

                        } else {
                            handleEmptyTransactions();
                        }
                    } catch (Exception e) {
                        Log.e("Volley Error", e.toString());
                        handleEmptyTransactions();
                    }
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    handleEmptyTransactions();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Access-Key", ACCESS_KEY);  // Ensure ACCESS_KEY is correct
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
    // Helper method to handle empty transactions case
    private void handleEmptyTransactions() {
        transactionList.clear();  // Clear the list
        transactionAdapter.notifyDataSetChanged();  // Notify adapter of the empty list
        txtTotalAmount.setText("0.00");  // Set total balance to 0
        txtCashAmount.setText("0.00");  // Set cash balance to 0
        txtBankAmount.setText("0.00");  // Set bank balance to 0
        Toast.makeText(getContext(), "No transactions found", Toast.LENGTH_SHORT).show();
    }


    // Method to open the Material Date Picker
    private void openDateRangePicker() {
        // Create calendar constraints to disallow future dates
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.now()); // Disallow future dates

        // Create the MaterialDatePicker for a date range selection
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");
        builder.setTheme(R.style.ThemeOverlay_MyCustomDatePicker); // Use dark theme
        builder.setCalendarConstraints(constraintsBuilder.build());

        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        // Show the date picker
        datePicker.show(getParentFragmentManager(), datePicker.toString());

        // Add a listener to handle the selected date range
        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                Long startDate = selection.first;
                Long endDate = selection.second;
                fetchTransactionsByDateRange(startDate, endDate);
            }
        });
    }

    private void fetchTransactionsByDateRange(Long startDate, Long endDate) {
        if (!sessionHandler.isLoggedIn()) {
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || endDate == null || startDate > endDate) {
            Toast.makeText(getContext(), "Invalid date range", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = sessionHandler.getUserDetails();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "User details not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        String apiKey = ApiKeyUtil.getDecodedApiKey(getContext());

        String startDateStr = convertLongToDateString(startDate);
        String endDateStr = convertLongToDateString(endDate);
        String url = String.format(GET_RECORDS + "?email=%s&api_key=%s&start_date=%s&end_date=%s", email, apiKey, startDateStr, endDateStr);

        showProgressDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    dismissProgressDialog();
                    try {
                        if (response.has("status") && response.getString("status").equals("error")) {
                            String message = response.getString("message");
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        } else {
                            generatePDF(response);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    dismissProgressDialog();
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_LONG).show();
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


    private String convertLongToDateString(Long dateLong) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(dateLong));
    }



    private void generatePDF(JSONObject response) {
        try {
            // Create the PDF file in the Documents directory
            File pdfFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports_new.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add a page to the document
            pdfDoc.addNewPage();

            // Add title with a rectangle border
            Paragraph title = new Paragraph("Transaction Report")
                    .setBold().setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER);

            // Create a rectangle with border around the title
            PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
            Rectangle rect = new Rectangle(36, pdfDoc.getDefaultPageSize().getTop() - 80,
                    pdfDoc.getDefaultPageSize().getWidth() - 72, 40);
            canvas.rectangle(rect);
            canvas.setStrokeColor(ColorConstants.BLACK).setLineWidth(1.5f).stroke();

            // Draw the title inside the rectangle
            document.add(title);

            // Add  message
            document.add(new Paragraph("This is a computer-generated report.")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
            // Extract Balances from response
            if (response.has("balances")) {
                JSONArray balances = response.getJSONArray("balances");

                // Add highlighted Balances Section
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
                String formattedDate = sdf.format(calendar.getTime());
                document.add(new Paragraph("Cash and cash equivalents as at " + formattedDate)
                        .setBold().setFontSize(16)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));

                Table balanceTable = new Table(new float[]{1, 3, 3}).setTextAlignment(TextAlignment.CENTER);
                balanceTable.addCell("Account Name");
                balanceTable.addCell("Account Type");
                balanceTable.addCell("Balance");

                double totalBalance = 0;

                for (int i = 0; i < balances.length(); i++) {
                    JSONObject balance = balances.getJSONObject(i);
                    double accountBalance = balance.getDouble("balance");

                    balanceTable.addCell(balance.getString("account_name"));
                    balanceTable.addCell(balance.getString("account_type"));
                    balanceTable.addCell(formatAmount(accountBalance));

                    totalBalance += accountBalance;
                }

                // Add total balance to the table
                balanceTable.addCell(""); // Empty cell for the first column
                balanceTable.addCell(new Paragraph("Total Balance").setBold());
                balanceTable.addCell(formatAmount(totalBalance));

                document.add(balanceTable);
            } else {
                // Handle the case where balances do not exist
                document.add(new Paragraph("No balances available.")
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER));
            }


            // Add highlighted Cash Flow Statement Section
            document.add(new Paragraph("Cash Flow Statement")
                    .setBold().setFontSize(16)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            double totalIncome = 0, totalExpense = 0;

            JSONArray transactions = response.getJSONArray("transactions");
            for (int i = 0; i < transactions.length(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                double amount = transaction.getDouble("amount");

                if (transaction.getString("transaction_type").equalsIgnoreCase("income")) {
                    totalIncome += amount;
                } else {
                    totalExpense += amount;
                }
            }

            document.add(new Paragraph(String.format("Total Income: %s", formatAmount(totalIncome))));
            document.add(new Paragraph(String.format("Total Expense: %s", formatAmount(totalExpense))));

            // Add highlighted Transactions Section
            document.add(new Paragraph("Transactions")
                    .setBold().setFontSize(16)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Table transactionTable = new Table(new float[]{1, 3, 2, 3}).setTextAlignment(TextAlignment.CENTER);
            transactionTable.addCell("Transaction ID");
            transactionTable.addCell("Amount");
            transactionTable.addCell("Type");
            transactionTable.addCell("Date");

            // Loop through transactions in reverse order
            for (int i = transactions.length() - 1; i >= 0; i--) {
                JSONObject transaction = transactions.getJSONObject(i);
                transactionTable.addCell(String.valueOf(transaction.getInt("transaction_id")));
                transactionTable.addCell(formatAmount(transaction.getDouble("amount")));
                transactionTable.addCell(transaction.getString("transaction_type"));
                transactionTable.addCell(transaction.getString("transaction_date"));
            }

            document.add(transactionTable);
            document.close();

            // Store the PDF file path for later use
            pdfUri = storePDFInMediaStore(pdfFile);
            openPDF(pdfUri); // Open the PDF after storing it

        } catch (IOException | JSONException e) {
            Log.e("PDF Error", e.toString());
        }
    }

    private String formatAmount(double amount) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setGroupingUsed(true);
        return "Rs." + formatter.format(amount);
    }

    private Uri storePDFInMediaStore(File pdfFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, pdfFile.getName());
        values.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        Uri uri = getContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

        if (uri != null) {
            try (OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
                 FileInputStream inputStream = new FileInputStream(pdfFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return uri; // Return the Uri for later use
            } catch (IOException e) {
                Log.e("PDF Store Error", e.toString());
            }
        } else {
            Log.e("PDF Store Error", "Failed to get URI for MediaStore.");
        }
        return null; // Return null if failed
    }

    private void openPDF(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }


    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Wait while loading...");
            progressDialog.setCancelable(false); // Disable dismiss by tapping outside
        }
        progressDialog.show(); // Show the dialog
    }
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss(); // Dismiss the dialog
        }
    }


}
