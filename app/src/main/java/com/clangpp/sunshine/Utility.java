package com.clangpp.sunshine;

import android.content.Context;
import android.preference.PreferenceManager;

import com.clangpp.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ytzhang on 11/2/14.
 */
public class Utility {
    public static String getPreferredLocation(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.pref_location_key),
                        context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.pref_units_key),
                        context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
