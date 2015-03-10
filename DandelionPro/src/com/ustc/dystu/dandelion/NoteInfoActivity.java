package com.ustc.dystu.dandelion;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.bean.UserInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.LocationTask;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.RoundedImageView;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class NoteInfoActivity extends Activity implements OnClickListener{

	private static final String TAG = "NoteInfoActivity";
	private static final int REQUEST_GET_NOTE_INFO = 0x1;

	ImageView ivBack;
	ImageView ivShareOrEdit;
	ProgressBar pbProgress;

	NoteInfo mNoteInfo;

	ArrayList<FootInfo> mFootList = new ArrayList<FootInfo>();
	FootListAdapter mAdapter;

	ProgressDialog mProgress;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(TAG, "reciever messge");

			NoteInfo info = (NoteInfo) intent.getSerializableExtra("note_info");

			String action = intent.getAction();

			if (action.equals(Constants.ACTION_EDIT_NOTE_SUCCESS)) {
				if (info != null) {
					tvNoteTitle.setText(info.note_title);
				}
			}
		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ivShareOrEdit.setVisibility(View.VISIBLE);
			pbProgress.setVisibility(View.INVISIBLE);

			if (mProgress != null) {
				mProgress.dismiss();
			}

			switch (msg.what) {
			case REQUEST_GET_NOTE_INFO:
				prepareHeadId();

				if (!mFootList.isEmpty()) {
					updateHeaderView(mFootList.get(0).userInfo);

					footerView.setVisibility(View.VISIBLE);
				}

				mAdapter.notifyDataSetChanged();
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(NoteInfoActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(NoteInfoActivity.this, "网络异常",
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	private StickyListHeadersListView mStickyList;
	private PopupWindow mPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_note_info);
		mStickyList = (StickyListHeadersListView) findViewById(R.id.lv_sticky_list);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		ivShareOrEdit = (ImageView) findViewById(R.id.iv_share);
		ivShareOrEdit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isFromMe) {
					// 从个人主页进来, 弹出编辑选项
					if (mPopupWindow == null) {
						View view = NoteInfoActivity.this.getLayoutInflater()
								.inflate(R.layout.popup_note_edit, null, true);

						view.findViewById(R.id.ll_change_folder)
								.setOnClickListener(NoteInfoActivity.this);
						view.findViewById(R.id.ll_change_title)
								.setOnClickListener(NoteInfoActivity.this);
						view.findViewById(R.id.ll_share).setOnClickListener(
								NoteInfoActivity.this);
						view.findViewById(R.id.ll_edit).setOnClickListener(
								NoteInfoActivity.this);

						mPopupWindow = new PopupWindow(view,
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT, true);
						mPopupWindow.setOutsideTouchable(true);
						mPopupWindow
								.setBackgroundDrawable(new BitmapDrawable());
					}

					mPopupWindow.showAsDropDown(v, 0, 0);

				} else {
					// 从精选过来, 刷新界面
					// init();
					com.ustc.dystu.dandelion.utils.Utils.share(NoteInfoActivity.this);
				}
			}
		});

		pbProgress = (ProgressBar) findViewById(R.id.pb_refresh);
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("正在加载, 请稍候...");
		mProgress.setCanceledOnTouchOutside(false);

		Intent intent = getIntent();

		mNoteInfo = (NoteInfo) intent.getSerializableExtra("note_info");
		isFromMe = intent.getBooleanExtra("isfromMe", false);

		if (isFromMe) {
			ivShareOrEdit.setImageResource(R.drawable.btn_more_selector);
		} else {
			ivShareOrEdit.setImageResource(R.drawable.btn_share_selector);
		}

		View headerView = getLayoutInflater().inflate(
				R.layout.header_note_info, null);
		ivPortrait = (RoundedImageView) headerView
				.findViewById(R.id.iv_portrait);
		tvNoteTitle = (TextView) headerView.findViewById(R.id.tv_note_title);
		tvNoteInfo = (TextView) headerView.findViewById(R.id.tv_note_info);

		mStickyList.addHeaderView(headerView);

		footerView = getLayoutInflater().inflate(R.layout.footer_note_info,
				null);
		mStickyList.addFooterView(footerView);
		footerView.setVisibility(View.GONE);

		mAdapter = new FootListAdapter();
		mStickyList.setAdapter(mAdapter);

		mStickyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position > 0 && position <= mFootList.size()) {
					Intent intent = new Intent();
					intent.putExtra("index", position - 1);
					intent.putExtra("foot_list", mFootList);
					intent.setClass(NoteInfoActivity.this,
							NoteDetailActivity.class);
					startActivity(intent);
				}
			}
		});

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_EDIT_NOTE_SUCCESS);
		this.registerReceiver(receiver, intentFilter);

		init();
	}

	private void init() {
		final String[] weiboIds = mNoteInfo.weiboIds;
		if (weiboIds != null) {
			ivShareOrEdit.setVisibility(View.INVISIBLE);
			pbProgress.setVisibility(View.VISIBLE);
			mProgress.show();

			new Thread() {
				public void run() {
					ArrayList<FootInfo> weiboList = DandelionAPI.getInstance(
							NoteInfoActivity.this).getWeiboInfo(weiboIds);

					if (weiboList != null && !weiboList.isEmpty()) {
						Collections.sort(weiboList, comparator);

						mFootList.clear();
						mFootList.addAll(weiboList);

						mHandler.sendEmptyMessage(REQUEST_GET_NOTE_INFO);
					} else {
						mHandler.sendEmptyMessage(BaseFragment.ERROR_RESPONSE);
					}
				};
			}.start();
		}
	}

	private void updateHeaderView(UserInfo info) {
		if (info != null && mNoteInfo != null) {
			tvNoteTitle.setText(mNoteInfo.note_title);
			tvNoteInfo.setText(mNoteInfo.getFormatNoteFromTime() + "   "
					+ mNoteInfo.getTotalDays());

			ImageFetcher mImageWorker = new ImageFetcher(NoteInfoActivity.this,
					200);
			mImageWorker.setImageCache(new ImageCache(NoteInfoActivity.this,
					Constants.THUMNAIL_CACHE_PROFILE_BIG_PATH));
			mImageWorker.setLoadingImage(R.drawable.icon_vdisk);
			mImageWorker.setImageFadeIn(false);

			if (!TextUtils.isEmpty(info.profile_image_url)) {
				String url = info.profile_image_url.replace("/50/", "/180/");
				mImageWorker.loadImage(url, ivPortrait, R.drawable.icon_vdisk,
						true);
			} else {
				ivPortrait.setImageResource(R.drawable.icon_vdisk);
			}
		}
	}

	class FootListAdapter extends BaseAdapter implements
			StickyListHeadersAdapter {

		private ImageFetcher mImageWorker;
		private LocationTask locationUtils;

		public FootListAdapter() {
			int[] wh = Utils.getMidPicWidthAndHeight(NoteInfoActivity.this);
			mImageWorker = new ImageFetcher(NoteInfoActivity.this, wh[0], wh[1]);
			mImageWorker.setImageCache(new ImageCache(NoteInfoActivity.this,
					Constants.THUMNAIL_CACHE_PATH));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);

			locationUtils = new LocationTask(NoteInfoActivity.this);
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
						R.layout.head_item_note_info, parent, false);
				holder.tvHeaderNum = (TextView) convertView
						.findViewById(R.id.tv_num);
				holder.tvHeaderTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			} else {
				holder = (HeaderViewHolder) convertView.getTag();
			}

			Date firstDay = mFootList.get(0).date;
			Date currentDay = mFootList.get(position).date;

			holder.tvHeaderNum.setText(FootInfo.getTimeSort(firstDay,
					currentDay));
			holder.tvHeaderTime.setText(getItem(position).getFormatTime());

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
		TextView tvHeaderNum;
		TextView tvHeaderTime;
	}

	Comparator<FootInfo> comparator = new Comparator<FootInfo>() {

		@Override
		public int compare(FootInfo lhs, FootInfo rhs) {
			BigInteger lid = new BigInteger(lhs.id);
			BigInteger rid = new BigInteger(rhs.id);

			return lid.compareTo(rid);
		}
	};
	private boolean isFromMe;
	private RoundedImageView ivPortrait;
	private TextView tvNoteTitle;
	private TextView tvNoteInfo;
	private View footerView;

	private void prepareHeadId() {
		FootInfo preInfo = null;
		int currentHeaderId = 0;
		int size = mFootList.size();
		for (int i = 0; i < size; i++) {
			FootInfo info = mFootList.get(i);

			if (preInfo != null) {
				if (!info.created_at.equals(preInfo.created_at)) {
					currentHeaderId = i;
				}
			}

			preInfo = info;
			info.headerId = currentHeaderId;
		}
	}

	@Override
	public void onClick(View v) {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}

		switch (v.getId()) {
		case R.id.ll_change_folder:
			if (hasPics()) {
				Intent intent2 = new Intent();
				intent2.putExtra("foot_list", mFootList);
				intent2.putExtra("note_info", mNoteInfo);
				intent2.setClass(this, FolderEditActivity.class);
				startActivity(intent2);
			} else {
				Toast.makeText(this, "没有找到可供编辑的图片", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.ll_change_title:
			Intent intent = new Intent();
			intent.setClass(this, EditNoteTitleActivity.class);
			intent.putExtra("note_info", mNoteInfo);
			startActivity(intent);
			break;
		case R.id.ll_share:
			com.ustc.dystu.dandelion.utils.Utils.share(this);
			break;
		case R.id.ll_edit:

			break;

		default:
			break;
		}

	}

	private boolean hasPics() {
		for (FootInfo info : mFootList) {
			if (info.picIds != null && info.picIds.length > 0) {
				return true;
			}
		}

		return false;
	}

	
	@Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
}
