package enhabyto.com.viewzapps;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifTextView;

public class YoutubeAds extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;
    List<RecyclerViewInfo> list = new ArrayList<>();

    private DatabaseReference databaseReferenceParent = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference databaseReference = databaseReferenceParent.child("ads").child("youtubeAds");
    RelativeLayout gifTextView;
    ImageButton backButton_ib;

    RotateLoading loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_ads);

        loading = findViewById(R.id.ya_rotateLoading);
        gifTextView = findViewById(R.id.ya_emptyListGif);

        recyclerView = findViewById(R.id.rya_recyclerView);

        backButton_ib = findViewById(R.id.rya_backButton);

        recyclerView.setCameraDistance(2);

        recyclerView.setHasFixedSize(true);
        recyclerView.isDuplicateParentStateEnabled();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(YoutubeAds.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(mLayoutManager);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                loading.start();
                if(list!=null) {
                    list.clear();  // v v v v important (eliminate duplication of data)
                }

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecyclerViewInfo youtubeAdsInfo = postSnapshot.getValue(RecyclerViewInfo.class);
                    list.add(youtubeAdsInfo);
                }

                adapter = new YoutubeRecyclerViewAdapter(YoutubeAds.this, list);
                //   Collections.reverse(list);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                loading.stop();
                if (adapter.getItemCount() == 0){
                    gifTextView.setVisibility(View.VISIBLE);
                }
                else gifTextView.setVisibility(View.GONE);

                // Hiding the progress dialog.

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Hiding the progress dialog.
                loading.stop();


            }
        });

        //onclick
        backButton_ib.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

//        back button
        if (id == R.id.rya_backButton){
            super.onBackPressed();
        }
    }

    //end
}
