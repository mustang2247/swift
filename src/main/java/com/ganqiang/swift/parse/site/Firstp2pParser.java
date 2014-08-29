package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.FileUtil;

public class Firstp2pParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(Firstp2pParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [Firstp2pParser] begin parse from ["+url+"].");
        String[] array = url.split("/");
        String pageid = array[array.length -1];
        Result result = resultMap.get(pageid);
        if(result == null){
          continue;
        }
        result.setUrl(url);
        Document doc = Jsoup.parse(fetchedPage.getContent());

        String agency= doc.select("a[href*=/agency/]").text().replaceAll("\\(", "").replaceAll("\\)", "");
        result.setAgency(agency);

        if (!result.getCategory().equals("个人贷") && !result.getCategory().equals("车贷")) {
          String desc = "";
          if (result.getCategory().equals("房贷")) {
            desc = doc.select("td[colspan=7]").text();
          } else {
            desc = doc.select("div[style=overflow:hidden;width:760px;]").text();
          }
          result.setDetailDesc(desc);
        }
        

        String filename = FileUtil.downloadAvatar(instanceid, type, path, "", logo);
        result.setAvatar(instanceid, filename);
        Elements es = doc.select("tr[class*=f14]");
        result.setTotalNum(es.size());
        results.add(result);
        
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [Firstp2pParser] end parse from ["+ url +"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [Firstp2pParser] execute failure. ",e);
    }
    
    return results; 
  }

}
