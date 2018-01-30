package enhabyto.com.viewzapps;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPostYoutubeAd extends Fragment implements View.OnClickListener{

    private View view;


    public AdminPostYoutubeAd() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_post_youtube_ad, container, false);


        return view;
    }

//    onClick
    @Override
    public void onClick(View v) {

        int id = v.getId();


    }
}
