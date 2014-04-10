package com.pangff.listmediademo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pangff.listmediademo.witget.VoiceHolderTwo;
import com.pangff.listmediademo.witget.VoicePlayUtil;

public class VoiceAdapter extends BaseAdapter{
  VoicePlayUtil voicePlayUtil;
  BaseActivity context;
  public VoiceAdapter(Context context){
    this.context = (BaseActivity) context;
    voicePlayUtil = new VoicePlayUtil(this.context);
  }
  
  @Override
  public int getCount() {
    return 10;
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }
  
  public void setmediaPlay(Boolean flag) {
    if (flag == false) {
      voicePlayUtil.release();
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    VoiceHolderTwo voiceHolder;
    if(convertView==null){
      convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
      voiceHolder = new VoiceHolderTwo(convertView);
      convertView.setTag(voiceHolder);
    }else{
      voiceHolder = (VoiceHolderTwo) convertView.getTag();
    }
    WeiBoSoundBean weiBoSoundBean = new WeiBoSoundBean();
    weiBoSoundBean.setUrl("http://xdong.0943.com.cn/music/%E6%97%A0%E6%B3%AA%E7%9A%84%E9%81%97%E6%86%BE.mp3");
    weiBoSoundBean.setTimelen(300000);
    
    voiceHolder.bindSound(weiBoSoundBean, false, voicePlayUtil);
    return convertView;
  }

}
