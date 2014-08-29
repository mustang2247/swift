package com.ganqiang.swift.parse.site;

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
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class PpdaiParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(PpdaiParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    SiteType type = seed.getType();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    String logo = Constants.seed_map.get(type).getLogo();
    for(FetchedPage fetchedPage : fplist){
      String url = fetchedPage.getUrl();
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpdaiParser] begin parse from ["+url+"].");
      String[] spliturl = url.split("/");
      String pageid = spliturl[spliturl.length - 1];

      Result result = resultMap.get(pageid);
      if(result == null){
        continue;
      }
      
      if (!StringUtil.isNullOrBlank(result.getAvatar()) && result.getAvatar().contains("App_Themes")) {
        result.setAvatar(null);
      }
      String filename = FileUtil.downloadAvatar(instanceid, SiteType.PPDAI, path, pageid, logo,result.getAvatar());
      result.setAvatar(instanceid, filename);
      result.setUrl(url);
      Document doc = Jsoup.parse(fetchedPage.getContent());
      
      String catagory = "";
      Elements eles = doc.getElementsByTag("i");
      if(!eles.isEmpty()){
        for(Element e : eles){
          if(e.className().equals("fei")){
            catagory += "非提现标,";
          }else if(e.className().equals("an")){
            catagory += "应收款安全标,";
          }else if(e.className().equals("pei")){
            catagory += "审错就赔付标,";
          }
        }
      }
      if(!StringUtil.isNullOrBlank(catagory)){
        result.setCategory(catagory.substring(0,catagory.length()-1));
      } else {
        result.setCategory("普通标");
      }

      Elements right = doc.select("i[title=信用等级]");
      Elements down = doc.select("div[class=lend_detail_info] ul li");
      String borrower = doc.select("div[class=user_face_name] a").text();
      result.setDetailDesc(down.text().replaceAll("借款详情", ""));
      result.setBorrower(borrower);
      if (!right.isEmpty() && right.first() != null) {
        String creditRating = right.first().className();
        result.setCreditRating(creditRating);
      }
      String name = doc.select("td[class=list_tit]").text();
      result.setName(name);
      String money = doc.select("span[id=TotalAmount]").text().replaceAll(",", "").replaceAll("¥", "");
      if (!StringUtil.isNullOrBlank(money)) {
        result.setMoney(Double.valueOf(money));
      }
      
      String yearRate = doc.select("li[style=margin-top: 9px;] span").text().replaceAll("%", "");
      if (!StringUtil.isNullOrBlank(yearRate)) {
        result.setYearRate(Double.valueOf(yearRate));
      }
      String repayLimitTime = doc.select("li[style=margin-top: 7px; padding-bottom: 0px;] span").text().replaceAll(" ", "");
      result.setRepayLimitTime(repayLimitTime);
      Elements els = doc.select("td[width=330]");
      if (!els.isEmpty() &&  els.first() != null) {
        String[] repayMode = els.first().text().split("\\：");
        result.setRepayMode(repayMode[0].replaceAll("，", ",").trim());
        if (result.getRepayMode().contains("每月还款")) {
          result.setRepayPerMonth(Double.valueOf(repayMode[1].replaceAll(",", "").replaceAll("¥", "").trim()));
        }
      }
      
      String remainTime = doc.select("span[id=leftTime]").text();
      if (!StringUtil.isNullOrBlank(remainTime)) {
        if(remainTime.contains("结束时间")){
          result.setEndTime(remainTime.split("结束时间：")[1].replaceAll("/", "-")+" 00:00:00");
        } else {
          result.setRemainTime(remainTime);
        }
        if(StringUtil.isNullOrBlank(result.getEndTime())){
          if(!result.getRemainTime().contains("月")){
            remainTime = "0月"+result.getRemainTime();
          }
          if (!remainTime.contains("钟")) {
            result.setEndTime(DateUtil.getEndTime(remainTime)); 
          }
        }
      }

      String process = doc.select("div[id=progress]").text().trim().replaceAll("%", "");
      if (!StringUtil.isNullOrBlank(process)) {
        result.setProgress(Double.valueOf(process.replaceAll(",", "")));
        if(result.getProgress() < 100d){
          result.setStatus(Constants.status_tbz);
        }else if(result.getProgress() == 100d){
          result.setStatus(Constants.status_hkz);
        }
      }
      String remainMoney = doc.select("span[id=NeedAmount]").text().trim().replaceAll(",", "").replaceAll("¥", "");
      if (!StringUtil.isNullOrBlank(remainMoney)) {
        result.setRemainMoney(Double.valueOf(remainMoney));
      }
      Elements eel = doc.select("table[class=float_l] > tbody > tr > td > span");
      if (!eel.isEmpty() && eel.size() == 4) {
        String totalnum = eel.get(3).text().split("\\|")[0].replaceAll("总投标数：", "");
        if (!StringUtil.isNullOrBlank(totalnum)) {
          result.setTotalNum(Integer.valueOf(totalnum.trim()));
        }
      }
      

      
//      http://help.ppdai.com/helpdetail/335
//      http://help.ppdai.com/helpdetail/282
      
      results.add(result);
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpdaiParser] end parse from ["+url+"].");
    }
    if (resultMap!=null && !resultMap.isEmpty()){
      resultMap.clear();
      resultMap = null;
      event.remove(Event.results_key);
    }
    return results; 
  }
  


}
