package com.ustc.dystu.dandelion;

import java.text.SimpleDateFormat;

import cn.waps.AppConnect;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.ustc.dystu.dandelion.atk.AccessTokenKeeper;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.SharePrefUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.internal.widget.AppCompatPopupWindow;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * 
 * APP_ID:3a0bf7dd24f36940eecb5a2f2a48c82f
 * 
 * 
 * @author Administrator
 *
 */

public class SplashActivity extends Activity {

	private static final String TAG = "SplashActivity";
	private AuthInfo mAuthInfo;
	private Oauth2AccessToken mAccessToken;
	private SsoHandler mSsoHandler;

	private Button btnLogin;
	private EditText etTest;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			startActivity(new Intent(SplashActivity.this, MainActivity.class));
			finish();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		AppConnect.getInstance("3a0bf7dd24f36940eecb5a2f2a48c82f", "baidu", this);
		
		
		btnLogin = (Button) findViewById(R.id.btn_login);
		etTest = (EditText) findViewById(R.id.editText1);
		mAuthInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		mSsoHandler = new SsoHandler(SplashActivity.this, mAuthInfo);
		
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				mSsoHandler.authorize(new AuthDialogListener());
			}
		});

        mAccessToken = AccessTokenKeeper.readAccessToken(this);
		if (mAccessToken.isSessionValid()) {
			String date = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
					.format(new java.util.Date(mAccessToken.getExpiresTime()));
			btnLogin.setVisibility(View.GONE);
			mHandler.sendEmptyMessageDelayed(0, 1000);
		} else {
			btnLogin.setVisibility(View.VISIBLE);
			Animation alphaAnimation = AnimationUtils.loadAnimation(this,
					R.anim.anim_alpha2);
			btnLogin.startAnimation(alphaAnimation);
		}

	}

	
	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			String uid = values.getString("uid");
			Logger.d(TAG, "weibo uid-->" + uid);

			String myuid = etTest.getText().toString();

			if (!TextUtils.isEmpty(myuid)) {
				SharePrefUtils.setUid(SplashActivity.this, myuid);
			} else {
				SharePrefUtils.setUid(SplashActivity.this, uid);
			}

			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
						.format(new java.util.Date(mAccessToken
								.getExpiresTime()));
				

				AccessTokenKeeper.writeAccessToken(SplashActivity.this,
						mAccessToken);

				startActivity(new Intent(SplashActivity.this,
						MainActivity.class));
				Toast.makeText(SplashActivity.this, "认证成功", Toast.LENGTH_SHORT)
						.show();

				finish();
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + arg0.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
}
