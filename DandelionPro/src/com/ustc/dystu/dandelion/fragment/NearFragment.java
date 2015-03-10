package com.ustc.dystu.dandelion.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

import com.ustc.dystu.dandelion.NoteDetailActivity;
import com.ustc.dystu.dandelion.R;
import com.ustc.dystu.dandelion.app.DandelionApplication;
import com.ustc.dystu.dandelion.bean.NearInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.LocationTask;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class NearFragment extends BaseFragment {
	


	private static final String TAG = NearFragment.class.getSimpleName();

	private static final int REQUEST_GET_NEAR_LIST = 0x1;

	ListView lvList;
	ArrayList<NearInfo> mList = new ArrayList<NearInfo>();
	FootAdapter mAdapter;
	ImageView ivRefresh;
	ImageView ivBack;
	ProgressBar pbRefresh;
	ProgressDialog pbProgress;

	private TextView mEmptyView;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ivRefresh.setVisibility(View.VISIBLE);
			pbRefresh.setVisibility(View.INVISIBLE);
			if (pbProgress != null) {
				pbProgress.dismiss();
			}
			switch (msg.what) {
			case REQUEST_GET_NEAR_LIST:
				if (msg.obj != null) {
					ArrayList<NearInfo> list = (ArrayList<NearInfo>) msg.obj;

					if (!list.isEmpty()) {
						mList.clear();
						mList.addAll(list);

						Collections.sort(mList, comparator);

						if (mList.isEmpty()) {
							lvList.setEmptyView(mEmptyView);
						}

						lvList.setAdapter(mAdapter);
					}
				}
				break;
			case ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(getActivity(), (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				}
				
				if (mList.isEmpty()) {
					lvList.setEmptyView(mEmptyView);
				}
				break;

			default:
				break;
			}
		}
	};

	Comparator<NearInfo> comparator = new Comparator<NearInfo>() {

		@Override
		public int compare(NearInfo lhs, NearInfo rhs) {
			return lhs.footInfo.date.before(rhs.footInfo.date) ? 1 : -1;
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_near, null);

		lvList = (ListView) view.findViewById(R.id.lv_list);
		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("near_info", mList.get(position));
				intent.setClass(getActivity(), NoteDetailActivity.class);
				startActivity(intent);
			}
		});

		ivRefresh = (ImageView) view.findViewById(R.id.iv_refresh);
		pbRefresh = (ProgressBar) view.findViewById(R.id.pb_refresh);

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
				init();
			}
		});

		mAdapter = new FootAdapter(getActivity());
		lvList.setAdapter(mAdapter);

		pbProgress = new ProgressDialog(getActivity());
		pbProgress.setMessage("正在加载, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);

		mEmptyView = new TextView(getActivity());
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("没有在附近找到游记");

		((ViewGroup) lvList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		init();

		return view;
	}

	private void init() {
		ivRefresh.setVisibility(View.INVISIBLE);
		pbRefresh.setVisibility(View.VISIBLE);
		pbProgress.show();

		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				ArrayList<NearInfo> data = DandelionAPI.getInstance(
						getActivity()).getNearTravelNote(
						DandelionApplication.latitude, DandelionApplication.longtitude);

				if (data != null) {
					msg.obj = data;
					msg.what = REQUEST_GET_NEAR_LIST;
				} else {
					msg.what = BaseFragment.ERROR_RESPONSE;
					msg.obj = "附近没有找到游记";
				}

				mHandler.sendMessage(msg);
			};
		}.start();
	}

	class FootAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ImageFetcher mImageWorker;
		private ImageFetcher mPortraitWorker;
		private LocationTask locationUtils;

		public FootAdapter(Context context) {
			inflater = LayoutInflater.from(context);

			int[] wh = Utils.getMidPicWidthAndHeight(getActivity());
			mImageWorker = new ImageFetcher(context, wh[0], wh[1]);
			mImageWorker.setImageCache(new ImageCache(context,
					Constants.THUMNAIL_CACHE_PATH));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);

			mPortraitWorker = new ImageFetcher(getActivity(), 80);
			mPortraitWorker.setImageCache(new ImageCache(getActivity(),
					Constants.THUMNAIL_CACHE_PROFILE_PATH));
			mPortraitWorker.setLoadingImage(R.drawable.icon_vdisk);
			mPortraitWorker.setImageFadeIn(false);

			locationUtils = new LocationTask(context);
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
				convertView = inflater.inflate(R.layout.list_item_near, null);
				holder = new ViewHolder();
				holder.ivPic = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.ivPortrait = (ImageView) convertView
						.findViewById(R.id.iv_portrait);
				holder.tvInfo = (TextView) convertView
						.findViewById(R.id.tv_info);
				holder.tvLocation = (TextView) convertView
						.findViewById(R.id.tv_location);
				holder.tvText = (TextView) convertView
						.findViewById(R.id.tv_text);
				holder.tvTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tvScreenName = (TextView) convertView
						.findViewById(R.id.tv_screen_name);
				holder.tvPicNum = (TextView) convertView
						.findViewById(R.id.tv_pic_num);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NearInfo info = mList.get(position);

			if (info.footInfo != null) {
				// 微博图片
				if (!TextUtils.isEmpty(info.footInfo.original_pic)) {
					holder.ivPic.setVisibility(View.VISIBLE);
					mImageWorker.loadImage(info.footInfo.original_pic,
							holder.ivPic, R.drawable.share_public_headview_bg,
							true);
				} else {
					holder.ivPic
							.setImageResource(R.drawable.share_public_headview_bg);
					holder.ivPic.setVisibility(View.GONE);
				}

				// 微博文本
				holder.tvText.setText(info.footInfo.text);
				holder.tvTime.setText(com.ustc.dystu.dandelion.utils.Utils
						.getTimeBefore(info.footInfo.date));

				// 位置信息
				if (!TextUtils.isEmpty(info.footInfo.formate_loaction)) {
					holder.tvLocation.setText(info.footInfo.getFormatLoaciton());
				} else {
					holder.tvLocation.setText("");
					locationUtils
							.loadLocation(info.footInfo, holder.tvLocation);
				}

				if (info.footInfo.picIds != null
						&& info.footInfo.picIds.length > 1) {
					holder.tvPicNum.setVisibility(View.VISIBLE);
					holder.tvPicNum.setText(info.footInfo.picIds.length + "P");
				} else {
					holder.tvPicNum.setVisibility(View.GONE);
				}

				if (info.footInfo.userInfo != null) {
					// 微博图片
					if (!TextUtils
							.isEmpty(info.footInfo.userInfo.profile_image_url)) {
						mPortraitWorker.loadImage(
								info.footInfo.userInfo.profile_image_url,
								holder.ivPortrait, R.drawable.icon_vdisk, true);
					} else {
						holder.ivPortrait
								.setImageResource(R.drawable.icon_vdisk);
					}

					holder.tvScreenName
							.setText(info.footInfo.userInfo.screen_name);

					holder.tvInfo.setText(String.format("在 \"%s\" 中添加了一条内容",
							info.noteInfo.note_title));
				}
			}

			return convertView;
		}

	}

	private static class ViewHolder {
		public ImageView ivPortrait;
		public ImageView ivPic;
		public TextView tvInfo;
		public TextView tvScreenName;
		public TextView tvTime;
		public TextView tvText;
		public TextView tvLocation;
		public TextView tvPicNum;
	}


}
