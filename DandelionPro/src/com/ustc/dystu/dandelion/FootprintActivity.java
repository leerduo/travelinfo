package com.ustc.dystu.dandelion;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ustc.dystu.dandelion.view.NoScrollGridView;

public class FootprintActivity extends Activity {

	private static final String TAG = FootprintActivity.class.getSimpleName();
	private static final int REQUEST_GET_FOOT_LIST = 0x1;

	ListView lvList;
	TextView tvTitle;
	ImageView ivBack;
	ImageView ivRefresh;
	ImageView ivMap;
	ProgressBar pbRefresh;
	private View mFootView;
	ProgressBar footBar;
	ArrayList<FootInfo> mFootList = new ArrayList<FootInfo>();

	FootprintAdapter mAdapter;
	private FansInfo mFansInfo;

	private int mCurrentPage = 1;
	private int mTotalNum;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pbRefresh.setVisibility(View.INVISIBLE);
			ivRefresh.setVisibility(View.VISIBLE);
			footBar.setVisibility(View.GONE);
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
						lvList.removeFooterView(mFootView);
						lvList.addFooterView(mFootView);
					} else {
						lvList.removeFooterView(mFootView);
					}

					mCurrentPage++;
					mAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(FootprintActivity.this, "足迹列表为空!",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(FootprintActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_footprint);
		lvList = (ListView) findViewById(R.id.list);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
		pbRefresh = (ProgressBar) findViewById(R.id.pb_refresh);
		ivMap = (ImageView) findViewById(R.id.iv_map);

		ivMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("footList", mFootList);
				intent.setClass(FootprintActivity.this, FootMapActivity.class);

				startActivity(intent);
			}
		});

		mFootView = this.getLayoutInflater().inflate(
				R.layout.friends_list_foot, null);

		((TextView) mFootView.findViewById(R.id.tv_foot_view)).setText("更多");
		footBar = (ProgressBar) mFootView.findViewById(R.id.pb_foot_refresh);
		// footBar.setVisibility(View.GONE);

		lvList.addFooterView(mFootView);

		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (position == mFootList.size()) {
					Logger.d(TAG, "foot clicked!");
					footBar.setVisibility(View.VISIBLE);
					initList();
				} else {
					Logger.d(TAG, "item clicked-->" + position);
				}
			}
		});

		ivRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCurrentPage = 1;
				initList();
			}
		});

		mAdapter = new FootprintAdapter();
		lvList.setAdapter(mAdapter);

		mFansInfo = (FansInfo) getIntent().getSerializableExtra("fans_info");
		tvTitle.setText(mFansInfo.screen_name + "的足迹");
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		initList();
	}

	private void initList() {
		pbRefresh.setVisibility(View.VISIBLE);
		ivRefresh.setVisibility(View.INVISIBLE);
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

	class FootprintAdapter extends BaseAdapter {
		private LocationTask locationUtils;

		public FootprintAdapter() {
			locationUtils = new LocationTask(FootprintActivity.this);
		}

		@Override
		public int getCount() {
			return mFootList.size();
		}

		@Override
		public Object getItem(int position) {
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
						R.layout.list_item_foot, null);
				holder = new ViewHolder();
				holder.tvTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tvLocation = (TextView) convertView
						.findViewById(R.id.tv_location);
				holder.gvPics = (NoScrollGridView) convertView
						.findViewById(R.id.gv_pics);
				holder.gvPics.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Integer pos = (Integer) parent.getTag();
						Logger.d(TAG, "grid view item clicked!-->" + pos);
					}
				});

				holder.tvText = (TextView) convertView
						.findViewById(R.id.tv_text);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			FootInfo info = mFootList.get(position);

			holder.tvTime.setText(info.created_at);
			if (!TextUtils.isEmpty(info.formate_loaction)) {
				holder.tvLocation.setText(info.getFormatLoaciton());
			} else {
				holder.tvLocation.setText("");
				locationUtils.loadLocation(info, holder.tvLocation);
			}

			holder.tvText.setText(info.text);

			if (!TextUtils.isEmpty(info.bmiddle_pic)) {
				holder.gvPics.setVisibility(View.VISIBLE);
				holder.gvPics.setTag(position);
				if (info.picIds != null && info.picIds.length>0) {
					holder.gvPics.setAdapter(new PicAdapter(info.picIds,
							FootprintActivity.this));
				} else {
					holder.gvPics.setVisibility(View.GONE);
				}
			} else {
				holder.gvPics.setVisibility(View.GONE);
			}

			return convertView;
		}
	}

	private static final class ViewHolder {
		public TextView tvTime;
		public TextView tvLocation;
		public TextView tvText;
		public NoScrollGridView gvPics;
	}

	public static class PicAdapter extends BaseAdapter {
		String[] picIds;
		private ImageFetcher mImageWorker;
		Context ctx;
		LayoutInflater inflater;

		public PicAdapter(String[] picIds, Context ctx) {
			this.picIds = picIds;
			this.ctx = ctx;
			mImageWorker = new ImageFetcher(ctx, 80);
			mImageWorker.setImageCache(new ImageCache(ctx,
					Constants.THUMNAIL_CACHE_SMALL_PATH));
			mImageWorker.setLoadingImage(R.drawable.preview_card_pic_loading);
			mImageWorker.setImageFadeIn(false);

			inflater = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return picIds.length;
		}

		@Override
		public String getItem(int position) {
			return picIds[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_foot_pic,
						null);
				ImageView ivPic = (ImageView) convertView
						.findViewById(R.id.iv_pic);
				convertView.setTag(ivPic);
			}

			ImageView ivPic = (ImageView) convertView.getTag();
			mImageWorker.loadImage("http://ww1.sinaimg.cn/bmiddle/"
					+ getItem(position) + ".jpg", ivPic,
					R.drawable.preview_card_pic_loading, true);

			return convertView;
		}

	}

}
