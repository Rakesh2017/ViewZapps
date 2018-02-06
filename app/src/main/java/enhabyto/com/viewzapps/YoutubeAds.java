package enhabyto.com.viewzapps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YoutubeAds extends AppCompatActivity {

    RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;
    List<RecyclerViewInfo> list = new ArrayList<>();

    private DatabaseReference databaseReferenceParent = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference databaseReference = databaseReferenceParent.child("ads").child("youtubeAds");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_ads);

        recyclerView = findViewById(R.id.rya_recyclerView);

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

                // Hiding the progress dialog.

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Hiding the progress dialog.


            }
        });


    }

    //end
}