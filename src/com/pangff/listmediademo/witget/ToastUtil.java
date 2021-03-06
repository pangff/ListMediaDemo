package com.pangff.listmediademo.witget;

import android.widget.Toast;

import com.pangff.listmediademo.BaseApplication;

public class ToastUtil {
  static Toast toast;

  public static void hide() {
    if (toast != null) {
      toast.cancel();
    }
  }

  public static void show(String text) {

    showCustomToast(text, Toast.LENGTH_SHORT);
  }


  public static void show(String text, int time) {

    showCustomToast(text, time);
  }

  
  // 自定义Toast 居中显示
  private static void showCustomToast(final String msg, final int time) {
    if (toast == null) {
      toast = new Toast(BaseApplication.self);
    }
    BaseApplication.self.handlerCommon.post(new Runnable() {

      @Override
      public void run() {
        toast.makeText(BaseApplication.self, msg, time);
        toast.setDuration(time);
        toast.show();
      }
    });
  }

  public static void showConnectionFailed() {
    show("您的网络不给力哦");
  }
}
