package com.ricky.sendlocation.util;

import android.content.res.Resources;

import com.ricky.sendlocation.AppContext;


public class Screen {

  private static float density;
  private static float scaledDensity;

  public static int dp(float dp) {
    if (density == 0f) {
      density = AppContext.getInstance().getResources().getDisplayMetrics().density;
    }

    return (int) (dp * density + .5f);
  }

  public static int sp(float sp) {
    if (scaledDensity == 0f) {
      scaledDensity = AppContext.getInstance().getResources().getDisplayMetrics().scaledDensity;
    }

    return (int) (sp * scaledDensity + .5f);
  }

  public static int getWidth() {
    return AppContext.getInstance().getResources().getDisplayMetrics().widthPixels;
  }

  public static int getHeight() {
    return AppContext.getInstance().getResources().getDisplayMetrics().heightPixels;
  }

  public static int getStatusBarHeight() {

    int result = 0;
    int resourceId = AppContext.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = AppContext.getInstance().getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

  public static int getNavbarHeight() {
    if (hasNavigationBar()) {
      int resourceId = AppContext.getInstance().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
      if (resourceId > 0) {
        return AppContext.getInstance().getResources().getDimensionPixelSize(resourceId);
      }
    }
    return 0;
  }

  public static boolean hasNavigationBar() {
    Resources resources = AppContext.getInstance().getResources();
    int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
    return (id > 0) && resources.getBoolean(id);
  }

  public static float getDensity() {
    return density;
  }
}