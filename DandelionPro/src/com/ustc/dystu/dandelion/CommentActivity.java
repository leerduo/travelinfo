package com.ustc.dystu.dandelion;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.CommentInfo;
import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;

public class CommentActivity extends Activity implements OnClickListener{

	private static final int REQUEST_GET_COMMENT_LIST = 0x1;
	private static final int REQUEST_CREATE_COMMENT = 0x2;

	Button btnComment;
	EditText etComment;
	ImageView ivBack;
	ListView lvList;

	CommentAdapter mAdapter;
	ArrayList<CommentInfo> mList = new ArrayList<CommentInfo>();
	FootInfo mFootInfo;

	private TextView mEmptyView;

	ProgressDialog pbProgress;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (pbProgress != null) {
				pbProgress.dismiss();
			}
			switch (msg.what) {
			case REQUEST_GET_COMMENT_LIST:
				if (mList.isEmpty()) {
					lvList.setEmptyView(mEmptyView);
				}

				mAdapter.notifyDataSetChanged();
				break;
			case REQUEST_CREATE_COMMENT:
				hideInputMethod();
				if (msg.obj != null) {
					CommentInfo info = (CommentInfo) msg.obj;
					mList.add(info);
				}

				mAdapter.notifyDataSetChanged();
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(CommentActivity.this, (String) msg.obj,
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
		setContentView(R.layout.activity_comment);
		btnComment = (Button) findViewById(R.id.btn_comment);
		btnComment.setOnClickListener(this);
		etComment = (EditText) findViewById(R.id.et_comment);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		lvList = (ListView) findViewById(R.id.lv_list);

		mAdapter = new CommentAdapter();
		lvList.setAdapter(mAdapter);

		mEmptyView = new TextView(this);
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("沙发居然还在...");

		((ViewGroup) lvList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		mFootInfo = (FootInfo) getIntent().getSerializableExtra("foot_info");

		pbProgress = new ProgressDialog(this);
		pbProgress.setMessage("正在加载, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);

		init();
	}

	private void init() {
		pbProgress.show();
		DandelionAPI.getInstance(this).getComments(
				new DandRequestListener(mHandler) {

					@Override
					public void onComplete(String arg0) {
						Message msg = Message.obtain();
						try {
							JSONObject jo = new JSONObject(arg0);
							JSONArray array = jo.getJSONArray("comments");
							ArrayList<CommentInfo> list = CommentInfo.create(array);

							mList.clear();
							mList.addAll(list);

							msg.what = REQUEST_GET_COMMENT_LIST;
						} catch (Exception e) {
							e.printStackTrace();
							msg.what = BaseFragment.ERROR_RESPONSE;
							msg.obj = "网络异常";
						}

						mHandler.sendMessage(msg);
					}
				}, mFootInfo.id);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_comment:
			String comment = etComment.getText().toString();

			if (!TextUtils.isEmpty(comment)) {
				pbProgress.setMessage("正在生成评论, 请稍候...");
				pbProgress.show();
				DandelionAPI.getInstance(this).setComments(
						new DandRequestListener(mHandler) {

							@Override
							public void onComplete(String arg0) {
								Message msg = Message.obtain();
								try {
									JSONObject jo = new JSONObject(arg0);

									CommentInfo info = CommentInfo.create(jo);
									msg.what = REQUEST_CREATE_COMMENT;
									msg.obj = info;
								} catch (Exception e) {
									e.printStackTrace();
									msg.what = BaseFragment.ERROR_RESPONSE;
									msg.obj = "网络异常";
								}

								mHandler.sendMessage(msg);
							}
						}, mFootInfo.id, comment);
			} else {
				Toast.makeText(this, "您没有输入任何内容哦!", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.iv_back:
			finish();
			break;

		default:
			break;
		}
	}

	class CommentAdapter extends BaseAdapter {
		private ImageFetcher mImageWorker;

		public CommentAdapter() {
			mImageWorker = new ImageFetcher(CommentActivity.this, 80);
			mImageWorker.setImageCache(new ImageCache(CommentActivity.this,
					Constants.THUMNAIL_CACHE_PROFILE_PATH));
			mImageWorker.setLoadingImage(R.drawable.icon_vdisk);
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
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.list_item_comment, null);
				holder.tvScreenName = (TextView) convertView
						.findViewById(R.id.tv_screen_name);
				holder.tvComment = (TextView) convertView
						.findViewById(R.id.tv_comment);
				holder.tvTime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.ivIcon = (ImageView) convertView
						.findViewById(R.id.iv_icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			CommentInfo info = mList.get(position);

			holder.tvComment.setText(info.text);
			holder.tvTime.setText(info.getFormatTime());

			if (info.userInfo != null) {
				holder.tvScreenName.setText(info.userInfo.screen_name);
				if (!TextUtils.isEmpty(info.userInfo.profile_image_url)) {
					mImageWorker.loadImage(info.userInfo.profile_image_url,
							holder.ivIcon, true);
				} else {
					holder.ivIcon.setImageResource(R.drawable.icon_vdisk);
				}
			}

			return convertView;
		}

		@Override
		public void notifyDataSetChanged() {
			Collections.sort(mList, CommentInfo.comparator);

			super.notifyDataSetChanged();
		}
	}

	class ViewHolder {
		public TextView tvScreenName;
		public TextView tvComment;
		public TextView tvTime;
		public ImageView ivIcon;
	}

	public void hideInputMethod() {
		if (etComment != null) {
			etComment.clearFocus();
			etComment.setText("");
			InputMethodManager imm = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etComment.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
