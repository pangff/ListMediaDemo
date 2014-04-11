package com.pangff.listmediademo.witget.play;

import android.os.Environment;

public class FileStorage {
  public static final String SD_ROOT_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  //临时文件目录
  public static final String TEMP_PATH = SD_ROOT_PATH + "/jhss/tmp/";


}
