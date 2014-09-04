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
import com.ganqiang.swift.util.CalculateUtil;
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
    Elements list = doc.select("ul[class=l_con] li");
    for (int i = 0; i < list.size(); i++) {
      Element ele = list.get(i);
      Result result = new Result(type);
      Elements fe = ele.select("p[class=list_tit]");
      result.setName(fe.text());
      Elements title = fe.select("img");
      if(!title.isEmpty()){
    	   String img = title.attr("title");
    	   if(img.contains("天标")){
    		   result.setCategory(img);
    	             }
      			}
      String moneystr = ele.select("div[class*=txt_item] span").get(0).text().replaceAll("￥", "").replaceAll(",", "");
      if(moneystr.contains("万元")){
    	    moneystr = moneystr.replaceAll("万元", "");
    	  	 result.setMoney(CalculateUtil.mul(Double.valueOf(moneystr), 10000));
    		}else{
    			  moneystr = moneystr.replaceAll("元", "");
       	  result.setMoney(Double.valueOf(moneystr));
      			}
      String yearatestr = ele.select("div[class*=pro_item] span").get(0).text().replaceAll("%", "");
      result.setYearRate(Double.valueOf(yearatestr));
      String remainmoney = ele.select("div[class*=last_item]").get(0).select("span").text().replaceAll("%", "");
      if(remainmoney.contains("万元")){
    	    remainmoney = moneystr.replaceAll("万元", "");
  	  	  result.setRemainMoney(CalculateUtil.mul(Double.valueOf(moneystr), 10000));
      }else{
    	    remainmoney = moneystr.replaceAll("元", "");
     	   result.setRemainMoney(Double.valueOf(moneystr));
    			}
      
      String cr = ele.select("div[class*=txt_item]").get(1).select("img").attr("src");
      if(cr.contains("ico_1")){
    	  	result.setCreditRating("HR");
     	}else if(cr.contains("ico_2")){
    	  	result.setCreditRating("E");
      }else if(cr.contains("ico_3")){
    	  	result.setCreditRating("D");
     	}else if(cr.contains("ico_4")){
    	  	result.setCreditRating("C");
     	}else if(cr.contains("ico_5")){
    	  	result.setCreditRating("B");
      }else if(cr.contains("ico_6")){
    	  	result.setCreditRating("A");
      }else if(cr.contains("ico_7")){
    	  	result.setCreditRating("AA");
      			}
      
      String progress = ele.select("div[class*=pro_item]").get(1).select("div[class=progress_num]").text().replaceAll("%", "");
      result.setProgress(Double.valueOf(progress));
      
      String repayLimitTime = ele.select("div[class*=last_item]").get(1).select("span").text();
      result.setRepayLimitTime(repayLimitTime);
      
      String state = ele.select("div[class=list_wrap] a").text();
      if(state.contains("立即认购")){
    	   result.setStatus(Constants.status_tbz);
     	}else if(state.contains("还款中")){
    	   result.setStatus(Constants.status_hkz);
      }else if(state.contains("还款完成")){
    	   result.setStatus(Constants.status_yhw);
      			}
    		  
      Element aes = ele.select("a[href*=financeDetail.do?id]").first();
      String link = aes.attr("href");
      String pageid = link.split("=")[1];
      resultMap.put(pageid, result);
      unvisitlink.add(detaillink + pageid);
    }
    event.put(Event.unVisitedLinks_key, unvisitlink);
    event.put(Event.results_key, resultMap);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [RoyidaiLinkFetcher] from ["+url+"].");
  }

}
