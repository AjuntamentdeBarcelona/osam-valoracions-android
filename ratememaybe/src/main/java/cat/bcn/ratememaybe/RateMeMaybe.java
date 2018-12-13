package cat.bcn.ratememaybe;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static cat.bcn.ratememaybe.RateMeMaybeFragment.UNDEFINED;

public class RateMeMaybe implements RateMeMaybeFragment.RMMFragInterface {
    private static final String TAG = "RateMeMaybe";
    private static final int TIMEOUT_VALUE = 10 * 1000;

    private FragmentActivity mActivity;

    private SharedPreferences mPreferences;

    private int mIcon;

    private int mButtonsTextColor = UNDEFINED;

    private int mTitleColor = UNDEFINED;

    private int mMessageColor = UNDEFINED;

    private int mBackgroundColor = UNDEFINED;

    private int mMinLaunchesUntilInitialPrompt = 0;

    private int mMinDaysUntilInitialPrompt = 0;

    private int mMinLaunchesUntilNextPrompt = 0;

    private int mMinDaysUntilNextPrompt = 0;

    private int tmin;

    private int numApert;

    private Map<String, String> messages;

    private String serviceUrl;

    private Boolean mHandleCancelAsNeutral = true;

    private Boolean mRunWithoutPlayStore = false;

    private OnRMMUserChoiceListener mListener;

    public RateMeMaybe(FragmentActivity activity) {
        mActivity = activity;
        mPreferences = mActivity.getSharedPreferences(PREF.NAME, Context.MODE_PRIVATE);
    }

    /**
     * Reset the launch logs
     */
    public static void resetData(FragmentActivity activity) {
        activity.getSharedPreferences(PREF.NAME, Context.MODE_PRIVATE).edit().clear().apply();
        Log.d(TAG, "Cleared RateMeMaybe shared preferences.");
    }

    public int getIcon() {
        return mIcon;
    }

    /**
     * @param customIcon Drawable id of custom icon
     */
    public void setIcon(int customIcon) {
        mIcon = customIcon;
    }

    public void setButtonsTextColor(int mButtonsTextColor) {
        this.mButtonsTextColor = mButtonsTextColor;
    }

    public void setTitleColor(int mTitleColor) {
        this.mTitleColor = mTitleColor;
    }

