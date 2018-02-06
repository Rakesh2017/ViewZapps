package enhabyto.com.viewzapps;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import util.android.textviews.FontTextView;

/**
 * Created by this on 02-Feb-18.
 */

public class YoutubeRecyclerViewAdapter extends RecyclerView.Adapter<YoutubeRecyclerViewAdapter.ViewHolder> {

    Context context;
    private List<RecyclerViewInfo> MainImageUploadInfoList;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public YoutubeRecyclerViewAdapter(Context context, List<RecyclerViewInfo> TempList) {

        this.MainImageUploadInfoList = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_youtube_ad_items, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RecyclerViewInfo UploadInfo = MainImageUploadInfoList.get(position);

        holder.title_tx.setText(UploadInfo.getYoutubeTitle());
        holder.url_tx.setText(UploadInfo.getYoutubeUrl());

        Picasso.with(context)
                .load(UploadInfo.getYoutubeAdImageUrl())
                .fit()
                .centerCrop()
                .into(holder.adImage_iv);

        databaseReference.child("users").child(UploadInfo.getUserUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.uploaderName_tx.setText(dataSnapshot.child("profile_details")
                        .child("name").getValue(String.class));

                String url = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                Uri uri = Uri.parse(url);
                Glide.with(context)
                        .load(uri)
                        .into(holder.userImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return MainImageUploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        FontTextView title_tx, url_tx, uploaderName_tx;
        ImageView adImage_iv;
        CircularImageView userImage;


        ViewHolder(View itemView) {
            super(itemView);

            title_tx = itemView.findViewById(R.id.rya_titleTextView);
            url_tx = itemView.findViewById(R.id.rya_urlTextView);

            uploaderName_tx = itemView.findViewById(R.id.rya_uploaderNameTextView);
            adImage_iv = itemView.findViewById(R.id.rya_adImageView);
            userImage = itemView.findViewById(R.id.rya_profileImage);
        }


    }


//end
}