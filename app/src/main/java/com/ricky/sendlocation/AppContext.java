package com.ricky.sendlocation;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.ricky.sendlocation.service.LocationService;

/**
 * Created by RICKY on 2016/2/18.
 */
public class AppContext extends Application {

  public LocationService locationService;
  private static AppContext instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    Fresco.initialize(getApplicationContext());
    SDKInitializer.initialize(getApplicationContext());
    locationService = new LocationService(getApplicationContext());
  }

  public static AppContext getInstance() {
    return instance;
  }
}
