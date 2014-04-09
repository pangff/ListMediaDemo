package com.pangff.listmediademo;

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
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  @Override
  protected void onDestroy() {
    adapter.setmediaPlay(false);
    super.onDestroy();
  }

}
