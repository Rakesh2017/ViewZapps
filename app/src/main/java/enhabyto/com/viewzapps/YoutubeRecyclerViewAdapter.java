package enhabyto.com.viewzapps;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.lzyzsd.randomcolor.RandomColor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import util.android.textviews.FontTextView;

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_youtube_ad_items, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RecyclerViewInfo UploadInfo = MainImageUploadInfoList.get(position);


        /*generating random color
        RandomColor randomColor = new RandomColor();
        int color = randomColor.randomColor();
        int alpha = 200; //between 0-255
        @ColorInt
        int alphaColor = ColorUtils.setAlphaComponent(color, alpha);*/

        //holder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.transparentAppColor));
        holder.title_tx.setText(UploadInfo.getAdTitle());
        //holder.url_tx.setText(UploadInfo.getYoutubeUrl());

//        ad image
        Picasso.with(context)
                .load(UploadInfo.getYoutubeAdImageUrl())
                .placeholder(R.drawable.ic_youtube_placeholder)
                .error(R.drawable.ic_warning)
                .fit()
                .centerCrop()
                .noFade()
                .into(holder.adImage_iv);

        try {
            databaseReference.child("users").child(UploadInfo.getUserUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.uploaderName_tx.setText(dataSnapshot.child("profile_details")
                            .child("name").getValue(String.class));

                    String url = dataSnapshot.child("profile_image").child("profile_image_url").getValue(String.class);
                    Uri uri = Uri.parse(url);

//                    user image
                    Picasso.with(context)
                            .load(uri)
                            .placeholder(R.drawable.ic_profile_image_placeholder)
                            .error(R.drawable.ic_warning)
                            .noFade()
                            .into(holder.userImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

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

        FontTextView title_tx, url_tx, uploaderName_tx;
        ImageView adImage_iv;
        CircleImageView userImage;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            title_tx = itemView.findViewById(R.id.rya_titleTextView);
          //  url_tx = itemView.findViewById(R.id.rya_urlTextView);

            uploaderName_tx = itemView.findViewById(R.id.rya_uploaderNameTextView);
            adImage_iv = itemView.findViewById(R.id.rya_adImageView);
            userImage = itemView.findViewById(R.id.rya_profileImage);
            cardView = itemView.findViewById(R.id.rya_cardview);
        }


    }

//end
}