package com.ganqiang.swift.fetch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.core.UrlQueue;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.prep.DelayHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.ConcurrentQueue;
import com.ganqiang.swift.util.StringUtil;

public final class PageFetch implements Process
{
  private static final Logger logger = Logger.getLogger(PageFetch.class);

  @SuppressWarnings("unchecked")
  @Override
  public void execute(Event event)
  {
    List<FetchedPage> list = new ArrayList<FetchedPage>();
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String listUrl = seed.getListUrl();
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PageFetch] begin.");
    String key = seed.getKey();
    boolean useproxy = false;
    if (type.equals(SiteType.OUTSIDE)) {
      useproxy = Constants.outside_use_proxy_map.get(instanceid);
    } else {
      useproxy = Constants.inside_use_proxy_map.get(instanceid);
    }
    ConcurrentQueue<String> unVisitedLinks = (ConcurrentQueue<String>) event.get(Event.unVisitedLinks_key);
    while(unVisitedLinks !=null && !unVisitedLinks.isEmpty()){
      String link = unVisitedLinks.outFirst().toString();
      if(StringUtil.isNullOrBlank(link)){
        continue;
      }
      logger.info("Worker [" + Thread.currentThread().getName() + "] grab detail page ["+link+"] .");
      
      FetchedPage fetchedPage = retry(key, instanceid, type, link, useproxy);
      while (fetchedPage == null) {
        fetchedPage = retry(key, instanceid,  type, link, useproxy);
      }
      list.add(fetchedPage);
    }
    event.put(Event.fetchedPages_key, list);
    UrlQueue.addVisitedUrl(key, listUrl);

    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PageFetch] end.");
  }
  
  private FetchedPage retry(String key, String instanceid, SiteType type, String link, boolean useproxy){
    DelayHelper.delay(key, type, instanceid);
    FetchedPage fp = HttpHelper.getPage(key, instanceid, type, link, useproxy);
//    Boolean jsflag = Constants.JS_SUPPORT_MAP.get(instanceid);
//    if (jsflag) {
//      fp = JavaScriptHelper.getPage(instanceid, type, link);
//    } else {
//      fp = HttpUtil.getPage(instanceid, type, link);
//    }
    boolean flag = check(fp);
    if(!flag){
      if (useproxy) {
        DelayHelper.delay(key, type, instanceid);
        logger.info("Worker [" + Thread.currentThread().getName() + "] switch ip proxy in order to access detail page.");
        fp = HttpHelper.getPage(key, instanceid,type, link, useproxy);
      } else {
        try {
          logger.info("Worker [" + Thread.currentThread().getName() + "] waiting for 65 seconds in order to access detail page. ");
          Thread.sleep(65000);
        } catch (InterruptedException e) {
          logger.error("Worker [" + Thread.currentThread().getName() + "] waiting for detail page failed. ", e);
        }
      }
    }
    
    if (flag){
      return fp;
    } else {
      return null;
    }
    
  }

  private boolean check(FetchedPage fetchedPage){
    if(isAntiScratch(fetchedPage)){
      return false;
    }
    return true;
  }
  
  private boolean isStatusValid(int statusCode){
    if(statusCode >= 200 && statusCode < 400){
      return true;
    }
    return false;
  }
  
  private boolean isAntiScratch(FetchedPage fetchedPage){
    int code = fetchedPage.getStatusCode();
    String content = fetchedPage.getContent();
    if((!isStatusValid(code)) && code == 403){
      return true;
    }
    if(StringUtil.isNullOrBlank(content)){
      return true;
    }
    if(content.contains("禁止访问") || content.contains("刷新太频繁")){
      logger.warn("Worker [" + Thread.currentThread().getName() + "] --- " +
      		"The url: ["+fetchedPage.getUrl()+"] has prevented continuously refresh function!");
      return true;
    }
    return false;
  }

}
