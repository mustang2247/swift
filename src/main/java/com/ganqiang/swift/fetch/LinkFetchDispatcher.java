package com.ganqiang.swift.fetch;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.prep.DelayHelper;
import com.ganqiang.swift.seed.InsideSeed;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;

public final class LinkFetchDispatcher implements Process
{

  private static final Logger logger = Logger.getLogger(LinkFetchDispatcher.class);

  @Override
  public void execute(Event event)
  {
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin LinkFetchAdaptor.");
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
//    boolean use_proxy = Constants.inside_use_proxy_map.get(instanceid);
    DelayHelper.delay(key, type, instanceid);

//    if (use_proxy) {
//      HttpProxyLooper proxy = Constants.proxy_map.get(key);
//      while (proxy == null) {
//        proxy = Constants.proxy_map.get(key);
//      }
//    }
    InsideSeed insideseed = Constants.seed_map.get(type);
    insideseed.getFetcher().fetch(event);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end LinkFetchAdaptor.");
  }

}
