package com.pangff.listmediademo.witget.record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.pangff.listmediademo.BaseApplication;
import com.pangff.listmediademo.R;


public class RecordView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

  private static final String TAG = RecordView.class.getSimpleName();
  private boolean show = true;
  private int backgroundColor = 0xffe5eded;
  private int circleColor = 0xff9bb6c7;
  private int circleCount = 5;
  private int recordMaxTime = 60;
  private Drawable centerDrawable;
  private MediaRecorder mRecorder;
  private float timeFontSize = 14f;
  private SurfaceHolder mSurfaceHolder;
  private Paint mPaint;
  private int currentLevel = 1;
  private int oldLevel = 1;
  private File currentRecordFile;
  private List<File> tempAmrFiles;
  private VoiceWatcher voiceWatcher;
  private float timeRectHeight;
  private float timeProgressUndoHeigt;
  private float timeProgressDoHeigt;
  private float timeProgressUndoWidth;
  private long startMillisTime = -1;
  private int remainTime = 0;
  private static final int DELAY_MILLIS = 50;

  public void setVoiceWatcher(VoiceWatcher voiceWatcher) {
    this.voiceWatcher = voiceWatcher;
  }
  
  public RecordView(Context context){
    this(context,null);
  }

  public RecordView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView);
    backgroundColor = typedArray.getColor(R.styleable.RecordView_backgroundcolor, backgroundColor);
    circleColor = typedArray.getColor(R.styleable.RecordView_circlecolor, circleColor);
    centerDrawable = typedArray.getDrawable(R.styleable.RecordView_center);
    if (centerDrawable == null) {
      centerDrawable = getResources().getDrawable(R.drawable.voice_recording);
    }
    circleCount = typedArray.getInt(R.styleable.RecordView_circlecount, circleCount);
    recordMaxTime = typedArray.getInt(R.styleable.RecordView_recordmaxtime, recordMaxTime);
    timeFontSize = getResources().getDimension(R.dimen.defualt_time_font_size);
    timeFontSize = typedArray.getDimension(R.styleable.RecordView_timefontsize, timeFontSize);
    timeRectHeight = getResources().getDimension(R.dimen.defualt_time_rect_heigt);
    timeRectHeight = typedArray.getDimension(R.styleable.RecordView_timerectheight, timeRectHeight);
    timeProgressUndoHeigt = getResources().getDimension(R.dimen.defualt_time_progress_undo_heigt);
    timeProgressUndoHeigt =
        typedArray.getDimension(R.styleable.RecordView_timeprogressundoheight,
            timeProgressUndoHeigt);
    timeProgressUndoWidth = getResources().getDimension(R.dimen.defualt_time_progress_undo_width);
    timeProgressUndoWidth =
        typedArray
            .getDimension(R.styleable.RecordView_timeprogressundowidth, timeProgressUndoWidth);
    timeProgressDoHeigt = getResources().getDimension(R.dimen.defualt_time_progress_do_heigt);
    timeProgressDoHeigt =
        typedArray.getDimension(R.styleable.RecordView_timeprogressdoheight, timeProgressDoHeigt);
    typedArray.recycle();
    init();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // TODO Auto-generated method stub
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private void init() {
    mSurfaceHolder = this.getHolder();
    mSurfaceHolder.addCallback(this);
    mPaint = new Paint();
    tempAmrFiles = new ArrayList<File>();
  }

  public void startRecord() {
    try {
      createMediaRecord();
      mRecorder.prepare();
      mRecorder.start();
      startMillisTime = System.currentTimeMillis();
    } catch (Exception e) {
      e.printStackTrace();
      if (voiceWatcher != null) voiceWatcher.onVoiceRecordFailed(e);
    }
  }

  public void stopRecord() {
    stop();
    remainTime = 0;
    new AmrFileProcessThread(false).start();
  }

  public void pauseRecord() {
    stop();
  }

  public void finishRecord() {
    stop();
    remainTime = 0;
    new AmrFileProcessThread(true).start();
  }

  private void stop() {
    if (mRecorder != null) {
      try {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
      remainTime += (int) ((System.currentTimeMillis() - startMillisTime) / 1000);
      startMillisTime = -1;
      tempAmrFiles.add(currentRecordFile);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    BaseApplication.self.handlerCommon.postDelayed(this, DELAY_MILLIS);
    new Thread(this).start();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.i(TAG, "width:" + width + "height" + height);
    show = height != 0;
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    BaseApplication.self.handlerCommon.removeCallbacks(this);
  }

  @Override
  public void run() {
    synchronized (this) {
      drawDecibel();
      BaseApplication.self.handlerCommon.postDelayed(this, DELAY_MILLIS);
    }
  }

  private void drawDecibel() {
    if (getVisibility() != VISIBLE && !show) {
      return;
    }
    Canvas canvas = mSurfaceHolder.lockCanvas();
    if (canvas == null || canvas.getHeight() == 0) {
      return;
    }
    int canvasWidth = canvas.getWidth();
    int canvasHeight = canvas.getHeight();
    int dbHeight = (int) (canvas.getHeight() - timeRectHeight);
    canvas.drawColor(backgroundColor);
    int centerWidth = centerDrawable.getIntrinsicWidth();
    int centerHeight = centerDrawable.getIntrinsicHeight();
    centerDrawable.setBounds((canvasWidth - centerWidth) / 2, (dbHeight - centerHeight) / 2,
        (canvasWidth + centerWidth) / 2, (dbHeight + centerHeight) / 2);
    centerDrawable.draw(canvas);

    if (currentLevel == oldLevel) {
      oldLevel = getCurrentVUSize();
    }

    double baseRadius = Math.max(centerWidth / 2, centerHeight / 2) * Math.sqrt(2);
    double unitLength =
        Math.min(canvasWidth / 2 - baseRadius, dbHeight / 2 - baseRadius) / circleCount;
    mPaint.reset();
    mPaint.setColor(circleColor);
    mPaint.setStyle(Style.STROKE);
    mPaint.setStrokeWidth(2);
    mPaint.setAntiAlias(true);
    //去掉动画
//    for (int i = 0; i < Math.max(currentLevel, 1); i++) {
//      canvas.drawCircle(canvasWidth / 2, dbHeight / 2, (float) (baseRadius + i * unitLength),
//          mPaint);
//    }
    if (currentLevel > oldLevel) {
      currentLevel--;
    } else if (currentLevel < oldLevel) {
      currentLevel++;
    }
    mPaint.reset();
    // 区域
    // mPaint.setColor(Color.BLACK);
    // mPaint.setStyle(Style.STROKE);
    // canvas.drawRect(0, dbHeight, canvasWidth, canvasHeight, mPaint);

    mPaint.setStyle(Style.FILL);
    mPaint.setColor(0xffd0d5d9);
    float undoProgressX = (canvasWidth - timeProgressUndoWidth) / 2;
    float undoProgressY = canvasHeight - (timeRectHeight + timeProgressUndoHeigt) / 2;
    canvas.drawArc(new RectF(undoProgressX, undoProgressY, undoProgressX + timeProgressUndoHeigt,
        undoProgressY + timeProgressUndoHeigt), 90, 180, true, mPaint);
    canvas.drawRect(new RectF(undoProgressX + timeProgressUndoHeigt / 2, undoProgressY,
        undoProgressX + timeProgressUndoWidth - timeProgressUndoHeigt / 2, undoProgressY
            + timeProgressUndoHeigt), mPaint);
    canvas.drawArc(
        new RectF(undoProgressX + timeProgressUndoWidth - timeProgressUndoHeigt, undoProgressY,
            undoProgressX + timeProgressUndoWidth, undoProgressY + timeProgressUndoHeigt), -90,
        180, true, mPaint);
    mPaint.setColor(0xff00aeff);
    float doProgressY = canvasHeight - (timeRectHeight + timeProgressDoHeigt) / 2;
    canvas.drawArc(new RectF(undoProgressX, doProgressY, undoProgressX + timeProgressUndoHeigt,
        doProgressY + timeProgressUndoHeigt), 90, 180, true, mPaint);
    canvas.drawRect(new RectF(undoProgressX + timeProgressUndoHeigt / 2, doProgressY, undoProgressX
        + (timeProgressUndoWidth - timeProgressUndoHeigt) * getRecordPecent()
        + timeProgressUndoHeigt / 2, doProgressY + timeProgressUndoHeigt), mPaint);
    canvas.drawArc(new RectF(undoProgressX + (timeProgressUndoWidth - timeProgressUndoHeigt)
        * getRecordPecent(), doProgressY, undoProgressX
        + (timeProgressUndoWidth - timeProgressUndoHeigt) * getRecordPecent()
        + timeProgressUndoHeigt, doProgressY + timeProgressUndoHeigt), -90, 180, true, mPaint);
    mPaint.reset();
    mPaint.setTextSize(timeFontSize);
    mPaint.setColor(0xff0a70b0);
    mPaint.setTextAlign(Align.RIGHT);
    mPaint.setAntiAlias(true);
    canvas.drawText("0\"", undoProgressX - 2.5f * timeProgressUndoHeigt, undoProgressY
        + timeProgressUndoHeigt * 1.5f, mPaint);
    mPaint.setTextAlign(Align.LEFT);
    canvas.drawText(recordMaxTime + "\"", undoProgressX + timeProgressUndoWidth + 2.5f
        * timeProgressUndoHeigt, undoProgressY + timeProgressUndoHeigt * 1.5f, mPaint);
    mSurfaceHolder.unlockCanvasAndPost(canvas);

  }

  private float getRecordPecent() {
    float percent = 0f;
    if (startMillisTime != -1) {
      int hasRcordTime = (int) ((System.currentTimeMillis() - startMillisTime) / 1000);
      percent = (remainTime + hasRcordTime) * 1f / recordMaxTime;
    } else {
      percent = remainTime * 1f / recordMaxTime;
    }
    if (percent >= 1.0f) {
      finishRecord();
    }
    return percent;
  }

  private int getCurrentVUSize() {
    int level = 0;
    try {
      level = mRecorder == null ? 0 : (circleCount + 1) * mRecorder.getMaxAmplitude() / 32768;
    } catch (Exception e) {}
    return level;
  }

  @SuppressWarnings("deprecation")
  private void createMediaRecord() throws IOException {
    File parentPath = new File(AudioUtils.RECORD_SAVE_PATH);
    if (!parentPath.exists()) {
      parentPath.mkdirs();
    }
    currentRecordFile =
        File.createTempFile("temp_" + System.currentTimeMillis(), ".amr", parentPath);
    mRecorder = new MediaRecorder();
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    mRecorder.setOutputFile(currentRecordFile.getAbsolutePath());
  }

  public interface VoiceWatcher {

    public void onVoiceRecordFailed(Exception e);

    public void onStartCombineAMRFile();

    public void onAMRFileComplete(File amrFile);

    public void onRecordCancel();
  }

  class AmrFileProcessThread extends Thread {

    private boolean needCombine;

    public AmrFileProcessThread(boolean needCombine) {
      this.needCombine = needCombine;
    }

    @Override
    public void run() {
      synchronized (AmrFileProcessThread.class) {
        File amrFile = null;
        ArrayList<File> copyFiles = new ArrayList<File>(tempAmrFiles);
        tempAmrFiles.clear();
        if (needCombine) {
          if (voiceWatcher != null) voiceWatcher.onStartCombineAMRFile();
          amrFile = AudioUtils.buildAmrFile(copyFiles);
        }
        for (File tempAmrFile : copyFiles) {
          tempAmrFile.delete();
        }
        if (voiceWatcher != null) voiceWatcher.onAMRFileComplete(amrFile);
      }
    }
  }
}
