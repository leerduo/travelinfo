package com.ustc.dystu.dandelion;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.ustc.dystu.dandelion.bean.FootInfo;

public class FootMapActivity extends Activity {

	MapView mMapView = null;

	ArrayList<FootInfo> mFootList;

	private BaiduMap baiduMap;

	private Marker marker;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_foot_map);

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		
		

		baiduMap = mMapView.getMap();

		mFootList = (ArrayList<FootInfo>) getIntent().getSerializableExtra(
				"footList");

		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(5);
		baiduMap.animateMapStatus(mapStatusUpdate);
		
		initMaker();
		
	}

	private void initMaker() {

		for (FootInfo info : mFootList) {
			if (info.geo != null) {

				Double lat = Double.valueOf(info.geo.latitude);
				Double lon = Double.valueOf(info.geo.longitude);

				// 定义Maker坐标点
				LatLng point = new LatLng(lat,lon);
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_gcoding);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).zIndex(19);
				marker = (Marker)baiduMap.addOverlay(option);
				
			}
		}
		
		
		mMapView.invalidate();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

}
