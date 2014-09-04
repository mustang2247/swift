package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;

public class NiwodaiParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(NiwodaiParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
//    String homepage = Constants.seed_map.get(type).getHomePage();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [NiwodaiParser] begin parse from ["+url+"].");

        Result result = new Result(type);
        result.setUrl(fetchedPage.getUrl());
        Document doc = Jsoup.parse(fetchedPage.getContent());
        String title = doc.select("a[class=fs16]").first().text();
        result.setName(title);
        
        Elements tes =  doc.select("div[class*=pad] span[class*=fs_32]");
        String money = tes.get(0).text().replaceAll(",", "").replaceAll("元", "");
        result.setMoney(Double.valueOf(money));
        
        String yearrate = tes.get(1).text().replaceAll("%", "");
        result.setYearRate(Double.valueOf(yearrate));
        
        String repayLimitTime = tes.get(2).text();
        result.setRepayLimitTime(repayLimitTime+doc.select("div[class*=pad] span[class*=fs_18]").get(2).text());
        
        Elements bdoc = doc.select("ul[class*=line2] li span");
//        Elements ele1 = bdoc.child(0).children();
//        for(Element ele : ele1){
//          
//        }
        String ele2 = bdoc.get(0).text();
        result.setRepayMode(ele2);
        
        Element es = doc.select("ul[class*=line2]").get(1);
        String progress = es.select("span[class*=progressBar] span[class*=barIn]").first().attr("style").replaceAll("width:", "").replaceAll("%", "");
        result.setProgress(Double.valueOf(progress));
        
        String total = es.child(2).text().split("\\：")[1].split("人")[0];
        result.setTotalNum(Integer.valueOf(total.trim()));
        
        if (result.getProgress() != 100) {
          String et = es.child(3).select("span[id=time]").text();
          Date endtime = DateUtil.strToDate(et, "yyyy/MM/dd HH:mm:ss");
          result.setEndTime(DateUtil.dateToStr(endtime));
          result.setRemainTime(DateUtil.getRemainTime(endtime));
        }
        
        String amount = doc.select("span[id=needAmount]").text();
        result.setRemainMoney(Double.valueOf(amount));
        
        String status = doc.select("li[class=line5] a").text();
        if (status.contains("马上投资")) {
          result.setStatus(Constants.status_tbz);
        }else if (status.contains("已经满标")) {
          result.setStatus(Constants.status_ymb);
        }else if (status.contains("借款")){
          result.setStatus(Constants.status_yjk);
        }
        
        String company = doc.select("div[class=r]").select("div[class=no1]").text();
        result.setAgency(company);
        
        
        
//        Elements left = doc.select("ul.list-ul").select(".clearfix").select(".dotted").first().children();
//        String money = "";
//        for (int i=0; i< left.size(); i++) {
//          String text = left.get(i).text().replaceAll("\\：", "");
//          if (i == 0) {
//            money = text.replaceAll("借款金额", "").replaceAll("\\$", "").replaceAll("￥", "").replaceAll(",", "").replaceAll("元", "");
//          } else if (i == 1){
//            result.setYearRate(Double.valueOf(text.replaceAll("借款年利率", "").replaceAll("%", "")));
//          } else if (i == 2){
//            result.setRepayLimitTime(text.replaceAll("借款期限", "").trim());
//          }
//        }
//        if (!money.contains("*")) {
//          result.setMoney(Double.valueOf(money));
//        } else {
//          logger.warn("Worker [" + Thread.currentThread().getName() + "] --- [NiwodaiParser] the money was hidden,so jump this item : ["+url+"]");
//          continue;
//        }

        // 你我贷对出借客户实行100%本息保障计划（注：网商贷只保本金）；
        // http://www.niwodai.com/index.do?method=indexStaticPage1Detail&pid=45&artId=394&acId=47
        result.setSecurityMode("100%本息保障");
        
//        Elements left2 = doc.select("ul.list-ul").select(".clearfix").get(1).children();
//        for (int i=0; i< left2.size(); i++) {
//          String text = left2.get(i).text().replaceAll("\\：", "");
//          if (i == 0) {
//            result.setRepayMode(text.replaceAll("还款方式", "").trim());
//          } else if (i == 1){
//            result.setProgress(Double.valueOf(text.replaceAll("投标进度", "").replaceAll("%", "").trim().replaceAll(" ", "")));
//          } else if (i == 2){
//            result.setRemainMoney(Double.valueOf(text.replaceAll("还需资金", "").split("\\$")[0].trim()));
//          } else if (i == 3){
//            if (result.getProgress() != 100d) {
//              String end = text.replaceAll("剩余时间", "").trim();
//              Date endtime = DateUtil.strToDate(end, "yyyy/MM/dd HH:mm:ss");
//              result.setEndTime(DateUtil.dateToStr(endtime));
//              result.setRemainTime(DateUtil.getRemainTime(endtime));
//            }
//          } else if(i == left2.size() -1){
//            result.setTotalNum(Integer.valueOf(text.split("投标人数/浏览人数")[1].split("人")[0].trim()));
//          }
//        }
//        
//        Elements right = doc.select("div.box-in").select(".module3").get(1).children();
//        String filename = "";
//        for (int i=0; i< right.size(); i++) {
//          Element e = right.get(i);
//          if (i == 0) {
//            filename = e.child(0).attr("src");
//          } else if (i == 1){
//            result.setBorrower(e.text().replaceAll("用户名：", ""));
//          }
//        }
//        
//        if (filename.contains("http://")) {
//          filename = FileUtil.downloadAvatar(instanceid, type, path, "tx", logo, filename);
//        } else if(filename.contains("tx.jpg")){
//          filename = FileUtil.downloadAvatar(instanceid, type, path, "tx", logo, homepage + StringUtil.getAbsolutePath(filename));
//        } else {
//          String pageid = url.split("v-")[1].split("=.html")[0];
//          String downloadurl = homepage + StringUtil.getAbsolutePath(filename);
//          filename = path + pageid + FileUtil.getPicSuffixName(downloadurl);
//          File imgfile = new File(filename);
//          if (!imgfile.exists()) {
//            HttpHelper.downloadImage(instanceid, downloadurl, false, filename);
//          }
//        }
//        result.setAvatar(instanceid, filename);
        
        String filename = FileUtil.downloadAvatar(instanceid, type, path, "", logo);
        result.setAvatar(instanceid, filename);
        
        result.setDetailDesc(doc.select("p[class=content]").get(1).text());
        
        String catelog = doc.select("i[class*=ico_all]").attr("title");
        result.setCategory(catelog);
        
//        String classname = doc.select("i[class^=ico-32]").attr("class").replaceAll("ico-32 ", "");
//        if ("ico-item-7".equals(classname)) {
//          result.setCategory("薪金贷");
//        } else if ("ico-item-9".equals(classname)) {
//          result.setCategory("企业贷");
//        } else if ("ico-item-5".equals(classname)) {
//          result.setCategory("车贷");
//        } else if ("ico-item-100".equals(classname)) {
//          result.setCategory("网商贷");
//        } else if ("ico-item-51".equals(classname)) {
//          result.setCategory("净值贷");
//        } else if ("ico-item-10".equals(classname)) {
//          result.setCategory("物业贷");
//        } else if ("ico-item-14".equals(classname)) {
//          result.setCategory("丽人贷");
//        } else if ("ico-item-12".equals(classname)) {
//          result.setCategory("POS贷");
//        } else if ("ico-item-13".equals(classname)) {
//          result.setCategory("卡易贷");
//        } else if ("ico-item-15".equals(classname)) {
//          result.setCategory("精英贷");
//        } else if ("ico-item-8".equals(classname)) {
//          result.setCategory("商户贷");
//        }
        
//        if (result.getProgress() == 100d) {
//          String button = doc.select("button[style=cursor: default;]").text();
//          if (button.contains("满标")){
//            result.setStatus(Constants.status_ymb);
//          } else if (button.contains("借款")){
//            result.setStatus(Constants.status_yjk);
//          }
//        } else {
//          String button = doc.select("input[type=button]").attr("value");
//          if (button.contains("投标")) {
//            result.setStatus(Constants.status_tbz);
//          }
//        }
        
        
//        http://www.niwodai.com/baozhang/
//        result.setCreditRating("");

        results.add(result);
        
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [NiwodaiParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [NiwodaiParser] execute failure. ",e);
    }
    
    return results; 
  }
  
  public static void main(String... args){

    NiwodaiParser pp = new NiwodaiParser();
    Event event = new Event();
    List<FetchedPage> fplist = new ArrayList<FetchedPage>();
    String url = "http://www.niwodai.com/xiangmu/v-ADRUNlY2VT8FZFRkUDFeaQo7VW0CZgJjBTQFPABlU2E=.html";
    String content = HttpHelper.getContentFromUrl(url);
    fplist.add(new FetchedPage(url, content, 200));
//    event.put(Event.fetchedPages_key, fplist);
//    event.put(Event.siteType, SiteType.NIWODAI);
//    event.put(Event.results, new HashMap<String,Result>());
    pp.parse(event);


  }


}
