package enhabyto.com.viewzapps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdPostAdmin extends AppCompatActivity implements View.OnClickListener {

    ImageButton youtube_btn, back_btn;
    TextView youtubeAdminAds_tv, youtubeUserAds_tv, youtubeTotalAds_tv;
    int youtubeAdminAds_i, youtubeUserAds_i, youtubeTotalAds_i;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_post_admin);

//        ids
        youtube_btn = findViewById(R.id.aap_youtubeImageButton);
        back_btn = findViewById(R.id.aap_backButton);

        youtubeAdminAds_tv = findViewById(R.id.aap_youtubeAdsByAdmin);

//        on click
        youtube_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
    }

    //    onClick
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            //        youtube button
            case R.id.aap_youtubeImageButton:
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_ad_post_admin, new AdminPostYoutubeAd()).addToBackStack("adminPostYoutubeAd").commit();
                break;
//                back pressed
            case R.id.aap_backButton:
                super.onBackPressed();
        }//switch

    }//onclick

    public void onStart() {
        super.onStart();
        youtubeAdsCount();
    }

//    getting number of ads
    public void youtubeAdsCount(){

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               youtubeAdminAds_i =  (int)(dataSnapshot.child("admin_posted_ads").child("youtubeAds").getChildrenCount());

               youtubeAdminAds_tv.setText(youtubeAdminAds_tv.getText()+"\n"+youtubeAdminAds_i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
//    getting number of ads

    //end
}
