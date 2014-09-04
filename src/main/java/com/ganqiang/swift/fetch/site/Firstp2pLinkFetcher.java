package com.ganqiang.swift.fetch.site;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;

public final class Firstp2pLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(Firstp2pLinkFetcher.class);
  
  public static void main(String... args){
    System.out.println("\u8054\u5408\u521b\u4e1a\u62c5\u4fdd\u96c6\u56e2\u6709\u9650\u516c\u53f8");
  }
  
  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [Firstp2pLinkFetcher] from ["+url+"].");
    Map<String, Result> resultMap = new HashMap<String, Result>();
//    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, false);
    JSONObject jsonObject = new JSONObject(fp.getContent());
    String detaillink = Constants.seed_map.get(type).getPreDetailLink();
    String logo = Constants.seed_map.get(type).getLogo();
    String path = Constants.inside_avatar_path_map.get(key);
    JSONArray tables = jsonObject.getJSONArray("data");
    for (int i = 0; i < tables.length(); i++) {
      JSONObject obj = tables.getJSONObject(i);
      Result result = new Result(type);
      result.setCategory(obj.getString("type"));
      String pageid = obj.getInt("productID") + "";
      result.setUrl(detaillink + pageid);
      result.setName(obj.getString("title"));
      String money = obj.getString("total").replaceAll("\\,", "");
      if (money.contains("万")) {
        money = money.replaceAll("万", "");
        result.setMoney(CalculateUtil.mul(Double.valueOf(money), 10000));
      } else {
        result.setMoney(Double.valueOf(money));
      }

      int security = obj.getInt("warrant");
      if (security == 0) {
        result.setSecurityMode("无担保");
      } else if(security == 1){
        result.setSecurityMode("担保本金");
      } else if(security == 2){
        result.setSecurityMode("担保本息");
      }
      Double yearrate = obj.getDouble("income_base_rate");
      result.setYearRate(yearrate);
      Double reward = obj.getDouble("income_ext_rate");
      result.setReward(reward + "%");

      String repaylimittime = obj.getString("timelimit");
      result.setRepayLimitTime(repaylimittime);

      String repayMode = obj.getString("repayment");
      result.setRepayMode(repayMode);

      int status = obj.getInt("stats");
      if (status == 0) {
        result.setStatus(Constants.status_dqr);
      } else if (status == 1) {
        result.setStatus(Constants.status_tbz);
      } else if (status == 2) {
        result.setStatus(Constants.status_ymb);
      } else if (status == 3) {
        result.setStatus(Constants.status_ylb);
      } else if (status == 4) {
        result.setStatus(Constants.status_hkz);
      } else if (status == 5) {
        result.setStatus(Constants.status_yhw);
      }

      Object percent = obj.get("point_percent");
      if(percent != null && !percent.toString().equals("null")){System.out.println(percent);
		    	  Double progress = CalculateUtil.mul(CalculateUtil.getDoubleHalfValue(obj.getDouble("point_percent")), 100);
          result.setProgress(progress);
          if (progress == 0) {
            result.setRemainMoney(result.getMoney());
          } else if(progress == 100){
            result.setRemainMoney(Constants.zero_remain_money);
          } else {
            result.setRemainMoney(Double.valueOf(obj.getString("avaliable").replaceAll(",", "")));
          					}
     	}else{
     		  Double avalible = Double.valueOf(obj.get("avaliable").toString().replaceAll(",", ""));
      		 result.setProgress(CalculateUtil.mul(CalculateUtil.div(avalible, result.getMoney(), 2), 100));;
      			}

      String startTime = obj.getString("start_time") + ":00";
      result.setStartTime(startTime);

      String endTime = obj.getString("end_time") + ":00";
      result.setEndTime(endTime);
      if (result.getProgress() > 0 && result.getProgress() < 100) {
        String remainTime = DateUtil.getRemainTime(DateUtil.parse(endTime));
        result.setRemainTime(remainTime);
      			}

      Object desc = obj.get("description");
      if(desc != null && !desc.toString().trim().equals("null")){
    	  		result.setDetailDesc(desc.toString());
      			}

      String agency = obj.getString("agency");
      result.setAgency(agency);

      String filename = FileUtil.downloadAvatar(instanceid, type, path, "", logo);
      result.setAvatar(instanceid, filename);

      Object totalnum = obj.get("buy_count");
      if(totalnum != null && !totalnum.toString().trim().equals("null")){
    	  		result.setTotalNum(Integer.valueOf(totalnum.toString()));
      			}
      
      String remainTime = obj.getString("remain_time");
      result.setRemainTime(remainTime);

      resultMap.put(pageid, result);
    }
    event.put(Event.results_key, resultMap);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [Firstp2pLinkFetcher] from ["+url+"].");
  }

