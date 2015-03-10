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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class LatestTravelNoteActivity extends Activity {
	


	private static final int REQUEST_GET_TRAVEL_LIST = 0x1;

	ListView lvList;
	ArrayList<NoteInfo> mList = new ArrayList<NoteInfo>();
	NoteAdapter mAdapter;
	ImageView ivRefresh;
	ProgressBar pbRefresh;

	private TextView mEmptyView;

	ProgressDialog pbProgress;

	ImageView ivBack;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ivRefresh.setVisibility(View.VISIBLE);
			pbRefresh.setVisibility(View.INVISIBLE);
			if (pbProgress != null) {
				pbProgress.dismiss();
			}
			switch (msg.what) {
			case REQUEST_GET_TRAVEL_LIST:
				if (msg.obj != null) {
					ArrayList<NoteInfo> list = (ArrayList<NoteInfo>) msg.obj;
					mList.clear();
					mList.addAll(list);

					if (mList.isEmpty()) {
						lvList.setEmptyView(mEmptyView);
					}

					mAdapter.notifyDataSetChanged();
				}
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(LatestTravelNoteActivity.this,
							(String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_latest_travel);

		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LatestTravelNoteActivity.this.finish();
			}
		});

		lvList = (ListView) findViewById(R.id.lv_list);
		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("note_info", mList.get(position));
				intent.setClass(LatestTravelNoteActivity.this,
						NoteInfoActivity.class);
				startActivity(intent);
			}
		});

		ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
		pbRefresh = (ProgressBar) findViewById(R.id.pb_refresh);

		ivRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				init();
			}
		});

		mAdapter = new NoteAdapter(LatestTravelNoteActivity.this);
		lvList.setAdapter(mAdapter);

		mEmptyView = new TextView(LatestTravelNoteActivity.this);
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("没有最新的游记");

		((ViewGroup) lvList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		pbProgress = new ProgressDialog(LatestTravelNoteActivity.this);
		pbProgress.setMessage("正在加载, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);

		init();
	};

	private void init() {
		ivRefresh.setVisibility(View.INVISIBLE);
		pbRefresh.setVisibility(View.VISIBLE);
		pbProgress.show();
		DandelionAPI.getInstance(LatestTravelNoteActivity.this)
				.getLatestTravelNotes(new DandRequestListener(mHandler) {

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
				}, 20);
	}

	class NoteAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ImageFetcher mImageWorker;

		public NoteAdapter(Context context) {
			inflater = LayoutInflater.from(context);

			int[] wh = Utils
					.getMidPicWidthAndHeight(LatestTravelNoteActivity.this);
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
				holder.tvTitle = (TextView) convertView
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

			holder.tvTitle.setText(info.note_title);
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
		public TextView tvTitle;
		public TextView tvTime;
		public TextView tvTimeUsed;
	}
	
	


}
