package com.ricky.sendlocation.util;

import android.view.View;
import android.view.animation.AlphaAnimation;


public class ViewUtils {

  public static void goneView(View view) {
    goneView(view, true);
  }

  public static void goneView(final View view, boolean isAnimated) {
    goneView(view, isAnimated, true);
  }

  public static void goneView(final View view, boolean isAnimated, boolean isSlow) {
    if (view == null) {
      return;
    }
    if (isAnimated) {
      if (view.getVisibility() != View.GONE) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(isSlow ? 300 : 150);
        alphaAnimation.setInterpolator(MaterialInterpolator.getInstance());
        view.clearAnimation();
        view.startAnimation(alphaAnimation);
        view.setVisibility(View.GONE);
      }
    } else {
      view.setVisibility(View.GONE);
    }
  }

  public static void showView(View view) {
    showView(view, true);
  }

  public static void showView(final View view, boolean isAnimated) {
    showView(view, isAnimated, true);
  }

  public static void showView(final View view, boolean isAnimated, boolean isSlow) {
    if (view == null) {
      return;
    }

    if (isAnimated) {
      if (view.getVisibility() != View.VISIBLE) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(isSlow ? 300 : 150);
        alphaAnimation.setInterpolator(MaterialInterpolator.getInstance());
        view.clearAnimation();
        view.startAnimation(alphaAnimation);
        view.setVisibility(View.VISIBLE);
      }
    } else {
      view.setVisibility(View.VISIBLE);
    }
  }
}
