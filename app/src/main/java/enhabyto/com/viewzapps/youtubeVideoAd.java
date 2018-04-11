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
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
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
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class youtubeVideoAd extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    ImageButton playVideo_ib, pauseVideo_ib, initializeVideo_ib;
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
    private static final String PROFILE_DETAILS = "profile_details";
    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String USER_UID = "userUid";
    private static final String PROFILE_IMAGE = "profile_image";
    private static final String PROFILE_IMAGE_URL = "profile_image_url";
    private static final String AD_TITLE = "ad_title";
    private static final String USER_ACCOUNT_EMAIL = "UserAccountEmail";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBE_FORCE_SSL, YouTubeScopes.YOUTUBE };

    private static final String APPLICATION_NAME = "viewZapps";
    private static int FORTY_FIVE_000 = 45000;

    private TextView mOutputText, name_tv;
    String name_tx;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    private static final String BUTTON_TEXT = "Call YouTube Data API";

    CircularImageView profileImage_civ;

    private static final String YOUTUBE_AD_ITEM_PREF = "youtube_ad_item_pref";
    private static final String AD_KEY = "ad_key";
    private static final String USERS = "users";
    private static final String ADS = "ads";
    private static final String AD_URL = "adUrl";
    private static final String YOUTUBE_ADS = "youtubeAds";
    private static final String API_KEY = "AIzaSyCcE1PubW2vHl4slJGvxaPi1bStuFKUKlI";
    NumberProgressBar videoProgress;

    TextView adTitle_tv;

    YouTubePlayer myYouTubePlayer = null;

    SharedPreferences sharedpreferences;

    //    database reference
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    int i = 0;
    boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_ad);

        sharedpreferences = getSharedPreferences(YOUTUBE_AD_ITEM_PREF, MODE_PRIVATE);


//        Text view
        adTitle_tv = findViewById(R.id.yva_adTitleTextView);
//        text view

//        image button
        playVideo_ib = findViewById(R.id.yva_playImageButton);
        pauseVideo_ib = findViewById(R.id.yva_pauseImageButton);
        initializeVideo_ib = findViewById(R.id.yva_initializeVideoImageButton);
        initializeVideo_ib.setEnabled(false); // onclick disable untl listener is ready, otherwise null pointer exception
//        image button

//        Progress bar
        videoProgress = findViewById(R.id.yva_videoProgress);
//        progress bar

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

        playVideo_ib.setOnClickListener(this);
        initializeVideo_ib.setOnClickListener(this);

    }
    //onCreate ends


//    initialise video, play video as activity starts
    private void PlayVideoAfterInitialization() {

        databaseReference.child(ADS).child(YOUTUBE_ADS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
//                        playing video

                        final  String ad_key = sharedpreferences.getString(AD_KEY, "");


                        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
                            @Override

                            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                                myYouTubePlayer = youTubePlayer;

                                final String ad_url = dataSnapshot.child(ad_key).child("watchId").getValue(String.class);
                                youTubePlayer.setFullscreen(false);
                                youTubePlayer.loadVideo(ad_url);
                                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                                youTubePlayer.setManageAudioFocus(true);
                                youTubePlayer.addFullscreenControlFlag(0);

//checking if user watched the video for 45 seconds and reward zap
                              final CountDownTimer countDownTimer = new CountDownTimer(FORTY_FIVE_000, 500)
                                {
                                    public void onTick(long millisUntilFinished)
                                    {
                                        try{
                                            i = youTubePlayer.getCurrentTimeMillis();
                                            int value = Math.abs(i * 100 / FORTY_FIVE_000);
                                            Log.w("raky", "value of i: "+i);
                                            videoProgress.setProgress(value);
                                            if (i  >= 45000) {
                                                videoProgress.setProgress(100);
                                                cancel();
                                                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                                            }
                                        }
                                        catch (Exception e){
                                            cancel();
                                        }

                                    }

                                    public void onFinish()
                                    {
                                        Log.w("raky", "finished!");
                                        if (i < 45000){
                                            start();
                                        }
                                    }
                                };
//checking if user watched the video for 45 seconds and reward zap ENDS

//                                Player State changed
                                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                                    @Override
                                    public void onLoading() {

                                        Log.w("raky", "onLoading");
                                    }

                                    @Override
                                    public void onLoaded(String s) {
                                        Log.w("raky", "onLoaded "+s);
                                    }

                                    @Override
                                    public void onAdStarted() {
                                        Log.w("raky", "adStarted");

                                    }

                                    @Override
                                    public void onVideoStarted() {
                                        Log.w("raky", "videoStarted");
                                        countDownTimer.start();
                                    }

                                    @Override
                                    public void onVideoEnded() {
                                        Log.w("raky", "videoEndede");
                                    }

                                    @Override
                                    public void onError(YouTubePlayer.ErrorReason errorReason) {
                                        Log.w("raky", "error"+ errorReason);
                                    }
                                });

//                                Player State changed ends

//                                playback event listener
                                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {

                                    @Override
                                    public void onPlaying() {
                                        Log.w("raky", "isPlaying");

                                    }
                                    @Override
                                    public void onPaused() {
                                        // i = youTubePlayer.getCurrentTimeMillis();
                                        Log.w("raky", "paused");
                                    }

                                    @Override
                                    public void onStopped() {
                                        Log.w("raky", "stopped");
                                    }

                                    @Override
                                    public void onBuffering(boolean b) {
                                        Log.w("raky", "buffering");
                                    }

                                    @Override
                                    public void onSeekTo(int i) {

                                    }

                                });


