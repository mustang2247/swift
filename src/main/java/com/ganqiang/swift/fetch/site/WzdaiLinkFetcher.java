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
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.StringUtil;

public final class WzdaiLinkFetcher implements Fetchable
{
  private static final Logger logger = Logger.getLogger(WzdaiLinkFetcher.class);

  @Override
  public void fetch(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String url = seed.getListUrl();
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    String homepage = Constants.seed_map.get(type).getHomePage();
    String detaillink = Constants.seed_map.get(type).getPreDetailLink();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin [WzdaiLinkFetcher] from ["+url+"].");
    String path = Constants.inside_avatar_path_map.get(key);
    Map<String, Result> resultMap = new HashMap<String, Result>();
    boolean useproxy = Constants.inside_use_proxy_map.get(instanceid);
    FetchedPage fp = HttpHelper.getFetchedPage(key, instanceid, type, url, useproxy);
    Document doc = Jsoup.parse(fp.getContent());

    Elements tables = doc.select(".listmain").first().children();

    for (int i = 0; i < tables.size(); i++) {
      Element ele = tables.get(i);
      Result result = new Result(type);
      Elements title = ele.select("li.titleli span a");
      result.setName(title.text().trim().replaceAll(" ", ""));
      String link = StringUtil.getAbsolutePath(title.attr("href"));
      String pageid = link.split("=")[1];
      result.setUrl(detaillink + pageid);
      Elements money = ele.select("li.titleli span strong font");
      result.setMoney(Double.valueOf(money.text().replaceAll("元", "").replaceAll(",", "")));
      
      Elements category = ele.select("li.titleli a img");
      if (!category.isEmpty()) {
        String src = category.attr("src");
        if(src.contains("jin.gif")){
          result.setCategory("净值标");
        } else if(src.contains("xin.jpg")){
          result.setCategory("信用标");
        } else if(src.contains("day.jpg")){
          result.setCategory("天标");
        } else if(src.contains("fast.gif")){
          result.setCategory("给力标");
        } else if(src.contains("lock.gif")){
          result.setCategory("定向标");
        } else if(src.contains("liu")){
          result.setCategory("流转标");
        } else {
          result.setCategory("秒还标");
        } 
      }
      
      Element rank = ele.getElementsByClass("rank").first();
      if(rank != null){
        result.setCreditRating(rank.attr("title").replaceAll("\\,", "")+"分");
      }

      Element eee = ele.getElementById("endtime");
      double hour = CalculateUtil.div(Double.valueOf(eee.attr("data-time")), 3600);
      result.setRemainTime(String.valueOf(hour).replaceAll("\\.0", "") + "小时");

      Elements infos = ele.select("ul.list-ul li");
      for (int j = 1; j < infos.size(); j ++) {
        String e = infos.get(j).text();
        if (e.contains("发布者")) {
          result.setBorrower(e.replaceAll("发布者：", ""));
        } else if (e.contains("年利率")){
          result.setYearRate(Double.valueOf(e.replaceAll("年利率：", "").replaceAll("%", "")));
        } else if (e.contains("已完成")){
          result.setProgress(Double.valueOf(e.replaceAll("已完成：", "").replaceAll("%", "").trim().replaceAll(" ", "")));
        } else if (e.contains("借款期限")){
          result.setRepayLimitTime(e.replaceAll("借款期限：", "").trim());
        } else if (e.contains("投标奖励")){
          if (!e.contains("没有奖励")) {
            result.setReward(e.replaceAll("投标奖励：", "").replaceAll("奖励", "").replaceAll("￥", "").trim());
          }
        } else if (j == infos.size() - 1){
          result.setRepayMode(e);
        }
      }

      String status = ele.select("div.list-btnbox a").text().trim();
      if (status.contains("立即投标")) {
        result.setStatus(Constants.status_tbz);
      } else if (status.contains("还款中")) {
        result.setStatus(Constants.status_hkz);
      } else {
        result.setStatus(status);
      }

      String img = ele.select("img.productimg").attr("src");
      String imgsrc = homepage + StringUtil.getAbsolutePath(img);
      String filename = path + pageid ;
      File imgfile = new File(filename);
      if (!imgfile.exists()) {
        filename = HttpHelper.downloadImage(instanceid, imgsrc, true, filename);
      }
      result.setAvatar(instanceid, filename);

//      if (result.getProgress() > 0 && result.getProgress() < 100) {
//        result.setRemainMoney(CalculateUtil.getDoubleHalfValue(CalculateUtil.mul(result.getMoney(), (1 - result.getProgress() / 100) )));
//      }

//      http://www.wzdai.com/article/detail.html?catalog=102&id=13
//      温州贷对于VIP会员提供坏账本金100%风险金补偿服务。
//      result.setSecurityMode("");

      resultMap.put(pageid, result);
    }

    event.put(Event.results_key, resultMap);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end [WzdaiLinkFetcher] from ["+url+"].");
  }


}
