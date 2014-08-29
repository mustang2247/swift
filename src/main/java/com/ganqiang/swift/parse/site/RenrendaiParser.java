package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;

public final class RenrendaiParser implements Parsable{

  private static final Logger logger = Logger.getLogger(RenrendaiParser.class);

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
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiParser] begin parse from ["+url+"].");
        String id = url.split("=")[1];
        
        Document doc = Jsoup.parse(fetchedPage.getContent());
        Element ele = doc.getElementById("credit-info-data");
        if (ele == null) {
          continue;
        }
        String json = ele.html();
        JSONObject jsonObject = new JSONObject(json);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject obj = data.getJSONObject("loan");

        Result result = new Result(type);
        result.setUrl(url);
        result.setMoney(obj.getDouble("amount"));
        result.setName(obj.getString("title"));
        result.setYearRate(obj.getDouble("interest"));
        result.setRepayLimitTime(obj.getInt("months") + "个月");
        result.setBorrower(obj.getString("nickName"));
        result.setProgress(CalculateUtil.getDoubleHalfValue(obj.getDouble("finishedRatio"),0));
        result.setRemainMoney(obj.getDouble("surplusAmount"));
        result.setCreditRating(obj.getString("borrowerLevel"));
        String status = obj.getString("status");
        if(status.equals("IN_PROGRESS")){
          result.setStatus(Constants.status_hkz);
        } else if(status.equals("FIRST_READY")){
          result.setStatus(Constants.status_ymb);
        } else if(status.equals("OPEN")){
          result.setStatus(Constants.status_tbz);
        }
        String startTime = "";
        if(obj.has("beginBidTime")){
          startTime = obj.getString("beginBidTime");
        } else {
          startTime = obj.getString("openTime");
        }
        result.setStartTime(DateUtil.parse(startTime,DateUtil.renrendai_format));
        // 投标结束时间有可能是openTime，passTime，readyTime
        if (!result.getStatus().equals(Constants.status_tbz)) {
          result.setEndTime(DateUtil.parse(obj.getString("startTime"),DateUtil.renrendai_format));
        }
        if(result.getProgress() < 100d){
          String remainTime = ele.select("span[class=basic-progress-time]").text().trim().replaceAll("剩余时间", "");
          result.setRemainTime(remainTime);
        }
        result.setDetailDesc(obj.getString("description"));

        result.setAgency(obj.getString("productName"));

//        String avatar = obj.getString("avatar");
//        String pic = obj.getString("picture");
//        if (!StringUtil.isNullOrBlank(avatar)) {
//          avatar = site + StringUtil.getAbsolutePath(avatar);
//        }
//        if (!StringUtil.isNullOrBlank(pic)) {
//          pic = site + StringUtil.getAbsolutePath(pic);
//        }
//        String filename = FileUtil.downloadAvatar(instanceid, type, path, id, SiteCache.renrendai_logo, avatar, pic);
        
        String filename = FileUtil.downloadAvatar(instanceid, type, path, id, logo);        
        result.setAvatar(instanceid, filename);
        Element title = doc.select(".ui-box-title").select(".fn-clear").first().child(0);
        result.setCategory(title.attr("title"));
        Element up = doc.select(".p20").select(".fn-clear").first();
        String text = up.text().split("保障方式")[1].split("详情参见")[0];
        StringTokenizer st = new StringTokenizer(text.trim());
        int i = 0;
        while(st.hasMoreElements()){
          String str = st.nextElement().toString();
          if(i == 0){
            result.setSecurityMode(str);
          } else if(i == 5){
            result.setRepayMode(str);
          } else if(i == 8){
            result.setRepayPerMonth(Double.valueOf(str.replaceAll(",", "")));
          }
          i ++ ;
        }
//        String sss = up.text().split("待还本息（元）")[1].split("剩余期数")[0]; //待还本息
//        result.setRepayRemainMoney(sss.replaceAll("￥", "").replaceAll(",", ""));

//        在人人贷上的借款都是以借款人的信用评级为基础的、不涉及到抵押物及其他人的担保。
//        http://www.renrendai.com/help/borrow.action
//        result.setAgency(null);

//        查看投标记录，需要登陆

//        String totalnum = doc.select("dd[class*=num-xl]").text();
//        result.setTotalNum(Integer.valueOf(totalnum));
        results.add(result);

        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [RenrendaiParser] execute failure. ",e);
    }
    
    
    return results; 
  }
  
}
