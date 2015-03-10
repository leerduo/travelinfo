package com.ustc.dystu.dandelion.app;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.ustc.dystu.dandelion.utils.Logger;

import android.app.Application;

public class DandelionApplication extends Application {
	
	public static final String TAG = "DandelionApplication";

	private static DandelionApplication mInstance = null;
	
	public LocationClient mLocationClient = null;
	
	public MyLocationListener myListener;
	
	public static double latitude;
	
	public static double longtitude;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mLocationClient = new LocationClient(getApplicationContext());
		myListener = new MyLocationListener();
		mLocationClient.registerLocationListener(myListener);
	}
	
	
	public static DandelionApplication getInstance(){
		return mInstance;
	}
	
	public class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			latitude = location.getLatitude();
			longtitude = location.getLongitude();
			Logger.i(TAG, "latitude=" + latitude + ";longitude="+longtitude);
		}
		
	}

}
