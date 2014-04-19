package com.pangff.listmediademo.witget.record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;


public class RecordView {

  private static final String TAG = RecordView.class.getSimpleName();
  private MediaRecorder mRecorder;
  private File currentRecordFile;
  private List<File> tempAmrFiles;
  private VoiceWatcher voiceWatcher;
  private long startMillisTime = -1;
  private int remainTime = 0;

  public void setVoiceWatcher(VoiceWatcher voiceWatcher) {
    this.voiceWatcher = voiceWatcher;
  }

  public RecordView(Context context) {
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
