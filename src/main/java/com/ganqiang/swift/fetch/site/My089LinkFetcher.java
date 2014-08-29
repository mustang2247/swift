package com.ganqiang.swift.fetch.site;

import java.io.File;
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
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class My089LinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(My089LinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [My089LinkFetcher] from ["+url+"].");
    String path = Constants.inside_avatar_path_map.get(key);
    ConcurrentQueue<String> unvisitlink = new ConcurrentQueue<String>();
    Map<String, Result> resultMap = new HashMap<String, Result>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    Document doc = Jsoup.parse(fp.getContent());
    String homepage = Constants.seed_map.get(type).getHomePage();
    String logo = Constants.seed_map.get(type).getLogo();
    Elements tables = doc.select("dl[class=LoanList]");
    for (int i = 1; i < tables.size(); i++) {
      Element ele = tables.get(i);
//      if(ele.className().contains("yema") || ele.className().contains("clear")){
//        continue;
//      }
      Result result = new Result(type);

      Elements aes = ele.select("a[href*=sid]");
      String link = aes.attr("href");
      result.setUrl(homepage + "Loan/" +link);
      result.setName(aes.text());
      String pageid = link.split("=")[1];
      
      result.setId(pageid.toString());

      Elements imges = ele.select("img[src]");
      String imgsrc = imges.attr("src");
      String filename = "";
      if (imgsrc.contains("http://")) {
        filename = path + pageid + ".jpg";
        File img = new File(filename);
        if (!img.exists()) {
          filename =  HttpHelper.downloadImage(instanceid, imgsrc,true, filename);
        }
      } else {
        if (imgsrc.contains("icon_notimage.gif")) {
          filename = FileUtil.downloadAvatar(instanceid, type, path, "noavatar", logo, homepage + StringUtil.getAbsolutePath(imgsrc));
        } else {
          filename = FileUtil.downloadAvatar(instanceid, type, path, pageid, logo, homepage + StringUtil.getAbsolutePath(imgsrc));
        }
      }
      result.setAvatar(instanceid, filename);

      String classname = ele.select("b[class*=Sub]").first().className();
      String category = "";
      if ("SubL1".equals(classname)) {
        category = "信用标";
      } else if ("SubL20".equals(classname)) {
        category = "秒还标";
      } else if ("SubL90".equals(classname)) {
        category = "净值标";
      } else if ("SubL10".equals(classname)) {
        category = "担保标";
      } else if ("SubL30".equals(classname)) {
        category = "重组标";
      } else if ("SubL60".equals(classname)) {
        category = "推荐标";
      } else if ("SubL50".equals(classname)) {
        category = "快借标";
      } else if ("SubL110".equals(classname)) {
        category = "资产标";
      } else if ("SubL40".equals(classname)) {
        category = "阳光贷";
      } else if ("SubL80".equals(classname)) {
        category = "成长贷";
      }
      result.setCategory(category);
//      String dengji = ele.select("em[class*=My_VIP_]").attr("class").replaceAll("My_VIP_", "");
//      result.setCreditRating(dengji.replaceAll("信用等级：", ""));
      
      String borrower = ele.select("a[class*=lf]").get(1).text();
      result.setBorrower(borrower);
      String startTime = ele.select("span[class*=lf]").text();
      result.setStartTime(startTime);
      String money = ele.select("dd[class*=dd_3]").select("span[class=number]").text().replaceAll("￥", "").replaceAll("\\,", "");
      result.setMoney(Double.valueOf(money));
      String rate = ele.select("dd[class*=dd_2]").select("span[class=number]").text().replaceAll("%", "");
      if(rate.contains("/日")){
        result.setDayRate(Double.valueOf(rate.replaceAll("/日", "").trim()));
      }else if(rate.contains("/年")){
        result.setYearRate(Double.valueOf(rate.replaceAll("/年", "").trim()));
      }
//      String reward = ddes.get(0).text().replaceAll("奖励：", "").replaceAll("利率：", "");
//      if(!reward.contains("未设投标奖励")){
//        result.setReward(reward);
//      }
      String process = ele.select("dd[class*=dd_6]").text().replaceAll("%", "");
      result.setProgress(Double.valueOf(process));
      
      String[] qhe = ele.select("dd[class*=dd_4]").text().split("/");
      result.setRepayLimitTime(qhe[0]);
      result.setRepayMode(qhe[1]);
      
      
      resultMap.put(pageid, result);
      unvisitlink.add(result.getUrl());
    }
    event.put(Event.results_key, resultMap);
    event.put(Event.unVisitedLinks_key, unvisitlink);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [My089LinkFetcher] from ["+url+"].");
  }


}
