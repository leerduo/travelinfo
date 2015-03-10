package com.ustc.dystu.dandelion.fragment;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.dystu.dandelion.EditNoteTitleActivity;
import com.ustc.dystu.dandelion.NoteInfoActivity;
import com.ustc.dystu.dandelion.R;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.bean.UserInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.net.DandRequestListener;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.CacheUtils;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.SharePrefUtils;
import com.ustc.dystu.dandelion.utils.image.ImageCache;
import com.ustc.dystu.dandelion.utils.image.ImageFetcher;
import com.ustc.dystu.dandelion.utils.image.Utils;

public class MeFragment extends BaseFragment {

	public static final String TAG = "MeFragment";
	public static final int REQUEST_USER_INFO = 0x1;
	private static final int REQUEST_GET_TRAVEL_LIST = 0x2;
	private static final int REQUEST_DELETE_NOTE = 0x3;

	private ImageView ivAddNote;
	private ImageFetcher mImageWorker;

	private TextView tvTitle;

	NoteAdapter mAdapter;

	private ListView lvList;
	ArrayList<NoteInfo> mList = new ArrayList<NoteInfo>();

	private TextView mEmptyView;

	private BroadcastReceiver receiver;

	ProgressDialog pbProgress;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REQUEST_GET_TRAVEL_LIST:
				if (pbProgress != null) {
					pbProgress.dismiss();
				}

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
			case REQUEST_USER_INFO:
				if (msg.obj != null) {
					UserInfo info = (UserInfo) msg.obj;
					tvTitle.setText(info.screen_name);
				}
				break;
			case REQUEST_DELETE_NOTE:
				Toast.makeText(getActivity(), "删除成功!", Toast.LENGTH_SHORT)
						.show();

				int i = (Integer) msg.obj;
				mList.remove(i);

				if (mList.isEmpty()) {
					lvList.setEmptyView(mEmptyView);
				}

				mAdapter.notifyDataSetChanged();
				break;
			case ERROR_RESPONSE:
				if (pbProgress != null) {
					pbProgress.dismiss();
				}
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
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.d(TAG, "onCreateView...");

		View view = inflater.inflate(R.layout.fragment_me, null);
		ivAddNote = (ImageView) view.findViewById(R.id.iv_make_travel_note);
		ivAddNote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(),
						EditNoteTitleActivity.class));
			}
		});

		tvTitle = (TextView) view.findViewById(R.id.tv_title);

		lvList = (ListView) view.findViewById(R.id.lv_travel);
		lvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("note_info", mList.get(position));
				intent.putExtra("isfromMe", true);
				intent.setClass(getActivity(), NoteInfoActivity.class);
				startActivity(intent);
			}
		});

		lvList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showChooseDialog(position);
				return true;
			}
		});

		mAdapter = new NoteAdapter(getActivity());
		lvList.setAdapter(mAdapter);

		mEmptyView = new TextView(getActivity());
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setTextColor(getResources().getColor(R.color.black));
		mEmptyView.setText("轻轻点击，取回你散落微博中的旅程");

		mEmptyView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(),
						EditNoteTitleActivity.class));
			}
		});

		((ViewGroup) lvList.getParent()).addView(mEmptyView);
		mEmptyView.setVisibility(View.GONE);

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Logger.d(TAG, "reciever messge");

				NoteInfo info = (NoteInfo) intent
						.getSerializableExtra("note_info");

				String action = intent.getAction();

				if (action.equals(Constants.ACTION_CREATE_NOTE_SUCCESS)) {
					if (info != null) {
						mList.add(info);
						mAdapter.notifyDataSetChanged();
					}
				} else if (action.equals(Constants.ACTION_EDIT_NOTE_SUCCESS)) {
					if (info != null) {
						mList.remove(info);
						mList.add(info);
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_CREATE_NOTE_SUCCESS);
		intentFilter.addAction(Constants.ACTION_EDIT_NOTE_SUCCESS);
		getActivity().registerReceiver(receiver, intentFilter);

		pbProgress = new ProgressDialog(getActivity());
		pbProgress.setMessage("正在加载, 请稍候...");
		pbProgress.setCanceledOnTouchOutside(false);

		return view;
	}

	@Override
	protected void afterActivityCreated() {
		Logger.d(TAG, "after acitivty created...");
		init();
	}

	private void showChooseDialog(final int position) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setTitle("选择");

		ArrayList<String> menus = new ArrayList<String>();
		menus.add("删除");
		builder.setItems(menus.toArray(new CharSequence[0]),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DandelionAPI.getInstance(getActivity()).deleteNote(
								new DandRequestListener(mHandler) {

									@Override
									public void onComplete(String arg0) {
										Logger.d(TAG, "delete response-->"
												+ arg0);
										Message msg = Message.obtain();
										try {
											JSONObject jo = new JSONObject(arg0);
											String code = jo
													.getString("error_code");

											if (Integer.parseInt(code) == 0) {
												msg.what = REQUEST_DELETE_NOTE;
												msg.obj = position;
											} else {
												msg.what = ERROR_RESPONSE;
												msg.obj = "网络异常";
											}
										} catch (Exception e) {
											e.printStackTrace();
											msg.what = ERROR_RESPONSE;
											msg.obj = "数据解析异常";
										}

										mHandler.sendMessage(msg);

									}
								}, mList.get(position));
					}

				});
		final AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(true);
		alert.getWindow().setGravity(Gravity.CENTER);
		alert.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	private void init() {
		DandelionAPI.getInstance(getActivity()).getUserInfo(
				new DandRequestListener(mHandler) {
					@Override
					public void onComplete(String response) {
						handleUserInfoResult(response, false);
					}

					@Override
					public void onCache(String result) {
						handleUserInfoResult(result, true);
					}
				}, DandelionAPI.getInstance(getActivity()).getUid());

		pbProgress.show();
		DandelionAPI.getInstance(getActivity()).getTravelNotes(
				new DandRequestListener(mHandler) {

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
				}, SharePrefUtils.getUid(getActivity()));
	}

	private void handleUserInfoResult(String response, boolean useCache) {

		Message msg = Message.obtain();
		try {
			if (TextUtils.isEmpty(response) || response.contains("error_code")) {
				msg.what = ERROR_RESPONSE;
				JSONObject obj = new JSONObject(response);
				msg.obj = obj.getString("error");
			} else {
				UserInfo userInfo = UserInfo.create(new JSONObject(response));
				msg.obj = userInfo;
				msg.what = REQUEST_USER_INFO;

				if (!useCache) {
					CacheUtils.updateCache(MeFragment.this.getActivity(),
							CacheUtils.getKey(
									CacheUtils.CACHE_USER_INFO,
									new String[] {
											"uid",
											DandelionAPI.getInstance(
													getActivity()).getUid() }),
							response);
				}
			}

			mHandler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
			msg.what = ERROR_RESPONSE;
			msg.obj = "数据解析异常";
			mHandler.sendMessage(msg);
		}
	}

	class NoteAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ImageFetcher mImageWorker;

		public NoteAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			int[] wh = Utils.getMidPicWidthAndHeight(context);
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
				holder.tvInfo = (TextView) convertView
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

			holder.tvInfo.setText(info.note_title);
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
		public TextView tvInfo;
		public TextView tvTime;
		public TextView tvTimeUsed;
	}
}
