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
import com.ganqiang.swift.net.http.JavaScriptHelper;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class Edai365LinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(Edai365LinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [Edai365LinkFetcher] from ["+url+"].");
    String path = Constants.inside_avatar_path_map.get(key);
    Map<String, Result> resultMap = new HashMap<String, Result>();
    FetchedPage fp = null;
    Boolean jsflag = Constants.js_support_map.get(instanceid);
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    if (jsflag) {
      fp = JavaScriptHelper.getFetchedPage(key, instanceid, type, url);
    } else {
      fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    }
    Document doc = Jsoup.parse(fp.getContent());
    String homepage = Constants.seed_map.get(type).getHomePage();
    String logo = Constants.seed_map.get(type).getLogo();

    Element lendlist = doc.select("ul[class=lendlist]").first();
//    while (jsflag) {
//      fp = JavaScriptHelper.getFetchedPage(key, instanceid, type, url);
//      doc = Jsoup.parse(fp.getContent());
//      lendlist = doc.select("ol.lendlist").first();
//    }
    Elements tables = lendlist.children();

    for (int i = 0; i < tables.size(); i++) {
      Element ele = tables.get(i);
      if(ele.className().contains("tablepaginator")){
        continue;
      }
      
      Result result = new Result(type);
      
      Elements linkele = ele.select("div.titlename a");
      String link = StringUtil.getAbsolutePath(linkele.attr("href"));
      String pageid = link.split("=")[1];
      result.setUrl(homepage + link);

      String nameele = ele.select("div.titlename").text();
      result.setName(nameele);
      
      String category = ele.select("div.titlepic img").attr("title");
      result.setCategory(category);

      Elements bcele = ele.select("div[class=Part-2] div[style*=_margin-top]");
      result.setBorrower(bcele.select("a").text());
      
      boolean flag = bcele.text().contains("(网上评级)");
      
//    http://www.edai365.cn/WebInfo/CompensationRules.aspx
      if (!result.getName().contains("淮安")) {
        if (category.contains("信用") && flag) {
          result.setSecurityMode("本金");
        } else {
          result.setSecurityMode("本息");
        }
      } else {
        result.setSecurityMode("本息");
      }
      
      Elements eles = ele.select("div[class=Part-3] table tr td");
      for (int j=0; j<eles.size(); j++){
        String econtent = eles.get(j).text().trim();
        if (j == 0) {
          String money = econtent.replaceAll("¥", "").replaceAll("\\,", "");
          result.setMoney(Double.valueOf(money));
        } else if(j == 1){
          String yearRate = econtent.replaceAll("%", "");
          result.setYearRate(Double.valueOf(yearRate));
        } else if(j == 2){
          String repaylimittime = econtent.replaceAll("月", "个月");
          result.setRepayLimitTime(repaylimittime);
        } else if(j == 3){
          result.setRepayMode(econtent);
        } else if(j == 4){
          if (!econtent.contains("-")) {
            result.setReward(econtent);
          }
          break;
        }
      }
                                                             
      String progress = ele.select("div[class=Part-4] div[id*=UC_Lend_loanlist1_repLoan_] div[class=percent_num]").text();
      result.setProgress(Double.valueOf(progress.replaceAll("%", "").trim()));

      String status = ele.select("div[class=Part-5]").text();
      if (status.contains("我要投标")){
        result.setStatus(Constants.status_tbz);
      } else if (status.contains("等待审核")){
        result.setStatus(Constants.status_dsh);
      } else if (status.contains("还款中")){
        result.setStatus(Constants.status_hkz);
      } else if (status.contains("借款成功")){
        result.setStatus(Constants.status_yjk);
      } else {
        result.setStatus(status);
      }
      
      String img = homepage + StringUtil.getAbsolutePath(ele.select("img.people_pic").attr("src"));
      
      String filename = FileUtil.downloadAvatar(instanceid, type, path, pageid, logo, img);
      result.setAvatar(instanceid, filename);

      resultMap.put(pageid, result);
    }

    event.put(Event.results_key, resultMap);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [Edai365LinkFetcher] from ["+url+"].");
  }

}
