package edu.poly.nhtr.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

import edu.poly.nhtr.R;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    public  PreferenceManager(Context context)
    {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }
    public void putBoolean (String key, Boolean value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public Boolean getBoolean (String key)
    {
        return sharedPreferences.getBoolean(key, false);
    }
    public void putString (String key, String value )
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getString(String key)
    {
        return sharedPreferences.getString(key, null);
    }

    public void putInt (String key, int value )
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public Integer getInt(String key)
    {
        return sharedPreferences.getInt(key, -1);
    }

    public void putSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public Set<String> getSet(String key) {
        return sharedPreferences.getStringSet(key, new HashSet<>());
    }
    public void clear( )
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void removePreference(String keyUserId) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(keyUserId);
            editor.apply();
    }



    // Have key
    public void putString(String key, String value, String homeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key + "_" + homeId, value);
        editor.apply();
    }

    public String getString(String key, String homeId) {
        return sharedPreferences.getString(key + "_" + homeId, null);
    }

    public void putBoolean(String key, boolean value, String homeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key + "_" + homeId, value);
        editor.apply();
    }

    public boolean getBoolean(String key, String homeId) {
        return sharedPreferences.getBoolean(key + "_" + homeId, false);
    }

    public void putHome(String key, Home home, String userID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String homeJson = gson.toJson(home); // Convert Home object to JSON string
        editor.putString(key + "_" + userID, homeJson);
        editor.apply();
    }

    public Home getHome(String key, String userID) {
        String homeJson = sharedPreferences.getString(key + "_" + userID, null);
        if (homeJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(homeJson, Home.class); // Convert JSON string back to Home object
        }
        return null; // Return null if no value found for the key
    }

    public void putRoom(String key, Room room, String homeID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String roomJson = gson.toJson(room); // Convert Room object to JSON string
        editor.putString(key + "_" + homeID, roomJson);
        editor.apply();
    }

    public Room getRoom(String key, String homeID) {
        String roomJson = sharedPreferences.getString(key + "_" + homeID, null);
        if (roomJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(roomJson, Room.class); // Convert JSON string back to Home object
        }
        return null; // Return null if no value found for the key
    }

    public void removeString(String key, String homeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key + "_" + homeId);
        editor.apply();
    }

    public void removeBoolean(String key, String homeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key + "_" + homeId);
        editor.apply();
    }

    public void removeHome(String key, String userID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key + "_" + userID);
        editor.apply();
    }

    public void removeRoom(String key, String homeID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key + "_" + homeID);
        editor.apply();
    }




}
