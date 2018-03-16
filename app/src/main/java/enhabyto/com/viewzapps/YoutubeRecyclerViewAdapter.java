package enhabyto.com.viewzapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import org.w3c.dom.Text;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/*
  Created by this on 02-Feb-18.
 */

public class YoutubeRecyclerViewAdapter extends RecyclerView.Adapter<YoutubeRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RecyclerViewInfo> MainImageUploadInfoList;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

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


        /*generating random color
        RandomColor randomColor = new RandomColor();
        int color = randomColor.randomColor();
        int alpha = 200; //between 0-255
        @ColorInt
        int alphaColor = ColorUtils.setAlphaComponent(color, alpha);*/

        //holder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.transparentAppColor));
        holder.title_tv.setText(UploadInfo.getAdTitle());

        holder.likes_tv.setText(UploadInfo.getAdLikesLeft()+"\nLikes left");
        holder.views_tv.setText(UploadInfo.getAdViewsLeft()+"\nviews left");
        holder.subscribers_tv.setText(UploadInfo.getAdSubscribersLeft()+"\nSubs left");
        holder.uploaderName_tv.setText(UploadInfo.getUserName());

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
                .noFade()
                .into(holder.adImage_iv);
//        ad image

        //                    user image
        String profileImageUrl = UploadInfo.getProfileImageUrl();
        try {
//            if else
            if (profileImageUrl.isEmpty()){
                Picasso.with(context)
                        .load(R.drawable.ic_profile_image_placeholder)
                        .noFade()
                        .into(holder.userImage);
            }
            else {
                Picasso.with(context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_profile_image_placeholder)
                        .error(R.drawable.ic_warning)
                        .noFade()
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