//  @Override
//  public void fetch(Event event)
//  {
//    Seed seed = (Seed) event.get(Event.seed_key);
//    String url = seed.getListUrl();
//    SiteType type = seed.getType();
//    String instanceid = seed.getId();
//    String key = seed.getKey();
//    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [Firstp2pLinkFetcher] from ["+url+"].");
//    Map<String, Result> resultMap = new HashMap<String, Result>();
//    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
//    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
//    Document doc = Jsoup.parse(fp.getContent());
//    String mark = Constants.seed_map.get(type).getDetailLinkMark();
//    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
//    Elements tables = doc.select("div[class*=product_bd2] table tbody").first().children();
//    for (int i = 0; i < tables.size(); i++) {
//      Element ele = tables.get(i);
//      Result result = new Result(type);
//
//      String aes = ele.select("i[class*=icon_]").attr("class");
//      if (aes.contains("car")) {
//        result.setCategory("车贷");
//      } else if (aes.contains("personal")) {
//        result.setCategory("个人贷");
//      } else if (aes.contains("room")) {
//        result.setCategory("房贷");
//      } else if (aes.contains("melting")) {
//        result.setCategory("产融贷");
//      } else if (aes.contains("enterprise")) {
//        result.setCategory("企业贷");
//      } else if (aes.contains("assets")) {
//        result.setCategory("资产转让");
//      }
//      Elements e1 = ele.select("div[class*=pro_name]");
//      String link = e1.select("p a").attr("href");
//      result.setUrl(link);
//      result.setName(e1.select("p a").attr("title"));
//      String pageid = link.split(mark)[1];
//
//      Elements e2 = ele.select("div[class*=pro_links]");
//      String money = e2.text().replaceAll("总额：", "").replaceAll("\\,", "");
//      if (money.contains("万")) {
//        money = money.replaceAll("万", "");
//        result.setMoney(CalculateUtil.mul(Double.valueOf(money), 10000));
//      } else {
//        result.setMoney(Double.valueOf(money));        
//      }
//      String security = e2.select("i[class=badge]").attr("title");
//      result.setSecurityMode(security);
//      
//      Elements e3 = ele.select("p.btm").select(".f14").select(".tc");
//      
//      String rate = e3.get(0).text();
//      if (rate.contains("+")) {
//        String[] array = rate.split("\\+");
//        result.setYearRate(Double.valueOf(array[0].replaceAll("%", "")));
//        result.setReward(array[1]);
//      } else {
//        result.setYearRate(Double.valueOf(rate.replaceAll("%", "")));
//      }
//      
//      String repaylimittime = e3.get(1).text();
//      result.setRepayLimitTime(repaylimittime);
//      
//      String repayMode = ele.select("p.date").select(".tc").text();
//      result.setRepayMode(repayMode);
//      
//      String status = ele.select("div[class=table_cell]").text();
//      if (status.contains("投资")) {
//        result.setStatus(Constants.status_tbz);
//      } else if (status.contains("还款中")) {
//        result.setStatus(Constants.status_hkz);
//      } else if (status.contains("确认") || status.contains("查看")) {
//        result.setStatus(Constants.status_dqr);
//      } else if (status.contains("满标")) {
//        result.setStatus(Constants.status_ymb);
//      } else if (status.contains("已还")) {
//        result.setStatus(Constants.status_yhw);
//      } else if (status.contains("流标")) {
//        result.setStatus(Constants.status_ylb);
//      }
//      
//
//      Elements e4 = ele.select("em[class=color-yellow1]");
//      if (!e4.isEmpty()) {
//        Double remainmoney = Double.valueOf(e4.text().replaceAll("元", "").replaceAll("\\,", ""));
//        if (result.getStatus().equals(Constants.status_ymb)) {
//          result.setRemainMoney(Constants.zero_remain_money);
//        } else {
//          result.setRemainMoney(remainmoney);
//          result.setProgress(result.getMoney(), remainmoney);
//        }
//      } else {
//        if (!result.getStatus().equals(Constants.status_dqr)) {
//          result.setRemainMoney(Constants.zero_remain_money);
//        } else {
//          result.setRemainMoney(result.getMoney());
//          result.setProgress(0d);
//        }
//      }
//      if(result.getProgress() == 100){
//        result.setRemainTime(Constants.zero_remain_time);
//        String endTime = ele.select("div[class=pl40] p").get(1).text().replaceAll("成功时间：", "").replaceAll("月", "-").replaceAll("日", "");
//        if (!endTime.contains("年")) {
//          Calendar calendar = Calendar.getInstance();
//          int year = calendar.get(Calendar.YEAR);
//          endTime = year + "-"+endTime;
//        }
//        result.setEndTime(DateUtil.parse(endTime, DateUtil.yyyyMMdd));
//      } else {
//        System.out.println("====================="+result.getName());
//        if (result.getStatus().equals(Constants.status_dqr)) {
//          String beginTime = ele.select("div[class=pl40] p").text().replaceAll("开始时间：", "").replaceAll("等待确认", "");
//          result.setStartTime(DateUtil.getYear() + "-" + beginTime + ":00");
//        } else {
//          String remainTime = ele.select("div[class=pl40] p").get(1).text().replaceAll("剩余时间：", "");
//          result.setRemainTime(remainTime);
//          result.setEndTime(DateUtil.getEndTime(remainTime));
//        }
//        
//      }
//      
//      
//      unvisitlink.add(link);
//      resultMap.put(pageid, result);
//    }
//    event.put(Event.unVisitedLinks_key, unvisitlink);
//    event.put(Event.results_key, resultMap);
//    logger.info("Worker [" + Thread.currentThread().getName() + "] end [Firstp2pLinkFetcher] from ["+url+"].");
//  }


}
