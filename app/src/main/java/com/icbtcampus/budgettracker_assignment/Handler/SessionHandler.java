package com.icbtcampus.budgettracker_assignment.Handler;

import android.content.Context;
import android.content.SharedPreferences;

import com.icbtcampus.budgettracker_assignment.Model.User;

import java.util.Date;

public class SessionHandler {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMPTY = "";
    private static final long SESSION_DURATION = 7 * 24 * 60 * 60 * 1000; // 7 days
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPreferences;

    public SessionHandler(Context mContext) {
        this.mContext = mContext;
        mPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.mEditor = mPreferences.edit();
    }

    /**
     * Logs in the user by saving user details and setting session
     *
     * @param email
     * @param fullName
     */
    public void loginUser(String email, String fullName) {
        mEditor.putString(KEY_EMAIL, email);
        mEditor.putString(KEY_FULL_NAME, fullName);
        long expiryMillis = new Date().getTime() + SESSION_DURATION;
        mEditor.putLong(KEY_EXPIRES, expiryMillis);
        mEditor.apply();
    }

    /**
     * Checks whether user is logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        long expiryMillis = mPreferences.getLong(KEY_EXPIRES, 0);
        if (expiryMillis == 0) {
            return false;
        }
        return new Date().before(new Date(expiryMillis));
    }

    /**
     * Fetches and returns user details
     *
     * @return user details
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        User user = new User();
        user.setEmail(mPreferences.getString(KEY_EMAIL, KEY_EMPTY));
        user.setFullName(mPreferences.getString(KEY_FULL_NAME, KEY_EMPTY));
        user.setSessionExpiryDate(new Date(mPreferences.getLong(KEY_EXPIRES, 0)));
        return user;
    }

    /**
     * Logs out user by clearing the session
     */
    public void logoutUser() {
        mEditor.clear();
        mEditor.apply();
    }
}
