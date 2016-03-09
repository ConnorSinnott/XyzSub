package com.example.xyzreader.ui.ArticleList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.Utilities;
import com.example.xyzreader.ui.ArticleDetail.ArticleDetailFragment;
import com.example.xyzreader.ui.Other.ImageLoaderHelper;

/**
 * Created by Spectre on 3/6/2016.
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private Activity mContext;
    private Cursor mCursor;

    public ArticleListAdapter(Activity parentActivity, Cursor cursor) {
        mCursor = cursor;
        mContext = parentActivity;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_article, parent, false);

        ArticleViewHolder vh = new ArticleViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {

            private ArticleViewHolder mViewHolder;

            public View.OnClickListener setViewHolder(ArticleViewHolder viewHolder) {
                this.mViewHolder = viewHolder;
                return this;
            }

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(getItemId(mViewHolder.getAdapterPosition())));
                intent.putExtra(ArticleDetailFragment.EXTRA_TRANSITION, mViewHolder.thumbnailView.getTransitionName());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mContext, mViewHolder.thumbnailView, mViewHolder.thumbnailView.getTransitionName());

                mContext.startActivity(intent, options.toBundle());

            }

        }.setViewHolder(vh));

        return vh;
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR));
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        holder.thumbnailView.setTransitionName(Utilities.generateTransitionName(position));
        holder.thumbnailView.setTag(Utilities.generateTransitionName(position));
        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(mContext).getImageLoader());
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

}


