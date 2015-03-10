package com.ustc.dystu.dandelion.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.FriendsInfoActivity;
import com.ustc.dystu.dandelion.R;
import com.ustc.dystu.dandelion.bean.FansInfo;
import com.ustc.dystu.dandelion.bean.UserInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.CacheUtils;
import com.ustc.dystu.dandelion.utils.DandAsyncTask;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;

public class FriendsFragment extends BaseFragment implements OnScrollListener {

	private static final String TAG = FriendsFragment.class.getSimpleName();

	private static final int REQUEST_GET_FANS_LIST = 0x1;

	private ArrayList<FansInfo> mListData = new ArrayList<FansInfo>();
	private ArrayList<FansInfo> mSavedList = new ArrayList<FansInfo>();// 联系人列表数据缓存

	private ListView listView;
	private FansListAdapter mAdapter;

	private int currentPage = -1;

	private long mTotalCount = 0;
	private int mLastSavedTotalCount = -1;
	private View mFooterView;

	private static final int PAGE_SIZE = 200;
	private static final String SEARCH_SIZE = "100";

	private ViewHolder holder;
	private TextView footText;
	private ProgressBar footBar;
	private ImageView ivBack;

	private boolean isSearching = false;

	private SearchFansTask mSearchFansTask;

	private SearchView mSvSearch;

	private ViewStub mVSSearch;

	private String mSearchKey;

	private ImageView ivRefresh;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideFooterView();

