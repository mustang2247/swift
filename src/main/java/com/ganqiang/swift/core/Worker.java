package com.ganqiang.swift.core;

import org.apache.log4j.Logger;

import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.ConcurrentQueue;

public class Worker implements Runnable
{
  private static final Logger log = Logger.getLogger(Worker.class);
  private Seed seed;

  public Worker(Seed seed){
    this.seed = seed;
  }

  @Override
  public void run()
  {
    if (seed == null) {
      return;
    }
    String id = seed.getId();
    SiteType type = seed.getType();
    String key = seed.getKey();
    ConcurrentQueue<String> urlQueue = UrlQueue.getUnVisitedUrl(key);
    while (urlQueue!= null && !urlQueue.isEmpty()) {
      log.info("Worker [" + Thread.currentThread().getName() + "] startup...");
      Object obj = urlQueue.outFirst();
      if (obj == null) {
        break;
      }
      String url = obj.toString();
      seed.setListUrl(url);
      Chain chain = Constants.chain_map.get(key);
      while (chain == null) {
        chain = Constants.chain_map.get(key);
      }
      Event event = new Event();
      event.put(Event.seed_key, seed);
      if (type.equals(SiteType.OUTSIDE)) {
        ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
        unvisitlink.add(url);
        event.put(Event.unVisitedLinks_key, unvisitlink);
      }
      chain.execute(event);
      log.info("Worker [" + Thread.currentThread().getName() + "] execute job ["+id+"].");
      UrlQueue.addVisitedUrl(key, url);
    }
  }

}