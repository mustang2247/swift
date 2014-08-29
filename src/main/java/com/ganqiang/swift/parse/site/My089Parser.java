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
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.StringUtil;

public class My089Parser implements Parsable
{
  private static final Logger logger = Logger.getLogger(My089Parser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    List<Result> results = new ArrayList<Result>();
    
    for(FetchedPage fetchedPage : fplist){
      String url = fetchedPage.getUrl();
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [My089Parser] begin parse from ["+url+"].");
      String[] spliturl = url.split("=");
      String pageid = spliturl[spliturl.length - 1];
      Document doc = Jsoup.parse(fetchedPage.getContent());
      Result result = resultMap.get(pageid);

      if(result == null){
        continue;
      }

      String dengji = doc.select("a[class=lf]").attr("title").replaceAll("信用等级：", "");
      result.setCreditRating(dengji);
      
      Elements ets = doc.select("p[class=time] > span");
      result.setTotalNum(Integer.valueOf(ets.get(0).text().replaceAll("已投标：", "").replaceAll("笔", "")));
      result.setRemainMoney(Double.valueOf(ets.get(1).text().replaceAll("还需：", "").replaceAll("￥", "").replaceAll("元", "").replaceAll("\\,", "")));
      String remainTime = ets.get(2).text().replaceAll("剩余时间：", "");
      if (!remainTime.equals("已结束")){
        result.setRemainTime(remainTime);
      }
      String status = doc.select("a[class*=Bid_but]").text();
      // 红岭创投对于VIP会员提供本金保障，逾期垫付的服务。
      // http://www.my089.com/Service/Questions.aspx?iType=15&qid=11081711-0137-2860-0005-152317151967
      if (status.contains("还款中")) {
        result.setStatus(Constants.status_hkz);
      } else if(status.contains("立即投标")){
        result.setStatus(Constants.status_tbz);
      } else {
        result.setStatus(status);
      }
      
      result.setDetailDesc(doc.select("div[class*=textbox]").text());
      
      
      if (result.getProgress() != 100 && !StringUtil.isNullOrBlank(result.getRemainTime())) {
        result.setEndTime(DateUtil.getEndTime(result.getRemainTime()));
      }
      
      if (result.getRemainMoney() == null) {
        Double yitou = CalculateUtil.div(CalculateUtil.mul(result.getProgress(), result.getMoney()), 100, 2);
        result.setRemainMoney(CalculateUtil.sub(result.getMoney(),yitou));
      }

      results.add(result);
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [My089Parser] end parse from ["+url+"].");
    }
    if (resultMap!=null && !resultMap.isEmpty()){
      resultMap.clear();
      resultMap = null;
      event.remove(Event.results_key);
    }
    
    return results; 
  }


}
