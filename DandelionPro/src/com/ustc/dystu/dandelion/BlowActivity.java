package com.ustc.dystu.dandelion;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.blow.Parameter;
import com.ustc.dystu.dandelion.utils.blow.RecordThread;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class BlowActivity extends Activity implements OnClickListener{


	private final String TAG = BlowActivity.class.getSimpleName();
	public static final int BLOW_FINISHED = 0;
	private static final int REQUEST_RANDOM_NOTE = 1;

	private Animation alphaAnimation;
	private Animation alphaAnimation2;
	private Animation scaleAnimation;;
	private Animation scaleAnimation2;;
	private Animation singleBlowAnim;;

	// 向右动画
	private Animation loadAnimation1;
	private Animation loadAnimation2;
	private Animation loadAnimation3;
	private Animation loadAnimation4;
	private Animation loadAnimation5;
	private Animation loadAnimation6;
	private Animation loadAnimation7;
	private Animation loadAnimation8;
	private Animation loadAnimation9;
	private Animation loadAnimation10;
	private Animation loadAnimation11;

	// 向左动画
	private Animation loadAnimation1_1;
	private Animation loadAnimation2_1;
	private Animation loadAnimation3_1;
	private Animation loadAnimation4_1;
	private Animation loadAnimation5_1;
	private Animation loadAnimation6_1;
	private Animation loadAnimation7_1;
	private Animation loadAnimation8_1;
	private Animation loadAnimation9_1;
	private Animation loadAnimation10_1;

	// 向前动画
	private Animation loadAnimation1_2;
	private Animation loadAnimation2_2;
	private Animation loadAnimation3_2;
	private Animation loadAnimation4_2;
	private Animation loadAnimation5_2;
	private Animation loadAnimation6_2;
	private Animation loadAnimation7_2;
	private Animation loadAnimation8_2;
	private Animation loadAnimation9_2;
	private Animation loadAnimation10_2;

	private ImageView ivDandelion;
	private ImageView ivDandelionLeft;
	private RelativeLayout rlResultControl;

	private ImageView ivSingleBlow;

	private ImageView iv1;
	private ImageView iv2;
	private ImageView iv3;
	private ImageView iv4;
	private ImageView iv5;
	private ImageView iv6;
	private ImageView iv7;
	private ImageView iv8;
	private ImageView iv9;
	private ImageView iv10;
	private ImageView iv11;

	private ImageView iv1_1;
	private ImageView iv2_1;
	private ImageView iv3_1;
	private ImageView iv4_1;
	private ImageView iv5_1;
	private ImageView iv6_1;
	private ImageView iv7_1;
	private ImageView iv8_1;
	private ImageView iv9_1;
	private ImageView iv10_1;

	private ImageView iv1_2;
	private ImageView iv2_2;
	private ImageView iv3_2;
	private ImageView iv4_2;
	private ImageView iv5_2;
	private ImageView iv6_2;
	private ImageView iv7_2;
	private ImageView iv8_2;
	private ImageView iv9_2;
	private ImageView iv10_2;

	RecordThread tt = null;
	ImageView ivBack;

	RelativeLayout rlSeedResult;
	private NoteInfo mNoteInfo;
	private MediaPlayer mediaPlayer;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BLOW_FINISHED:
				startAnim();
				break;
			case REQUEST_RANDOM_NOTE:
				if (msg.obj != null) {
					mNoteInfo = (NoteInfo) msg.obj;
					Logger.d(TAG, "info result-->" + mNoteInfo);
					updateSeedResultInfo();
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blow);

		tvTitle = (TextView) findViewById(R.id.tv_title);
		// tvTitle.setOnClickListener(this);

		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		initViews();
		initData();
		initBeepSound();
	}

	boolean canRecord = true;

	@Override
	protected void onResume() {
		if (canRecord) {
			startRecord();
		}

		super.onResume();
	}

	private void initViews() {
		initSeedResultInfo();
		initAnim();
	}

	private void initData() {
		clearSeedResult();
		new Thread() {
			public void run() {
				NoteInfo info = DandelionAPI.getInstance(BlowActivity.this)
						.getRandomTravelNote();
				Message msg = Message.obtain();
				msg.what = REQUEST_RANDOM_NOTE;
				msg.obj = info;
				mHandler.sendMessage(msg);
			};
		}.start();
	}

	private TextView tvResultLocation;
	private TextView tvResultTitle;
	private ImageView ivResultPortrait;
	private ImageView ivResultFolder;
	private TextView tvResultTime;

	private Button btnLetFlow;
	private Button btnEnter;

	private ImageFetcher mImageWorker;
	private ImageFetcher mPortraitWorker;
	private TextView tvTitle;

	private void initSeedResultInfo() {
		rlResultControl = (RelativeLayout) findViewById(R.id.rl_result_control);

		tvResultLocation = (TextView) findViewById(R.id.tv_result_location);
		tvResultTitle = (TextView) findViewById(R.id.tv_result_title);
		ivResultPortrait = (ImageView) findViewById(R.id.iv_result_portrait);
		ivResultFolder = (ImageView) findViewById(R.id.iv_result_folder);
		tvResultTime = (TextView) findViewById(R.id.tv_result_time);

		btnLetFlow = (Button) findViewById(R.id.btn_let_blow);
		btnLetFlow.setOnClickListener(this);
		btnEnter = (Button) findViewById(R.id.btn_enter_note);
		btnEnter.setOnClickListener(this);

		int[] wh = Utils.getMidPicWidthAndHeight(BlowActivity.this);
		mImageWorker = new ImageFetcher(this, wh[0], wh[1]);
		mImageWorker.setImageCache(new ImageCache(this,
				Constants.THUMNAIL_CACHE_PATH));
		mImageWorker.setLoadingImage(R.drawable.share_public_headview_bg);
		mImageWorker.setImageFadeIn(false);

		mPortraitWorker = new ImageFetcher(this, 80);
		mPortraitWorker.setImageCache(new ImageCache(this,
				Constants.THUMNAIL_CACHE_PROFILE_PATH));
		mPortraitWorker.setLoadingImage(R.drawable.icon_vdisk);
		mPortraitWorker.setImageFadeIn(false);

	}

	private void updateSeedResultInfo() {
		if (mNoteInfo != null) {
			tvResultLocation.setText(mNoteInfo.getFirstLocation());
			tvResultTitle.setText(mNoteInfo.note_title);
			tvResultTime.setText(mNoteInfo.getFormatNoteFromTime() + "   "
					+ mNoteInfo.getTotalDays());

			if (!TextUtils.isEmpty(mNoteInfo.note_folder_url)) {
				mImageWorker.loadImage(mNoteInfo.note_folder_url,
						ivResultFolder, true);
			} else {
				ivResultFolder
						.setImageResource(R.drawable.share_public_headview_bg);
			}

			if (mNoteInfo.userIno != null) {
				if (!TextUtils.isEmpty(mNoteInfo.userIno.profile_image_url)) {
					mPortraitWorker.loadImage(
							mNoteInfo.userIno.profile_image_url,
							ivResultPortrait, true);
				} else {
					ivResultPortrait.setImageResource(R.drawable.icon_vdisk);
				}
			}
		}
	}

	private void clearSeedResult() {
		tvResultLocation.setText("...");
		tvResultTitle.setText("...");
		tvResultTime.setText("...");

		ivResultFolder.setImageResource(R.drawable.share_public_headview_bg);
		ivResultPortrait.setImageResource(R.drawable.icon_vdisk);
	}

	private static final float BEEP_VOLUME = 0.80f;

	private void initBeepSound() {
		if (mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.blow1);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	private void initAnim() {
		alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
		alphaAnimation2 = AnimationUtils
				.loadAnimation(this, R.anim.anim_alpha2);
		scaleAnimation = AnimationUtils.loadAnimation(this,
				R.anim.anim_seed_result);
		scaleAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				rlResultControl.setVisibility(View.VISIBLE);
				rlResultControl.startAnimation(alphaAnimation2);
			}
		});

		scaleAnimation2 = AnimationUtils.loadAnimation(this,
				R.anim.anim_seed_result_dispear);
		scaleAnimation2.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Logger.d(TAG, "satrt single blow anim...");
				startSingleBlowAnim();
				initData();
			}
		});

		singleBlowAnim = AnimationUtils.loadAnimation(this,
				R.anim.anim_single_blow);
		singleBlowAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				ivSingleBlow.setVisibility(View.GONE);
				tvTitle.setEnabled(true);

				ivDandelion.setVisibility(View.VISIBLE);
				ivDandelion.setEnabled(true);
				ivDandelionLeft.setVisibility(View.INVISIBLE);
				canRecord = true;
				startRecord();
			}
		});

		loadAnimation1 = AnimationUtils.loadAnimation(this, R.anim.anim_1);
		loadAnimation2 = AnimationUtils.loadAnimation(this, R.anim.anim_2);
		loadAnimation3 = AnimationUtils.loadAnimation(this, R.anim.anim_3);
		loadAnimation4 = AnimationUtils.loadAnimation(this, R.anim.anim_4);
		loadAnimation5 = AnimationUtils.loadAnimation(this, R.anim.anim_5);
		loadAnimation6 = AnimationUtils.loadAnimation(this, R.anim.anim_6);
		loadAnimation7 = AnimationUtils.loadAnimation(this, R.anim.anim_7);
		loadAnimation8 = AnimationUtils.loadAnimation(this, R.anim.anim_8);
		loadAnimation9 = AnimationUtils.loadAnimation(this, R.anim.anim_9);
		loadAnimation10 = AnimationUtils.loadAnimation(this, R.anim.anim_10);
		loadAnimation11 = AnimationUtils.loadAnimation(this, R.anim.anim_11);

		loadAnimation1_1 = AnimationUtils.loadAnimation(this, R.anim.anim_1_1);
		loadAnimation2_1 = AnimationUtils.loadAnimation(this, R.anim.anim_2_1);
		loadAnimation3_1 = AnimationUtils.loadAnimation(this, R.anim.anim_3_1);
		loadAnimation4_1 = AnimationUtils.loadAnimation(this, R.anim.anim_4_1);
		loadAnimation5_1 = AnimationUtils.loadAnimation(this, R.anim.anim_5_1);
		loadAnimation6_1 = AnimationUtils.loadAnimation(this, R.anim.anim_6_1);
		loadAnimation7_1 = AnimationUtils.loadAnimation(this, R.anim.anim_7_1);
		loadAnimation8_1 = AnimationUtils.loadAnimation(this, R.anim.anim_8_1);
		loadAnimation9_1 = AnimationUtils.loadAnimation(this, R.anim.anim_9_1);
		loadAnimation10_1 = AnimationUtils
				.loadAnimation(this, R.anim.anim_10_1);

		loadAnimation1_2 = AnimationUtils.loadAnimation(this, R.anim.anim_2_2);
		loadAnimation2_2 = AnimationUtils.loadAnimation(this, R.anim.anim_2_2);
		loadAnimation3_2 = AnimationUtils.loadAnimation(this, R.anim.anim_3_2);
		loadAnimation4_2 = AnimationUtils.loadAnimation(this, R.anim.anim_4_2);
		loadAnimation5_2 = AnimationUtils.loadAnimation(this, R.anim.anim_5_2);
		loadAnimation6_2 = AnimationUtils.loadAnimation(this, R.anim.anim_6_2);
		loadAnimation7_2 = AnimationUtils.loadAnimation(this, R.anim.anim_7_2);
		loadAnimation8_2 = AnimationUtils.loadAnimation(this, R.anim.anim_8_2);
		loadAnimation9_2 = AnimationUtils.loadAnimation(this, R.anim.anim_9_2);
		loadAnimation10_2 = AnimationUtils
				.loadAnimation(this, R.anim.anim_10_2);

		loadAnimation11.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				startSeedResultAnim();
			}
		});

		iv1 = (ImageView) findViewById(R.id.iv_1);
		iv2 = (ImageView) findViewById(R.id.iv_2);
		iv3 = (ImageView) findViewById(R.id.iv_3);
		iv4 = (ImageView) findViewById(R.id.iv_4);
		iv5 = (ImageView) findViewById(R.id.iv_5);
		iv6 = (ImageView) findViewById(R.id.iv_6);
		iv7 = (ImageView) findViewById(R.id.iv_7);
		iv8 = (ImageView) findViewById(R.id.iv_8);
		iv9 = (ImageView) findViewById(R.id.iv_9);
		iv10 = (ImageView) findViewById(R.id.iv_10);
		iv11 = (ImageView) findViewById(R.id.iv_11);

		iv1_1 = (ImageView) findViewById(R.id.iv_1_1);
		iv2_1 = (ImageView) findViewById(R.id.iv_2_1);
		iv3_1 = (ImageView) findViewById(R.id.iv_3_1);
		iv4_1 = (ImageView) findViewById(R.id.iv_4_1);
		iv5_1 = (ImageView) findViewById(R.id.iv_5_1);
		iv6_1 = (ImageView) findViewById(R.id.iv_6_1);
		iv7_1 = (ImageView) findViewById(R.id.iv_7_1);
		iv8_1 = (ImageView) findViewById(R.id.iv_8_1);
		iv9_1 = (ImageView) findViewById(R.id.iv_9_1);
		iv10_1 = (ImageView) findViewById(R.id.iv_10_1);

		iv1_2 = (ImageView) findViewById(R.id.iv_1_2);
		iv2_2 = (ImageView) findViewById(R.id.iv_2_2);
		iv3_2 = (ImageView) findViewById(R.id.iv_3_2);
		iv4_2 = (ImageView) findViewById(R.id.iv_4_2);
		iv5_2 = (ImageView) findViewById(R.id.iv_5_2);
		iv6_2 = (ImageView) findViewById(R.id.iv_6_2);
		iv7_2 = (ImageView) findViewById(R.id.iv_7_2);
		iv8_2 = (ImageView) findViewById(R.id.iv_8_2);
		iv9_2 = (ImageView) findViewById(R.id.iv_9_2);
		iv10_2 = (ImageView) findViewById(R.id.iv_10_2);

		setSeedsInvisible();

		ivDandelion = (ImageView) findViewById(R.id.iv_dandelion);
		ivDandelion.setOnClickListener(this);
		ivDandelionLeft = (ImageView) findViewById(R.id.iv_dandelion_left);
		rlSeedResult = (RelativeLayout) findViewById(R.id.rl_seed_result);

		ivSingleBlow = (ImageView) findViewById(R.id.iv_single_blow);
	}

	private void setSeedsVisible() {
		iv1.setVisibility(View.VISIBLE);
		iv2.setVisibility(View.VISIBLE);
		iv3.setVisibility(View.VISIBLE);
		iv4.setVisibility(View.VISIBLE);
		iv5.setVisibility(View.VISIBLE);
		iv6.setVisibility(View.VISIBLE);
		iv7.setVisibility(View.VISIBLE);
		iv8.setVisibility(View.VISIBLE);
		iv9.setVisibility(View.VISIBLE);
		iv10.setVisibility(View.VISIBLE);
		iv11.setVisibility(View.VISIBLE);

		iv1_1.setVisibility(View.VISIBLE);
		iv2_1.setVisibility(View.VISIBLE);
		iv3_1.setVisibility(View.VISIBLE);
		iv4_1.setVisibility(View.VISIBLE);
		iv5_1.setVisibility(View.VISIBLE);
		iv6_1.setVisibility(View.VISIBLE);
		iv7_1.setVisibility(View.VISIBLE);
		iv8_1.setVisibility(View.VISIBLE);
		iv9_1.setVisibility(View.VISIBLE);
		iv10_1.setVisibility(View.VISIBLE);

		iv1_2.setVisibility(View.VISIBLE);
		iv2_2.setVisibility(View.VISIBLE);
		iv3_2.setVisibility(View.VISIBLE);
		iv4_2.setVisibility(View.VISIBLE);
		iv5_2.setVisibility(View.VISIBLE);
		iv6_2.setVisibility(View.VISIBLE);
		iv7_2.setVisibility(View.VISIBLE);
		iv8_2.setVisibility(View.VISIBLE);
		iv9_2.setVisibility(View.VISIBLE);
		iv10_2.setVisibility(View.VISIBLE);
	}

	private void setSeedsInvisible() {
		iv1.setVisibility(View.INVISIBLE);
		iv2.setVisibility(View.INVISIBLE);
		iv3.setVisibility(View.INVISIBLE);
		iv4.setVisibility(View.INVISIBLE);
		iv5.setVisibility(View.INVISIBLE);
		iv6.setVisibility(View.INVISIBLE);
		iv7.setVisibility(View.INVISIBLE);
		iv8.setVisibility(View.INVISIBLE);
		iv9.setVisibility(View.INVISIBLE);
		iv10.setVisibility(View.INVISIBLE);
		iv11.setVisibility(View.INVISIBLE);

		iv1_1.setVisibility(View.INVISIBLE);
		iv2_1.setVisibility(View.INVISIBLE);
		iv3_1.setVisibility(View.INVISIBLE);
		iv4_1.setVisibility(View.INVISIBLE);
		iv5_1.setVisibility(View.INVISIBLE);
		iv6_1.setVisibility(View.INVISIBLE);
		iv7_1.setVisibility(View.INVISIBLE);
		iv8_1.setVisibility(View.INVISIBLE);
		iv9_1.setVisibility(View.INVISIBLE);
		iv10_1.setVisibility(View.INVISIBLE);

		iv1_2.setVisibility(View.INVISIBLE);
		iv2_2.setVisibility(View.INVISIBLE);
		iv3_2.setVisibility(View.INVISIBLE);
		iv4_2.setVisibility(View.INVISIBLE);
		iv5_2.setVisibility(View.INVISIBLE);
		iv6_2.setVisibility(View.INVISIBLE);
		iv7_2.setVisibility(View.INVISIBLE);
		iv8_2.setVisibility(View.INVISIBLE);
		iv9_2.setVisibility(View.INVISIBLE);
		iv10_2.setVisibility(View.INVISIBLE);
	}

	private void startAnim() {
		Parameter.isblow = false;

		if (mediaPlayer != null) {
			mediaPlayer.start();
		}

		ivDandelion.startAnimation(alphaAnimation);
		ivDandelion.setVisibility(View.INVISIBLE);
		ivDandelionLeft.setVisibility(View.VISIBLE);
		ivDandelionLeft.startAnimation(alphaAnimation2);

		canRecord = false;

		setSeedsVisible();

		iv1.startAnimation(loadAnimation1);
		iv2.startAnimation(loadAnimation2);
		iv3.startAnimation(loadAnimation3);
		iv4.startAnimation(loadAnimation4);
		iv5.startAnimation(loadAnimation5);
		iv6.startAnimation(loadAnimation6);
		iv7.startAnimation(loadAnimation7);
		iv8.startAnimation(loadAnimation8);
		iv9.startAnimation(loadAnimation9);
		iv10.startAnimation(loadAnimation10);
		iv11.startAnimation(loadAnimation11);

		iv1_1.startAnimation(loadAnimation1_1);
		iv2_1.startAnimation(loadAnimation2_1);
		iv3_1.startAnimation(loadAnimation3_1);
		iv4_1.startAnimation(loadAnimation4_1);
		iv5_1.startAnimation(loadAnimation5_1);
		iv6_1.startAnimation(loadAnimation6_1);
		iv7_1.startAnimation(loadAnimation7_1);
		iv8_1.startAnimation(loadAnimation8_1);
		iv9_1.startAnimation(loadAnimation9_1);
		iv10_1.startAnimation(loadAnimation10_1);

		iv1_2.startAnimation(loadAnimation1_2);
		iv2_2.startAnimation(loadAnimation2_2);
		iv3_2.startAnimation(loadAnimation3_2);
		iv4_2.startAnimation(loadAnimation4_2);
		iv5_2.startAnimation(loadAnimation5_2);
		iv6_2.startAnimation(loadAnimation6_2);
		iv7_2.startAnimation(loadAnimation7_2);
		iv8_2.startAnimation(loadAnimation8_2);
		iv9_2.startAnimation(loadAnimation9_2);
		iv10_2.startAnimation(loadAnimation10_2);

		setSeedsInvisible();
	}

	private void startSeedResultAnim() {
		rlSeedResult.setVisibility(View.VISIBLE);
		rlSeedResult.startAnimation(scaleAnimation);
	}

	private void disappearSeedResultAnim() {
		rlSeedResult.startAnimation(scaleAnimation2);
		rlSeedResult.setVisibility(View.INVISIBLE);
	}

	private void startSingleBlowAnim() {
		ivSingleBlow.setVisibility(View.VISIBLE);
		ivSingleBlow.startAnimation(singleBlowAnim);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_title:
			// tt = new RecordThread(r); // 点击按钮，启动线程
			// tt.start();
			tvTitle.setEnabled(false);
			startAnim();
			// startSeedResultAnim();
			// disappearSeedResultAnim();
			break;
		case R.id.iv_back:
			finish();
			break;
		case R.id.btn_let_blow:
			rlResultControl.startAnimation(alphaAnimation);
			rlResultControl.setVisibility(View.GONE);
			disappearSeedResultAnim();
			break;
		case R.id.btn_enter_note:
			Intent intent = new Intent();
			intent.putExtra("note_info", mNoteInfo);// 没有微博uids
			intent.setClass(this, NoteInfoActivity.class);
			startActivity(intent);

			reset();
			break;
		case R.id.iv_dandelion:
			ivDandelion.setEnabled(false);
			startAnim();
			break;
		default:
			break;
		}
	}

	private void reset() {
		rlResultControl.setVisibility(View.GONE);
		rlSeedResult.setVisibility(View.INVISIBLE);
		ivDandelion.setVisibility(View.VISIBLE);
		ivDandelion.setEnabled(true);
		ivDandelionLeft.setVisibility(View.INVISIBLE);
		tvTitle.setEnabled(true);

		canRecord = true;
	}

	private void startRecord() {
		Logger.d(TAG, "start record...");
		tt = new RecordThread(mHandler); // 点击按钮，启动线程
		tt.start();
	}

	private void stopRecord() {
		Parameter.isblow = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopRecord();

		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}



}
