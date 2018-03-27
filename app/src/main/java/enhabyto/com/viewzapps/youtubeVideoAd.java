package enhabyto.com.viewzapps;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class youtubeVideoAd extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks {

    Button button;
    YouTubePlayerView youTubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;

    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String USER_ACCOUNT_EMAIL = "UserAccountEmail";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    private static final String APPLICATION_NAME = "viewZapps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_ad);

        button = findViewById(R.id.button2);
        youTubePlayerView = findViewById(R.id.yva_youTubePlayer);



        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    youTubePlayer.loadVideo("4M4R-dwnTQw");
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.setManageAudioFocus(true);
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                }
        };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youTubePlayerView.initialize("AIzaSyCcE1PubW2vHl4slJGvxaPi1bStuFKUKlI", onInitializedListener);
            }
        });

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

//        authenticate youtube user
        getResultsFromApi();


    }
//onCreate ends

    // get Result for authentication
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            new SweetAlertDialog(this)
                    .setCustomImage(R.drawable.no_internet_icon)
                    .setTitleText("No network connection available.")
                    .show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }
// get Result for authentication


// to check if wifi or mobile data is connected
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
// to check if wifi or mobile data is connected


//    permissions to access account of user
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, android.Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                Log.w("raky", "accName: "+mCredential.getSelectedAccountName());
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
              /*  SharedPreferences shared = getSharedPreferences(USER_ACCOUNT_EMAIL, MODE_PRIVATE);
                accountName = shared.getString("email", null);
                mCredential.setSelectedAccountName(accountName);
                if (accountName != null) {
                    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                    getResultsFromApi();
                }*/
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
            Log.w("raky", "This also hit");
        }
    }
//    permissions to access account of user

//    is google play services are available
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
//    is google play services are available

//    if google play services not available then acquire
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
//    if google play services not available then acquire

//    googlePlayServices Error
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                youtubeVideoAd.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
//    googlePlayServices Error

//    handling the result
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    new SweetAlertDialog(this)
                            .setCustomImage(R.drawable.ic_warning)
                            .setTitleText("Google Play Services required!")
                            .setContentText("This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.")
                            .show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();

                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }
    //    handling the result


//    getting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
//        do nothing
    }
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
    }
//    getting permissions



    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        mService.videos().rate("Iix0awnFDS0", "like").execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.w("raky", "io: "+e.getCause());
                    }
                    try {

                        HashMap<String, String> parameters = new HashMap<>();
                        parameters.put("part", "snippet");
                        Subscription subscription = new Subscription();
                        SubscriptionSnippet subscriptionSnippet = new SubscriptionSnippet();
                        ResourceId resourceId = new ResourceId();
                        resourceId.set("channelId", "UCW2Ji4Ok_oBgsMpfcjKxiCQ");
                        resourceId.set("kind", "youtube#channel");
                        subscriptionSnippet.setResourceId(resourceId);
                        subscription.setSnippet(subscriptionSnippet);


                        YouTube.Subscriptions.Insert subscriptionsInsertRequest = mService.subscriptions().insert(parameters.get("part"), subscription);

                        Subscription response = subscriptionsInsertRequest.execute();
                        Log.w("raky", "response: "+response);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("raky", "error: "+e.getCause());
                    }

                }
            });
            thread.start();
            return null;
        }


        @Override
        protected void onPreExecute() {
           // mOutputText.setText("");
           // mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
           // mProgress.hide();
            if (output == null || output.size() == 0) {
            //    mOutputText.setText("No results returned.");
                Log.w("raky", "No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                Log.w("raky", TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
           // mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            youtubeVideoAd.REQUEST_AUTHORIZATION);
                } else {
                //    mOutputText.setText("The following error occurred:\n"
                          //  + mLastError.getMessage());
                    Log.w("raky", ("The following error occurred:\n"
                            + mLastError.getMessage()));
                }
            } else {
               // mOutputText.setText("Request cancelled.");
                Log.w("raky", "request  Cancelled");
            }
        }
    }

//    ends
}

