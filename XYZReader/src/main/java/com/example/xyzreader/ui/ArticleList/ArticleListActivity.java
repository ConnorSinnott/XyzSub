package com.example.xyzreader.ui.ArticleList;

import android.app.SharedElementCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.data.Utilities;
import com.example.xyzreader.remote.UpdateResultReceiver;
import com.example.xyzreader.ui.ArticleDetail.ArticleDetailActivity;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 * <p/>
 * To setup correct return transitions, for when the user has changed pages before returning,
 * I followed the example of alexjlockwood at https://github.com/alexjlockwood/activity-transitions
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private boolean mIsRefreshing = false;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                // Update Refreshing UI
                mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
            }
        }
    };

    private Bundle reEnterBundle;
    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (reEnterBundle != null) {
                boolean isPhotoVisible = reEnterBundle.getBoolean(ArticleDetailActivity.TOOLBAR_PHOTO_VISIBLE);
                if (isPhotoVisible) {
                    int startingPosition = reEnterBundle.getInt(ArticleDetailActivity.EXTRA_STARTING_INDEX);
                    int endingPosition = reEnterBundle.getInt(ArticleDetailActivity.EXTRA_ENDING_INDEX);
                    if (startingPosition != endingPosition) {
                        String transitionName = Utilities.generateTransitionName(endingPosition);
                        View sharedElement = mRecyclerView.findViewWithTag(transitionName);
                        if (sharedElement != null) {
                            names.clear();
                            names.add(transitionName);
                            sharedElements.clear();
                            sharedElements.put(transitionName, sharedElement);
                        }
                    }
                } else {
                    names.clear();
                    sharedElements.clear();
                }
                reEnterBundle = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setExitSharedElementCallback(mCallback);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startService(new Intent(ArticleListActivity.this, UpdaterService.class));
                UpdateResultReceiver.setOnUpdateSucceeded(new UpdateResultReceiver.OnUpdateSucceeded() {
                    @Override
                    public void onUpdateSucceeded() {
                        UpdateResultReceiver.setOnUpdateSucceeded(null);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        UpdateResultReceiver.setOnUpdateFailed(new UpdateResultReceiver.OnUpdateFailed() {
            @Override
            public void onUpdateFailed() {
                Snackbar snackbar = Snackbar.make(mRecyclerView, getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(ArticleListActivity.this, UpdaterService.class));
                    }
                });
                snackbar.show();
            }
        });

        if (savedInstanceState == null) {
            startService(new Intent(this, UpdaterService.class));
        }

        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        reEnterBundle = new Bundle(data.getExtras());

        int startingPosition = reEnterBundle.getInt(ArticleDetailActivity.EXTRA_STARTING_INDEX);
        int endingPosition = reEnterBundle.getInt(ArticleDetailActivity.EXTRA_ENDING_INDEX);

        if (startingPosition != endingPosition) {
            mRecyclerView.scrollToPosition(endingPosition);
        }
        supportPostponeEnterTransition();
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                supportStartPostponedEnterTransition();
                return true;
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        // Set Recycler View Adapter
        ArticleListAdapter adapter = new ArticleListAdapter(ArticleListActivity.this, cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);

        // Set Staggered Grid Layout Manager
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }


}
