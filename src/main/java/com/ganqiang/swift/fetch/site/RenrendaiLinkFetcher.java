package com.ganqiang.swift.fetch.site;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.util.ConcurrentQueue;

public final class RenrendaiLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(RenrendaiLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    String detaillink = Constants.seed_map.get(type).getPreDetailLink();
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiLinkFetcher] begin fetch from ["+url+"].");
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    String html = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy).getContent();
    try {
      Document doc = Jsoup.parse(html);
      Element as = doc.getElementById("loan-list-rsp");
      if (as == null) {
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiLinkFetcher] Renrendai page revision from ["+url+"].");
        return;
      }
      String json = as.html();
      JSONObject jsonObject = new JSONObject(json);
      JSONObject data = jsonObject.getJSONObject("data");
//      int totalpage = data.getInt("totalPage");
//      if (totalpage == 0) {
//        logger.error("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiLinkFetcher] ");
//        Thread.currentThread().stop();
//      }
      JSONArray jsonArray = data.getJSONArray("loans");
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject obj = jsonArray.getJSONObject(i);
        String link = detaillink + obj.getLong("loanId");
        unvisitlink.add(link);
//        URLQueue.addUnVisitedLink(key, link);
      }
      event.put(Event.unVisitedLinks_key, unvisitlink);
    } catch (Exception e) {
      e.printStackTrace();
      logger.info("fetch [RenrendaiLinkFetcher] url : [" + url + "] ");
    }

    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiLinkFetcher] end fetch from ["+url+"].");
  }

}
