package com.pangff.listmediademo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pangff.listmediademo.witget.ViewVoiceHolder;
import com.pangff.listmediademo.witget.VoicePlayUtil;

public class VoiceAdapter extends BaseAdapter{
  VoicePlayUtil voicePlayUtil;
  BaseActivity context;
  boolean isAutoPalay = false;
  
  List<SoundBean> viewList = new ArrayList<SoundBean>();
  public VoiceAdapter(Context context){
    this.context = (BaseActivity) context;
    voicePlayUtil = new VoicePlayUtil(this.context);//初始化音频控制对象
  }
  
  @Override
  public int getCount() {
    return viewList.size();
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }
  
  /**
   * 是否销毁
   * @param flag
   */
  public void setMediaPlay(Boolean flag) {
    if (flag == false) {
      voicePlayUtil.release();
    }
  }
  
  /**
   * 设置是否自动播放
   * @param isAutoPalay
   */
  public void setAutoPlay(Boolean isAutoPalay) {
    this.isAutoPalay = isAutoPalay;
  }
  
  /**
   * 数据刷新
   * @param list
   */
  public void refreshData(List<SoundBean> list){
    viewList.clear();
    if(list!=null){
      viewList.addAll(list);
    }
    notifyDataSetChanged();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ViewHolder viewHolder;
    if(convertView==null){
      convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
      viewHolder = new ViewHolder(convertView);
      convertView.setTag(viewHolder);
    }else{
      viewHolder = (ViewHolder) convertView.getTag();
    }
    SoundBean soundBean = viewList.get(position);
    viewHolder.voiceHolder.bindSound(soundBean, voicePlayUtil);
    if(position==0 && isAutoPalay){//如果自动播放，自动播放第一个
      viewHolder.voiceHolder.getTrigger().postDelayed(new Runnable() {
        @Override
        public void run() {
          viewHolder.voiceHolder.getTrigger().performClick();
        }
      }, 300);
    }
    return convertView;
  }
  
  static class ViewHolder extends ViewVoiceHolder{
    public ViewHolder(View parent){
     super(parent);
    }
  }
}
