package enhabyto.com.viewzapps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ImageButton logout_ib;

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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //image button
        logout_ib = findViewById(R.id.dash_logOut);




        // set on click
        logout_ib.setOnClickListener(this);
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
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_dashboard, new Profile()).addToBackStack("profileFragment").commit();
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
            // logout button
            case R.id.dash_logOut:

                        new SweetAlertDialog(DashBoard.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure to Logout?")
                                .setContentText("You can use left side icon to switch betwwn accounts!")
                                .setConfirmText("Yes")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(final SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        mAuth.signOut();
                                        DashBoard.this.finish();
                                        String myFile = "/viewZapp/image.jpg";
                                        String myPath = Environment.getExternalStorageDirectory() + myFile;
                                        File f = new File(myPath);
                                        if (f.exists()) {
                                            f.delete();
                                        }
                                        SharedPreferences sharedpreferences = getSharedPreferences("LogDetail", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("firstScreen", "Login");
                                        editor.apply();
                                       /* new CheckNetworkConnection(DashBoard.this, new CheckNetworkConnection.OnConnectionCallback() {
                                            @Override
                                            public void onConnectionSuccess() {

                                            }

                                            @Override
                                            public void onConnectionFail(String msg) {
                                               new SweetNoInternetConnection().noInternet(DashBoard.this);

                                            }
                                        }).execute();*/

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
                break;
        }
    }


// end
}