// pause video
                                pauseVideo_ib.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        youTubePlayer.pause();
                                    }
                                });
// pause video


//                                play video
                                playVideo_ib.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        youTubePlayer.play();
                                    }
                                });
//                                play video

                            }
                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                            }
                        };

                    }//onDataChange ends

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }); //databaseReference ends

    }
//    initialise video, play video as activity starts


    public void onStart(){
        super.onStart();

        PlayVideoAfterInitialization();
        setImage();
        adDetails();
        setDataOfAdPoster();
        initializeVideo_ib.setEnabled(true); // onclick enabled once listener is ready, otherwise null pointer exception

    }

//    ad details
    private void adDetails() {

        final String ad_title = sharedpreferences.getString(AD_TITLE, "");
        adTitle_tv.setText(ad_title);
    }
//ad details

    //    onclick
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
//            initialize video
            case R.id.yva_initializeVideoImageButton:
                youTubePlayerView.initialize(API_KEY, onInitializedListener);
                break;


        }
    }
    //    onclick ends

//   getting and setting name and profile pic
    private void setDataOfAdPoster() {
        databaseReference.child(ADS).child(YOUTUBE_ADS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final  String ad_key = sharedpreferences.getString(AD_KEY, "");

                        final String user_uid = dataSnapshot.child(ad_key).child(USER_UID).getValue(String.class);

                        if (!TextUtils.isEmpty(user_uid)){
                            databaseReference.child(USERS).child(user_uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final String user_name = dataSnapshot.child(PROFILE_DETAILS).child(NAME).getValue(String.class);
                                            final String profile_image = dataSnapshot.child(PROFILE_IMAGE).child(PROFILE_IMAGE_URL).getValue(String.class);
//                                          setting ad user name
                                            if (!TextUtils.isEmpty(user_name)){
                                                name_tv.setText(user_name);
                                            }Log.w("raky", "image url: "+profile_image);
//                                            setting ad profile image
                                            if (!TextUtils.isEmpty(profile_image)){
                                                Picasso.with(youtubeVideoAd.this)
                                                        .load(profile_image)
                                                        .placeholder(R.drawable.ic_profile_image_placeholder)
                                                        .error(R.drawable.ic_warning)
                                                        .into(profileImage_civ);
                                            }
                                            else {
                                                Picasso.with(youtubeVideoAd.this)
                                                        .load(R.drawable.ic_profile_image_placeholder)
                                                        .into(profileImage_civ);
                                            }

                                        } //onData change ends

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }//  ad user profile if

                    }//onDataChange ends
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }); //databaseReference ends
    }
//   getting and setting name and profile pic



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
                accountName = "rakeshsince1994@gmail.com";
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

            /*try {
                  mService.videos().rate("zX7I_Rw8Q0I", "like").execute();
                  Log.w("raky", "this is being hit, liked by "+mCredential.getSelectedAccountName());
                  return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                Log.w("raky", "error: "+e.getLocalizedMessage());
                return null;
            }*/

            try {
                String channelId = "UCW2Ji4Ok_oBgsMpfcjKxiCQ";
                Log.w("raky", "You chose " + channelId + " to subscribe.");

                // Create a resourceId that identifies the channel ID.
                ResourceId resourceId = new ResourceId();
                resourceId.setChannelId(channelId);
                resourceId.setKind("youtube#channel");

                // Create a snippet that contains the resourceId.
                SubscriptionSnippet snippet = new SubscriptionSnippet();
                snippet.setResourceId(resourceId);

                // Create a request to add the subscription and send the request.
                // The request identifies subscription metadata to insert as well
                // as information that the API server should return in its response.
                Subscription subscription = new Subscription();
                subscription.setSnippet(snippet);

                YouTube.Subscriptions.Insert subscriptionInsert = mService.subscriptions().insert("snippet,contentDetails", subscription);
                Subscription returnedSubscription = subscriptionInsert.execute();


                // Print information from the API response.
                Log.w("raky", "\n================== Returned Subscription ==================\n");
                Log.w("raky", "  - Id: " + returnedSubscription.getId());
                Log.w("raky", "  - Title: " + returnedSubscription.getSnippet().getTitle());
                Log.w("raky", "  - sdsa: " + returnedSubscription.getSnippet());
                Log.w("raky", "  - user: " + mCredential.getSelectedAccountName());

                return null;
            }
            catch (Exception e){
                mLastError = e;
                cancel(true);
                Log.w("raky", "sub error: "+e.getLocalizedMessage());
                return null;
            }




         /*   try {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "subscriberSnippet");
                parameters.put("channelId", "UCW2Ji4Ok_oBgsMpfcjKxiCQ");
                parameters.put("mine", "true");

                YouTube.Subscriptions.List subscriptionsListByChannelIdRequest = mService.subscriptions().list(parameters.get("part"));
                if (parameters.containsKey("channelId") && parameters.get("channelId") != "") {
                    subscriptionsListByChannelIdRequest.setChannelId(parameters.get("channelId"));
                }

                SubscriptionListResponse response = subscriptionsListByChannelIdRequest.execute();
              //  System.out.println(response);
                Log.w("raky", "subscriber name: "+mCredential.getSelectedAccountName());
                Log.w("raky", "subscriber list: "+response);
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                Log.w("raky", "sub error: "+e.getLocalizedMessage());
                return null;
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
                return null;

            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                Log.w("raky", "error: "+e.getCause());
                return null;
            }*/


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




//    ends
}

