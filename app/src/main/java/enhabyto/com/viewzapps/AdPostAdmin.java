package enhabyto.com.viewzapps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class AdPostAdmin extends AppCompatActivity implements View.OnClickListener {

    ImageButton youtube_btn, back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_post_admin);

//        ids
        youtube_btn = findViewById(R.id.aap_youtubeImageButton);
        back_btn = findViewById(R.id.aap_backButton);

//        on click
        youtube_btn.setOnClickListener(this);
    }

    //    onClick
    @Override
    public void onClick(View v) {
        int id = v.getId();

//        youtube
        if (id == R.id.aap_youtubeImageButton){
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_ad_post_admin, new AdminPostYoutubeAd()).addToBackStack("adminPostYoutubeAd").commit();
        }
//        back pressed
        else if (id == R.id.aap_backButton){
            super.onBackPressed();
        }
    }//onclick

    //end
}
