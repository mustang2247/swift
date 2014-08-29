package com.ganqiang.swift.timer;

import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.ganqiang.swift.conf.LocalConfigHandler;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.JobExecutor;
import com.ganqiang.swift.core.UrlQueue;
import com.ganqiang.swift.core.Worker;
import com.ganqiang.swift.seed.InsideSeed;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.StringUtil;

public class JobController extends TimerTask implements JobExecutor
{
  private static final Logger log = Logger.getLogger(JobController.class);
  private String name;
  private Instance instance;
  private volatile boolean condition = false;

  public JobController(Instance instance, boolean condition){
    this.name = instance.getId();
    this.instance = instance;
    this.condition = condition;
  }

  private void loadUrl(){
    String[] inseeds = instance.getInSeeds();
    if(inseeds != null){
      for(String site : inseeds){
        if (StringUtil.isNullOrBlank(site)) {
          continue;
        }
        String key = Seed.getKey(instance.getId(), site);
        SiteType type = SiteType.getType(site);
        InsideSeed inseed = Constants.seed_map.get(type);
        while (inseed == null) {
          inseed = Constants.seed_map.get(type);
        }
        int pageSize = Constants.seed_map.get(type).getPageSize();
        String listurl = Constants.seed_map.get(type).getPreListLink();
        if(type.equals(SiteType.YIRENDAI) || type.equals(SiteType.FIRSTP2P)){
          String[] seed = listurl.split("\\" + Constants.split_str);
          for (int i = 0; i < pageSize; i++) {
            UrlQueue.addUnVisitedUrl(key, seed[0] + i + seed[1]);
          }
        } else if(type.equals(SiteType.LUFAX) || type.equals(SiteType.YOOLI)){
          String[] seed = listurl.split("\\" + Constants.split_str);
          for (int i = 1; i <= pageSize; i++) {
            UrlQueue.addUnVisitedUrl(key, seed[0] + i + seed[1]);
          }
        } else{
          for (int i = 1; i <= pageSize; i++) {
            UrlQueue.addUnVisitedUrl(key, listurl + i);
          }
        }
      }
    }
    String[] outseeds = instance.getOutSeeds();
    if(outseeds != null){
      for(String site : outseeds){
        String key = Seed.getKey(instance.getId(), site);
        UrlQueue.addUnVisitedUrl(key, site);
      }
    }
  }

  private void startup() {
    int thread_num = Runtime.getRuntime().availableProcessors();
    if (Constants.thread_num  == 1) {
      String[] inseeds = instance.getInSeeds();
      if(inseeds != null){
        for(String inseed : inseeds){
          Seed seed = new Seed();
          seed.setId(instance.getId());
          seed.setSeedName(inseed);
          Worker worker = new Worker(seed);
          worker.run();
        }
      }
      String[] outseeds = instance.getOutSeeds();
      if(outseeds != null){
        for(String site : outseeds){
          Seed seed = new Seed();
          seed.setId(instance.getId());
          seed.setSeedName(site);
          Worker worker = new Worker(seed);
          worker.run();
        }
      }
    } else {
//      ExecutorService executorService = Executors.newFixedThreadPool(thread_num);
      ExecutorService executorService = Executors.newCachedThreadPool();
      for (int i = 0; i < thread_num; i++) {
        String[] inseeds = instance.getInSeeds();
        if(inseeds != null){
          for(String inseed : inseeds){
            Seed seed = new Seed();
            seed.setId(instance.getId());
            seed.setSeedName(inseed);
            Worker worker = new Worker(seed);
            executorService.execute(worker);
          }
        }
        String[] outseeds = instance.getOutSeeds();
        if(outseeds != null){
          for(String site : outseeds){
            Seed seed = new Seed();
            seed.setId(instance.getId());
            seed.setSeedName(site);
            Worker worker = new Worker(seed);
            executorService.execute(worker);
          }
        }
      }
//      executorService.shutdown();  
    }
  }

  @Override
  public void run()
  {
    while(!condition) {
      log.info("Job " + name + " has paused...");
      try {
        synchronized(this){
          this.wait();
        }
      } catch (InterruptedException e) {
         Thread.interrupted();
      }
    }
    log.info("Job " + name + " has startup...");
    LocalConfigHandler lch = new LocalConfigHandler();
    lch.updateTime(instance.getId());
    loadUrl();
    startup();
  }

  @Override
  public void continues()
  {
    condition = true;
    synchronized(this){
      this.notify();
    }
    log.info("Job " + name + " has continued to run.");
  }

  @Override
  public void destory(String jobid)
  {
    boolean flag = this.cancel();
    if (flag) {
      Constants.task_map.remove(jobid);
      log.info("Job " + jobid + " has destoryed.");
    }
  }

  @Override
  public void pause()
  {
    condition = false;
    log.info("Job " + name + " has paused.");
  }  

}