			switch (msg.what) {
			case REQUEST_GET_FANS_LIST:
				if (msg.obj != null) {
					ArrayList<FansInfo> list = (ArrayList<FansInfo>) msg.obj;
					if (!isSearching) {
						mListData.addAll(list);
						mAdapter.notifyDataSetChanged();
						mSavedList.clear();
						mSavedList.addAll(mListData);
					} else {
						mSavedList.addAll(list);
					}
				} else {
					Toast.makeText(getActivity(), "好友列表为空!", Toast.LENGTH_SHORT)
							.show();
				}

				break;
			case ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(getActivity(), (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_friends, null);

		mVSSearch = (ViewStub) view.findViewById(R.id.vs_search);
		ivRefresh = (ImageView) view.findViewById(R.id.iv_refresh);
		ivBack = (ImageView) view.findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		ivRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mVSSearch.setLayoutResource(R.layout.search_view);
			mVSSearch.inflate();

			mSvSearch = (SearchView) view.findViewById(R.id.sv_search);
			mSvSearch.setIconifiedByDefault(false);
			mSvSearch.setQueryHint(getString(R.string.search_friends));
			mSvSearch.setOnCloseListener(new OnCloseListener() {
				@Override
				public boolean onClose() {
					searchCancel();
					return true;
				}
			});
			mSvSearch.setOnQueryTextListener(new OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					// 这里访问搜索接口
					if (TextUtils.isEmpty(newText)) {
						searchCancel();
					} else {
						mSearchKey = newText;
						isSearching = true;
						mListData.clear();
						showFooterView();
						if (mSearchFansTask != null
								&& mSearchFansTask.getStatus() == DandAsyncTask.Status.RUNNING) {
							mSearchFansTask.cancel(true);
						}
						mSearchFansTask = new SearchFansTask(newText,
								SEARCH_SIZE);
						mSearchFansTask.execute();
					}

					return false;
				}
			});
		}

		mAdapter = new FansListAdapter(FriendsFragment.this.getActivity(),
				mListData);

		listView = (ListView) view.findViewById(R.id.list);
		listView.setAdapter(mAdapter);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		mFooterView = inflater.inflate(R.layout.friends_list_foot, null);
		mFooterView.setEnabled(false);
		footText = (TextView) mFooterView.findViewById(R.id.tv_foot_view);
		footBar = (ProgressBar) mFooterView.findViewById(R.id.pb_foot_refresh);
		hideFooterView();

		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				hideInputMethod();

				FansInfo fansInfo = mListData.get(position);
				Intent intent = new Intent();
				intent.putExtra("fans_info", fansInfo);
				intent.setClass(getActivity(), FriendsInfoActivity.class);
				startActivity(intent);
			}
		});

		return view;
	};

	@Override
	protected void afterActivityCreated() {
		initFansList();
	}

	private void initFansList() {
		currentPage = 1;
		DandelionAPI.getInstance(getActivity()).getFansList(
				new DandRequestListener(mHandler) {
					@Override
					public void onComplete(String response) {
						Message msg = Message.obtain();
						try {
							if (TextUtils.isEmpty(response)
									|| response.contains("error_code")) {
								msg.what = ERROR_RESPONSE;
								JSONObject obj = new JSONObject(response);
								msg.obj = obj.getString("error");

								Logger.d(TAG, "error-->" + msg.obj);
							} else {
								if (!response.equals("[]")) {
									JSONObject obj = new JSONObject(response);

									String total = obj.getString("total_number");

									if (Integer.parseInt(total) > 0) {
										JSONArray jsonArray = obj
												.getJSONArray("users");

										ArrayList<FansInfo> list = FansInfo
												.create(jsonArray);

										msg.obj = list;
									}
								}

								msg.what = REQUEST_GET_FANS_LIST;
							}

							mHandler.sendMessage(msg);
						} catch (JSONException e) {
							e.printStackTrace();
							msg.what = ERROR_RESPONSE;
							msg.obj = "数据解析异常";
							mHandler.sendMessage(msg);
						}
					}
				}, currentPage, PAGE_SIZE);

		showFooterView();
	}

	private GetUserInfoTask task;
	private int mStartIndex;
	private int mEndIndex;

	private class FansListAdapter extends BaseAdapter {

		private ArrayList<FansInfo> list;
		private LayoutInflater inflater;
		private ImageFetcher mImageWorker;

		public FansListAdapter(Context context, ArrayList<FansInfo> list) {
			this.list = list;
			inflater = LayoutInflater.from(context);
			mImageWorker = new ImageFetcher(context, 80);
			mImageWorker.setImageCache(new ImageCache(context,
					Constants.THUMNAIL_CACHE_PROFILE_PATH));
			mImageWorker.setLoadingImage(R.drawable.icon_vdisk);
			mImageWorker.setImageFadeIn(false);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.fans_list_item, null);
				holder = new ViewHolder();
				holder.fansIcon = (ImageView) convertView
						.findViewById(R.id.iv_fans_icon);
				holder.fansName = (TextView) convertView
						.findViewById(R.id.iv_fans_name);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			FansInfo info = list.get(position);

			holder.fansName.setText(info.screen_name);
			if (info.profile_image_url != null
					&& !info.profile_image_url.trim().equals("")) {
				mImageWorker.loadImage(info.profile_image_url, holder.fansIcon,
						R.drawable.icon_vdisk, true);
			} else {
				holder.fansIcon.setImageResource(R.drawable.icon_vdisk);
			}

			return convertView;
		}
	}

	private static final class ViewHolder {
		public ImageView fansIcon;
		public TextView fansName;
	}

	private void refresh() {
		if (!isSearching) {
			mListData.clear();

			initFansList();
		} else {
			if (!TextUtils.isEmpty(mSearchKey)) {
				mListData.clear();
				showFooterView();
				if (mSearchFansTask != null
						&& mSearchFansTask.getStatus() == DandAsyncTask.Status.RUNNING) {
					mSearchFansTask.cancel(true);
				}
				mSearchFansTask = new SearchFansTask(

				mSearchKey, SEARCH_SIZE);
				mSearchFansTask.execute();
			} else {
				searchCancel();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (visibleItemCount > 0
				&& (firstVisibleItem + visibleItemCount == totalItemCount)) {
			if (totalItemCount != mLastSavedTotalCount && !isSearching) {
				mLastSavedTotalCount = totalItemCount;
				Logger.i(TAG, "currentPage---->" + currentPage + "/"
						+ mTotalCount);

				// long i = 0;
				// if ((mTotalCount % PAGE_SIZE) == 0) {
				// i = mTotalCount / PAGE_SIZE;
				// } else {
				// i = mTotalCount / PAGE_SIZE + 1;
				// }

				// if (currentPage < i) {
				// // 加载下一页
				// currentPage++;
				// try {
				// initFansList();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// }
			}
		}
		if (isSearching) {
			// Logger.i(TAG, "onScroll---->isSearching");
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount) {
				mEndIndex = totalItemCount;
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && isSearching) {
			// Logger.i(TAG, "onScrollSateChanged---->isSearching");
			if (task != null
					&& task.getStatus() == DandAsyncTask.Status.RUNNING) {
				task.cancel(true);
			}
			task = new GetUserInfoTask(mStartIndex, mEndIndex);
			task.execute();
		}
	}

	private void hideFooterView() {
		Logger.i(TAG, "hide footer view called");
		if (listView != null) {
			if (mFooterView != null) {
				listView.removeFooterView(mFooterView);
			}
		}
	}

	private void showFooterView() {
		if (mFooterView != null) {
			// 保证只有一个FooterView
			listView.removeFooterView(mFooterView);

			footText.setText("正在加载");
			footBar.setVisibility(View.VISIBLE);
			listView.addFooterView(mFooterView);
			listView.setAdapter(mAdapter);
		}
	}

	private void showEmptyView() {
		if (mFooterView != null) {
			// 保证只有一个FooterView
			listView.removeFooterView(mFooterView);

			footText.setText("无结果");
			footBar.setVisibility(View.GONE);

			listView.addFooterView(mFooterView);
			listView.setAdapter(mAdapter);
		}
	}

	private void searchCancel() {
		isSearching = false;

		if (task != null && task.getStatus() == DandAsyncTask.Status.RUNNING) {
			task.cancel(true);
		}

		mStartIndex = 0;
		mEndIndex = 0;

		if (mSavedList != null) {
			mListData.clear();
			mListData.addAll(mSavedList);
			mAdapter.notifyDataSetChanged();
		}

		hideFooterView();

		if (currentPage < mTotalCount / PAGE_SIZE) {
			showFooterView();
		}
	}

	// TODO: 移动到VDiskApi的类里面去
	public class GetUserInfoTask extends DandAsyncTask<Object[], Void, String> {
		int mStartIndex;
		int mEndIndex;

		public GetUserInfoTask(int mStartIndex, int mEndIndex) {
			this.mStartIndex = mStartIndex;
			this.mEndIndex = mEndIndex;
		}

		@Override
		protected void onPostExecute(String url) {
			mAdapter.notifyDataSetChanged();
		}

		@Override
		protected String doInBackground(Object[]... params) {
			try {
				for (; mStartIndex < mEndIndex; mStartIndex++) {
					Logger.i(TAG, "mStartIndex-->" + mStartIndex
							+ ";mEndIndex-->" + mEndIndex);
					try {
						FansInfo info = (FansInfo) mAdapter
								.getItem(mStartIndex);
						Logger.d("FansInfo", info.id + "-->" + info.screen_name);
						String[] response = DandelionAPI.getInstance(
								getActivity()).getUserInfo(info.id);

						if (response != null) {
							UserInfo userInfo = UserInfo.create(new JSONObject(
									response[1]));
							info.profile_image_url = userInfo.profile_image_url;

							if ("false".equals(response[0])) {
								CacheUtils
										.updateCache(
												getActivity(),
												CacheUtils
														.getKey(CacheUtils.CACHE_USER_INFO,
																new String[] {
																		"uid",
																		info.id }),
												response[1]);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					publishProgress();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 搜索粉丝列表，使用Weibo API
	 */
	public class SearchFansTask extends DandAsyncTask<Void, Void, Object> {

		String q;
		String count;

		public SearchFansTask(String q, String count) {
			this.q = q;
			this.count = count;
		}

		@Override
		protected Object doInBackground(Void... params) {

			try {
				String response = DandelionAPI.getInstance(getActivity())
						.searchFans(q, count);
				JSONArray array = new JSONArray(response);
				ArrayList<FansInfo> fansList = FansInfo.sCreate(array);

				return fansList;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			hideFooterView();
			if (result != null) {
				if (isSearching) {
					ArrayList<FansInfo> slist = (ArrayList<FansInfo>) result;
					Logger.i(TAG, "search fans list succeed!");
					mListData.clear();
					mListData.addAll(slist);

					if (mListData.isEmpty()) {
						showEmptyView();
					} else {
						mAdapter.notifyDataSetChanged();
						if (task != null
								&& task.getStatus() == DandAsyncTask.Status.RUNNING) {
							task.cancel(true);
						}
						if (mAdapter.getCount() < 10) {
							mEndIndex = mAdapter.getCount();
							task = new GetUserInfoTask(mStartIndex, mEndIndex);
							task.execute();
						} else {
							mEndIndex = 10;
							task = new GetUserInfoTask(mStartIndex, mEndIndex);
							task.execute();
						}
					}
				}
			} else {
				Toast.makeText(getActivity(), "搜索失败", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public void hideInputMethod() {
		if (mSvSearch != null) {
			mSvSearch.clearFocus();
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSvSearch.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
