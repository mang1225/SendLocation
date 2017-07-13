package com.ricky.sendlocation.util;

import android.util.Log;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.ricky.sendlocation.bean.LocationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RICKY on 2016/1/21.
 */
public class SearchPoiUtil {

  public static PoiSearch mPoiSearch = null;
  public static PoiSearchListener mPoiSearchListener = null;

  public interface PoiSearchListener {

    void onGetSucceed(List<LocationBean> locationList, PoiResult res);

    void onGetFailed();
  }

  public static void getPoiByPoiSearch(String cityName, String keyName,
      int pageNum, PoiSearchListener listener) {
    mPoiSearchListener = listener;
    if (cityName == null || keyName == null) {
      if (mPoiSearchListener != null) {
        mPoiSearchListener.onGetFailed();
      }
      return;
    }
    if (mPoiSearch == null) {
      mPoiSearch = PoiSearch.newInstance();
    }
    mPoiSearch.setOnGetPoiSearchResultListener(new MyPoiSearchListener());
    mPoiSearch.searchInCity((new PoiCitySearchOption()).city(cityName)
        .keyword(keyName).pageNum(pageNum));
  }

  public static class MyPoiSearchListener implements OnGetPoiSearchResultListener {


    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
      if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        return;
      }
    }

    @Override
    public void onGetPoiResult(PoiResult res) {
      if (res == null
          || res.error == SearchResult.ERRORNO.RESULT_NOT_FOUND
          || res.getAllPoi() == null) {
        if (mPoiSearchListener != null) {
          mPoiSearchListener.onGetFailed();
        }
        return;
      }
      List<LocationBean> searchPoiList = new ArrayList<LocationBean>();
      if (res.getAllPoi() != null) {
        for (PoiInfo info : res.getAllPoi()) {
          LocationBean cityPoi = new LocationBean();
          cityPoi.setAddStr(info.address);
          cityPoi.setCity(info.city);
          cityPoi.setLatitude(info.location.latitude);
          cityPoi.setLongitude(info.location.longitude);
          cityPoi.setUid(info.uid);
          cityPoi.setLocName(info.name);
          searchPoiList.add(cityPoi);
          Log.i("huan", "lat==" + info.location.latitude + "--lon=="
              + info.location.longitude + "--热点名==" + info.name);
        }
      }
      if (mPoiSearchListener != null) {
        mPoiSearchListener.onGetSucceed(searchPoiList, res);
      }
    }
  }

}
