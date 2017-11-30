package com.joekelly.mapsandlocation;

import android.app.Activity;
import android.content.SharedPreferences;

/**

 */

public class CityPref {
    SharedPreferences prefs;

    public CityPref(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    String getCity(){
        return prefs.getString("city", "Dublin, IE");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}
