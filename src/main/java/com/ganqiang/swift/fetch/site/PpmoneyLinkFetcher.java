package com.ganqiang.swift.fetch.site;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.ConcurrentQueue;
import com.ganqiang.swift.util.StringUtil;

public final class PpmoneyLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(PpmoneyLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    String prefix = Constants.seed_map.get(type).getPreDetailLink();
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpmoneyLinkFetcher] begin fetch from ["+url+"].");
    Map<String, Result> resultMap = new HashMap<String, Result>();
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    String html = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy).getContent();
    Document doc = Jsoup.parse(html);
    Elements tables = doc.select(".table1").select(".tc").select(".ml15").select("tr");
    try {
      for (int i = 1; i < tables.size(); i++) {
        Element ele = tables.get(i);
        String reurl = ele.getElementsByTag("a").attr("href");
        if (StringUtil.isNullOrBlank(reurl)) {
          continue;
        }
        String[] array = reurl.split("/");
        String pageid = array[array.length -1];
        String absurl = prefix  + pageid;
        Result result = new Result(type);
        Elements td = ele.getElementsByTag("td");
        for (int j = 0; j < td.size(); j ++) {
          if (j == 5) {
            String[] all = td.get(j).text().replaceAll("%", "").replaceAll("元", "").replaceAll("万", "").split(" ");
            result.setProgress(Double.valueOf(all[0]));
            result.setRemainMoney(Double.valueOf(all[1].replaceAll(",", "")));
          } else if (j == 7){
            String status = td.get(j).text();
            result.setStatus(status);
          }
        }
        resultMap.put(pageid, result);
        unvisitlink.add(absurl);
//        URLQueue.addUnVisitedLink(key, absurl);
      }

      event.put(Event.results_key, resultMap);
      event.put(Event.unVisitedLinks_key, unvisitlink);
    }  catch (Exception e) {
        logger.info("fetch [PpmoneyLinkFetcher] url : [" + url + "] failure.", e);
      }

    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpmoneyLinkFetcher] end fetch from ["+url+"].");
  }
  

}
