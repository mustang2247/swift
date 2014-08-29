package com.ganqiang.swift.fetch.site;

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
import com.ganqiang.swift.util.ConcurrentQueue;
import com.ganqiang.swift.util.StringUtil;

public final class CommonLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(CommonLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    String homepage = Constants.seed_map.get(type).getHomePage();
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [CommonLinkFetch] begin fetch link from ["+url+"].");
    String[] prefix = Constants.seed_map.get(type).getDetailLinkMark().split("\\"+Constants.split_str);
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    Document doc = Jsoup.parse(fp.getContent());
    Elements as = doc.getElementsByTag("a");
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    for (Element link : as) {
      String detailurl = link.attr("href");
      for(String pstr : prefix){
        if (detailurl.contains(pstr)) {
          if (StringUtil.isNullOrBlank(detailurl)) {
            continue;
          }
          detailurl = StringUtil.getAbsolutePath(detailurl);
          String absurl = "";
          if (detailurl.contains("http://") || detailurl.contains("https://")) {
            absurl = detailurl;
          } else {
            absurl = homepage + detailurl;
          }
          if (unvisitlink.contains(absurl)) {
            continue;
          }
          unvisitlink.add(absurl);
          logger.info("Worker [" + Thread.currentThread().getName() + "] --- [CommonLinkFetch] have already extracted link ["+absurl+"].");
        }
      }
    }
    event.put(Event.unVisitedLinks_key, unvisitlink);
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [CommonLinkFetch] end fetch link from ["+url+"].");
  }

}
