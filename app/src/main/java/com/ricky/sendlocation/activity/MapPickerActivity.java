package com.ricky.sendlocation.activity;

import static com.ricky.sendlocation.util.ViewUtils.goneView;
import static com.ricky.sendlocation.util.ViewUtils.showView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.ricky.sendlocation.AppContext;
import com.ricky.sendlocation.R;
import com.ricky.sendlocation.adapter.MapPickerAdapter;
import com.ricky.sendlocation.adapter.MapPickerSearchAdapter;
import com.ricky.sendlocation.bean.LocationBean;
import com.ricky.sendlocation.service.LocationService;
import com.ricky.sendlocation.util.SearchPoiUtil;
import java.util.ArrayList;
import java.util.List;


public class MapPickerActivity extends AppCompatActivity
    implements
    AdapterView.OnItemClickListener,
    AbsListView.OnScrollListener {

  private static final String LOG_TAG = "MapPickerActivity";

  private ListView list;
  private TextView status;
  private ProgressBar loading;
  private View defineMyLocationButton;

  //百度地图相关
  private LocationService locationService;
  private MapView mMapView;
  private BaiduMap mBaiduMap;
  // 当前经纬度和地理信息
  private LatLng mLoactionLatLng;
  private String mAddress;
  private String mStreet;
  private String mName;
  private String mCity;
  // 设置第一次定位标志
  private boolean isFirstLoc = true;
  // MapView中央对于的屏幕坐标
  private Point mCenterPoint = null;
  // 地理编码
  private GeoCoder mGeoCoder = null;
  // 位置列表
  MapPickerAdapter mAdapter;
  ArrayList<PoiInfo> mInfoList;
  PoiInfo mCurentInfo;


  private ListView searchList;
  private View searchContainer;
  private TextView searchEmptyView;
  private TextView searchHintView;

  private boolean isSearchVisible = false;
  private MapPickerSearchAdapter searchAdapter;
  private List<LocationBean> searchDisplay;
  private SearchView searchView;
  private MenuItem searchMenu;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.picker_activity_map_picker);
    initMap();
    getSupportActionBar().setTitle(R.string.map_send_text);
    getSupportActionBar().setDisplayShowHomeEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowTitleEnabled(true);
    getSupportActionBar().setDisplayShowCustomEnabled(false);
    findViewById(R.id.root).setBackgroundColor(Color.parseColor("#ffffff"));

    defineMyLocationButton = findViewById(R.id.define_my_location);
    defineMyLocationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        turnBack();
      }
    });
    searchList = (ListView) findViewById(R.id.searchList);

    searchContainer = findViewById(R.id.searchCont);
    searchContainer.setBackgroundColor(Color.parseColor("#ffffff"));
    searchEmptyView = (TextView) findViewById(R.id.empty);
    searchHintView = (TextView) findViewById(R.id.searchHint);
    searchEmptyView.setTextColor(Color.parseColor("#7A000000"));
    searchHintView.setTextColor(Color.parseColor("#7A000000"));
    searchHintView.setVisibility(View.GONE);
    searchEmptyView.setVisibility(View.GONE);
    searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        BitmapDescriptor mSelectIco = BitmapDescriptorFactory
            .fromResource(R.drawable.picker_map_geo_icon);
        mBaiduMap.clear();
        LocationBean info = (LocationBean) searchAdapter.getItem(position);
        LatLng la = new LatLng(info.getLatitude(), info.getLongitude());
        // 动画跳转
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(la);
        mBaiduMap.animateMapStatus(u);
        // 添加覆盖物
        OverlayOptions ooA = new MarkerOptions().position(la)
            .icon(mSelectIco).anchor(0.5f, 0.5f);
        mBaiduMap.addOverlay(ooA);
        mLoactionLatLng = la;
        mAddress = info.getAddStr();
        mName = info.getLocName();
        mStreet = info.getStreet();
        mCity = info.getCity();
        // 发起反地理编码检索
        mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
            .location(la));
        loading.setVisibility(View.VISIBLE);
        if (isSearchVisible) {
          hideKeyBoard();
          hideSearch();
        }
      }
    });
  }

  private void initMap() {
    //ricky init baidumap begin
    mMapView = (MapView) findViewById(R.id.bmapView);
    mBaiduMap = mMapView.getMap();
    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
    mMapView.showZoomControls(false);
    MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
    mBaiduMap.setMapStatus(msu);
    mBaiduMap.setOnMapTouchListener(touchListener);
    // 初始化POI信息列表
    mInfoList = new ArrayList<PoiInfo>();
    // 初始化当前MapView中心屏幕坐标，初始化当前地理坐标
    mCenterPoint = mBaiduMap.getMapStatus().targetScreen;
    mLoactionLatLng = mBaiduMap.getMapStatus().target;
    // 定位
    mBaiduMap.setMyLocationEnabled(true);
    // 隐藏百度logo ZoomControl
    int count = mMapView.getChildCount();
    for (int i = 0; i < count; i++) {
      View child = mMapView.getChildAt(i);
      if (child instanceof ImageView || child instanceof ZoomControls) {
        child.setVisibility(View.INVISIBLE);
      }
    }
    // 隐藏比例尺
    //mMapView.showScaleControl(false);
    // 地理编码
    mGeoCoder = GeoCoder.newInstance();
    mGeoCoder.setOnGetGeoCodeResultListener(GeoListener);
    list = (ListView) findViewById(R.id.list);
    list.setOnScrollListener(this);
    list.setOnItemClickListener(this);
    list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    loading = (ProgressBar) findViewById(R.id.loading);
    status = (TextView) findViewById(R.id.status);
    mAdapter = new MapPickerAdapter(MapPickerActivity.this, mInfoList);
    list.setAdapter(mAdapter);
  }

  private void showSearch() {
    if (isSearchVisible) {
      return;
    }
    isSearchVisible = true;

    searchDisplay = new ArrayList<>();
    searchAdapter = new MapPickerSearchAdapter(this, searchDisplay);

    searchList.setAdapter(searchAdapter);

    showView(searchHintView, false);
    goneView(searchEmptyView, false);

    showView(searchContainer);
  }

  private void hideSearch() {
    if (!isSearchVisible) {
      return;
    }
    isSearchVisible = false;

    if (searchDisplay != null) {
      searchDisplay.clear();
      searchDisplay = null;
    }
    searchAdapter = null;
    searchList.setAdapter(null);

    goneView(searchContainer);

    if (searchMenu != null) {
      if (searchMenu.isActionViewExpanded()) {
        searchMenu.collapseActionView();
      }
    }
  }

  public void turnBack() {
    MyLocationData location = mBaiduMap.getLocationData();
    // 实现动画跳转
    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(new LatLng(location.latitude, location.longitude));
    mBaiduMap.animateMapStatus(u);
    mBaiduMap.clear();
    // 发起反地理编码检索
    mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
        .location(new LatLng(location.latitude, location.longitude)));

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        onBackPressed();
        break;
    }
    if (id == R.id.menu_send) {
      if (mLoactionLatLng != null) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("latitude", mLoactionLatLng.latitude);
        returnIntent.putExtra("longitude", mLoactionLatLng.longitude);
        returnIntent.putExtra("street", mStreet);
        returnIntent.putExtra("place", mName);
        setResult(RESULT_OK, returnIntent);
        finish();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.picker_map, menu);
    searchMenu = menu.getItem(0);
    searchView = (SearchView) menu.getItem(0).getActionView();
    searchView.setIconifiedByDefault(true);
    MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem item) {
        showSearch();
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem item) {
        hideSearch();
        return true;
      }
    });
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        //fetchPlaces(s);
        if (isSearchVisible) {
          searchPlaces(mCity, s, 0);
          hideKeyBoard();
        }
        return false;
      }

      @Override
      public boolean onQueryTextChange(String s) {
        if (isSearchVisible) {
          if ("".equals(s)) {
            if (searchDisplay != null) {
              searchDisplay.clear();
              searchAdapter.notifyDataSetChanged();
            }
            showView(searchHintView, false);
            goneView(searchEmptyView, false);
          } else {
            searchPlaces(mCity, s, 0);
          }
        }
        return false;
      }
    });
    searchView.setOnCloseListener(new SearchView.OnCloseListener() {
      @Override
      public boolean onClose() {
        hideKeyBoard();
        return false;
      }
    });

    return true;
  }

  @Override
  public void onBackPressed() {
    if (isSearchVisible) {
      hideSearch();
    }
    super.onBackPressed();
  }

  private void searchPlaces(String cityName, final String keyName,
      int pageNum) {
    SearchPoiUtil.getPoiByPoiSearch(cityName,
        keyName, pageNum,
        new SearchPoiUtil.PoiSearchListener() {

          @Override
          public void onGetSucceed(List<LocationBean> locationList,
              PoiResult res) {
            if (keyName.length() > 0) {
              if (locationList.size() > 0) {
                goneView(searchEmptyView);
                goneView(searchHintView);
              } else {
                goneView(searchHintView);
                showView(searchEmptyView);
              }
              if (searchDisplay == null) {
                searchDisplay = new ArrayList<LocationBean>();
              }
              searchDisplay.clear();
              searchDisplay.addAll(locationList);
              searchAdapter.notifyDataSetChanged();
            }
          }

          @Override
          public void onGetFailed() {
            if (searchDisplay != null) {
              searchDisplay.clear();
              searchAdapter.notifyDataSetChanged();
            }
            goneView(searchHintView);
            showView(searchEmptyView);
            Toast.makeText(MapPickerActivity.this, "抱歉，未能找到结果",
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  void hideKeyBoard() {
    searchView.clearFocus();
    this.getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    View focusedView = this.getCurrentFocus();
    if (focusedView != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
    // 通知是适配器第position个item被选择了
    mAdapter.setNotifyTip(position);
    mAdapter.notifyDataSetChanged();
    BitmapDescriptor mSelectIco = BitmapDescriptorFactory
        .fromResource(R.drawable.picker_map_geo_icon);
    mBaiduMap.clear();
    PoiInfo info = (PoiInfo) mAdapter.getItem(position);
    LatLng la = info.location;
    // 动画跳转
    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(la);
    mBaiduMap.animateMapStatus(u);
    // 添加覆盖物
    OverlayOptions ooA = new MarkerOptions().position(la)
        .icon(mSelectIco).anchor(0.5f, 0.5f);
    mBaiduMap.addOverlay(ooA);
    mLoactionLatLng = info.location;
    mAddress = info.address;
    mName = info.name;
    mStreet = info.address;
    mCity = info.city;
  }

  @Override
  public void onScrollStateChanged(AbsListView absListView, int i) {
    switch (i) {
      case SCROLL_STATE_TOUCH_SCROLL:
        hideKeyBoard();
        break;
    }
  }

  @Override
  public void onScroll(AbsListView absListView, int i, int i2, int i3) {

  }

  /***
   * Stop location service
   */
  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    locationService.stop(); // 停止定位服务
    super.onStop();
  }

  @Override
  protected void onStart() {
    // TODO Auto-generated method stub
    super.onStart();
    // -----------location config ------------
    locationService = AppContext.getInstance().locationService;
    // 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
    locationService.registerListener(mListener);
    // 注册监听
    locationService.setLocationOption(locationService.getDefaultLocationClientOption());

  }

  @Override
  protected void onResume() {
    super.onResume();
    //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
    locationService.start();
    mMapView.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
    locationService.unregisterListener(mListener); // 注销掉监听
    locationService.stop();
    mMapView.onDestroy();
    mGeoCoder.destroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
    locationService.stop();
    mMapView.onPause();
    hideSearch();
  }

  /*****
   * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
   */
  private BDLocationListener mListener = new BDLocationListener() {

    @Override
    public void onReceiveLocation(BDLocation location) {
      // TODO Auto-generated method stub
      if (null != location && location.getLocType() != BDLocation.TypeServerError) {
        //

        MyLocationData data = new MyLocationData.Builder()//
            // .direction(mCurrentX)//
            .accuracy(location.getRadius())//
            .latitude(location.getLatitude())//
            .longitude(location.getLongitude())//
            .build();
        mBaiduMap.setMyLocationData(data);
        // 设置自定义图标
        MyLocationConfiguration config = new MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.NORMAL, true, null);
        mBaiduMap.setMyLocationConfigeration(config);
        mAddress = location.getAddrStr();
        mName = location.getStreet();
        mStreet = location.getStreet();
        mCity = location.getCity();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mLoactionLatLng = currentLatLng;
        // 是否第一次定位
        if (isFirstLoc) {
          isFirstLoc = false;
          // 实现动画跳转
          MapStatusUpdate u = MapStatusUpdateFactory
              .newLatLng(currentLatLng);
          mBaiduMap.animateMapStatus(u);
          mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
              .location(currentLatLng));
          return;
        }
      }

    }

  };
  // 地理编码监听器
  OnGetGeoCoderResultListener GeoListener = new OnGetGeoCoderResultListener() {
    public void onGetGeoCodeResult(GeoCodeResult result) {
      if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        // 没有检索到结果
      }
      // 获取地理编码结果
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
      if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        // 没有找到检索结果
        status.setText(R.string.picker_internalerror);
        status.setVisibility(View.VISIBLE);
      }
      // 获取反向地理编码结果
      else {
        status.setVisibility(View.GONE);
        // 当前位置信息
        mLoactionLatLng = result.getLocation();
        mAddress = result.getAddress();
        mName = result.getAddressDetail().street;
        mStreet = result.getAddressDetail().street;
        mCity = result.getAddressDetail().city;
        mCurentInfo = new PoiInfo();
        mCurentInfo.address = result.getAddress();
        mCurentInfo.location = result.getLocation();
        mCurentInfo.name = "[位置]";
        mInfoList.clear();
        mInfoList.add(mCurentInfo);
        // 将周边信息加入表
        if (result.getPoiList() != null) {
          mInfoList.addAll(result.getPoiList());
        }
        mAdapter.setNotifyTip(0);
        // 通知适配数据已改变
        mAdapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);

      }
    }
  };
  // 地图触摸事件监听器
  BaiduMap.OnMapTouchListener touchListener = new BaiduMap.OnMapTouchListener() {
    @Override
    public void onTouch(MotionEvent event) {
      // TODO Auto-generated method stub
      if (event.getAction() == MotionEvent.ACTION_UP) {

        if (mCenterPoint == null) {
          return;
        }
        // 获取当前MapView中心屏幕坐标对应的地理坐标
        LatLng currentLatLng;
        currentLatLng = mBaiduMap.getProjection().fromScreenLocation(
            mCenterPoint);
        // 发起反地理编码检索
        mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
            .location(currentLatLng));
        loading.setVisibility(View.VISIBLE);

      }
    }
  };
}
