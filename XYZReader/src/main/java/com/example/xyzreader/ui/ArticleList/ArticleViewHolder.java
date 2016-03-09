package com.example.xyzreader.ui.ArticleList;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.Other.DynamicHeightNetworkImageView;

/**
 * Created by Spectre on 3/6/2016.
 */
class ArticleViewHolder extends RecyclerView.ViewHolder {
    public DynamicHeightNetworkImageView thumbnailView;
    public TextView titleView;
    public TextView subtitleView;

    public ArticleViewHolder(View view) {
        super(view);
        thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
        titleView = (TextView) view.findViewById(R.id.article_title);
        subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
    }
}
