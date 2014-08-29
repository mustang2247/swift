package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public class YooliParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(YooliParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    SiteType type = seed.getType();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [YooliParser] begin parse from ["+url+"].");

        Result result = new Result(type);
        result.setUrl(url);
        result.setBorrower("月息通");
        Document doc = Jsoup.parse(fetchedPage.getContent());
        String totalnum = doc.select("span[class*=r5]").text();
        if (!StringUtil.isNullOrBlank(totalnum)) {
          result.setTotalNum(Integer.valueOf(totalnum));
        }
        Elements ag = doc.select("div[class*=agency-logo] img");
        if(!ag.isEmpty() && ag.first() != null){
          String agent = ag.first().attr("alt").trim();
          result.setAgency(agent);
        }
        String securityMode = doc.select("div[class*=agency-logo] div").text().trim();
        if (!StringUtil.isNullOrBlank(securityMode)) {
          result.setSecurityMode(securityMode);
        }
        String creditRating = doc.select("div[class*=credit-rating] strong").text().trim();
        if (!StringUtil.isNullOrBlank(creditRating)) {
          result.setCreditRating(creditRating);
        }
        String name = doc.select("div[class=model-box] div[class*=head] h2").text();
        if (!StringUtil.isNullOrBlank(name)) {
          result.setName(name);
        }
        String desc = doc.select("div[class=explain]").text().replaceAll("借款用途说明", "").replaceAll("\\.\\.\\.", "").replaceAll("\\[详情\\]", "");
        if (!StringUtil.isNullOrBlank(desc)) {
          result.setDetailDesc(desc.trim());
        }
        String rm = doc.select("div[class=doned]").text().replaceAll("可投资金额：", "").replaceAll("元", "").replaceAll("剩余", "");
        if (!StringUtil.isNullOrBlank(rm)) {
          Double remainMoney = Double.valueOf(rm);
          result.setRemainMoney(remainMoney);
        }
        
        String status = "";
        if (url.contains("yuexitong/detail/")){
          result.setCategory("普通标");
          String money = doc.select("dl[class=f] dd em").text().replaceAll("\\,", "");
          if (!StringUtil.isNullOrBlank(money)) {
            result.setMoney(Double.valueOf(money));
          }
          String yearRate = doc.select("div[class=profit] dl").get(1).select("dd").text().replaceAll("%", "");
          if (!StringUtil.isNullOrBlank(yearRate)) {
            result.setYearRate(Double.valueOf(yearRate));
          }
          String repayLimitTime = doc.select("div[class=profit] dl").get(2).select("dd").text();
          if (!StringUtil.isNullOrBlank(repayLimitTime)) {
            result.setRepayLimitTime(StringUtil.strikeBlankString(repayLimitTime));
          }
          String process = doc.select("strong[class=ratio]").text().replaceAll("%", "");
          if (!StringUtil.isNullOrBlank(process)) {
            result.setProgress(Double.valueOf(process));
          }
          String remainTime = doc.select("div[class=expl] ul li").get(1).text().replaceAll("时间剩余", "").trim();
          result.setRemainTime(remainTime);
          String endtime = doc.select("div[class*=iwant-in] span[class*=time]").text().replaceAll("售完", "");
          if (!StringUtil.isNullOrBlank(endtime)) {
            result.setEndTime(DateUtil.parse(endtime, DateUtil.yooli_format));
          }
          status = doc.select("div[class*=iwant-in] span[class*=gbtn]").text();
          result.setRepayMode("每月等额本息还款");
          String repaypermonth = doc.select("span[class=t] em").text().replaceAll(",", "");
          if (!StringUtil.isNullOrBlank(repaypermonth)) {
            result.setRepayPerMonth(Double.valueOf(repaypermonth));
          }
          
        } else if(url.contains("yuexitong/zhuan/")){
          result.setCategory("转让标");
          Elements user = doc.select("div[class=pro-user] dl");
          String money = user.get(1).select("dd em").text().replaceAll("\\,", "");
          if (!StringUtil.isNullOrBlank(money)) {
            result.setMoney(Double.valueOf(money));
          }
          
//          Double sjrate = Double.valueOf(user.get(2).select("dt em").text());
          String yrate = user.get(2).select("dd em").text();
          if (!StringUtil.isNullOrBlank(yrate)) {
            Double yearate = Double.valueOf(yrate);
            result.setYearRate(yearate);
          }
          
//          result.setReward(CalculateUtil.sub(sjrate, yearate)+"%");
          String remainTime = user.get(3).select("dt p").text();
          result.setRemainTime(remainTime);
          String repayLimitTime = user.get(3).select("dd p").text();
          result.setRepayLimitTime(repayLimitTime);
          status = doc.select("div[class*=iwant-in] a").text();
        }

        if (status.contains(Constants.status_yzr)) {
          result.setStatus(Constants.status_yzr);
        } else if (status.contains(Constants.status_ysw)) {
          result.setStatus(Constants.status_ysw);
        } else if (status.contains(Constants.status_hkz)) {
          result.setStatus(Constants.status_hkz);
        } else if (status.contains("投资")) {
          result.setStatus(Constants.status_tbz);
        } else {
          result.setStatus(status);
        }
        
        String filename = FileUtil.downloadAvatar(instanceid, SiteType.YOOLI, path, "", logo);
        result.setAvatar(instanceid, filename);
        results.add(result);
             
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [YooliParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [YooliParser] execute failure. ",e);
    }
    
    return results; 
  }
  
}
