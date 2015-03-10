package com.ustc.dystu.dandelion;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class FolderEditActivity extends Activity {

	private static final int REQUEST_UPDATE_NOTE_INFO = 0x1;

	ImageView ivBack;
	ImageView ivOk;
	ImageView ivFolder;

	TextView tvTitle;
	TextView tvTime;
	TextView tvTimeUsed;

	GridView gvPics;

	PicAdapter picAdapter;

	ArrayList<FootInfo> mFootList;
	NoteInfo mNoteInfo;
	private ArrayList<String> picList = new ArrayList<String>();

	String currentPic;

	private ImageFetcher mImageWorker;
	ProgressDialog pbProgress;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pbProgress.dismiss();
			switch (msg.what) {
			case REQUEST_UPDATE_NOTE_INFO:
				Toast.makeText(FolderEditActivity.this, "更新成功!",
						Toast.LENGTH_SHORT).show();

				Intent broadcast = new Intent();
				broadcast.putExtra("note_info", mNoteInfo);
				broadcast.setAction(Constants.ACTION_EDIT_NOTE_SUCCESS);
				sendBroadcast(broadcast);
				finish();
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(FolderEditActivity.this, (String) msg.obj,
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

		setContentView(R.layout.activity_folder_edit);

		ivFolder = (ImageView) findViewById(R.id.iv_icon);
		tvTitle = (TextView) findViewById(R.id.tv_info);
		tvTime = (TextView) findViewById(R.id.tv_time);
		tvTimeUsed = (TextView) findViewById(R.id.tv_time_used);

		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ivOk = (ImageView) findViewById(R.id.iv_ok);
		ivOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateFolder();
			}
		});

		Intent intent = getIntent();
		mFootList = (ArrayList<FootInfo>) intent
				.getSerializableExtra("foot_list");
		mNoteInfo = (NoteInfo) intent.getSerializableExtra("note_info");
		
		tvTitle.setText(mNoteInfo.note_title);
		tvTime.setText(mNoteInfo.getFormatNoteFromTime());
		tvTimeUsed.setText(mNoteInfo.getTotalDays());

		String url = mNoteInfo.note_folder_url;
		currentPic = url.substring(url.lastIndexOf("/") + 1,
				url.lastIndexOf("."));

		getPicIds();
		gvPics = (GridView) findViewById(R.id.gv_pics);
		picAdapter = new PicAdapter(this);
		gvPics.setAdapter(picAdapter);

		gvPics.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentPic = picList.get(position);
				picAdapter.notifyDataSetChanged();

				String url = "http://ww3.sinaimg.cn/large/" + currentPic
						+ ".jpg";
				mImageWorker.loadImage(url, ivFolder, true);
				mNoteInfo.note_folder_url = url;
			}
		});

		int[] wh = Utils.getMidPicWidthAndHeight(this);
		mImageWorker = new ImageFetcher(this, wh[0], wh[1]);

		mImageWorker.setImageCache(new ImageCache(this,
				Constants.THUMNAIL_CACHE_PATH));
		mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
		mImageWorker.setImageFadeIn(false);

		mImageWorker.loadImage("http://ww3.sinaimg.cn/large/" + currentPic
				+ ".jpg", ivFolder, true);
	}

	private void updateFolder() {
		pbProgress = new ProgressDialog(this);
		pbProgress.setMessage("正在更新数据, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);
		pbProgress.show();
		DandelionAPI.getInstance(this).updateNote(
				new DandRequestListener(mHandler) {

					@Override
					public void onComplete(String arg0) {
						JSONObject jo;
						Message msg = Message.obtain();
						try {
							jo = new JSONObject(arg0);
							String code = jo.getString("error_code");

							if (Integer.parseInt(code) == 0) {
								msg.what = REQUEST_UPDATE_NOTE_INFO;
							} else {
								msg.what = BaseFragment.ERROR_RESPONSE;
								msg.obj = "网络异常";
							}
						} catch (JSONException e) {
							e.printStackTrace();
							msg.what = BaseFragment.ERROR_RESPONSE;
							msg.obj = "数据解析异常";
						}

						mHandler.sendMessage(msg);

					}
				}, mNoteInfo);
	}

	private void getPicIds() {
		for (FootInfo info : mFootList) {
			if (info.picIds != null && info.picIds.length > 0) {
				for (int i = 0; i < info.picIds.length; i++) {
					picList.add(info.picIds[i]);
				}
			}
		}
	}

	class PicAdapter extends BaseAdapter {
		private ImageFetcher mImageWorker;
		Context ctx;
		LayoutInflater inflater;

		public PicAdapter(Context ctx) {
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
			return picList.size();
		}

		@Override
		public String getItem(int position) {
			return picList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.list_item_folder_edit,
						null);
				holder.ivPic = (ImageView) convertView
						.findViewById(R.id.iv_pic);
				holder.rlBg = (RelativeLayout) convertView
						.findViewById(R.id.rl_grid_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (getItem(position).equals(currentPic)) {
				holder.rlBg.setBackgroundColor(ctx.getResources().getColor(
						R.color.green));
			} else {
				holder.rlBg.setBackgroundColor(ctx.getResources().getColor(
						R.color.white));
			}

			mImageWorker.loadImage("http://ww1.sinaimg.cn/bmiddle/"
					+ getItem(position) + ".jpg", holder.ivPic,
					R.drawable.preview_card_pic_loading, true);

			return convertView;
		}

	}

	class ViewHolder {
		public ImageView ivPic;
		public RelativeLayout rlBg;
	}
	
}
