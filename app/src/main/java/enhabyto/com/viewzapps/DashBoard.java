package enhabyto.com.viewzapps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class DashBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    CircularImageView profileImageMainPage_iv;
    TextView name_tv, email_tv, phone_tv, zapNumber_tv;
    String name_tx, email_tx, phone_tx;

    CircularImageView youtube_btn, facebook_btn, insta_btn, android_btn, apple_btn, movie_btn;

//    database reference
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Log.w("qwerty", FirebaseAuth.getInstance().getCurrentUser().getProviderId());

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        image view
        profileImageMainPage_iv = findViewById(R.id.bar_profileImage);
        Glide.with(DashBoard.this)
                .load(R.drawable.ic_profile_image_placeholder)
                .into(profileImageMainPage_iv);

//        Image button iss
        youtube_btn = findViewById(R.id.dash_youtubeImageButton);
        facebook_btn = findViewById(R.id.dash_facebookImageButton);
        insta_btn = findViewById(R.id.dash_instagramImageButton);
        android_btn = findViewById(R.id.dash_androidImageButton);
        //apple_btn = findViewById(R.id.dash_appleButton);
        movie_btn = findViewById(R.id.dash_moviesImageButton);

        //  FontTextView ids
        name_tv = navigationView.getHeaderView(0).findViewById(R.id.header_nameTextView);
        email_tv = navigationView.getHeaderView(0).findViewById(R.id.header_emailTextView);
        phone_tv = navigationView.getHeaderView(0).findViewById(R.id.header_phoneTextView);
        zapNumber_tv = navigationView.getHeaderView(0).findViewById(R.id.header_zapTextView);

        //on click
        youtube_btn.setOnClickListener(this);

//        load animations
        LoadAnimations();
    }

//    load animations
    private void LoadAnimations() {
        YoYo.with(Techniques.BounceIn)
                .duration(1200)
                .playOn(youtube_btn);
        YoYo.with(Techniques.BounceIn)
                .duration(1400)
                .playOn(facebook_btn);
        YoYo.with(Techniques.BounceIn)
                .duration(1600)
                .playOn(insta_btn);
        YoYo.with(Techniques.BounceIn)
                .duration(1800)
                .playOn(android_btn);
      //  YoYo.with(Techniques.Bounce)
              //  .duration(2000)
              //  .playOn(apple_btn);
        YoYo.with(Techniques.BounceIn)
                .duration(2000)
                .playOn(movie_btn);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    onStart
    public void onStart(){
        super.onStart();
        //function calls
        setImage();
        setProfileData();
    }

//    setting profile data
    private void setProfileData() {
        SharedPreferences sharedpreferences = getSharedPreferences("profileDetails", MODE_PRIVATE);
        name_tx = sharedpreferences.getString("name","");
        email_tx = sharedpreferences.getString("email","");
        phone_tx = sharedpreferences.getString("phone","");
//        setting values
        name_tv.setText(name_tx);
        email_tv.setText(email_tx);
        phone_tv.setText(phone_tx);
       // zapNumber_tv.setText(phone_tx);

        databaseReference.child("users").child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        getting values
                        name_tx = dataSnapshot.child("profile_details").child("name").getValue(String.class);
                        email_tx = dataSnapshot.child("profile_details").child("email").getValue(String.class);
                        phone_tx = dataSnapshot.child("profile_details").child("phone_number").getValue(String.class);
//                        setting values
                        name_tv.setText(name_tx);
                        email_tv.setText(email_tx);
                        phone_tv.setText(phone_tx);
//                        storing values in shared preferences
                        try {
                            SharedPreferences sharedpreferences = getSharedPreferences("profileDetails", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("name", name_tx);
                            editor.putString("email", email_tx);
                            editor.putString("phone", phone_tx);
                            editor.apply();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });//setting profile data

//        setting profile pic

        //setting profile pic

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
//            profile
            case R.id.nav_profile:
                if (checkStoragePermission())
                startActivity(new Intent(DashBoard.this, EditProfile.class));
                break;

//                admin post ad
            case R.id.nav_adminPostAd:
                      startActivity(new Intent(DashBoard.this, AdPostAdmin.class));
                break;

//                log out
            case R.id.nav_logOut:
                Logout();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // on click
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
//                youtube
            case R.id.dash_youtubeImageButton:
                Intent intent = new Intent(DashBoard.this, YoutubeAds.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
               // startActivity(new Intent(DashBoard.this, YoutubeAds.class));

        }
    }//onclick

    //storage permission
    public boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(DashBoard.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(DashBoard.this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashBoard.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else return true;
    }

    //handling profile image
    //    setting image
    public void setImage(){
        databaseReference.child("users").child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imagePath = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);

                        Picasso.with(DashBoard.this)
                                .load(imagePath)
                                .placeholder(R.drawable.ic_profile_image_placeholder)
                                .error(R.drawable.ic_warning)
                                .into(profileImageMainPage_iv);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    } // set image ends

//    log out
    public void Logout(){
        new SweetAlertDialog(DashBoard.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure to Logout?")
                .setContentText("You can use left side icon to switch between accounts!")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        FirebaseAuth.getInstance().signOut();
                        DashBoard.this.finish();

                        SharedPreferences sharedpreferences = getSharedPreferences("LogDetail", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("firstScreen", "Login");
                        editor.apply();
                                      new CheckNetworkConnection(DashBoard.this, new CheckNetworkConnection.OnConnectionCallback() {
                                            @Override
                                            public void onConnectionSuccess() {

                                            }

                                            @Override
                                            public void onConnectionFail(String msg) {
                                               new SweetNoInternetConnection().noInternet(DashBoard.this);

                                            }
                                        }).execute();

                                    }
                                })
                                .setCancelText("No")
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
    }
//    logout


// end
}

