package com.example.xyzreader.ui.ArticleDetail;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.Utilities;
import com.example.xyzreader.ui.ArticleList.ArticleListActivity;
import com.example.xyzreader.ui.Other.ImageLoaderHelper;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    public static String EXTRA_TRANSITION = "Transition";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private ImageView mPhotoView;
    private TextView mSubtitle;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    private String mTransitionName = "";
    private Utilities.OnTransitionReadyListener mListener;
    private boolean mUIReady = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, String transitionName) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putString(EXTRA_TRANSITION, transitionName);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public void setOnTransitionReadyListener(Utilities.OnTransitionReadyListener listener) {
        mListener = listener;
        if (mUIReady) {
            listener.onTransitionReady();
        }
    }

    public void setOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
        mOnOffsetChangedListener = onOffsetChangedListener;
        if (mUIReady) {
            mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);
        }
    }

    public View getTransitionView() {
        return mPhotoView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        if (getArguments().containsKey(EXTRA_TRANSITION)) {
            mTransitionName = getArguments().getString(EXTRA_TRANSITION);
        }

        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, ArticleDetailFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsingToolbarLayout);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.AppBarBase);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.AppBarBase);

        mAppBarLayout = (AppBarLayout) mRootView.findViewById(R.id.fragment_article_detail2_appbar);
        if (mOnOffsetChangedListener != null) {
            mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        mToolbar = (Toolbar) mRootView.findViewById(R.id.fragment_article_detail2_toolbar);
        getActivityCast().setSupportActionBar(mToolbar);
        if (getActivityCast().getSupportActionBar() != null) {
            getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSubtitle = (TextView) mRootView.findViewById(R.id.fragment_article_detail2_subtitle);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.fragment_article_detail2_image);
        mPhotoView.setTransitionName(mTransitionName);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();

        mUIReady = true;

        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null) {
            color = Color.argb(255,
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }

        mCollapsingToolbarLayout.setContentScrimColor(color);
        mSubtitle.setBackgroundColor(color);

    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView bodyView = (TextView) mRootView.findViewById(R.id.fragment_article_detail2_text);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            mToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            mSubtitle.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mPhotoView.setBackgroundColor(mMutedColor);
                                if (mListener != null) {
                                    mListener.onTransitionReady();
                                }
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            mToolbar.setTitle("N/A");
            mToolbar.setSubtitle("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

}
