package com.icbtcampus.budgettracker_assignment.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ApiKeyUtil {

    // Method to get and decode the API key
    public static String getDecodedApiKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        String encryptedApiKey = sharedPreferences.getString("api_key", null);

        if (encryptedApiKey != null) {
            // Decrypt the API key
            return CryptoUtil.decodeBase64(encryptedApiKey);
        }

        return null;
    }
}
