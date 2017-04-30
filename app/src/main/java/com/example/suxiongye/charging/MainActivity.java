package com.example.suxiongye.charging;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.suxiongye.bean.Charging;
import com.example.suxiongye.util.ChargingData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private Dialog selectDialog, loadingDialog;

    //百度地图
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient = null;
    private BDLocationListener mListener = new MyLocationListener();

    private BDLocation loc;
    private int mDistance = 3000;

    private Charging mCharging = null;
    private ArrayList<Charging> mList;
    private Marker lastMarker = null;


    private Toast mToast;

    ImageView iv_list, iv_loc;
    TextView tv_title_right, tv_name, tv_status, tv_used;
    private LinearLayout ll_summary;
    private ChargingData chargingData = null;

    public static final int SHOW_RESPONSE = 0;
    public static final int SHOW_CHARGING = 1;

    //获取所有充电桩
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //获取请求结果
                case SHOW_RESPONSE:
                    String result = (String) msg.obj;
                    //loadingDialog.dismiss();
                    showToast(result);
                    break;
                //获取充电桩
                case SHOW_CHARGING:
                    mList = (ArrayList<Charging>) msg.obj;
                    setMarker(mList);
                    //loadingDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mContext = this;
        chargingData = new ChargingData(handler);
        initView();
        //setContentView(R.layout.activity_main);
        //mMapView = (MapView) findViewById(R.id.bmapView);
    }

    private void initView() {
        //界面初始化
        iv_list = (ImageView) findViewById(R.id.iv_list);
        iv_list.setOnClickListener(this);
        iv_loc = (ImageView) findViewById(R.id.iv_loc);
        iv_loc.setOnClickListener(this);

        //设置可选距离
        tv_title_right = (TextView) findViewById(R.id.tv_title_button);
        tv_title_right.setText("3km" + ">");
        tv_title_right.setVisibility(View.VISIBLE);
        tv_title_right.setOnClickListener(this);

        //设置充电桩详细界面
        ll_summary = (LinearLayout) findViewById(R.id.ll_summary);
        ll_summary.setOnClickListener(this);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_used = (TextView) findViewById(R.id.tv_used);

        //百度地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
        mBaiduMap.setMyLocationEnabled(true);
        //定位
        mLocationClient = new LocationClient(mContext);
        mLocationClient.registerLocationListener(mListener);

        LocationClientOption option = new LocationClientOption();
        //高精度模式
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //坐标系
        option.setCoorType("bd0911");
        //扫描间隔
        option.setScanSpan(0);
        //是否需要地址信息
        option.setIsNeedAddress(true);
        //是否需要设备方向
        option.setNeedDeviceDirect(true);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (location == null) {
                return;
            }
            loc = location;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            searchCharging(location.getLatitude(), location.getLongitude(), mDistance);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        handler = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mLocationClient.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.stop();
    }



    public void setMarker(ArrayList<Charging> list) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.marker, null);
        final TextView tv = (TextView) view.findViewById(R.id.tv_marker);
        for (int i = 0; i < list.size(); i++) {
            Charging charging = list.get(i);
            tv.setText((i + 1) + "");
            if (i == 0) {
                tv.setBackgroundResource(R.drawable.icon_focus_mark);
            } else {
                tv.setBackgroundResource(R.drawable.icon_mark);
            }
            //设置坐标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(tv);
            LatLng latLng = new LatLng(charging.getLatitude(), charging.getLongitude());
            Bundle b = new Bundle();
            b.putParcelable("c", list.get(i));
            OverlayOptions oo = new MarkerOptions().position(latLng).icon(bitmap).title((i + 1) + "").extraInfo(b);

            if (i == 0) {
                lastMarker = (Marker) mBaiduMap.addOverlay(oo);
                mCharging = charging;
                showLayoutInfo((i + 1) + "", mCharging);
            } else {
                mBaiduMap.addOverlay(oo);
            }
        }

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (lastMarker != null) {
                    tv.setText(lastMarker.getTitle());
                    tv.setBackgroundResource(R.drawable.icon_mark);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(tv);
                    lastMarker.setIcon(bitmap);
                }
                lastMarker = marker;
                String position = marker.getTitle();
                tv.setText(position);
                tv.setBackgroundResource(R.drawable.icon_focus_mark);
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(tv);
                marker.setIcon(bitmap);
                mCharging = marker.getExtraInfo().getParcelable("c");
                showLayoutInfo(position, mCharging);
                return false;
            }
        });
    }

    public void showLayoutInfo(String position, Charging charging) {
        tv_name.setText(position + "." + charging.getName());
        if (charging.getStatus().equals("normal")) tv_status.setText("正常");
        else tv_status.setText("故障中");
        if (charging.getUsed().equals("used")) tv_used.setText("使用中");
        else tv_used.setText("可使用");
        ll_summary.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_list:

                break;
            //单击返回定位点
            case R.id.iv_loc:
                int r = mLocationClient.requestLocation();
                switch (r) {
                    case 1:
                        showToast("服务没有启动。");
                        break;
                    case 2:
                        showToast("没有监听函数。");
                        break;
                    case 6:
                        showToast("请求间隔果断");
                        break;
                    default:
                        break;
                }
                break;
            case R.id.tv_title_button:
                showSelectDialog();
                break;
            //跳转到详情
            case R.id.ll_summary:
                Intent infoIntent = new Intent(mContext, ChargingInfoActivity.class);
                infoIntent.putExtra("c", mCharging);
                infoIntent.putExtra("locLat", loc.getLatitude());
                infoIntent.putExtra("locLong", loc.getLongitude());
                startActivity(infoIntent);
                break;
            default:
                break;
        }
    }


    /**
     * 显示范围选择dialog
     */
    @SuppressLint("InflateParams")
    private void showSelectDialog() {
        if (selectDialog != null) {
            selectDialog.show();
            return;
        }
        selectDialog = new Dialog(mContext, R.style.dialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_distance, null);
        selectDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        selectDialog.setCanceledOnTouchOutside(true);
        selectDialog.show();
    }
//
//    @SuppressLint("InflateParams")
//    private void showLoadingDialog() {
//        if (loadingDialog != null) {
//            loadingDialog.show();
//            return;
//        }
//        loadingDialog = new Dialog(mContext, R.style.dialog_loading);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading, null);
//        loadingDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        loadingDialog.setCancelable(false);
//        loadingDialog.show();
//    }

    /**
     * 显示通知
     *
     * @param msg
     */
    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }

    //查询附近充电桩
    public void searchCharging(double lat, double lon, int distance) {
        // showLoadingDialog();
        mBaiduMap.clear();
        ll_summary.setVisibility(View.GONE);
        chargingData.getChargingData();
    }

    /**
     * dialog点击事件
     *
     * @param v 点击的view
     */
    public void onDialogClick(View v) {
        switch (v.getId()) {
            case R.id.bt_3km:
                //distanceSearch("3km >", 3000);
                break;
            case R.id.bt_5km:
                //distanceSearch("5km >", 5000);
                break;
            case R.id.bt_8km:
                //distanceSearch("8km >", 8000);
                break;
            case R.id.bt_10km:
                //distanceSearch("10km >", 10000);
                break;
            default:
                break;
        }
    }
}
