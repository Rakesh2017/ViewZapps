package enhabyto.com.viewzapps;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
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

    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String USER_ACCOUNT_EMAIL = "UserAccountEmail";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY
            , YouTubeScopes.YOUTUBE_FORCE_SSL, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBE
    , Scopes.PROFILE };

    Credential credential;
    GoogleClientSecrets clientSecrets;

    private static final String APPLICATION_NAME = "viewZapps";
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), ".credentials/java-youtube-api-tests");


    private static FileDataStoreFactory DATA_STORE_FACTORY;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    private static final Collection<String> SCOPES1 = Arrays.asList("YouTubeScopes.https://www.googleapis.com/auth/youtube.force-ssl YouTubeScopes.https://www.googleapis.com/auth/youtubepartner", YouTubeScopes.YOUTUBE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_ad);

        button = findViewById(R.id.button2);
        youTubePlayerView = findViewById(R.id.youPlayer);



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

        try {
            HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            Log.w("raky1", String.valueOf(HTTP_TRANSPORT));
        } catch (Exception t) {
            t.printStackTrace();
            Log.w("raky2", t.getLocalizedMessage());
        }

        Reader r = new StringReader("{\"installed\":{\"client_id\":\"1094700917534-61sc62n1vbfuq34rus5oqdevt1d2jsah.apps.googleusercontent.com\",\"project_id\":\"viewzapps\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}");
        InputStream in = youtubeVideoAd.class.getResourceAsStream("/client_secret.json");

        try {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, r);
            Log.w("raky3", String.valueOf(clientSecrets));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.w("raky3", "kk"+ e.getLocalizedMessage());
        }


        Thread thread = new Thread() {
            @Override
            public void run() {

                // Build flow and trigger user authorization request.
                GoogleAuthorizationCodeFlow flow = null;
                try {
                    flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES1)
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .setAccessType("offline")
                            .build();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    credential = new AuthorizationCodeInstalledApp(
                            flow, new LocalServerReceiver()).authorize("enhabyto.rakesh@gmail.com");

                } catch (IOException | NullPointerException | NoClassDefFoundError e) {
                   // Log.w("raky7", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                System.out.println(
                        "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());

                final YouTube youTube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, mCredential).setApplicationName(APPLICATION_NAME).build();
                Log.w("raky4", String.valueOf(youTube));

                final HashMap<String, String> parameters = new HashMap<>();
                parameters.put("id", "E6UTz_Doic8");
                parameters.put("rating", "like");


                YouTube.Videos.Rate videosRateRequest = null;
                try {
                    videosRateRequest = youTube.videos().rate(parameters.get("id"), parameters.get("rating"));
                    videosRateRequest.execute();
                } catch (GoogleJsonResponseException e) {
                    e.printStackTrace();
                    Log.w("raky5", String.valueOf(e.getDetails().getCode()) + e.getDetails().getMessage());
                    System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
                } catch (Exception t) {
                    t.printStackTrace();
                    Log.w("raky6", "hello "+ t.getCause().getLocalizedMessage());
                }
            }
        };
        thread.start();



    }
//onCreate ends

    // get Result for authentication
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (! isDeviceOnline()) {
            new SweetAlertDialog(this)
                    .setCustomImage(R.drawable.no_internet_icon)
                    .setTitleText("No network connection available.")
                    .show();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
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
                Toast.makeText(this, ""+accountName, Toast.LENGTH_SHORT).show();
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
               /* SharedPreferences shared = getSharedPreferences(USER_ACCOUNT_EMAIL, MODE_PRIVATE);
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
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
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


//    ends
}

