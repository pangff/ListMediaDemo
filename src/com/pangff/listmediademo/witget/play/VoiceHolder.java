package com.pangff.listmediademo.witget.play;

import java.io.File;

import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pangff.listmediademo.R;
import com.pangff.listmediademo.witget.play.AppDownloadTask.DownloadProgressListener;
import com.pangff.listmediademo.witget.play.IPlayVoiceProgressListener.VoiceProgressChangedEvent;

public class VoiceHolder {
  // 语音相关
  public TextView tvTotalTime;
  public LinearLayout playButton;
  public ProgressBar seekBar;
  public ProgressBar pbVoiceDownloading;
  public RelativeLayout container_voice;
  public ImageView playIcon;
  public ImageView stopIcon;
  public String voiceId;
  public boolean downloading;
  IPlayVoiceProgressListener listener;

  public VoiceHolder(View parent) {
    playButton = (LinearLayout) parent.findViewById(R.id.image_play);
    seekBar = (ProgressBar) parent.findViewById(R.id.seekBar);
    pbVoiceDownloading = (ProgressBar) parent.findViewById(R.id.voice_downloading);
    playButton.setTag(seekBar);
    tvTotalTime = (TextView) parent.findViewById(R.id.textView_totaltime);
    container_voice = (RelativeLayout) parent.findViewById(R.id.voiceView);
    // forwordVoiceView
    playIcon = (ImageView) parent.findViewById(R.id.image_play_icon);
    stopIcon = (ImageView) parent.findViewById(R.id.image_stop_icon);

    listener = new IPlayVoiceProgressListener() {

      @Override
      public void onVoiceProgressChanged(VoiceProgressChangedEvent event) {
        if (event == null || event.voiceId == null) {
          return;
        }
        Log.d("onVoiceProgressChanged", "voiceId = " + voiceId + ", " + event.voiceId + ", "
            + event.progess);
        if (event.voiceId.equals(voiceId)) {
          if (event.progess < 0) {
            // 下载事件接收
            downloading = true;
            setDownloadingStatus();
          } else if (event.progess == 0) {
            // 进度为0有2种情况：
            downloading = false;
            if (event.playing) {// 播放中；
              setPlaying(0);
            } else { // 或者，播放完毕；
              setReadyPlayStatus();
            }
          } else {
            // 播放进度大于0的情况
            downloading = false;
            int diff = seekBar.getMax() - event.progess;
            setPlaying(event.progess);
            if (diff <= 100) {
              if (seekBar.getMax() < 1000) {
                seekBar.setProgress(seekBar.getMax());
              } else {
                seekBar.postDelayed(new Runnable() {

                  @Override
                  public void run() {
                    seekBar.setProgress(seekBar.getMax());
                  }
                }, diff);
              }
            } else {
              seekBar.setProgress(event.progess);
            }
          }
        } else {
          // 忽略2种情况：
          // 1.当前处于下载状态，忽略进度信息；
          // 2.收到非自己的下载进度event
          if (!downloading && event.progess >= 0) {
            setReadyPlayStatus();
          }
        }
      }
    };
  }


