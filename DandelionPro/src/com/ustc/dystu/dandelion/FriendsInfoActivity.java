package com.ustc.dystu.dandelion;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.FansInfo;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class FriendsInfoActivity extends Activity {



	private static final int REQUEST_GET_TRAVEL_LIST = 0x2;

	private FansInfo mFansInfo;

	private TextView mTvFansName;
	private ImageView mBtnBack;
	private ImageView mIvPortrait;
	private Button mBtnFoot;
	private ImageFetcher mImageWorker;

	ArrayList<NoteInfo> mList = new ArrayList<NoteInfo>();
	NoteAdapter mAdapter;

	private ListView lvList;

	ProgressDialog pbProgress;

	TextView tvNoteNum;
	private TextView mEmptyView;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REQUEST_GET_TRAVEL_LIST:
				if (pbProgress != null) {
					pbProgress.dismiss();
				}

				if (msg.obj != null) {
					ArrayList<NoteInfo> list = (ArrayList<NoteInfo>) msg.obj;
					mList.clear();
					mList.addAll(list);

					if (list.isEmpty()) {
						tvNoteNum.setText("没有游记");
//						Toast.makeText(FriendsInfoActivity.this,
//								"没有游记, 看看他的微博足迹吧!", Toast.LENGTH_SHORT).show();

						if (mList.isEmpty()) {
							lvList.setEmptyView(mEmptyView);
						}
					} else {
						tvNoteNum.setText(list.size() + "篇游记");
					}

					mAdapter.notifyDataSetChanged();
				}
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (pbProgress != null) {
					pbProgress.dismiss();
				}
				if (msg.obj != null) {
					Toast.makeText(FriendsInfoActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friends_info);
		tvNoteNum = (TextView) findViewById(R.id.tv_travel_num);

		Intent intent = getIntent();
		mFansInfo = (FansInfo) intent.getSerializableExtra("fans_info");

		lvList = (ListView) findViewById(R.id.lv_travel);
		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("note_info", mList.get(position));
				intent.setClass(FriendsInfoActivity.this,
						NoteInfoActivity.class);
				startActivity(intent);
			}
		});

		mAdapter = new NoteAdapter(this);
		lvList.setAdapter(mAdapter);

		mEmptyView = new TextView(this);
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("没有游记, 看看他的微博足迹吧!");
		((ViewGroup) lvList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		pbProgress = new ProgressDialog(this);
		pbProgress.setMessage("正在加载, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);

		initViews();
		init();
	}

	private void initViews() {
		mImageWorker = new ImageFetcher(this, 200);
		mImageWorker.setImageCache(new ImageCache(this,
				Constants.THUMNAIL_CACHE_PROFILE_BIG_PATH));
		mImageWorker.setLoadingImage(R.drawable.icon_vdisk);
		mImageWorker.setImageFadeIn(false);

		mTvFansName = (TextView) findViewById(R.id.tv_friends_name);
		mTvFansName.setText(mFansInfo.screen_name);

		mIvPortrait = (ImageView) findViewById(R.id.iv_portrait);
		if (mFansInfo.profile_image_url != null
				&& !mFansInfo.profile_image_url.trim().equals("")) {
			String url = mFansInfo.profile_image_url.replace("/50/", "/180/");
			mImageWorker.loadImage(url, mIvPortrait, R.drawable.icon_vdisk,
					true);
		} else {
			mIvPortrait.setImageResource(R.drawable.icon_vdisk);
		}

		mBtnBack = (ImageView) findViewById(R.id.iv_back);
		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mBtnFoot = (Button) findViewById(R.id.btn_foot);
		mBtnFoot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("fans_info", mFansInfo);
				intent.setClass(FriendsInfoActivity.this,
						FriendFootprintActivity.class);
				startActivity(intent);
			}
		});
	}

	private void init() {
		pbProgress.show();
		DandelionAPI.getInstance(this).getTravelNotes(
				new DandRequestListener(mHandler) {

					@Override
					public void onComplete(String arg0) {
						Message msg = Message.obtain();

						try {
							JSONArray array = new JSONArray(arg0);
							ArrayList<NoteInfo> data = NoteInfo.create(array);

							msg.obj = data;
							msg.what = REQUEST_GET_TRAVEL_LIST;

						} catch (JSONException e) {
							e.printStackTrace();
							msg.what = BaseFragment.ERROR_RESPONSE;
							msg.obj = "数据解析异常";
						}

						mHandler.sendMessage(msg);
					}
				}, mFansInfo.id);
	}

	class NoteAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ImageFetcher mImageWorker;

		public NoteAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			int[] wh = Utils.getMidPicWidthAndHeight(FriendsInfoActivity.this);
			mImageWorker = new ImageFetcher(context, wh[0], wh[1]);
			mImageWorker.setImageCache(new ImageCache(context,
					Constants.THUMNAIL_CACHE_PATH));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_note, null);
				holder = new ViewHolder();
				holder.ivFolder = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.tvInfo = (TextView) convertView
						.findViewById(R.id.tv_info);
				holder.tvTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tvTimeUsed = (TextView) convertView
						.findViewById(R.id.tv_time_used);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NoteInfo info = mList.get(position);

			if (!TextUtils.isEmpty(info.note_folder_url)) {
				mImageWorker.loadImage(info.note_folder_url, holder.ivFolder,
						R.drawable.share_public_headview_bg, true);
			} else {
				holder.ivFolder
						.setImageResource(R.drawable.share_public_headview_bg);
			}

			holder.tvInfo.setText(info.note_title);
			holder.tvTime.setText(info.getFormatNoteFromTime());
			holder.tvTimeUsed.setText(info.getTotalDays());

			return convertView;
		}

		@Override
		public void notifyDataSetChanged() {
			Collections.sort(mList, NoteInfo.comparator);
			super.notifyDataSetChanged();
		}

	}

	private static class ViewHolder {
		public ImageView ivFolder;
		public TextView tvInfo;
		public TextView tvTime;
		public TextView tvTimeUsed;
	}

	


}
