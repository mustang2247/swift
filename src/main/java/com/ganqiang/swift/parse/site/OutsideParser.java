package com.ganqiang.swift.parse.site;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.ConcurrentQueue;
import com.ganqiang.swift.util.StringUtil;

public class OutsideParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(OutsideParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    String path = Constants.outside_page_path_map.get(key);
    List<Result> results = new ArrayList<Result>();
    ConcurrentQueue<String> visitedUrlQueue = new ConcurrentQueue<String>();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] begin parse from ["+url+"].");
        visitedUrlQueue.add(url);
        List<String> links = HttpHelper.extractOutsideLinks(url, url, path);
        reVisit(instanceid, visitedUrlQueue, links, url, path);
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] execute failure. ",e);
    }
    
    return results; 
  }

  private void reVisit(String instanceid, ConcurrentQueue<String> visitedUrlQueue, List<String> links, String host, String path){
    try {
      for (String link : links) {
        if (StringUtil.isNullOrBlank(link)) {
          continue;
        }
        link = link.trim();
        if (visitedUrlQueue.contains(link)) {
          continue;
        }
        if (StringUtil.isContainChinese(link)) {
          link = HttpHelper.encodingUrl(link);
        }
        URI innerUri = new URI(link);
        if (!link.contains(host)) {
          continue;
        }
        if (innerUri.getPath().equals("/")) {
          continue;
        }
        String uripath = link.replaceAll(host, "").substring(1);
        int level = uripath.split("/").length ;
        if (level > Constants.depth_map.get(instanceid)) {
          continue;
        }
        visitedUrlQueue.add(link);
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] have extract ["+link+"].");
        List<String> nlinks = HttpHelper.extractOutsideLinks(link, host, path);
        reVisit(instanceid, visitedUrlQueue, nlinks, host, path);
      }

      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] have extract ["+visitedUrlQueue.size()+"] pages.");
    } catch (URISyntaxException e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [OutsideParser] extracting page execute failure. ", e);
    } 
  }
  

}
