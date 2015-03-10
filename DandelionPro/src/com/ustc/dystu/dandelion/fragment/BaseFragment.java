package com.ustc.dystu.dandelion.fragment;

import com.ustc.dystu.dandelion.utils.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseFragment extends Fragment {

	private final String TAG = BaseFragment.class.getSimpleName();
	
	public static final int ERROR_RESPONSE = 0x99;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		setMenuVisibility(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// onAttach(getActivity());
		afterActivityCreated();
		super.onActivityCreated(savedInstanceState);
		onFragmentShow();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.d(TAG, "requestCode:"+requestCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onFragmentShow() {
		Logger.d(TAG, "onFragmentShow: " + this.getClass().getName());
	}

	public void onFragmentHide() {
		Logger.d(TAG, "onFragmentHide: " + this.getClass().getName());
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		Logger.d(TAG, "onHiddenChanged: " + this.getClass().getName());

		if (!hidden) {
			onFragmentShow();
		} else {
			onFragmentHide();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		Logger.d(TAG, "onResume: " + this.getClass().getName());
		super.onResume();
		// onFragmentResume();
	}

	protected void afterActivityCreated() {
	}

	@Override
	public void onPause() {
		Logger.d(TAG, "onPause: " + this.getClass().getName());
		super.onPause();
		// onFragmentPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
