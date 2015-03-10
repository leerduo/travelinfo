package com.ustc.dystu.dandelion;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.ustc.dystu.dandelion.bean.FansInfo;
import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.LocationTask;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class FriendFootprintActivity extends Activity implements OnScrollListener{

	private static final String TAG = "FriendFootprintActivity";
	ImageView ivBack;
	ProgressBar pbProgress;

	ArrayList<FootInfo> mFootList = new ArrayList<FootInfo>();
	FootListAdapter mAdapter;

	ProgressDialog mProgress;

	private static final int REQUEST_GET_FOOT_LIST = 0x1;

	ListView lvList;
	ImageView ivMap;
	ProgressBar pbRefresh;

	private FansInfo mFansInfo;

	private int mCurrentPage = 1;
	private int mTotalNum;

	TextView tvTitle;
	private TextView mEmptyView;
	boolean needLoadMore = true;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (mProgress != null) {
				mProgress.dismiss();
			}

			ivMap.setVisibility(View.VISIBLE);
			pbProgress.setVisibility(View.INVISIBLE);

			switch (msg.what) {
			case REQUEST_GET_FOOT_LIST:
				if (msg.obj != null) {
					ArrayList<FootInfo> list = (ArrayList<FootInfo>) msg.obj;

					if (mCurrentPage == 1) {
						mFootList.clear();
					}

					mFootList.addAll(list);

					if (mFootList.size() < mTotalNum) {
						// 仍需要加载
						needLoadMore = true;
					} else {
						// 不需要加载了...
						needLoadMore = false;
					}

					mCurrentPage++;
					mAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(FriendFootprintActivity.this, "足迹列表为空!",
							Toast.LENGTH_SHORT).show();
					mStickyList.setEmptyView(mEmptyView);
				}

				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(FriendFootprintActivity.this,
							(String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	private StickyListHeadersListView mStickyList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friend_footprint);
		mStickyList = (StickyListHeadersListView) findViewById(R.id.lv_sticky_list);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		tvTitle = (TextView) findViewById(R.id.tv_title);
		ivMap = (ImageView) findViewById(R.id.iv_map);

		ivMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("footList", mFootList);
				intent.setClass(FriendFootprintActivity.this,
						FootMapActivity.class);

				startActivity(intent);
			}
		});

		pbProgress = (ProgressBar) findViewById(R.id.pb_refresh);
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("正在加载, 请稍候...");
		mProgress.setCanceledOnTouchOutside(false);

		mAdapter = new FootListAdapter();
		mStickyList.setAdapter(mAdapter);

		mFansInfo = (FansInfo) getIntent().getSerializableExtra("fans_info");
		tvTitle.setText(mFansInfo.screen_name + "的足迹");

		mEmptyView = new TextView(this);
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("没有找到好友的足迹...");

		((ViewGroup) mStickyList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		mStickyList.setOnScrollListener(this);

		init();
	}

	private void init() {
		ivMap.setVisibility(View.INVISIBLE);
		pbProgress.setVisibility(View.VISIBLE);
		DandelionAPI.getInstance(this).getLocationStatus(
				new DandRequestListener(mHandler) {
					@Override
					public void onComplete(String response) {
						Message msg = Message.obtain();
						try {
							if (TextUtils.isEmpty(response)
									|| response.contains("error_code")) {
								msg.what = BaseFragment.ERROR_RESPONSE;
								JSONObject obj = new JSONObject(response);
								msg.obj = obj.getString("error");

								Logger.d(TAG, "error-->" + msg.obj);
							} else {
								Logger.d(TAG, "response-->" + response);
								if (!response.equals("[]")) {
									JSONObject obj = new JSONObject(response);

									String total = obj.getString("total_number");

									mTotalNum = Integer.parseInt(total);

									if (mTotalNum > 0) {
										JSONArray jsonArray = obj
												.getJSONArray("statuses");
										ArrayList<FootInfo> list = FootInfo
												.create(jsonArray,
														mFootList.size());

										msg.obj = list;
									}
								}

								msg.what = REQUEST_GET_FOOT_LIST;
							}

							mHandler.sendMessage(msg);
						} catch (JSONException e) {
							e.printStackTrace();
							msg.what = BaseFragment.ERROR_RESPONSE;
							msg.obj = "数据解析异常";
							mHandler.sendMessage(msg);
						}
					}
				}, mFansInfo.id, mCurrentPage);
	}

	class FootListAdapter extends BaseAdapter implements
			StickyListHeadersAdapter {

		private ImageFetcher mImageWorker;
		private LocationTask locationUtils;

		public FootListAdapter() {
			int[] wh = Utils
					.getMidPicWidthAndHeight(FriendFootprintActivity.this);
			mImageWorker = new ImageFetcher(FriendFootprintActivity.this,
					wh[0], wh[1]);
			mImageWorker
					.setImageCache(new ImageCache(FriendFootprintActivity.this,
							Constants.THUMNAIL_CACHE_PATH));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);

			locationUtils = new LocationTask(FriendFootprintActivity.this);
		}

		@Override
		public int getCount() {
			return mFootList.size();
		}

		@Override
		public FootInfo getItem(int position) {
			return mFootList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.list_item_note_info, null);
				holder = new ViewHolder();
				holder.tvLocation = (TextView) convertView
						.findViewById(R.id.tv_location);
				holder.ivIcon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.tvText = (TextView) convertView
						.findViewById(R.id.tv_text);
				holder.tvTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tvPicNum = (TextView) convertView
						.findViewById(R.id.tv_pic_num);
				holder.rlIcon = (RelativeLayout) convertView
						.findViewById(R.id.rl_icon);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			FootInfo info = mFootList.get(position);

			if (!TextUtils.isEmpty(info.formate_loaction)) {
				holder.tvLocation.setText(info.getFormatLoaciton());
			} else {
				holder.tvLocation.setText("");
				locationUtils.loadLocation(info, holder.tvLocation);
			}

			holder.tvText.setText(info.text);
			holder.tvTime.setText(info.getDetailFormatTime());

			if (!TextUtils.isEmpty(info.original_pic)) {
				holder.rlIcon.setVisibility(View.VISIBLE);
				mImageWorker.loadImage(info.original_pic, holder.ivIcon, true);
			} else {
				holder.rlIcon.setVisibility(View.GONE);
			}

			if (info.picIds != null && info.picIds.length > 1) {
				holder.tvPicNum.setVisibility(View.VISIBLE);
				holder.tvPicNum.setText(info.picIds.length + "P");
			} else {
				holder.tvPicNum.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			HeaderViewHolder holder;
			if (convertView == null) {
				holder = new HeaderViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.head_item_footprint, parent, false);
				holder.tvHeader = (TextView) convertView
						.findViewById(R.id.tv_head);
				convertView.setTag(holder);
			} else {
				holder = (HeaderViewHolder) convertView.getTag();
			}

			holder.tvHeader.setText(getItem(position).created_at);

			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			return getItem(position).headerId;
		}
	}

	class ViewHolder {
		public TextView tvTime;
		public TextView tvLocation;
		public TextView tvText;
		public TextView tvPicNum;
		public ImageView ivIcon;
		public RelativeLayout rlIcon;
	}

	class HeaderViewHolder {
		TextView tvHeader;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		// 当不滚动时
		case OnScrollListener.SCROLL_STATE_IDLE:
			// 判断滚动到底部
			if (mStickyList.getLastVisiblePosition() == (mStickyList.getCount() - 1)) {
				Logger.d(TAG, "reach bottom...");
				if (needLoadMore) {
					init();
				}
			}
			// 判断滚动到顶部
			if (mStickyList.getFirstVisiblePosition() == 0) {
				Logger.d(TAG, "reach top...");
			}

			break;
		}

	}

	boolean flag;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount == totalItemCount && !flag) {
			flag = true;
		} else {
			flag = false;
		}
	}
	
	
}
