package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
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
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public class LufaxParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(LufaxParser.class);

  // 被隐藏在CSS中
  private static final String anyidai_logo = "https://static.lufax.com/list/images/logo_anyidai_7172.png";
  
  private static final String totalnum_json_pre_url = "https://list.lufax.com/list/service/product/";
  
  private static final String totalnum_json_suf_url = "/get-bid-history?_=";

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    String path = Constants.inside_avatar_path_map.get(key);
    List<Result> results = new ArrayList<Result>();
    
    for(FetchedPage fetchedPage : fplist){
      String url = fetchedPage.getUrl();
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [LufaxParser] begin parse from ["+url+"].");
      String[] spliturl = url.split("=");
      String pageid = spliturl[spliturl.length - 1];
      Document doc = Jsoup.parse(fetchedPage.getContent());
      Result result = resultMap.get(pageid);

      if(result == null){
        continue;
      }

      Elements eles = doc.select(".progress-wrap").select(".clearfix");
      Elements dElements = null;
      if(eles.isEmpty()){
        result.setProgress(100d);
        result.setDetailDesc("“稳盈-安e贷债权转让服务”是指为已投资“稳盈-安e贷”的个人用户，" +
            "在陆金所网站可将符合相关条件的“稳盈-安e贷”债权（“借款债权”）转让给他人，" +
            "而由陆金所为债权出让人和债权受让人双方提供的中介服务。“稳盈-安e贷债权转让服务”" +
            "仅向符合中华人民共和国有关法律法规及陆金所相关规定的合格债权出让人和债权受让人提供。");
        dElements = doc.select(".data").select(".dataTitle");
      }else{
        String text = eles.text().split("项目进度：")[1].replaceAll("%", "");
        result.setProgress(Double.valueOf(text.trim()));
        result.setDetailDesc("“稳盈-安e贷”二期服务是陆金所面向个人借款者和个人出借人推出的个人借贷中介服务。 " +
            "借贷双方通过“稳盈-安e贷”二期服务可以快捷方便地达成借款交易并完成资金的借出和借入。在“稳盈-安e贷”二期服务中， " +
            "陆金所为借贷双方提供中介服务，发布借款需求、管理借贷双方以及担保公司之间的借贷及担保活动、借贷资金的划拨。 " +
            "“稳盈-安e贷”二期服务仅向符合中华人民共和国有关法律法规及陆金所相关规定的合格出借人和借款人提供。 ");
        dElements = doc.select(".descriptData").select(".raise-data").select(".dataTitle");
        
        String html = HttpHelper.getContentFromUrl(totalnum_json_pre_url + pageid + totalnum_json_suf_url + System.currentTimeMillis());
        try {
          JSONArray json = new JSONArray(html);
          result.setTotalNum(json.length());
        } catch (JSONException e1) {
          logger.error("Worker [" + Thread.currentThread().getName() + "] --- [LufaxParser] get json data of totalnum error.", e1);
        }
      }
      for (Element e : dElements) {
        if (e.attr("title").contains("担保公司")) {
          result.setAgency(e.attr("title").split("担保公司：")[1].split("。")[0]);
          break;
        }
      }
      String logo = "";
      if(!StringUtil.isNullOrBlank(result.getName()) && result.getName().contains("安e贷")){
        logo = anyidai_logo;
      }
      String filename = FileUtil.downloadAvatar(instanceid, type, path, "", logo);
      result.setAvatar(instanceid, filename);
      result.setUrl(url);
      if (result.getProgress() == 100d && 
          StringUtil.isNullOrBlank(result.getStatus())) {
        result.setStatus(Constants.status_ymb);
      }

//      http://www.lufax.com/help/help_issue_invester_answer_16.html
      result.setSecurityMode("保本保息");

      results.add(result);
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [LufaxParser] end parse from ["+url+"].");
    }
    if (resultMap!=null && !resultMap.isEmpty()){
      resultMap.clear();
      resultMap = null;
      event.remove(Event.results_key);
    }
    
    return results; 
  }
  
  public static void main(String... args){
    
//    LufaxParser pp = new LufaxParser();
//    Event event = new Event();
//    List<FetchedPage> fplist = new ArrayList<FetchedPage>();
//    String url = "http://list.lufax.com/list/productDetail?productId=84467";
//    String content = HttpUtil.getContentFromUrl(url);
//    fplist.add(new FetchedPage(url, content, 200));
//    event.put(Event.fetchedPages, fplist);
//    event.put(Event.siteType, SiteType.LUFAX);
//    event.put(Event.results, new HashMap<String,Result>());
//    pp.parse(event);
    
  }


}
