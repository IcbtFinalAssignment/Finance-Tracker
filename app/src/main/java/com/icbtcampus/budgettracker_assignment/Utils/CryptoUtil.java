package com.icbtcampus.budgettracker_assignment.Utils;

import android.util.Base64;

public class CryptoUtil {

    // Decode the Base64 encoded string
    public static String decodeBase64(String encodedData) {
        try {

            String decodedOnce = new String(Base64.decode(encodedData, Base64.DEFAULT), "UTF-8");
            String decodedTwice = new String(Base64.decode(decodedOnce, Base64.DEFAULT), "UTF-8");
            String decodedThrice = new String(Base64.decode(decodedTwice, Base64.DEFAULT), "UTF-8");

            return decodedThrice;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle decoding failure
        }
    }

}

