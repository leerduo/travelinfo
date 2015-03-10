package com.ustc.dystu.dandelion.net;

import android.os.Handler;
import android.os.Message;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.ustc.dystu.dandelion.fragment.BaseFragment;

public abstract class DandRequestListener implements RequestListener {

	private Handler mHandler;

	public DandRequestListener(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void onComplete(String arg0) {

	}

	@Override
	public void onWeiboException(WeiboException arg0) {
		arg0.printStackTrace();
		Message msg = Message.obtain();
		msg.what = BaseFragment.ERROR_RESPONSE;
		msg.obj = "网络异常";

		mHandler.sendMessage(msg);
	}

	public void onCache(String res) {
		
	}

}
