package com.example.xyzreader.ui.ArticleDetail;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.Utilities;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String EXTRA_STARTING_INDEX = "StartingIndex";
    public static final String EXTRA_ENDING_INDEX = "EndingIndex";
    public static final String TOOLBAR_PHOTO_VISIBLE = "PhotoVisible";
    private int mStartIndex;
    private int mCurrentIndex;

    private Cursor mCursor;
    private long mStartId;

    private MyPagerAdapter mPagerAdapter;
    private ArticleDetailFragment mCurrentFragment = null;
    private boolean mIsToolbarExpanded = false;
    private boolean isLeaving = false;

    private final AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            mIsToolbarExpanded = verticalOffset > -210;
        }
    };

    private final SharedElementCallback mSharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (isLeaving) {
                if (!mIsToolbarExpanded) {
                    names.clear();
                    sharedElements.clear();
                } else {
                    if (mCurrentIndex != mStartIndex) {
                        names.clear();
                        sharedElements.clear();
                        names.add(mCurrentFragment.getTransitionView().getTransitionName());
                        sharedElements.put(mCurrentFragment.getTransitionView().getTransitionName(), mCurrentFragment.getTransitionView());
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        postponeEnterTransition();
        setEnterSharedElementCallback(mSharedElementCallback);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null && getIntent() != null && getIntent().getData() != null) {
            mStartId = ItemsContract.Items.getItemId(getIntent().getData());
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mCursor = cursor;

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        pager.setAdapter(mPagerAdapter);

        pager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {

                    mCurrentIndex = position;

                    if (mCurrentFragment != null) {
                        mCurrentFragment.setOnOffsetChangedListener(null);
                    }
                    mCurrentFragment = (ArticleDetailFragment) mPagerAdapter.instantiateItem(pager, mCurrentIndex);
                    mCurrentFragment.setOnOffsetChangedListener(mOnOffsetChangedListener);
                }
            }
        });

        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {

                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    mStartIndex = mCursor.getPosition();
                    mCurrentIndex = mStartIndex;
                    pager.setCurrentItem(mCurrentIndex, false);
                    mStartId = 0;
                }
                mCursor.moveToNext();
            }
        }

        mCurrentFragment = (ArticleDetailFragment) mPagerAdapter.instantiateItem(pager, mCurrentIndex);
        mCurrentFragment.setOnOffsetChangedListener(mOnOffsetChangedListener);
        mCurrentFragment.setOnTransitionReadyListener(new Utilities.OnTransitionReadyListener() {
            @Override
            public void onTransitionReady() {
                startPostponedEnterTransition();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends CustomPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            long queryID = mCursor.getLong(ArticleLoader.Query._ID);
            return ArticleDetailFragment.newInstance(queryID, Utilities.generateTransitionName(position));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        isLeaving = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_INDEX, mStartIndex);
        data.putExtra(EXTRA_ENDING_INDEX, mCurrentIndex);
        data.putExtra(TOOLBAR_PHOTO_VISIBLE, mIsToolbarExpanded);
        setResult(RESULT_OK, data);
        if (!mIsToolbarExpanded) {
            finish();
        }
        super.onBackPressed();
    }

}
