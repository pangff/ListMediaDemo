package com.pangff.listmediademo;

import com.pangff.listmediademo.witget.ISoundBean;
import com.pangff.listmediademo.witget.RootPojo;



public class WeiBoSoundBean extends RootPojo implements ISoundBean  {
  
  private int timelen;//语音时长
  private String url;//语音地址
  private boolean isDiskCache = false;

  @Override
  public int getTimelen() {
    return timelen;
  }

  @Override
  public void setTimelen(int timelen) {
    this.timelen = timelen;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isDiskCache() {
    return isDiskCache;
  }

  public void setDiskCache(boolean isDiskCache) {
    this.isDiskCache = isDiskCache;
  }

}
