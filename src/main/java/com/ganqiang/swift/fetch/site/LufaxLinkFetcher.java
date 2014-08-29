package com.ganqiang.swift.fetch.site;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.ConcurrentQueue;

public final class LufaxLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(LufaxLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [LufaxLinkFetcher] begin fetch from ["+url+"].");
    Map<String, Result> resultMap = new HashMap<String, Result>();
//    String key = instanceid + type;
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    String html = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy).getContent();
    String detaillink = Constants.seed_map.get(type).getPreDetailLink();
    try {
      JSONObject jsonObject = new JSONObject(html);
      JSONArray datas = jsonObject.getJSONArray("data");
      for (int i = 0; i < datas.length(); i++) {
        JSONObject obj = datas.getJSONObject(i);
        Long pageid = obj.getLong("productId");
        String link = detaillink + pageid;
        Result result = new Result(type);
        result.setId(pageid.toString());
        result.setName(obj.getString("productNameDisplay")+obj.getString("code"));
        result.setRepayMode(obj.getString("collectionModeDisplay"));
        result.setMoney(obj.getDouble("principal"));
        result.setYearRate(CalculateUtil.mul(obj.getDouble("interestRateDisplay"), 100));
        String flag = obj.getString("tradingMode");
        String category = "";
        if(flag.equals("00")){
          category = "转让标";
        }else if(flag.equals("06")){
          category = "竞拍标";
        }else if(flag.equals("07")){
          category = "普通标";
        }
        result.setCategory(category);
        result.setRemainMoney(obj.getDouble("remainingAmount"));
        result.setRepayLimitTime(obj.getString("investPeriodDisplay"));
        String beginTime = obj.getString("publishedAt")+" "+obj.getString("publishAtDateTime");
        result.setStartTime(beginTime);
//        Date endTime = DateUtil.getExpiryDate(obj.getString("investPeriodDisplay").replaceAll("个月", ""));
//        result.setEndTime(DateUtil.parse(endTime));
        String progress = obj.getString("productStatus");
        if(progress.equals("DONE")){
//          result.setProgress(100d);
          // 交易成功
          result.setStatus(Constants.status_ymb);
        }else if(progress.equals("ONLINE")){
          if(flag.equals("06")){
            result.setStatus(Constants.status_jpz);
          }else if(flag.equals("07")){
            // 投资
            result.setStatus(Constants.status_tbz);
          }
//          result.setRemainTime(DateUtil.getRemainTime(endTime, DateUtil.strToDate(beginTime)));
        }
        resultMap.put(result.getId(), result);
        unvisitlink.add(link);
//        URLQueue.addUnVisitedLink(key, link);
      }
      event.put(Event.results_key, resultMap);
      event.put(Event.unVisitedLinks_key, unvisitlink);
    } catch (Exception e) {
      e.printStackTrace();
      logger.info("fetch [RenrendaiLinkFetcher] url : [" + url + "] failure.");
    }

    logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpdaiLinkFetcher] end fetch from ["+url+"].");
  }

  public static void main(String... args){
//    String str = "http://list.lufax.com/list/productDetail?productId=80253";
//    System.out.println(HttpUtil.getContentFromUrl(str));
  }

}
