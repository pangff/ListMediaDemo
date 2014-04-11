package com.pangff.listmediademo;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.pangff.listmediademo.witget.play.ToastUtil;
import com.pangff.listmediademo.witget.record.AudioUtils;

public class MainActivity extends BaseActivity implements OnClickListener{

  ListView listView;
  VoiceAdapter adapter;
  Button recordBtn;
  
  Handler handler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what){
        case AudioUtils.MSG_AMRFILECOMPLETE:
          Bundle bundle = msg.getData();
          String errorMsg = bundle.getString("errorMsg");
          if(errorMsg!=null){
            ToastUtil.show(errorMsg);
          }
          break;
        case AudioUtils.MSG_AMRFILECANCEL:
          ToastUtil.show("录音取消");
          break;
      }
    }
  };
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    listView = (ListView) findViewById(R.id.listView);
    recordBtn = (Button) findViewById(R.id.recordBtn);
    recordBtn.setOnClickListener(this);
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

  @Override
  public void onClick(View v) {
    if(v.equals(recordBtn)){
      Intent intent = new Intent();
      intent.setClass(MainActivity.this, RecordActivity.class);
      startActivity(intent);
    }
  }

}
