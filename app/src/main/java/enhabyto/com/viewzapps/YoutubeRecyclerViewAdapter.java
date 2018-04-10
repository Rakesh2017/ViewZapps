package enhabyto.com.viewzapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/*
  Created by this on 02-Feb-18.
 */

public class YoutubeRecyclerViewAdapter extends RecyclerView.Adapter<YoutubeRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewInfo> MainImageUploadInfoList;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static final String YOUTUBE_AD_ITEM_PREF = "youtube_ad_item_pref";
    private static final String AD_KEY = "ad_key";
    private static final String AD_TITLE = "ad_title";
    private static final String AD_WATCH_ID = "watch_id";

    YoutubeRecyclerViewAdapter(Context context, List<RecyclerViewInfo> TempList) {

        this.MainImageUploadInfoList = TempList;

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_youtube_ad_items, parent, false);

        return new ViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RecyclerViewInfo UploadInfo = MainImageUploadInfoList.get(position);


        holder.title_tv.setText(UploadInfo.getAdTitle());

        holder.likes_tv.setText(UploadInfo.getAdLikesLeft()+"\nLikes left");
        holder.views_tv.setText(UploadInfo.getAdViewsLeft()+"\nviews left");
        holder.subscribers_tv.setText(UploadInfo.getAdSubscribersLeft()+"\nSubs left");

        final String uid = UploadInfo.getUserUid();
        databaseReference.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("profile_details").child("name").getValue(String.class);
                        holder.uploaderName_tv.setText(name);

//                    user image
                        String profileImageUrl = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                        try {
//            if else
                            Log.w("raky", "imageUrl: "+profileImageUrl);
                            if (profileImageUrl.isEmpty()){
                                Picasso.with(context)
                                        .load(R.drawable.ic_profile_image_placeholder)
                                        .into(holder.userImage);
                            }
                            else {
                                Picasso.with(context)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_profile_image_placeholder)
                                        .error(R.drawable.ic_warning)
                                        .into(holder.userImage);
                            }
//       if else ends
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        } //user image
                    }//onDataChange

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//        if any of it is empty
        if (UploadInfo.getAdViewsLeft().isEmpty()) holder.views_tv.setText("0\n Views Left");
        if (UploadInfo.getAdLikesLeft().isEmpty()) holder.likes_tv.setText("0\n Likes Left");
        if (UploadInfo.getAdSubscribersLeft().isEmpty()) holder.subscribers_tv.setText("0\n Subs Left");
//        if ends


        //        ad image
        Picasso.with(context)
                .load(UploadInfo.getYoutubeAdImageUrl())
                .placeholder(R.drawable.youtube_placeholder)
                .error(R.drawable.ic_warning)
                .fit()
                .centerCrop()
                .into(holder.adImage_iv);
//        ad image

        //                    user image
        String profileImageUrl = UploadInfo.getProfileImageUrl();
        try {
//            if else
            if (profileImageUrl.isEmpty()){
                Picasso.with(context)
                        .load(R.drawable.ic_profile_image_placeholder)
                        .into(holder.userImage);
            }
            else {
                Picasso.with(context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_profile_image_placeholder)
                        .error(R.drawable.ic_warning)
                        .into(holder.userImage);
            }
//       if else ends
        }
        catch (NullPointerException e){
            e.printStackTrace();
        } //user image

        //first position
        if( position == 0 ){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,(int) context.getResources().getDimension(R.dimen.cardViewPadding12),0,(int) context.getResources().getDimension(R.dimen.cardViewPadding12));
            holder.cardView.setLayoutParams(params);
        }

        holder.adImage_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ad_key = UploadInfo.getYoutubeAdKey();
                String ad_tile = UploadInfo.getAdTitle();
                String watch_id = UploadInfo.getWatchId();
                final SharedPreferences sharedpreferences = context.getSharedPreferences(YOUTUBE_AD_ITEM_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(AD_KEY, ad_key);
                editor.putString(AD_TITLE, ad_tile);
                editor.putString(AD_WATCH_ID, watch_id);
                editor.apply();

                context.startActivity(new Intent(context, youtubeVideoAd.class));
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

        TextView title_tv, uploaderName_tv, likes_tv, views_tv, subscribers_tv;
        ImageView adImage_iv;
        CircleImageView userImage;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            title_tv = itemView.findViewById(R.id.rya_titleTextView);

            uploaderName_tv = itemView.findViewById(R.id.rya_uploaderNameTextView);
            adImage_iv = itemView.findViewById(R.id.rya_adImageView);
            userImage = itemView.findViewById(R.id.rya_profileImage);
            cardView = itemView.findViewById(R.id.rya_cardview);

            likes_tv = itemView.findViewById(R.id.rya_likes);
            views_tv = itemView.findViewById(R.id.rya_views);
            subscribers_tv = itemView.findViewById(R.id.rya_subscribers);
        }
    }
//end
}