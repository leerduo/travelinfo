package com.ustc.dystu.dandelion.fragment;

import com.ustc.dystu.dandelion.BlowActivity;
import com.ustc.dystu.dandelion.FriendsActivity;
import com.ustc.dystu.dandelion.LatestTravelNoteActivity;
import com.ustc.dystu.dandelion.NearActivity;
import com.ustc.dystu.dandelion.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class DiscoverFragment extends BaseFragment implements OnClickListener{
	
	RelativeLayout llBlow;
	RelativeLayout llNew;
	RelativeLayout llNear;
	RelativeLayout llFriends;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_discover, null);
		
		llBlow = (RelativeLayout) view.findViewById(R.id.ll_blow);
		llBlow.setOnClickListener(this);
		llNew = (RelativeLayout) view.findViewById(R.id.ll_new);
		llNew.setOnClickListener(this);
		llNear = (RelativeLayout) view.findViewById(R.id.ll_near);
		llNear.setOnClickListener(this);
		llFriends = (RelativeLayout) view.findViewById(R.id.ll_friends);
		llFriends.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_blow:
			startActivity(new Intent(getActivity(), BlowActivity.class));
			break;
		case R.id.ll_new:
			startActivity(new Intent(getActivity(),
					LatestTravelNoteActivity.class));
			break;
		case R.id.ll_near:
			startActivity(new Intent(getActivity(), NearActivity.class));
			break;
		case R.id.ll_friends:
			startActivity(new Intent(getActivity(), FriendsActivity.class));
			break;
		default:
			break;
		}
		
	}

}
