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
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.ConcurrentQueue;

public final class RoyidaiLinkFetcher implements Fetchable
{

  private static final Logger logger = Logger.getLogger(RoyidaiLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [RoyidaiLinkFetcher] from ["+url+"].");
    Map<String, Result> resultMap = new HashMap<String, Result>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    Document doc = Jsoup.parse(fp.getContent());
    String detaillink = Constants.seed_map.get(type).getPreDetailLink();
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    Elements list = doc.getElementById("lastestborrow").children();
    for (int i = 0; i < list.size(); i++) {
      Element ele = list.get(i);
      Result result = new Result(type);
      Element aes = ele.select("a[href*=financeDetail.do?id]").first();
      String link = aes.attr("href");
      String pageid = link.split("=")[1];
      Elements es = ele.select("div.list_txt > table > tr > th > span");
      if (!es.isEmpty()) {
        result.setReward(es.text().trim());
      }
      resultMap.put(pageid, result);
      unvisitlink.add(detaillink + pageid);
    }
    event.put(Event.unVisitedLinks_key, unvisitlink);
    event.put(Event.results_key, resultMap);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [RoyidaiLinkFetcher] from ["+url+"].");
  }

}
