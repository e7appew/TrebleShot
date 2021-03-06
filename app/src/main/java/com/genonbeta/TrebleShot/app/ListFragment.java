package com.genonbeta.TrebleShot.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.widget.ListAdapter;

import java.util.ArrayList;

/**
 * Created by: veli
 * Date: 12/3/16 9:57 AM
 */

public abstract class ListFragment<T, E extends ListAdapter<T>> extends Fragment
{
	public static final String TAG = "ListFragment";

	public static final int TASK_ID_REFRESH = 0;

	private E mAdapter;
	private ListView mListView;
	private FrameLayout mListViewContainer;
	private FrameLayout mCustomViewContainer;
	private FrameLayout mDefaultViewContainer;
	private FrameLayout mContainer;
	private RelativeLayout mEmptyView;
	private TextView mEmptyText;
	private ImageView mEmptyImage;
	private ProgressBar mProgressView;
	private Button mEmptyActionButton;
	private LoaderCallbackRefresh mLoaderCallbackRefresh = new LoaderCallbackRefresh();

	final private Handler mHandler = new Handler();

	final private Runnable mRequestFocus = new Runnable()
	{
		@Override
		public void run()
		{
			mListView.focusableViewAvailable(mListView);
		}
	};

	final private AdapterView.OnItemClickListener mOnClickListener
			= new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id)
		{
			onListItemClick((ListView) parent, v, position, id);
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mAdapter = onAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		View view = getLayoutInflater().inflate(R.layout.abstract_list_fragment, container, false);

		mCustomViewContainer = view.findViewById(R.id.customListFragment_customViewContainer);
		mDefaultViewContainer = view.findViewById(R.id.customListFragment_defaultViewContainer);
		mListViewContainer = view.findViewById(R.id.customListFragment_listViewContainer);
		mContainer = view.findViewById(R.id.customListFragment_container);
		mEmptyView = view.findViewById(R.id.customListFragment_emptyView);
		mEmptyText = view.findViewById(R.id.customListFragment_emptyTextView);
		mEmptyImage = view.findViewById(R.id.customListFragment_emptyImageView);
		mProgressView = view.findViewById(R.id.customListFragment_progressView);
		mEmptyActionButton = view.findViewById(R.id.customListFragment_emptyActionButton);

		mListView = view.findViewById(R.id.customListFragment_listView);

		if (mListView == null)
			mListView = onListView(mContainer, mListViewContainer);

		mListView.setOnItemClickListener(mOnClickListener);
		mListView.setEmptyView(mEmptyView);

		return view;
	}

	protected ListView onListView(View mainContainer, ViewGroup listViewContainer)
	{
		ListView listView = new ListView(getContext());

		listView.setId(R.id.customListFragment_listView);

		listView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		listViewContainer.addView(listView);

		return listView;
	}

	public void onListItemClick(ListView l, View v, int position, long id)
	{
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setListAdapter(mAdapter);
		getLoaderManager().initLoader(TASK_ID_REFRESH, null, mLoaderCallbackRefresh);
	}

	public abstract E onAdapter();

	protected void onListRefreshed()
	{
	}

	protected Snackbar createSnackbar(int resId, Object... objects)
	{
		return Snackbar.make(getListView(), getString(resId, objects), Snackbar.LENGTH_LONG);
	}

	private void ensureList()
	{
		mListView.setEmptyView(mEmptyView);
		mHandler.post(mRequestFocus);
	}

	public E getAdapter()
	{
		return mAdapter;
	}

	public FrameLayout getCustomViewContainer()
	{
		return mCustomViewContainer;
	}

	public FrameLayout getDefaultViewContainer()
	{
		return mDefaultViewContainer;
	}

	public ImageView getEmptyImage()
	{
		ensureList();
		return mEmptyImage;
	}

	public TextView getEmptyText()
	{
		ensureList();
		return mEmptyText;
	}

	public LoaderCallbackRefresh getLoaderCallbackRefresh()
	{
		return mLoaderCallbackRefresh;
	}

	public E getListAdapter()
	{
		return mAdapter;
	}

	public ListView getListView()
	{
		ensureList();
		return mListView;
	}

	public ProgressBar getProgressView()
	{
		ensureList();
		return mProgressView;
	}

	public Button getEmptyActionButton()
	{
		return mEmptyActionButton;
	}

	public void refreshList()
	{
		getLoaderCallbackRefresh().requestRefresh();
	}

	public void setEmptyImage(int resId)
	{
		ensureList();
		mEmptyImage.setImageResource(resId);
	}

	public void setEmptyText(CharSequence text)
	{
		ensureList();
		mEmptyText.setText(text);
	}

	public void setListAdapter(E adapter)
	{
		boolean hadAdapter = mAdapter != null;
		mAdapter = adapter;

		if (mListView != null) {
			mListView.setAdapter(adapter);

			if (mContainer.getVisibility() != View.VISIBLE && !hadAdapter)
				setListShown(true, getView().getWindowToken() != null);
		}
	}

	public void setListShown(boolean shown)
	{
		setListShown(shown, true);
	}

	public void setListShown(boolean shown, boolean animate)
	{
		ensureList();

		if ((mContainer.getVisibility() == View.VISIBLE) == shown)
			return;

		if (animate) {
			Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
			Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);

			mProgressView.startAnimation(shown ? fadeOut : fadeIn);
			mContainer.startAnimation(shown ? fadeIn : fadeOut);
		} else {
			mProgressView.clearAnimation();
			mContainer.clearAnimation();
		}

		mContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
		mProgressView.setVisibility(shown ? View.GONE : View.VISIBLE);
	}

	public void showCustomView(boolean shown)
	{
		mCustomViewContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
		mDefaultViewContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
	}

	public boolean toggleCustomView()
	{
		boolean isVisible = getCustomViewContainer().getVisibility() == View.VISIBLE;

		showCustomView(!isVisible);

		return !isVisible;
	}

	public void useEmptyActionButton(String buttonText, View.OnClickListener clickListener)
	{
		mEmptyActionButton.setText(buttonText);
		mEmptyActionButton.setOnClickListener(clickListener);

		useEmptyActionButton(true);
	}

	public void useEmptyActionButton(boolean use)
	{
		mEmptyActionButton.setVisibility(use ? View.VISIBLE : View.GONE);
	}

	private class LoaderCallbackRefresh implements LoaderManager.LoaderCallbacks<ArrayList<T>>
	{
		private boolean mRunning = false;
		private boolean mReloadRequested = false;

		@Override
		public Loader<ArrayList<T>> onCreateLoader(int id, Bundle args)
		{
			mReloadRequested = false;
			mRunning = true;

			if (mAdapter.getCount() == 0)
				setListShown(false);

			return new ListAdapter.Loader<>(mAdapter);
		}

		@Override
		public void onLoadFinished(Loader<ArrayList<T>> loader, ArrayList<T> data)
		{
			if (isResumed()) {
				mAdapter.onUpdate(data);
				mAdapter.notifyDataSetChanged();

				setListShown(true);
				onListRefreshed();
			}

			if (isReloadRequested())
				refresh();

			mRunning = false;
		}

		@Override
		public void onLoaderReset(Loader<ArrayList<T>> loader)
		{

		}

		public boolean isRunning()
		{
			return mRunning;
		}

		public boolean isReloadRequested()
		{
			return mReloadRequested;
		}

		public void refresh()
		{
			getLoaderManager().restartLoader(TASK_ID_REFRESH, null, mLoaderCallbackRefresh);
		}

		public boolean requestRefresh()
		{
			if (isRunning() && isReloadRequested())
				return false;

			if (!isRunning())
				refresh();
			else
				mReloadRequested = true;

			return true;
		}
	}
}
