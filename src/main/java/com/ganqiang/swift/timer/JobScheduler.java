package com.ganqiang.swift.timer;

import java.util.Date;
import java.util.List;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.conf.RemoteConfigHandler;
import com.ganqiang.swift.core.Constants;

public final class JobScheduler
{

  public static void localRun(){
    List<Instance> instances =  Constants.local_config.getInstances();
    for (Instance instance : instances) {
      Long interval = instance.getInterval();
      Date starttime = instance.getStartTime();
      JobController task = new JobController(instance, true);
      Constants.task_map.put(instance.getId(), task);
      if (interval == null && starttime == null) {
        Constants.timer.schedule(task, 0l);
      }else if (interval == null || interval == 0) {
        Constants.timer.schedule(task, starttime);
      } else {
        Constants.timer.schedule(task, starttime, interval * 1000);
      }
    }
  }

  public static void remoteRun(Instance instance){
    Long interval = instance.getInterval();
    Date starttime = instance.getStartTime();
    JobController task = new JobController(instance, true);
    Constants.task_map.put(instance.getId(), task);
    if (interval == null && starttime == null) {
      Constants.timer.schedule(task, 0l);
    }else if (interval == null || interval == 0) {
      Constants.timer.schedule(task, starttime);
    } else {
      Constants.timer.schedule(task, starttime, interval * 1000);
    }
    RemoteConfigHandler lch = new RemoteConfigHandler();
    lch.updateTime(instance.getId());
  }

}
