package com.pangff.listmediademo.witget.play;

import android.media.MediaPlayer;

import com.pangff.listmediademo.BaseActivity;
import com.pangff.listmediademo.witget.play.IPlayVoiceProgressListener.VoiceProgressChangedEvent;

public class VoicePlayUtil {
  BaseActivity activity;

  public VoicePlayUtil(BaseActivity activity) {
    this.activity = activity;
  }

  public boolean isEnable() {
    return activity.hasWindowFocus();
  }

  // 每次延迟100毫秒再启动线程
  public String voiceId;
  // 音频相关
  public MediaPlayer mediaPlay = new MediaPlayer();

  public MediaPlayer getMediaPlayer() {
    if (mediaPlay == null) {
      mediaPlay = new MediaPlayer();
    }
    return mediaPlay;
  }

  public void release() {
    VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
    event.voiceId = voiceId;
    event.playing = false;
    event.progess = 0;
    voiceChangedPublisher.notifyDataChanged(event);
    getMediaPlayer().stop();
    mediaPlay.release();
    mediaPlay = null;
    task.stop();
    voiceId = null;
  }

  public EventPublish<IPlayVoiceProgressListener, VoiceProgressChangedEvent> voiceChangedPublisher =
      new EventPublish<IPlayVoiceProgressListener, VoiceProgressChangedEvent>() {

        @Override
        public void notifyEvent(IPlayVoiceProgressListener listener,
            VoiceProgressChangedEvent event) {
          listener.onVoiceProgressChanged(event);
        }
      };
  public RepeatTaskUtil task = new RepeatTaskUtil(new Runnable() {
    int count = 0;

    @Override
    public void run() {

      VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
      event.voiceId = voiceId;
      if (getMediaPlayer().isPlaying()) {
        event.playing = true;
        event.progess = mediaPlay.getCurrentPosition();
      } else {
        event.playing = false;
        event.progess = 0;
        count++;
        if (count > 3) {
          task.stop();
          count = 0;
        }
      }
      voiceChangedPublisher.notifyDataChanged(event);
    }
  }, 100);
}