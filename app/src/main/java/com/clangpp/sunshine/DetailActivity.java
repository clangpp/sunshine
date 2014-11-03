package com.clangpp.sunshine;

import android.app.Activity;
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
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.clangpp.sunshine.data.WeatherContract;


public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DetailFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final int DETAIL_LOADER = 1;
        private static final String LOCATION_KEY = "location";

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String date;
        private String location;
        private String sharedForecast;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                date = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (null != location && !location.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
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
            Uri weatherForLocationAndDateUri =
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            return new CursorLoader(
                    getActivity(),
                    weatherForLocationAndDateUri,
                    ForecastFragment.FORECAST_COLUMNS,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                boolean isMetric = Utility.isMetric(getActivity());
                String date =
                        Utility.formatDate(cursor.getString(ForecastFragment.COL_WEATHER_DATE));
                String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
                String high = Utility.formatTemperature(
                        cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
                String low = Utility.formatTemperature(
                        cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);

                TextView dateView = (TextView) getView().findViewById(R.id.detail_date_textview);
                TextView forecastView =
                        (TextView) getView().findViewById(R.id.detail_forecast_textview);
                TextView highView = (TextView) getView().findViewById(R.id.detail_high_textview);
                TextView lowView = (TextView) getView().findViewById(R.id.detail_low_textview);

                dateView.setText(date);
                forecastView.setText(forecast);
                highView.setText(high);
                lowView.setText(low);

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
}
