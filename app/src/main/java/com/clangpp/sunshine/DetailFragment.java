package com.clangpp.sunshine;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.clangpp.sunshine.data.WeatherContract;
import com.clangpp.sunshine.data.WeatherContract.LocationEntry;
import com.clangpp.sunshine.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_TABLE_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_HUMIDITY = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_PRESSURE = 9;
    public static final int COL_WEATHER_ID = 10;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WEATHER_ID
    };
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 1;
    private static final String LOCATION_KEY = "location";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String INDEX_KEY = "index";
    TextView dayView;
    TextView dateView;
    TextView forecastView;
    TextView highView;
    TextView lowView;
    TextView humidityView;
    TextView windView;
    TextView pressureView;
    ImageView iconView;

    private String location;
    private String sharedForecast;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(String date) {
        DetailFragment fragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putString(DetailActivity.DATE_KEY, date);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        dayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        forecastView =
                (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null && args.containsKey(DetailActivity.DATE_KEY) && location != null &&
                !location.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            location = savedInstanceState.getString(LOCATION_KEY);
        }
        Bundle args = getArguments();
        if (args != null && args.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharedForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_KEY, location);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        // ShareActionProvider shareActionProvider =
        //         (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) menuItem.getActionProvider();
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "ShareActionProvider is null.");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        location = Utility.getPreferredLocation(getActivity());
        String date = getArguments().getString(DetailActivity.DATE_KEY);
        Uri weatherForLocationAndDateUri =
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
        return new CursorLoader(
                getActivity(),
                weatherForLocationAndDateUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            boolean isMetric = Utility.isMetric(getActivity());
            String dbDate = cursor.getString(COL_WEATHER_DATE);
            String day = Utility.getDayName(getActivity(), dbDate);
            String date = Utility.getFormattedMonthDay(getActivity(), dbDate);
            String forecast = cursor.getString(COL_WEATHER_DESC);
            String high = Utility.formatTemperature(
                    getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP),
                    isMetric);
            String low = Utility.formatTemperature(
                    getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP),
                    isMetric);
            String humidity = String.format(
                    getActivity().getString(R.string.format_humidity),
                    cursor.getFloat(COL_WEATHER_HUMIDITY));
            String wind = Utility.getFormattedWind(
                    getActivity(), cursor.getFloat(COL_WEATHER_WIND_SPEED),
                    cursor.getFloat(COL_WEATHER_DEGREES));
            String pressure = String.format(
                    getActivity().getString(R.string.format_pressure),
                    cursor.getFloat(COL_WEATHER_PRESSURE));
            int weatherResourceId = WeatherContract.getArtResourceForWeatherCondition(
                    cursor.getInt(COL_WEATHER_ID));

            dayView.setText(day);
            dateView.setText(date);
            forecastView.setText(forecast);
            highView.setText(high);
            lowView.setText(low);
            humidityView.setText(humidity);
            windView.setText(wind);
            pressureView.setText(pressure);
            iconView.setImageResource(
                    (weatherResourceId != -1) ? weatherResourceId : R.drawable.ic_clear);
            iconView.setContentDescription(forecast);

            sharedForecast = String.format(
                    "%s - %s - %s/%s",
                    dateView.getText(),
                    forecastView.getText(),
                    highView.getText(),
                    lowView.getText());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }
}