  /**
   * 语音 播放
   * 
   * @param voiceHolder
   * @param newBean
   */
  public void bindSound(final ISoundBean sound, final VoicePlayUtil voicePlayUtil) {

    if (sound == null) {
      container_voice.setVisibility(View.GONE);
      voicePlayUtil.voiceChangedPublisher.register(listener);
      return;
    }
    voicePlayUtil.voiceChangedPublisher.register(listener);

    container_voice.setVisibility(View.VISIBLE);

    int total = sound.getTimelen();// 获取文件时长
    // 正式环境需要向服务器获取时长
    seekBar.setMax(total);
    int second = total / 1000;
    if (second >= 60) {
      tvTotalTime.setText("60\"");
    } else if (total % 1000 > 0) {
      tvTotalTime.setText(String.valueOf(second + 1) + "\"");
    } else {
      tvTotalTime.setText(second + "\"");
    }

    voiceId = sound.getUrl();
    if (voiceId.equals(voicePlayUtil.voiceId)) {
      if (voicePlayUtil.getMediaPlayer().isPlaying()) {
        setPlaying(voicePlayUtil.getMediaPlayer().getCurrentPosition());
      } else {
        setReadyPlayStatus();
      }
    } else {
      setReadyPlayStatus();
    }



    OnClickListener playListener = new OnClickListener() {

      @Override
      public void onClick(View v) {
        startVoice();
      }

      public void startVoice() {
        if (!PhoneUtils.isSDMounted()) {
          ToastUtil.show("您的SD卡不可用");
          return;
        }
        // 获取服务器的语音信息
        int start = sound.getUrl().lastIndexOf("/") + 1;
        final String voicesName = sound.getUrl().substring(start, sound.getUrl().length());
        final String path;
        if (sound.isDiskCache()) {
          path = sound.getUrl();
        } else {
          path = PhoneUtils.getVoiceOnSDPath(voicesName);
        }

        File filePath = new File(path);
        if (filePath.exists()) {
          playOrStop(sound, path);
          return;
        }
        String downloadUrl = sound.getUrl();
        AppDownloadTask task = DownloadMgr.getTaskByUrl(downloadUrl);
        if (task == null || task.getStatus() == Status.FINISHED) {
          // 下载任务不存在或者已经结束的情况下，创建新的下载任务
          AppDownloadRequest request = new AppDownloadRequest();
          request.downloadUrl = downloadUrl;
          request.appFile = path;
          task = new AppDownloadTask();
          task.execute(request);
          setDownloadingStatus();
          DownloadMgr.cache(downloadUrl, task);

          task.addDownloadProgressListener(new DownloadProgressListener() {

            @Override
            public void onStatusChanged(int status, AppDownloadRequest result) {
              if (status == AppDownloadTask.PROGRESS) {
                // 发布进度信息
                VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
                event.voiceId = voiceId;
                event.progess = -1;
                event.playing = false;
                voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
              } else if (status == AppDownloadTask.OK) {
                // 下载完毕
                if (voiceId.equals(voicePlayUtil.voiceId) && voicePlayUtil.isEnable()) {
                  playOrStop(sound, path);
                } else {
                  // 进入就绪状态
                  VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
                  event.voiceId = voiceId;
                  event.progess = 0;
                  event.playing = false;
                  voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
                }
              } else {
                // 错误处理，进入就绪状态
                VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
                event.voiceId = voiceId;
                event.progess = 0;
                event.playing = false;
                voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
              }
            }
          });
        }


        // 其他项在playing
        if (voicePlayUtil.getMediaPlayer().isPlaying()) {
          voicePlayUtil.voiceId = null;
          voicePlayUtil.task.stop();
          voicePlayUtil.mediaPlay.stop();
        }
        voicePlayUtil.voiceId = voiceId;
      }

      public void playOrStop(final ISoundBean sound, final String path) {
        boolean current = voiceId.equals(voicePlayUtil.voiceId);
        // self在playing
        if (current && voicePlayUtil.getMediaPlayer().isPlaying()) {
          return;
        }

        File file = new File(path);
        if (file != null && file.exists()) {
          // 其他项在playing
          if (voicePlayUtil.getMediaPlayer().isPlaying()) {
            voicePlayUtil.voiceId = null;
            voicePlayUtil.task.stop();
            voicePlayUtil.mediaPlay.stop();
          }

          // self to play
          voicePlayUtil.voiceId = sound.getUrl();
          setPlaying(0);
          try {
            voicePlayUtil.getMediaPlayer().reset();
            voicePlayUtil.mediaPlay.setDataSource(path);
            voicePlayUtil.mediaPlay.prepare();
            voicePlayUtil.mediaPlay.start();
            voicePlayUtil.task.start();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

    };
    playButton.setOnClickListener(playListener);
    stopIcon.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("stopIcon", "stopIcon on click");
        setReadyPlayStatus();
        VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
        event.voiceId = voiceId;
        event.playing = false;
        event.progess = 0;
        voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
        voicePlayUtil.voiceId = null;
        voicePlayUtil.task.stop();
        voicePlayUtil.getMediaPlayer().stop();
      }
    });
  }
  
  
  public LinearLayout getTrigger(){
   return  playButton;
  }

  public void setPlaying(final int progress) {
    stopIcon.post(new Runnable() {

      @Override
      public void run() {
        stopIcon.setVisibility(View.VISIBLE);
        playIcon.setVisibility(View.GONE);
        pbVoiceDownloading.setVisibility(View.GONE);
        seekBar.setProgress(progress);
      }
    });
  }

  public void setReadyPlayStatus() {
    stopIcon.post(new Runnable() {

      @Override
      public void run() {
        stopIcon.setVisibility(View.GONE);
        pbVoiceDownloading.setVisibility(View.GONE);
        playIcon.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
      }
    });

  }

  public void setDownloadingStatus() {
    stopIcon.post(new Runnable() {

      @Override
      public void run() {
        stopIcon.setVisibility(View.GONE);
        pbVoiceDownloading.setVisibility(View.VISIBLE);
        playIcon.setVisibility(View.GONE);
        seekBar.setProgress(0);
      }
    });

  }
}
