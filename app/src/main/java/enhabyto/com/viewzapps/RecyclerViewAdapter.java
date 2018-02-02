package enhabyto.com.viewzapps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import util.android.textviews.FontTextView;

/**
 * Created by this on 02-Feb-18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    Context context;
    private List<RecyclerViewInfo> MainImageUploadInfoList;

    public RecyclerViewAdapter(Context context, List<RecyclerViewInfo> TempList) {

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

        FontTextView title_tx, url_tx;


        ViewHolder(View itemView) {
            super(itemView);

            title_tx = itemView.findViewById(R.id.rya_titleTexxtView);
            url_tx = itemView.findViewById(R.id.rya_urlTextView);
        }


    }


//end
}