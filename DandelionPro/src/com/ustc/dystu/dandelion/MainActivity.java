package com.ustc.dystu.dandelion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.ustc.dystu.dandelion.adwaps.QuitPopAd;
import com.ustc.dystu.dandelion.app.DandelionApplication;
import com.ustc.dystu.dandelion.atk.AccessTokenKeeper;
import com.ustc.dystu.dandelion.fragment.BaseFragmentTabHost;
import com.ustc.dystu.dandelion.fragment.DiscoverFragment;
import com.ustc.dystu.dandelion.fragment.LeftBottomFragment;
import com.ustc.dystu.dandelion.fragment.MeFragment;
import com.ustc.dystu.dandelion.fragment.TravelFragment;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.Logger;

public class MainActivity extends SlidingFragmentActivity implements
		OnTabChangeListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private BaseFragmentTabHost mTabHost;

	private static final String TRAVEL = "travel";
	private static final String ME = "me";
	private static final String DISCOVER = "discover";

	private View viewTravel;
	private View viewMe;
	private View viewDiscover;

	private LocationClient LocationClient;

	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "gcj02";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Logger.d(TAG, "savedInstanceState is not null");
			savedInstanceState.remove("android:support:fragments");
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initsm();

		mTabHost = (BaseFragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		initViews();
		mTabHost.addTab(mTabHost.newTabSpec(TRAVEL).setIndicator(viewTravel),
				TravelFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(DISCOVER)
				.setIndicator(viewDiscover), DiscoverFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(ME).setIndicator(viewMe),
				MeFragment.class, null);

		mTabHost.getTabWidget().setVisibility(View.VISIBLE);

		mTabHost.setOnTabChangedListener(this);

		mTabHost.setCurrentTab(0);

		onTabChanged(TRAVEL);

		startLocation();

		

	}

	private void initsm() {
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new LeftBottomFragment()).commit();
		
		
		
		//getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftBottomFragment(), "TAG").commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		
		
	}

	View bottomSelect;
	View bottomDiscover;
	View bottomMe;

	private void initViews() {
		viewTravel = View.inflate(MainActivity.this, R.layout.tab_main, null);
		((TextView) viewTravel.findViewById(R.id.tab_textview_title))
				.setText(R.string.tab_travel);
		((ImageView) viewTravel.findViewById(R.id.tab_imageview_icon))
				.setImageResource(R.drawable.tab_travel);
		bottomSelect = viewTravel.findViewById(R.id.view_bottom_line);

		viewDiscover = View.inflate(MainActivity.this, R.layout.tab_main, null);
		((TextView) viewDiscover.findViewById(R.id.tab_textview_title))
				.setText(R.string.tab_discover);
		((ImageView) viewDiscover.findViewById(R.id.tab_imageview_icon))
				.setImageResource(R.drawable.tab_discover);

		bottomDiscover = viewDiscover.findViewById(R.id.view_bottom_line);

		viewMe = View.inflate(MainActivity.this, R.layout.tab_main, null);
		((TextView) viewMe.findViewById(R.id.tab_textview_title))
				.setText(R.string.tab_me);
		((ImageView) viewMe.findViewById(R.id.tab_imageview_icon))
				.setImageResource(R.drawable.tab_me);
		bottomMe = viewMe.findViewById(R.id.view_bottom_line);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout:
			AccessTokenKeeper.clear(this);
			DandelionAPI.getInstance(this).logout();
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startLocation() {
		LocationClient = ((DandelionApplication) getApplication()).mLocationClient;
		LocationClient.start();
		if (LocationClient != null && LocationClient.isStarted()) {
			InitLocation();
			LocationClient.requestLocation();
		}
	}

	@Override
	protected void onDestroy() {
		if (LocationClient != null) {
			LocationClient.stop();
		}
		super.onDestroy();
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);// 设置定位模式
		option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
		int span = 1000;
		option.setScanSpan(span);// 设置发起定位请求的间隔时间
		LocationClient.setLocOption(option);
	}

	@Override
	public void onTabChanged(String tabId) {
		FragmentManager manager = getSupportFragmentManager();

		// getBackStackEntryCount()返回堆栈的总数目
		if (manager.getBackStackEntryCount() > 0) {
			manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			Logger.d(TAG, "哈哈,popBackStack执行了");
		}

		if (TRAVEL.equals(tabId)) {
			bottomSelect.setVisibility(View.VISIBLE);
			bottomMe.setVisibility(View.INVISIBLE);
			bottomDiscover.setVisibility(View.INVISIBLE);
		} else if (ME.equals(tabId)) {
			bottomSelect.setVisibility(View.INVISIBLE);
			bottomMe.setVisibility(View.VISIBLE);
			bottomDiscover.setVisibility(View.INVISIBLE);
		} else if (DISCOVER.equals(tabId)) {
			bottomSelect.setVisibility(View.INVISIBLE);
			bottomMe.setVisibility(View.INVISIBLE);
			bottomDiscover.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
				// 调用退屏广告
				QuitPopAd.getInstance().show(this);
			
			
		}
		return true;
	}

}
