package com.ustc.dystu.dandelion;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.fragment.BaseFragment;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.Utils;

public class EditNoteTitleActivity extends Activity {private static final int REQUEST_UPDATE_NOTE_INFO = 0x1;

ImageView ivBack;
ImageView ivOK;

EditText etTitle;
TextView tvTitle;

NoteInfo mNoteInfo;

ProgressDialog pbProgress;

Handler mHandler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
		pbProgress.dismiss();
		switch (msg.what) {
		case REQUEST_UPDATE_NOTE_INFO:
			Toast.makeText(EditNoteTitleActivity.this, "更新成功!",
					Toast.LENGTH_SHORT).show();

			Intent broadcast = new Intent();
			broadcast.putExtra("note_info", mNoteInfo);
			broadcast.setAction(Constants.ACTION_EDIT_NOTE_SUCCESS);
			sendBroadcast(broadcast);
			finish();
			break;
		case BaseFragment.ERROR_RESPONSE:
			if (msg.obj != null) {
				Toast.makeText(EditNoteTitleActivity.this,
						(String) msg.obj, Toast.LENGTH_SHORT).show();
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

	setContentView(R.layout.activity_edit_note_title);

	ivBack = (ImageView) findViewById(R.id.iv_back);
	ivBack.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	});

	ivOK = (ImageView) findViewById(R.id.iv_add);
	ivOK.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			String content = etTitle.getText().toString();

			if (!TextUtils.isEmpty(content)) {
				if (Utils.getWeiboTextLength(content) > 22) {
					Toast.makeText(EditNoteTitleActivity.this,
							"标题内容太长, 请不要超过22个字符!", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				if (mNoteInfo != null) {
					mNoteInfo.note_title = content;
					updateTitle();
				} else {
					hideInputMethod();

					Intent intent = new Intent();
					intent.putExtra("note_title", content);
					intent.setClass(EditNoteTitleActivity.this,
							AddNoteActivity.class);

					startActivity(intent);

					finish();
				}
			} else {
				Toast.makeText(EditNoteTitleActivity.this, "标题内容不能为空!",
						Toast.LENGTH_SHORT).show();
			}
		}
	});

	etTitle = (EditText) findViewById(R.id.et_title);
	etTitle.setFocusable(true);

	etTitle.requestFocus();
	onFocusChange(etTitle.isFocused());

	tvTitle = (TextView) findViewById(R.id.tv_title);

	mNoteInfo = (NoteInfo) getIntent().getSerializableExtra("note_info");
	if (mNoteInfo != null) {
		etTitle.setText(mNoteInfo.note_title);
		tvTitle.setText("修改标题");
	}
}

private void onFocusChange(final boolean hasFocus) {
	mHandler.postDelayed(new Runnable() {

		@Override
		public void run() {
			InputMethodManager imm = (InputMethodManager) EditNoteTitleActivity.this
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			if (hasFocus) {
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			} else {
				imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
			}

		}
	}, 100);
}

private void updateTitle() {
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

public void hideInputMethod() {
	if (etTitle != null) {
		etTitle.clearFocus();
		InputMethodManager imm = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etTitle.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
}}
