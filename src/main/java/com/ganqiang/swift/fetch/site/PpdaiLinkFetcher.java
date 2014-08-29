package com.ganqiang.swift.fetch.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.ganqiang.swift.util.StringUtil;

public final class PpdaiLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(PpdaiLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    String homepage = Constants.seed_map.get(type).getHomePage();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [PpdaiLinkFetcher] from ["+url+"].");
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    Map<String, Result> resultMap = new HashMap<String, Result>();
    String prefix = Constants.seed_map.get(type).getDetailLinkMark();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    Document doc = Jsoup.parse(fp.getContent());
    Elements as = doc.getElementsByTag("a");
    Elements imgs = doc.getElementsByTag("img");
    List<String> imgList = new ArrayList<String>();
    for(Element img : imgs){
      if(img.attr("width").equals("52")){
        imgList.add(img.attr("src"));
      }
    }
    int i = 0;
    for (Element link : as) {
      String detailurl = link.attr("href");
      if (detailurl.contains(prefix)) {
        String reurl = link.attr("href");
        if (StringUtil.isNullOrBlank(reurl)) {
          continue;
        }
        reurl = StringUtil.getAbsolutePath(reurl);
        String absurl = homepage + reurl;
        String src = imgList.get(i);
        String[] array = reurl.split("/");
        Result result = new Result(type);
        result.setId(array[array.length-1]);
        result.setAvatar(instanceid, src);
        resultMap.put(result.getId(), result);
        i ++ ;
//        URLQueue.addUnVisitedLink(key, absurl);
        unvisitlink.add(absurl);
        logger.info("Worker [" + Thread.currentThread().getName() + "] [PpdaiLinkFetcher] have already extracted link: ["+absurl+"].");
      }
    }
    event.put(Event.results_key, resultMap);
    event.put(Event.unVisitedLinks_key, unvisitlink);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [PpdaiLinkFetcher] from ["+url+"].");
  }

  public static void main(String... args){
    String str = "asf.jp";
    System.out.println(str.split("\\_")[0]);
  }

}
