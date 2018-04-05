package enhabyto.com.viewzapps;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class youtubeVideoAd extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks {

    Button button;
    YouTubePlayerView youTubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String PROFILE_DETAIL_NAME = "profile_detail_name_yva";
    private static final String USER_ACCOUNT_EMAIL = "UserAccountEmail";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBE_FORCE_SSL, YouTubeScopes.YOUTUBE };

    private static final String APPLICATION_NAME = "viewZapps";

    private TextView mOutputText, name_tv;
    String name_tx;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    private static final String BUTTON_TEXT = "Call YouTube Data API";

    CircularImageView profileImage_civ;

    private static final String YOUTUBE_AD_ITEM_PREF = "youtube_ad_item_pref";
    private static final String AD_KEY = "ad_key";
    private static final String ADS = "ads";
    private static final String AD_URL = "adUrl";
    private static final String YOUTUBE_ADS = "youtubeAds";
    private static final String API_KEY = "AIzaSyCcE1PubW2vHl4slJGvxaPi1bStuFKUKlI";
    private String ad_key;
    //    database reference
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_ad);

        button = findViewById(R.id.button2);
        youTubePlayerView = findViewById(R.id.yva_youTubePlayer);

//        Circular image view
        profileImage_civ = findViewById(R.id.yva_profileImage);
//        placeholder
        Glide.with(youtubeVideoAd.this)
                .load(R.drawable.ic_profile_image_placeholder)
                .into(profileImage_civ);
//        circular image view

//        text view
        name_tv = findViewById(R.id.yva_nameTextView);
//        text view


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youTubePlayerView.initialize(API_KEY, onInitializedListener);
            }
        });


        mCallApiButton = findViewById(R.id.yva_youTubePlayerAuthenticate);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
            //    mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

    }
//onCreate ends

//    getting ad data
    public void getAdData(){
        //shared preferences get ad key
        final SharedPreferences sharedpreferences = getSharedPreferences(YOUTUBE_AD_ITEM_PREF, MODE_PRIVATE);
        final  String ad_key = sharedpreferences.getString(AD_KEY, "");
        //shared preferences

        databaseReference.child(ADS).child(YOUTUBE_ADS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                                final String ad_url = dataSnapshot.child(ad_key).child("watchId").getValue(String.class);
                                youTubePlayer.loadVideo(ad_url);
                                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                                youTubePlayer.setManageAudioFocus(true);
                            }
                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                            }
                        }; // onInitializedListener  ends

                    }//onDataChange ends

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }); //databaseReference ends
    }
    //    getting ad data

    public void onStart(){
        super.onStart();

        getAdData();
        setProfileData();
        setImage();
    }

    //getResultFromApi
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
           // mOutputText.setText("No network connection available.");
            Log.w("raky", "No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }//getResultFromApi

//    getting permission from user
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                accountName = "rk6039387@gmail.com";
                mCredential.setSelectedAccountName(accountName);

                    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                    getResultsFromApi();

              /*  startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER); */
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    //    getting permission from user


//    handling onActivity Result
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                   /* mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");*/
                    Log.w("raky", "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
          /*  case REQUEST_ACCOUNT_PICKER:
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
                break;*/
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }
//    handling onActivity Result

//    requesting permission
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
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
//    requesting permission


//    checking if device is online
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
//    checking if device is online

    //    checking if googlePlayServices is Available
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
    //    checking if googlePlayServices is Available

//    if googlePlayServices not available, then acquire
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
//    if googlePlayServices not available, then acquire

//    show error if google play services not available
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                youtubeVideoAd.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    //    show error if google play services not available


//    making Request to handle youtube data api requests, like, dislike, subscribe
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("viewZapp")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {

            try {
                  mService.videos().rate("zX7I_Rw8Q0I", "like").execute();
                  Log.w("raky", "this is being hit, liked by "+mCredential.getSelectedAccountName());
                  return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                Log.w("raky", "error: "+e.getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
          //  mOutputText.setText("");
          //  Log.w("raky", "nothing 2.");
          //  mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
          //  mProgress.hide();
            if (output == null || output.size() == 0) {
              //  mOutputText.setText("No results returned.");
                Log.w("raky", "No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
               // mOutputText.setText(TextUtils.join("\n", output));
                Log.w("raky", TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
          //  mProgress.hide();
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
                  //  mOutputText.setText("The following error occurred:\n"
                     //       + mLastError.getMessage());
                    Log.w("raky", "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
                Log.w("raky", "Request cancelled.");
            }
        }
    }
//    making Request to handle youtube data api requests, like, dislike, subscribe


    //    setting profile image
    public void setImage(){
        databaseReference.child("ads").child("youtubeAds").child("-L79O4JQke2IR4SadROI")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imagePath = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        Picasso.with(youtubeVideoAd.this)
                                .load(imagePath)
                                .placeholder(R.drawable.ic_profile_image_placeholder)
                                .error(R.drawable.ic_warning)
                                .into(profileImage_civ);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    } // set image ends

    //    setting profile data
    private void setProfileData() {
        SharedPreferences sharedpreferences = getSharedPreferences(PROFILE_DETAIL_NAME, MODE_PRIVATE);
        name_tx = sharedpreferences.getString("name","");
//        setting values
        name_tv.setText(name_tx);
        // zapNumber_tv.setText(phone_tx);

        databaseReference.child("ads").child("youtubeAds").child("-L79O4JQke2IR4SadROI")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        getting values
                        name_tx = dataSnapshot.child("profile_details").child("name").getValue(String.class);
//                        setting values
                        name_tv.setText(name_tx);
//                        storing values in shared preferences
                        try {
                            SharedPreferences sharedpreferences = getSharedPreferences(PROFILE_DETAIL_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("name", name_tx);
                            editor.apply();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }//setting profile data


//    ends
}

