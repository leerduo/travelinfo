package com.ustc.dystu.dandelion;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.NearInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.LocationTask;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class NoteDetailActivity extends Activity {

	private static final int REQUEST_GET_NOTE_INFO = 0x1;

	private static final String TAG = "NoteDetailActivity";

	ViewPager mViewPager;
	private NotePagerAdapter mPagerAdapter;
	ArrayList<FootInfo> mFootList;

	private Button btnBack;

	private ImageView btnShare;

	private LinearLayout btnComment;

	private TextView tvTime;

	private TextView tvLocation;

	private TextView tvTitle;
	private TextView tvText;
	private TextView tvCommentNum;
	private TextView tvBottomLocation;

	private ProgressDialog pdProgress;

	private int mTotalNum;
	private int mIndex;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REQUEST_GET_NOTE_INFO:
				if (pdProgress != null) {
					pdProgress.dismiss();
				}

				prepareFootList();

				if (mNearInfo != null) {
					mIndex = mFootList.indexOf(mNearInfo.footInfo);

					Logger.d(TAG, "current index-->" + mIndex);
					mTotalNum = mFootList.size();

					mViewPager.setAdapter(mPagerAdapter);
					mViewPager.setCurrentItem(mIndex);

					updateControlBar(mIndex);
				}
				break;
			case BaseFragment.ERROR_RESPONSE:
				if (msg.obj != null) {
					Toast.makeText(NoteDetailActivity.this, (String) msg.obj,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(NoteDetailActivity.this, "网络异常",
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_note_detail);
		mViewPager = (ViewPager) findViewById(R.id.tab_pager);
		mViewPager.setOffscreenPageLimit(1);

		Intent intent = getIntent();
		int index = intent.getIntExtra("index", 0);
		mFootList = (ArrayList<FootInfo>) intent
				.getSerializableExtra("foot_list");

		mPagerAdapter = new NotePagerAdapter();

		initViews();

		if (mFootList != null) {
			FootInfo info = mFootList.get(index);
			prepareFootList();

			mViewPager.setAdapter(mPagerAdapter);

			mIndex = mFootList.indexOf(info);

			mViewPager.setCurrentItem(mIndex);

			mTotalNum = mFootList.size();
			Logger.d(TAG, "foot list size-->" + mFootList.size()
					+ "; mIndex-->" + mIndex);

			updateControlBar(mIndex);
		} else {
			mFootList = new ArrayList<FootInfo>();
			mNearInfo = (NearInfo) intent.getSerializableExtra("near_info");

			final String[] weiboIds = mNearInfo.noteInfo.weiboIds;

			if (mNearInfo.noteInfo.weiboIds != null) {
				pdProgress = new ProgressDialog(this);
				pdProgress.setMessage("正在加载, 请稍候...");
				pdProgress.setCanceledOnTouchOutside(false);
				pdProgress.show();

				new Thread() {
					public void run() {
						ArrayList<FootInfo> weiboList = DandelionAPI
								.getInstance(NoteDetailActivity.this)
								.getWeiboInfo(weiboIds);

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
	}

	boolean isControlShow = true;
	LocationTask locationUtils;

	private RelativeLayout rlControl;;

	private void initViews() {
		locationUtils = new LocationTask(NoteDetailActivity.this);

		btnBack = (Button) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NoteDetailActivity.this.finish();
			}
		});
		btnShare = (ImageView) findViewById(R.id.btn_share);
		btnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				com.ustc.dystu.dandelion.utils.Utils.share(NoteDetailActivity.this);
			}
		});
		btnComment = (LinearLayout) findViewById(R.id.btn_comment);
		btnComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int i = mViewPager.getCurrentItem();
				FootInfo info = mFootList.get(i);
				Intent intent = new Intent();
				intent.putExtra("foot_info", info);
				intent.setClass(NoteDetailActivity.this, CommentActivity.class);
				startActivity(intent);
			}
		});

		tvTime = (TextView) findViewById(R.id.tv_time);
		tvLocation = (TextView) findViewById(R.id.tv_location);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvText = (TextView) findViewById(R.id.tv_text);
		tvCommentNum = (TextView) findViewById(R.id.tv_comment_num);
		tvBottomLocation = (TextView) findViewById(R.id.tv_bottom_location);

		rlControl = (RelativeLayout) findViewById(R.id.ll_control_bar);

		final GestureDetector mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						if (!isControlShow) {
							rlControl.setVisibility(View.VISIBLE);
							tvBottomLocation.setVisibility(View.GONE);
							isControlShow = true;
						} else {
							rlControl.setVisibility(View.GONE);
							tvBottomLocation.setVisibility(View.VISIBLE);
							isControlShow = false;
						}
						return true;
					}
				}, null, true);

		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				updateControlBar(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void updateControlBar(int pos) {
		FootInfo info = mFootList.get(pos);

		tvTime.setText(info.getDetailFormatTime2());
		tvTitle.setText((pos + 1) + "/" + mTotalNum);
		if (!TextUtils.isEmpty(info.formate_loaction)) {
			tvLocation.setText(info.getFormatLoaciton());
			tvBottomLocation.setText(info.getFormatLoaciton());
		} else {
			tvLocation.setText("");
			tvBottomLocation.setText("");
			locationUtils.loadLocation(info, tvLocation);
			locationUtils.loadLocation(info, tvBottomLocation);
		}

		if (!TextUtils.isEmpty(info.original_pic)) {
			tvText.setVisibility(View.VISIBLE);
			tvText.setText(info.text);
		} else {
			tvText.setVisibility(View.GONE);
		}

		tvCommentNum.setText("(" + info.comments_count + ")");
	}

	private void prepareFootList() {
		int size = mFootList.size();
		Logger.d(TAG, "mFootList size-->" + size);

		ArrayList<FootInfo> footList = new ArrayList<FootInfo>();
		for (int i = 0; i < size; i++) {
			FootInfo info = mFootList.get(i);

			if (!footList.contains(info)) {
				footList.add(info);
			}

			if (info.picIds != null && info.picIds.length > 1) {
				for (int j = 0; j < info.picIds.length; j++) {
					String orgUrl = getThumnailUrl(info.picIds[j]);

					FootInfo copy = FootInfo.copy(info);
					copy.original_pic = orgUrl;
					copy.defaultPicId = info.picIds[j];
					if (!footList.contains(copy)) {
						footList.add(copy);
					}
				}
			}
		}

		mFootList.clear();
		mFootList.addAll(footList);
	}

	private String getThumnailUrl(String picId) {
		// http://ww3.sinaimg.cn/large/590473f6jw1dwmgp7b8tjj.jpg
		return "http://ww3.sinaimg.cn/large/" + picId + ".jpg";
	}

	class NotePagerAdapter extends PagerAdapter {

		HashMap<Integer, View> views;

		private ImageFetcher mImageWorker;

		public NotePagerAdapter() {
			views = new HashMap<Integer, View>();

			int[] wh = Utils.getBigPicWidthAndHeight(NoteDetailActivity.this);
			mImageWorker = new ImageFetcher(NoteDetailActivity.this, wh[0],
					wh[1]);
			mImageWorker.setImageCache(new ImageCache(NoteDetailActivity.this,
					Constants.THUMNAIL_CACHE_PATH + "/.bigthumnail"));
			mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
			mImageWorker.setImageFadeIn(false);
		}

		@Override
		public int getCount() {
			return mFootList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((View) object);
		}

		public HashMap<Integer, View> getViews() {
			return views;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View pageView = views.get(position);

			if (pageView == null) {
				FootInfo info = mFootList.get(position);

				pageView = getLayoutInflater().inflate(
						R.layout.pager_item_foot_detail, null);
				ImageView ivIcon = (ImageView) pageView
						.findViewById(R.id.iv_icon);
				TextView tvText = (TextView) pageView
						.findViewById(R.id.tv_text);

				Logger.d(TAG, "get view org url-->" + info.original_pic);

				if (!TextUtils.isEmpty(info.original_pic)) {
					ivIcon.setVisibility(View.VISIBLE);
					tvText.setVisibility(View.GONE);
					mImageWorker.loadImage(info.original_pic, ivIcon, true);
				} else {
					ivIcon.setImageResource(R.drawable.share_public_headview_bg);
					ivIcon.setVisibility(View.GONE);
					tvText.setVisibility(View.VISIBLE);
					tvText.setText(info.text);
				}

				views.put(position, pageView);
			}

			container.addView(pageView);

			return pageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = views.get(position);
			container.removeView(views.get(position));
			views.remove(position);

			try {
				mImageWorker.cancelWork((ImageView) view
						.findViewById(R.id.iv_icon));

				Bitmap bitmap = mImageWorker.getImageCache()
						.getBitmapFromMemCache(
								mFootList.get(position).original_pic);

				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}

				mImageWorker.getImageCache().getMemCache()
						.remove(mFootList.get(position).original_pic);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	Comparator<FootInfo> comparator = new Comparator<FootInfo>() {

		@Override
		public int compare(FootInfo lhs, FootInfo rhs) {
			BigInteger lid = new BigInteger(lhs.id);
			BigInteger rid = new BigInteger(rhs.id);

			return lid.compareTo(rid);
		}
	};

	private NearInfo mNearInfo;

	
}
