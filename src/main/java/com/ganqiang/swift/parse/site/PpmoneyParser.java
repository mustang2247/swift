package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.FileUtil;

public class PpmoneyParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(PpmoneyParser.class);
  private static final String jsonurl = "http://www.ppmoney.com/investment/records?page=0&projectId=";

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    SiteType type = seed.getType();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpmoneyParser] begin parse from ["+url+"].");
        String[] array = url.split("/");
        String pageid = array[array.length -1];
        Result result = resultMap.get(pageid);

        if(result == null){
          continue;
        }
        
        if (!pageid.equals("3509")){
          continue;
        }
        
        result.setUrl(url);
        Document doc = Jsoup.parse(fetchedPage.getContent());
          
        Element title= doc.select(".div1").select(".pr").select(".w").first();
        Elements detail= doc.select(".fproject_explain_main").select(".w");
        result.setDetailDesc(detail.text().trim());

        String investurl = jsonurl + pageid;
        String content = HttpHelper.getContentFromUrl(investurl);
        if (!content.contains("Not Found")){
          JSONObject jo = new JSONObject(content);
          Object totalCount = jo.getJSONObject("Data").get("TotalCount");
          if (totalCount != null) {
            result.setTotalNum(Integer.valueOf(totalCount.toString()));
          }
        }
        
        if (title != null) {
          Elements titlechildren = title.children();
          String biao = titlechildren.first().attr("title");
          if (biao.contains("活动标")) {
            result.setCategory("活动标");
          } else if (biao.contains("新手")) {
            result.setCategory("新手体验标");
          } else if (biao.contains("直投")) {
            result.setCategory("直投标");
          } else if (biao.contains("流转")) {
            result.setCategory("流转标");
          } else {
            result.setCategory(biao);
          }
          result.setName(titlechildren.select("a").text());
          
          Elements reward = titlechildren.select("span[class*=png4]");
          if (!reward.isEmpty()) {
            result.setReward(reward.text());
          }
          
          Elements security = titlechildren.select("span[title*=保障类别]");
          if (!security.isEmpty()) {
            result.setSecurityMode(security.text().replaceAll("保", ""));
          }
        }
        
        
        Elements up = doc.select(".table1").select(".w");
        Elements me = up.select("span[class=fcc20]");
        if (!me.isEmpty()) {
          String money = me.get(0).text().replaceAll(",", "");
          if (money.contains("万元")) {
            money = money.replaceAll("万元", "");
            result.setMoney(CalculateUtil.mul(Double.valueOf(money), 10000d));
          } else {
            result.setMoney(Double.valueOf(money.replaceAll("元", "")));
          }
          
          
          String rate = me.get(1).text().replaceAll("%", "");
          if (rate.contains("年")) {
            rate = rate.replaceAll("/年", "");
            result.setYearRate(Double.valueOf(rate));
          } else if (rate.contains("日")){
            rate = rate.replaceAll("/日", "");
            result.setDayRate(Double.valueOf(rate));
          }
          
          String repayLimitTime = me.get(2).text();
          result.setRepayLimitTime(repayLimitTime+"个月"); 
          
          
          String creditRating = me.get(4).text();
          result.setCreditRating(creditRating);
          
        }
        
        
        Elements eo = up.select("td[class=td_br]");
        if (!eo.isEmpty()){
          String repayMode = eo.get(1).text().replaceAll("偿还方式:", "");
          result.setRepayMode(repayMode);
          
          String agency = eo.get(2).text().trim().replaceAll("专业保证:", "");
          result.setAgency(agency);
        }
        
        Elements eee = up.select("td[class=td_br1] p");
        if(!eee.isEmpty()){
          String agency = eee.get(0).text();
          result.setAgency(agency);
        }
        
        Elements te = up.select("tr[class=tr_bg] td[colspan=3]");
        if (!te.isEmpty()) {
          String[] tt = te.get(0).text().replaceAll("申购时间:", "").trim().replaceAll(" ", "").split("至");
          result.setStartTime(tt[0]);
          result.setEndTime(tt[1]);
        }
        
        String filename = FileUtil.downloadAvatar(instanceid, SiteType.PPMONEY, path, "", logo);
        result.setAvatar(instanceid, filename);
        
        results.add(result);
        
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [PpmoneyParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [PpmoneyParser] execute failure. ",e);
    }
    
    return results; 
  }



}
