package com.ustc.dystu.dandelion;

import com.ustc.dystu.dandelion.fragment.FriendsFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FriendsActivity extends FragmentActivity {

	private static final String FRIENDS_FRAGMENT_TAG = "friends_fragment_tag";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_friends);
		if (findViewById(R.id.fragment_container) != null) {
			try {
				FriendsFragment fragment = new FriendsFragment();
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, fragment,
								FRIENDS_FRAGMENT_TAG).commitAllowingStateLoss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
