package com.ustc.dystu.dandelion;

import com.ustc.dystu.dandelion.fragment.NearFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class NearActivity extends FragmentActivity {
	

	private static final String NEAR_FRAGMENT_TAG = "near_fragment_tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_near);

		if (findViewById(R.id.fragment_container) != null) {
			try {
				NearFragment fragment = new NearFragment();
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, fragment,
								NEAR_FRAGMENT_TAG).commitAllowingStateLoss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

}