    public void setMessageColor(int mMessageColor) {
        this.mMessageColor = mMessageColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public void setTmin(int tmim) {
        this.tmin = tmim;
    }

    public void setNumApert(int numApert) {
        this.numApert = numApert;
    }

    /**
     * Sets requirements for when to prompt the user.
     *
     * @param minLaunchesUntilInitialPrompt Minimum of launches before the user is prompted for the first
     *                                      time. One call of .run() counts as launch.
     * @param minDaysUntilInitialPrompt     Minimum of days before the user is prompted for the first time.
     * @param minLaunchesUntilNextPrompt    Minimum of launches before the user is prompted for each next
     *                                      time. One call of .run() counts as launch.
     * @param minDaysUntilNextPrompt        Minimum of days before the user is prompted for each next time.
     */
    private void setPromptMinimums(int minLaunchesUntilInitialPrompt,
            int minDaysUntilInitialPrompt, int minLaunchesUntilNextPrompt,
            int minDaysUntilNextPrompt) {
        this.mMinLaunchesUntilInitialPrompt = minLaunchesUntilInitialPrompt;
        this.mMinDaysUntilInitialPrompt = minDaysUntilInitialPrompt;
        this.mMinLaunchesUntilNextPrompt = minLaunchesUntilNextPrompt;
        this.mMinDaysUntilNextPrompt = minDaysUntilNextPrompt;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * @param handleCancelAsNeutral Standard is true. If set to false, a back press (or other
     *                              things that lead to the dialog being cancelled), will be
     *                              handled like a negative choice (click on "Never").
     */
    public void setHandleCancelAsNeutral(Boolean handleCancelAsNeutral) {
        this.mHandleCancelAsNeutral = handleCancelAsNeutral;
    }

    /**
     * Sets an additional callback for when the user has made a choice.
     */
    public void setAdditionalListener(OnRMMUserChoiceListener listener) {
        mListener = listener;
    }

    /**
     * Standard is false. Whether the run method is executed even if no Play
     * Store is installed on device.
     */
    public void setRunWithoutPlayStore(Boolean runWithoutPlayStore) {
        mRunWithoutPlayStore = runWithoutPlayStore;
    }

    /**
     * Actually show the dialog (if it is not currently shown)
     */
    private void showDialog() {
        if (mActivity.getSupportFragmentManager().findFragmentByTag(
                "rmmFragment") != null) {
            // the dialog is already shown to the user
            return;
        }
        RateMeMaybeFragment frag = new RateMeMaybeFragment();
        frag.setData(getIcon(), this, mButtonsTextColor, mTitleColor, mMessageColor, mBackgroundColor, messages);
        if (!mActivity.isFinishing()) {
            frag.show(mActivity.getSupportFragmentManager(), "rmmFragment");
        } else {
            mListener.handleError();
        }
    }

    /**
     * Forces the dialog to show, even if the requirements are not yet met. Does
     * not affect prompt logs. Use with care.
     */
    public void forceShow() {
        if (serviceUrl != null) {
            if (isNetworkAvailable()) {
                new GetParamsAsyncTask(mActivity, true).execute();
            }
        } else {
            showDialog();
        }
    }

    /**
     * Normal way to update the launch logs and show the user prompt if the
     * requirements are met.
     */
    public void run() {

        int version = mPreferences.getInt(PREF.VERSION, 0);
        if (BuildConfig.VERSION_CODE != version) {
            // different version code:
            mPreferences.edit().clear().putInt(PREF.VERSION, BuildConfig.VERSION_CODE).apply();
        }

        if (mPreferences.getBoolean(PREF.DONT_SHOW_AGAIN, false)) {
            mListener.handlePositive();
            return;
        }

        if (!isPlayStoreInstalled()) {
            Log.d(TAG, "No Play Store installed on device.");
            if (!mRunWithoutPlayStore) {
                mListener.handleError();
                return;
            }
        }

        if (serviceUrl != null) {
            if (isNetworkAvailable()) {
                new GetParamsAsyncTask(mActivity, false).execute();
            }
        } else {
            showDialogIfRequired();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void showDialogIfRequired() {
        Editor editor = mPreferences.edit();

        int totalLaunchCount = mPreferences.getInt(PREF.TOTAL_LAUNCH_COUNT, 0) + 1;
        editor.putInt(PREF.TOTAL_LAUNCH_COUNT, totalLaunchCount);

        long currentMillis = System.currentTimeMillis();

        long timeOfAbsoluteFirstLaunch = mPreferences.getLong(PREF.TIME_OF_ABSOLUTE_FIRST_LAUNCH, 0);
        if (timeOfAbsoluteFirstLaunch == 0) {
            // this is the first launch!
            timeOfAbsoluteFirstLaunch = currentMillis;
            editor.putLong(PREF.TIME_OF_ABSOLUTE_FIRST_LAUNCH, timeOfAbsoluteFirstLaunch);
        }

        long timeOfLastPrompt = mPreferences.getLong(PREF.TIME_OF_LAST_PROMPT, 0);

        int launchesSinceLastPrompt = mPreferences.getInt(PREF.LAUNCHES_SINCE_LAST_PROMPT, 0) + 1;
        editor.putInt(PREF.LAUNCHES_SINCE_LAST_PROMPT, launchesSinceLastPrompt);

        setPromptMinimums(numApert, tmin, numApert, tmin);
        boolean showDialog = false;
        if (mMinLaunchesUntilInitialPrompt > 0 && mMinLaunchesUntilNextPrompt > 0) {
            // if num_apert == 0 the popup is never shown

            if (totalLaunchCount >= mMinLaunchesUntilInitialPrompt
                    && ((currentMillis - timeOfAbsoluteFirstLaunch)) >= (mMinDaysUntilInitialPrompt
                    * DateUtils.DAY_IN_MILLIS)) {
                // requirements for initial launch are met

                if (timeOfLastPrompt == 0 /* user was not yet shown a prompt */
                        || (launchesSinceLastPrompt >= mMinLaunchesUntilNextPrompt &&
                        ((currentMillis - timeOfLastPrompt) >= (mMinDaysUntilNextPrompt * DateUtils.DAY_IN_MILLIS)))) {
                    editor.putLong(PREF.TIME_OF_LAST_PROMPT, currentMillis);
                    editor.putInt(PREF.LAUNCHES_SINCE_LAST_PROMPT, 0);
                    showDialog = true;
                }
            }
        }

        editor.apply();
        if (showDialog) {
            showDialog();
        } else {
            mListener.handleNeutral();
        }
    }

    @Override
    public void _handleCancel() {
        if (mHandleCancelAsNeutral) {
            _handleNeutralChoice();
        } else {
            _handleNegativeChoice();
        }
    }

    public void _handleNegativeChoice() {
        Editor editor = mPreferences.edit();
        editor.putBoolean(PREF.DONT_SHOW_AGAIN, true);
        editor.apply();
        if (mListener != null) {
            mListener.handleNegative();
        }
    }

    public void _handleNeutralChoice() {
        if (mListener != null) {
            mListener.handleNeutral();
        }
    }

    public void _handlePositiveChoice() {
        Editor editor = mPreferences.edit();
        editor.putBoolean(PREF.DONT_SHOW_AGAIN, true);
        editor.apply();

        final String appPackageName = mActivity.getPackageName(); // getPackageName() from Context or Activity object
        if (mListener != null) {
            mListener.handlePositive();
        }
        try {

            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mActivity.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    /**
     * @return Whether Google Play Store is installed on device
     */
    private Boolean isPlayStoreInstalled() {
        PackageManager pacman = mActivity.getPackageManager();
        try {
            pacman.getApplicationInfo("com.android.vending", 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public interface OnRMMUserChoiceListener {
        void handlePositive();

        void handleNeutral();

        void handleNegative();

        void handleError();
    }

    static class PREF {
        public static final String NAME = "rate_me_maybe";
        /**
         * How many times the app was launched in total
         */
        public static final String TOTAL_LAUNCH_COUNT = "PREF_TOTAL_LAUNCH_COUNT";
        /**
         * Timestamp of when the app was launched for the first time
         */
        public static final String TIME_OF_ABSOLUTE_FIRST_LAUNCH =
                "PREF_TIME_OF_ABSOLUTE_FIRST_LAUNCH";
        /**
         * How many times the app was launched since the last prompt
         */
        public static final String LAUNCHES_SINCE_LAST_PROMPT = "PREF_LAUNCHES_SINCE_LAST_PROMPT";
        /**
         * Timestamp of the last user prompt
         */
        public static final String TIME_OF_LAST_PROMPT = "PREF_TIME_OF_LAST_PROMPT";
        /**
         * The application version when last checked
         */
        public static final String VERSION = "VERSION";
        private static final String DONT_SHOW_AGAIN = "PREF_DONT_SHOW_AGAIN";
    }

    private class GetParamsAsyncTask extends AsyncTask<Void, Void, ParamsDto> {

        private ProgressDialog progressDialog;
        private boolean forceShow;

        public GetParamsAsyncTask(Activity mActivity, boolean forceShow) {
            progressDialog = new ProgressDialog(mActivity);
            this.forceShow = forceShow;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected ParamsDto doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(serviceUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(TIMEOUT_VALUE);
                urlConnection.setReadTimeout(TIMEOUT_VALUE);
                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                Log.d(TAG, stringBuilder.toString());
                ParamsDto paramsDto = new Gson().fromJson(stringBuilder.toString(), ParamsDto.class);
                return paramsDto;

            } catch (IOException e) {
                e.printStackTrace();
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(ParamsDto paramsModel) {
            if (paramsModel != null) {
                tmin = paramsModel.tmin;
                numApert = paramsModel.numApert;
                messages = new HashMap<String, String>();
                if (!paramsModel.messages.isEmpty()) {
                    for (Message message : paramsModel.messages) {
                        messages.put(message.languaje.toLowerCase(), message.content);
                    }
                }
            }
            if (!mActivity.isFinishing()) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            } else {
                mListener.handleError();
            }
            if (!mActivity.isFinishing()) {
                if (forceShow) {
                    showDialog();
                } else {
                    showDialogIfRequired();
                }
            } else {
                mListener.handleError();
            }
        }
    }



}
