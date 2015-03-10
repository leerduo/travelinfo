package com.ustc.dystu.dandelion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;
import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.APIUtils;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.LocationTask;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.SharePrefUtils;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class AddNoteActivity extends Activity implements OnScrollListener {
	private static final String TAG = AddNoteActivity.class.getSimpleName();
	private static final int REQUEST_GET_FOOT_LIST = 0x1;
	private static final int REQUEST_CREATE_NOTE = 0x2;

	FootListAdapter mAdapter;
	ArrayList<FootInfo> mFootList = new ArrayList<FootInfo>();

	private ArrayList<Integer> groupIds = new ArrayList<Integer>();

	// private View mFootView;
	ProgressBar footBar;
	private ImageView mBtnBack;
	private ImageView mBtnAdd;
	private TextView mTvTitle;
	ProgressBar pbRefresh;

	private int mCurrentPage = 1;
	private int mTotalNum;

	private ProgressDialog mProgress;
	private TextView mEmptyView;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pbRefresh.setVisibility(View.GONE);
			mBtnAdd.setVisibility(View.VISIBLE);

			if (mProgress != null) {
				mProgress.dismiss();
			}

			switch (msg.what) {
			case REQUEST_GET_FOOT_LIST:
				if (msg.obj != null) {
					ArrayList<FootInfo> list = (ArrayList<FootInfo>) msg.obj;

					if (mCurrentPage == 1) {
						mFootList.clear();
					}

					mFootList.addAll(list);

					Logger.d(TAG, "total num-->" + mTotalNum);
					if (mFootList.size() < mTotalNum) {
						// 仍需要加载
						needLoadMore = true;
					} else {
						// 不需要加载了...
						needLoadMore = false;
					}

					mCurrentPage++;
					mAdapter.notifyDataSetChanged();

					getGroupIds();
				} else {
					Toast.makeText(AddNoteActivity.this, "足迹列表为空!",
							Toast.LENGTH_SHORT).show();
					mStickyList.setEmptyView(mEmptyView);
				}

				break;
			case REQUEST_CREATE_NOTE:
				if (msg.obj != null) {
					Toast.makeText(AddNoteActivity.this, "创建成功!",
							Toast.LENGTH_SHORT).show();
					createNoteSuccess((NoteInfo) msg.obj);
				}
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(AddNoteActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.activity_add_note);

		mStickyList = (StickyListHeadersListView) findViewById(R.id.lv_sticky_list);
		pbRefresh = (ProgressBar) findViewById(R.id.pb_refresh);
		mProgress = new ProgressDialog(this);
		mProgress.setCanceledOnTouchOutside(false);

		mAdapter = new FootListAdapter();
		mStickyList.setAdapter(mAdapter);
		mStickyList.setOnScrollListener(this);
		mStickyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Logger.d(TAG, "item clicked-->" + position);
				mAdapter.toggleChecked(position, view);
			}
		});

		mStickyList.setOnHeaderClickListener(new OnHeaderClickListener() {

			@Override
			public void onHeaderClick(StickyListHeadersListView l, View header,
					int itemPosition, long headerId, boolean currentlySticky) {
				Logger.d(TAG, "header clicked-->" + headerId
						+ "; item position-->" + itemPosition
						+ "; current sticky-->" + currentlySticky);
				mAdapter.toggleHeaderChecked(headerId, header);
			}
		});

		mBtnBack = (ImageView) findViewById(R.id.iv_back);
		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mBtnAdd = (ImageView) findViewById(R.id.iv_add);
		mBtnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createTravleNote();
			}
		});

		mTvTitle = (TextView) findViewById(R.id.tv_title);

		mEmptyView = new TextView(this);
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("没有找到好友的足迹...");

		((ViewGroup) mStickyList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		initList();
	}

	private void createTravleNote() {
		ArrayList<FootInfo> selectedList = new ArrayList<FootInfo>();
		for (FootInfo info : mFootList) {
			if (info.isChecked) {
				selectedList.add(info);
			}
		}

		if (selectedList.isEmpty()) {
			Toast.makeText(AddNoteActivity.this, "您没有选择任何微博!",
					Toast.LENGTH_SHORT).show();
			return;
		} else if (selectedList.size() > 50) {
			Toast.makeText(AddNoteActivity.this, "选择的微博数量不能超过50个!",
					Toast.LENGTH_SHORT).show();
			return;
		}

		mProgress.setMessage("正在创建游记, 请稍候...");
		mProgress.show();

		ArrayList<Date> dateList = new ArrayList<Date>();
		HashMap<String, String> locationMap = new HashMap<String, String>();

		String picUrl = null;
		for (FootInfo info : selectedList) {
			dateList.add(info.date);

			if (info.formate_loaction != null) {
				locationMap.put(info.formate_loaction, null);
			}

			if (!TextUtils.isEmpty(info.original_pic)) {
				picUrl = info.original_pic;
			}
		}

		Comparator<Date> comparator = new Comparator<Date>() {

			@Override
			public int compare(Date lhs, Date rhs) {
				return lhs.before(rhs) ? -1 : 1;
			}
		};

		Collections.sort(dateList, comparator);

		Date before = dateList.get(0);
		Date after = dateList.get(dateList.size() - 1);

		String time_from = String.valueOf(before.getTime());
		String time_to = String.valueOf(after.getTime());

		Set<String> keySet = locationMap.keySet();

		if (keySet.isEmpty()) {
			Toast.makeText(AddNoteActivity.this, "网络繁忙, 请稍后重试!",
					Toast.LENGTH_SHORT).show();
			return;
		}

		StringBuffer sb = new StringBuffer();
		for (String string : keySet) {
			sb.append(string).append(",");
		}

		String location = sb.substring(0, sb.length() - 1);

		String title = getIntent().getStringExtra("note_title");

		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location)) {
			Toast.makeText(this, "信息不全, 无法提交!", Toast.LENGTH_SHORT).show();
			mProgress.dismiss();
		} else {
			Logger.d(TAG, "title:" + title);
			Logger.d(TAG, "location:" + location);
			Logger.d(TAG, "weibo_uid:" + SharePrefUtils.getUid(this));
			Logger.d(TAG, "folder_url:" + picUrl);

			NoteInfo noteInfo = new NoteInfo();
			noteInfo.note_folder_url = picUrl;
			noteInfo.note_location = location;
			noteInfo.note_title = title;
			noteInfo.note_uid = SharePrefUtils.getUid(this);
			noteInfo.note_is_suggest = 0;
			noteInfo.note_time_from = time_from;
			noteInfo.note_time_to = time_to;

			String json = APIUtils.buildJson(selectedList, 1, this);

			DandelionAPI.getInstance(this).createTravelNote(
					new DandRequestListener(mHandler) {

						@Override
						public void onComplete(String response) {
							Message msg = Message.obtain();
							try {
								NoteInfo info = NoteInfo.create(new JSONObject(
										response));
								msg.what = REQUEST_CREATE_NOTE;
								msg.obj = info;

							} catch (Exception e) {
								e.printStackTrace();
								msg.what = BaseFragment.ERROR_RESPONSE;
								msg.obj = "数据解析异常";
							}

							mHandler.sendMessage(msg);
						}
					}, noteInfo, json);

		}
	}

	private void createNoteSuccess(NoteInfo info) {
		Intent intent = new Intent();
		intent.putExtra("note_info", info);
		intent.putExtra("isfromMe", true);
		intent.setClass(this, NoteInfoActivity.class);
		startActivity(intent);

		Intent broadcast = new Intent();
		broadcast.putExtra("note_info", info);
		broadcast.setAction(Constants.ACTION_CREATE_NOTE_SUCCESS);
		sendBroadcast(broadcast);

		finish();
	}

	private void initList() {
		if (mCurrentPage == 1) {
			mProgress.setMessage("正在加载, 请稍候...");
			mProgress.show();
		}

		pbRefresh.setVisibility(View.VISIBLE);
		mBtnAdd.setVisibility(View.GONE);
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
				}, DandelionAPI.getInstance(this).getUid(), mCurrentPage);
	}

	class FootListAdapter extends BaseAdapter implements
			StickyListHeadersAdapter {

		private ImageFetcher mImageWorker;
		private LocationTask locationUtils;
		int padding;

		public FootListAdapter() {
			int[] wh = Utils.getMidPicWidthAndHeight(AddNoteActivity.this);
			mImageWorker = new ImageFetcher(AddNoteActivity.this, wh[0], wh[1]);
			mImageWorker.setImageCache(new ImageCache(AddNoteActivity.this,
					Constants.THUMNAIL_CACHE_PATH));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);

			locationUtils = new LocationTask(AddNoteActivity.this);
			padding = Utils.dip2px(AddNoteActivity.this, 3);
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
						R.layout.child_item_add_note, null);
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
				holder.llCard = (LinearLayout) convertView
						.findViewById(R.id.ll_card);
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
			// holder.cbCheck.setChecked(info.isChecked);

			if (info.isChecked) {
				holder.llCard
						.setBackgroundResource(R.drawable.note_info_item_bg_s);

				holder.llCard.setPadding(padding, padding, padding, padding);
			} else {
				holder.llCard
						.setBackgroundResource(R.drawable.note_info_item_bg);
				holder.llCard.setPadding(padding, padding, padding, padding);
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
						R.layout.head_item_add_note, parent, false);
				holder.tvHeaderName = (TextView) convertView
						.findViewById(R.id.tv_head);
				holder.cbHeaderCheck = (CheckBox) convertView
						.findViewById(R.id.cb_header_check);

				convertView.setTag(holder);
			} else {
				holder = (HeaderViewHolder) convertView.getTag();
			}

			holder.tvHeaderName.setText(getItem(position).created_at);

			boolean isChecked = true;
			for (FootInfo info : mFootList) {
				if (info.headerId == getHeaderPosition(position)
						&& !info.isChecked) {
					isChecked = false;
					break;
				}
			}

			Logger.d(TAG, "header is checked-->" + isChecked + "; pos->"
					+ position + "; info-->" + getItem(position).created_at);
			holder.cbHeaderCheck.setChecked(isChecked);

			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			return getItem(position).headerId;
		}

		public void toggleChecked(int position, View view) {
			FootInfo entry = (FootInfo) getItem(position);
			entry.isChecked = !entry.isChecked;

			Logger.d(TAG, "entry is checked-->" + entry.isChecked);
			LinearLayout llCard = (LinearLayout) view
					.findViewById(R.id.ll_card);

			if (entry.isChecked) {
				llCard.setBackgroundResource(R.drawable.note_info_item_bg_s);
				llCard.setPadding(padding, padding, padding, padding);
			} else {
				llCard.setBackgroundResource(R.drawable.note_info_item_bg);
				llCard.setPadding(padding, padding, padding, padding);
			}

			if (entry.isChecked) {
				mSelectedNum++;
			} else {
				mSelectedNum--;
			}

			updateTitle();
			notifyDataSetChanged();
		}

		public void toggleHeaderChecked(long headerId, View view) {
			CheckBox checkBox = (CheckBox) view
					.findViewById(R.id.cb_header_check);
			boolean isChecked = checkBox.isChecked();
			checkBox.setChecked(!isChecked);
			for (FootInfo info : mFootList) {
				if (info.headerId == headerId) {
					if (info.isChecked != !isChecked) {
						info.isChecked = !isChecked;

						if (info.isChecked) {
							mSelectedNum++;
						} else {
							mSelectedNum--;
						}
					}
				}
			}

			updateTitle();
			notifyDataSetChanged();
		}
	}

	private int mSelectedNum;

	private void updateTitle() {
		if (mSelectedNum > 0) {
			mTvTitle.setText(String.format("已选择(%s)", mSelectedNum));
		} else {
			mTvTitle.setText("添加游记");
			mSelectedNum = 0;
		}
	}

	class ViewHolder {
		public TextView tvTime;
		public TextView tvLocation;
		public TextView tvText;
		public TextView tvPicNum;
		public ImageView ivIcon;
		public RelativeLayout rlIcon;
		public LinearLayout llCard;
	}

	class HeaderViewHolder {
		TextView tvHeaderName;
		CheckBox cbHeaderCheck;
	}

	private void getGroupIds() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (FootInfo info : mFootList) {
			map.put(info.headerId, null);
		}

		Set<Integer> keySet = map.keySet();

		groupIds.clear();
		groupIds.addAll(keySet);

		Collections.sort(groupIds, comparator);

		Logger.d(TAG, "groupIds-->" + groupIds);
	}

	private int getHeaderPosition(int pos) {
		int size = groupIds.size();

		for (int i = 0; i < size; i++) {
			if (i < size - 1) {
				if (pos >= groupIds.get(i) && pos < groupIds.get(i + 1)) {
					return groupIds.get(i);
				}
			} else {
				return groupIds.get(i);
			}
		}

		return 0;
	}

	Comparator<Integer> comparator = new Comparator<Integer>() {

		@Override
		public int compare(Integer lhs, Integer rhs) {
			return lhs - rhs;
		}
	};

	boolean needLoadMore = true;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		// 当不滚动时
		case OnScrollListener.SCROLL_STATE_IDLE:
			// 判断滚动到底部
			if (mStickyList.getLastVisiblePosition() == (mStickyList.getCount() - 1)) {
				Logger.d(TAG, "reach bottom...");
				if (needLoadMore) {
					initList();
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
