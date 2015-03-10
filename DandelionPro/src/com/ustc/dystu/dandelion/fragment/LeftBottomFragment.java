package com.ustc.dystu.dandelion.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.waps.AppConnect;

import com.ustc.dystu.dandelion.AboutActivity;
import com.ustc.dystu.dandelion.R;
import com.ustc.dystu.dandelion.RobotFragmentActivity;
import com.ustc.dystu.dandelion.SplashActivity;
import com.ustc.dystu.dandelion.atk.AccessTokenKeeper;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.utils.image.RoundedImageView;


/**
 * 
 * 侧滑相关代码---需要完善
 * 
 * @author Administrator
 *
 */
public class LeftBottomFragment extends Fragment {

	private RoundedImageView avatar;
	private TextView name;
	private LinearLayout more_ll_waps;
	private LinearLayout ll_robot;
	private Button btn_logout;
	private LinearLayout adll_waps;
	private LinearLayout miniadll_waps;
	private LinearLayout ll_feedback;
	private LinearLayout ll_about;
	
	private NoteInfo mNoteInfo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater
				.inflate(R.layout.nav_slidingmenu,null);
		avatar = (RoundedImageView) view.findViewById(R.id.avatar);
		name = (TextView) view.findViewById(R.id.name);
		more_ll_waps = (LinearLayout) view
				.findViewById(R.id.more_ll_waps);
		ll_robot = (LinearLayout) view.findViewById(R.id.ll_robot);
		btn_logout = (Button) view.findViewById(R.id.btn_logout);
		ll_feedback = (LinearLayout) view.findViewById(R.id.ll_feedback);
		ll_about = (LinearLayout) view.findViewById(R.id.ll_about);
		adll_waps = (LinearLayout) view.findViewById(R.id.adll_waps);
		AppConnect.getInstance(getActivity()).showBannerAd(getActivity(), adll_waps);
		miniadll_waps = (LinearLayout) view.findViewById(R.id.miniadll_waps);
		AppConnect.getInstance(getActivity()).showMiniAd(getActivity(), miniadll_waps, 6);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		btn_logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AccessTokenKeeper.clear(getActivity());

				startActivity(new Intent(getActivity(), SplashActivity.class));
				getActivity().finish();
			}
		});

		ll_robot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(getActivity(),
						RobotFragmentActivity.class));
			}
		});
		
		more_ll_waps.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppConnect.getInstance(getActivity()).showAppOffers(getActivity());
			}
		});
		
		ll_feedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppConnect.getInstance(getActivity()).showFeedback(getActivity());
			}
		});
		ll_about.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(),
						AboutActivity.class));
			}
		});
	}
	
	/*private void updateHeaderView(UserInfo info) {
		if (info != null && mNoteInfo != null) {

			ImageFetcher mImageWorker = new ImageFetcher(getActivity(),
					200);

			if (!TextUtils.isEmpty(info.profile_image_url)) {
				String url = info.profile_image_url.replace("/50/", "/180/");
				mImageWorker.loadImage(url, avatar, R.drawable.icon_vdisk,
						true);
			} else {
				avatar.setImageResource(R.drawable.icon_vdisk);
			}
		}
	}*/

}
