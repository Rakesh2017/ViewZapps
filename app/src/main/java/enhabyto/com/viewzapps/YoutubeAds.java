package enhabyto.com.viewzapps;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
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
    SwipeRefreshLayout swipeRefreshLayout;
    RotateLoading loading;

    Boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_ads);

        loading = findViewById(R.id.ya_rotateLoading);
        gifTextView = findViewById(R.id.ya_emptyListGif);
        swipeRefreshLayout = findViewById(R.id.rya_swipeRefresh);

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

        //calling functions
        Refresh();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                check = false;
                Refresh();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
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


    public void Refresh(){
        if (check) loading.start();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

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

                if (adapter.getItemCount() == 0){
                    gifTextView.setVisibility(View.VISIBLE);
                }
                else gifTextView.setVisibility(View.GONE);

                // Hiding the progress dialog.
                if (check) loading.stop();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Hiding the progress dialog.
                if (check) loading.stop();


            }
        });
    }

    //end
}
