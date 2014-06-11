package fr.tvbarthel.apps.simplethermometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import fr.tvbarthel.apps.simplethermometer.openweathermap.OpenWeatherMapParserAsyncTask;
import fr.tvbarthel.apps.simplethermometer.openweathermap.OpenWeatherMapParserResult;
import fr.tvbarthel.apps.simplethermometer.utils.PreferenceUtils;

/**
 * A simple class that use an {@link fr.tvbarthel.apps.simplethermometer.openweathermap.OpenWeatherMapParserAsyncTask}
 * to load the temperature from the OpenWeatherMap Api.
 * The temperature loaded is stored in the DefaultSharedPreferences of the application.
 */
public class TemperatureLoader implements OpenWeatherMapParserAsyncTask.Listener {

    //automatic update interval (in Millis)
    public static final long UPDATE_INTERVAL_IN_MILLIS = 3600000;
    //manual update interval (in Millis)
    public static final long UPDATE_INTERVAL_IN_MILLIS_MANUAL = 600000;

    private Context mContext;
    private OpenWeatherMapParserAsyncTask mOpenWeatherMapParserAsyncTask;
    private Listener mListener;

    public TemperatureLoader(Listener listener, Context context) {
        mListener = listener;
        mContext = context;
        mOpenWeatherMapParserAsyncTask = null;
    }

    /**
     * Start the temperature update.
     * You should call this method only if you are sure that the current stored temperature is outdated
     * in order to keep the number of http request as little as possible.
     * <p/>
     * To check if the temperature is outdated use
     * {@link fr.tvbarthel.apps.simplethermometer.TemperatureLoader#isTemperatureOutdated(android.content.SharedPreferences, long)}
     */
    public void start(Location lastKnownLocation) {
        if (mOpenWeatherMapParserAsyncTask != null) {
            // there is already an async task running
            mListener.onTemperatureLoadingCancelled();
        } else {
            //Retrieve the latitude and the longitude from the location
            // and execute a weather loader
            final double latitude = lastKnownLocation.getLatitude();
            final double longitude = lastKnownLocation.getLongitude();

            //execute the AsyncTask
            mOpenWeatherMapParserAsyncTask = new OpenWeatherMapParserAsyncTask(this);
            mOpenWeatherMapParserAsyncTask.execute(String.format(
                    mContext.getResources().getString(R.string.url_open_weather_api), latitude, longitude));
        }
    }


    /**
     * Pause the loader
     */
    public void pause() {
        if (mOpenWeatherMapParserAsyncTask != null) {
            mOpenWeatherMapParserAsyncTask.cancel(true);
        }
    }

	/*
        OpenWeatherMapParserAsyncTask.Listener Overrides
	 */

    @Override
    public void onWeatherLoadingSuccess(OpenWeatherMapParserResult result) {
        if (result != null && result.getTemperatureValue() != null) {
            //Store the temperature and the time of the update
            //in the default shared preferences
            final float newTemperatureInCelsius = result.getTemperatureValue();
            final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            PreferenceUtils.storeTemperatureInCelsius(defaultSharedPreferences, newTemperatureInCelsius);
            mOpenWeatherMapParserAsyncTask = null;
            mListener.onTemperatureLoadingSuccess();
        }
    }

    @Override
    public void onWeatherLoadingFail(int stringResourceId) {
        mListener.onTemperatureLoadingFail(stringResourceId);
        mOpenWeatherMapParserAsyncTask = null;
    }

    @Override
    public void onWeatherLoadingProgress(int progress) {

    }

    @Override
    public void onWeatherLoadingCancelled() {
        mListener.onTemperatureLoadingCancelled();
        mOpenWeatherMapParserAsyncTask = null;
    }

    /**
     * A public interface to notify the temperature loading states
     */
    public interface Listener {
        public void onTemperatureLoadingSuccess();

        public void onTemperatureLoadingFail(int stringResourceId);

        public void onTemperatureLoadingCancelled();

    }

    /**
     * Check if the temperature stored in the sharedPreferences is outdated.
     *
     * @param sharedPreferences is used to retrieve the last update time
     * @param updateInterval    is used to define "outdated". (now - lastUpdate > updateInterval)
     * @return true if the temperature is outdated, false otherwise.
     */
    public static boolean isTemperatureOutdated(SharedPreferences sharedPreferences, long updateInterval) {
        boolean isOutdated = false;

        //Retrieve the current time and the time of the last update (in Millis)
        final long now = System.currentTimeMillis();
        final long lastUpdate = sharedPreferences.getLong(PreferenceUtils.PREF_KEY_LAST_UPDATE_TIME, 0);

        //Check if the temperature is outdated
        //according to updateInterval
        if (now - lastUpdate > updateInterval) {
            isOutdated = true;
        }

        return isOutdated;
    }
}
