package com.ganqiang.swift.net.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;

public final class FileSync implements Process
{
  private static final ReentrantLock plock = new ReentrantLock();
  private static final ReentrantLock alock = new ReentrantLock();
  private static volatile Long lastTime = null;
  private static final long maxtimeout = 5; // 5 seconds
  private static final Map<SiteType, Boolean> logoOnceSync = new HashMap<SiteType, Boolean>();

  static{
    logoOnceSync.put(SiteType.PPMONEY, true);
    logoOnceSync.put(SiteType.JIMUBOX, true);
    logoOnceSync.put(SiteType.LUFAX, true);
    logoOnceSync.put(SiteType.PPMONEY, true);
    logoOnceSync.put(SiteType.YOOLI, true);
    //虽然json中有图片地址，但目前图片地址全部为测试图片
    logoOnceSync.put(SiteType.RENRENDAI, true);
    //可上传头像，但目前全部都是一个图
    logoOnceSync.put(SiteType.ROYIDAI, true);
    logoOnceSync.put(SiteType.YIRENDAI, true);
  }
  
  private boolean isOnceSync(SiteType type){
    if (logoOnceSync.containsKey(type)) {
      return logoOnceSync.get(type);
    }
    return true;
  }
  
  private void setOnceSync(SiteType type){
    if (logoOnceSync.containsKey(type)) {
      logoOnceSync.put(type, false);
    }
  }
  
  private boolean isOverTime(){
    boolean flag = false;
    if (lastTime == null) {
      lastTime = System.currentTimeMillis() ;
    } else {
      long timeout = (System.currentTimeMillis() - lastTime) / 1000;
      if (timeout < maxtimeout) {
        flag = true;
      }
    }
    return flag;
  }

  @Override
  public void execute(Event event)
  {
    String pagedir = "";
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    if (type.equals(SiteType.OUTSIDE)) {
      pagedir = Constants.outside_page_path_map.get(key);
      plock.lock();
      try {
        if (!isOverTime()) {
          SyncCommandExecutor sce = new SyncCommandExecutor(instanceid, pagedir);
          sce.execute();
        }
      } finally {
        plock.unlock();
      }
    } else {
      String avatardir = Constants.inside_avatar_path_map.get(key);
      pagedir = Constants.inside_page_path_map.get(key);
      alock.lock();
      try{
        if (isOnceSync(type)) {
          SyncCommandExecutor sce = new SyncCommandExecutor(instanceid, avatardir);
          sce.execute();
          setOnceSync(type);
        }
        
      } finally {
        alock.unlock();
      }
      
      plock.lock();
      try {
        if (!isOverTime()) {
          SyncCommandExecutor sce = new SyncCommandExecutor(instanceid, pagedir);
          sce.execute();
        }
      } finally {
        plock.unlock();
      }
      
    }
    
    
  }
  

}
