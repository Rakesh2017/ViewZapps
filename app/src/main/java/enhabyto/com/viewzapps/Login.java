package enhabyto.com.viewzapps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.victor.loading.rotate.RotateLoading;
import com.wang.avi.AVLoadingIndicatorView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.droidsonroids.gif.GifImageView;



public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    ImageView loginTop_iv;
    GifImageView gifImageView;
    RelativeLayout relativeLayout2;
    FancyButton google_btn, insta_btn, fb_btn;

    Animation slideDown;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
    RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //avlLoading
        rotateLoading = findViewById(R.id.login_rotateLoading);

        // imageViews
        loginTop_iv = findViewById(R.id.login_topImageView);

        // gif
        gifImageView = findViewById(R.id.login_gif);


        //relative layout
        relativeLayout2 = findViewById(R.id.login_relativeLayout2);

        // fancy buttons
        google_btn = findViewById(R.id.login_googleButton);
        insta_btn = findViewById(R.id.login_instaButton);
        fb_btn = findViewById(R.id.login_faceBookButton);

        //Animation id
        slideDown = AnimationUtils.loadAnimation(Login.this, R.anim.slide_down);

        //google signin button
        //signInButton = findViewById(R.id.sign_in_button);

        layoutAnimations();


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(Login.this)
                .enableAutoManage(Login.this/* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

         mAuth = FirebaseAuth.getInstance();

        //onclick listeners
        google_btn.setOnClickListener(this);
      //  signInButton.setOnClickListener(this);

    //onCreate End
    }


    // onclick
    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){
            // google login
            case R.id.login_googleButton:
                rotateLoading.start();
                signIn();
              /*  new CheckNetworkConnection(Login.this, new CheckNetworkConnection.OnConnectionCallback() {
                    @Override
                    public void onConnectionSuccess() {
                        signIn();
                    }

                    @Override
                    public void onConnectionFail(String msg) {
                        new SweetNoInternetConnection().noInternet(Login.this);
                        avi.hide();
                    }
                }).execute(); */
                break;


        }

    }


    //google Sign in
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        rotateLoading.stop();
    }


    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Authentication Denied")
                        .setContentText("It seems like you have cancelled Authentication!")
                        .setConfirmText("Yes")
                        .show();
                rotateLoading.stop();
               // updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]



    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
       // showProgressDialog();
        rotateLoading.start();
        gifImageView.setVisibility(View.VISIBLE);
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                          //  Log.d(TAG, "signInWithCredential:success");
                            // opening Dashboard Activity
                            startActivity(new Intent(Login.this, DashBoard.class));
                            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                            }
                            SharedPreferences sharedpreferences = getSharedPreferences("LogDetail", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("firstScreen", "DashBoard");
                            editor.apply();
                            Login.this.finish();


                          //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            try{
                                new SweetAlertDialog(Login.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Sign In Failed")
                                        .setContentText(task.getException().getLocalizedMessage())
                                        .show();
                            }
                            catch (NullPointerException e) { e.printStackTrace(); }

                          //  updateUI(null);
                        }

                        // [START_EXCLUDE]
                        rotateLoading.stop();

                       // hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]



    // Loading Animations

    public void layoutAnimations(){

        loginTop_iv.startAnimation(slideDown);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                relativeLayout2.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn)
                        .duration(1000)
                        .repeat(0)
                        .playOn(relativeLayout2);

            }
        },800);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                gifImageView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.Landing)
                        .duration(3000)
                        .repeat(0)
                        .playOn(gifImageView);

            }
        },1500);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        new SweetAlertDialog(Login.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Connection Failed")
                .setContentText(""+connectionResult)
                .show();
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.stopAutoManage(Login.this);
        mGoogleApiClient.disconnect();
    }

//    onStart
    public void onStart(){
        super.onStart();
        SharedPreferences sharedpreferences = getSharedPreferences("LogDetail", MODE_PRIVATE);
        String decider = sharedpreferences.getString("firstScreen", "");
        if (TextUtils.equals(decider, "DashBoard")){
            startActivity(new Intent(Login.this, DashBoard.class));
            Login.this.finish();
        }

    }

//end
}
