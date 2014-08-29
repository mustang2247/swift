package com.ganqiang.swift.net.sync;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.CommondExecutor;
import com.ganqiang.swift.core.Constants;

public class SyncCommandExecutor implements CommondExecutor
{
  private static final Logger logger = Logger.getLogger(SyncCommandExecutor.class);

  private String instanceid;
  private String subDataPath;
  
  @SuppressWarnings("unused")
  private SyncCommandExecutor(){}

  public SyncCommandExecutor(String instanceid, String subDataPath){
    this.instanceid = instanceid;
    this.subDataPath = subDataPath;
  }

  @Override
  public void execute()
  {
    try{
      String dataPath = Constants.disk_path_map.get(instanceid);
      SyncContextHandler.interpreter.exec("bootstrap.main(map, '"+dataPath+"', '"+subDataPath+"' )");
      logger.info("Worker [" + Thread.currentThread().getName() + "] begin invoke python sync script...");
    } catch(Exception e) {
      logger.info("Worker [" + Thread.currentThread().getName() + "] begin invoke python sync script failed, ", e);
    }
  }

}
