package enhabyto.com.viewzapps;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import mehdi.sakout.fancybuttons.FancyButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPostAd extends Fragment implements View.OnClickListener{

    private View view;
    ImageButton youtube_btn;

    public AdminPostAd() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_post_ad, container, false);

        youtube_btn = view.findViewById(R.id.apa_youtubeButton);


        youtube_btn.setOnClickListener(this);
        return view;
    }

//    onClick
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.apa_youtubeButton){
            getFragmentManager().beginTransaction().add(R.id.fragment_container_dashboard, new AdminPostYoutubeAd()).addToBackStack("adminPostYoutubeAd").commit();
        }


    }
}
