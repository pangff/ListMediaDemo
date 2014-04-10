package com.pangff.listmediademo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class MainActivity extends BaseActivity {

  ListView listView;
  VoiceAdapter adapter;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    listView = (ListView) findViewById(R.id.listView);
    adapter = new VoiceAdapter(this);
    listView.setAdapter(adapter);
    initData();
  }
  
  private void initData(){
    List<SoundBean> dataList = new ArrayList<SoundBean>();
    for(int i=0;i<10;i++){
      SoundBean soundBean = new SoundBean();
      soundBean.setUrl("http://xdong.0943.com.cn/music/%E6%97%A0%E6%B3%AA%E7%9A%84%E9%81%97%E6%86%BE.mp3");
      soundBean.setTimelen(300000);
      dataList.add(soundBean);
    }
    adapter.refreshData(dataList);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  @Override
  protected void onDestroy() {
    adapter.setMediaPlay(false);
    super.onDestroy();
  }

}
