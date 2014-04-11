package com.pangff.listmediademo.witget.record;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.pangff.listmediademo.witget.play.ToastUtil;
import com.pangff.listmediademo.witget.record.RecordView.VoiceWatcher;

@SuppressLint("HandlerLeak")
public class RecordBtn extends TextView implements VoiceWatcher{

  private WindowManager windowManager = null;
  private WindowManager.LayoutParams windowParams = null;
  private RecordView recordView;
  Handler handler;
  public RecordBtn(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.setText("按下开始录音");
    handler = new Handler(){
      @Override
      public void handleMessage(Message msg) {
        switch(msg.what){
          case AudioUtils.MSG_AMRFILECOMPLETE:
            delRecordWindow();
            Bundle bundle = msg.getData();
            String errorMsg = bundle.getString("errorMsg");
            if(errorMsg!=null){
              ToastUtil.show(errorMsg);
            }
            break;
          case AudioUtils.MSG_AMRFILECANCEL:
            delRecordWindow();
            ToastUtil.show("录音取消");
            break;
        }
      }
    };
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    float x = event.getRawX();
    float y = event.getRawY();
    switch(action){
      case MotionEvent.ACTION_DOWN:
        addRecordWindow();
        recordView.startRecord();
        this.setText("抬起完成录音");
        return true;
        //break;
      case MotionEvent.ACTION_MOVE:
        int location[] = {0,0};
        this.getLocationInWindow(location);
        if(x<location[0]||x>location[0]+this.getWidth()||y<location[1]||y>location[1]+this.getHeight()){
          if(recordView!=null){
            recordView.finishRecord();
            delRecordWindow();
            return true;
          }
        }
        break;
      case MotionEvent.ACTION_OUTSIDE:
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if(recordView!=null){
          recordView.finishRecord();
          delRecordWindow();
        }
        break;
    }
    return super.onTouchEvent(event);
  }
  
  /**
   * popWindow
   * @param bm
   * @param x
   * @param y
   */
  private void addRecordWindow() {
    windowParams = new WindowManager.LayoutParams();
    windowParams.gravity = Gravity.CENTER;
    windowParams.height = 800;
    windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;;
    windowParams.flags =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
    windowParams.format = PixelFormat.TRANSLUCENT;
    windowParams.windowAnimations = 0;

    recordView = new RecordView(getContext());
    recordView.setVoiceWatcher(this);
    windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    windowManager.addView(recordView, windowParams);
  }
  
  private void delRecordWindow(){
    if(recordView != null){
      this.setText("按下开始录音");
      windowManager.removeView(recordView);
      recordView = null;
    }
  }

  @Override
  public void onVoiceRecordFailed(Exception e) {
    
  }

  @Override
  public void onStartCombineAMRFile() {
    
  }

  @Override
  public void onAMRFileComplete(File amrFile) {
    String errorMsg = null;
    if (amrFile != null) {
      try {
        MediaPlayer checkMediaPlayer = new MediaPlayer();
        checkMediaPlayer.setDataSource(amrFile.getAbsolutePath());
        checkMediaPlayer.prepare();
        int duration = checkMediaPlayer.getDuration();
        if (duration < 1000) {
          errorMsg = "录音时间过短";
        }
      } catch (Exception e) {
        errorMsg = null;
      }
    }
    Message msg = new Message();
    msg.what = AudioUtils.MSG_AMRFILECOMPLETE;
    Bundle bundle = new Bundle();
    bundle.putString("errorMsg", errorMsg);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @Override
  public void onRecordCancel() {
    Message msg = new Message();
    msg.what = AudioUtils.MSG_AMRFILECANCEL;
    handler.sendMessage(msg);
  }

}
