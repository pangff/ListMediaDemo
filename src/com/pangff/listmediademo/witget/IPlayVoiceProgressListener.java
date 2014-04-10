package com.pangff.listmediademo.witget;


public interface IPlayVoiceProgressListener extends IEventListener {

  public static class VoiceProgressChangedEvent {
    public String voiceId;
    public int progess;
    public boolean playing;
  }

  public void onVoiceProgressChanged(VoiceProgressChangedEvent event);

}
