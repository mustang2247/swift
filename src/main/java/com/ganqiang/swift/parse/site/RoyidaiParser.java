package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class RoyidaiParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(LufaxParser.class);
  
  private static final String default_avatar = "http://www.royidai.com/images/default-img.jpg";

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    SiteType type = seed.getType();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    Map<String, Result> resultMap = ( HashMap<String, Result> ) event.get(Event.results_key);
    String path = Constants.inside_avatar_path_map.get(key);
    List<Result> results = new ArrayList<Result>();
    String logo = Constants.seed_map.get(type).getLogo();
    
    for(FetchedPage fetchedPage : fplist){
      String url = fetchedPage.getUrl();
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RoyidaiParser] begin parse from ["+url+"].");
      String[] spliturl = url.split("=");
      String pageid = spliturl[spliturl.length - 1];
      Document doc = Jsoup.parse(fetchedPage.getContent());
      Result result = resultMap.get(pageid);
      if(result == null){
        continue;
      			}
      

      String detail = doc.select("div[class=explain]").text().replaceAll("借款用途说明", "").trim();
      result.setDetailDesc(detail);


      Elements middleup = doc.select("div[id=tabCon] div[class=fPanel] table tbody").get(3).children();

      String repayMode = doc.select("span[class=t]").text().replaceAll("还款方式 ：", "").trim();
      result.setRepayMode(repayMode);
      int totalnum = middleup.size() -1;
      result.setTotalNum(totalnum);
     
      // js 获取剩余时间
//      Element e = doc.getElementById("remainTimeTwo");
//      result.setRemainTime(e.text());


      String filename = FileUtil.downloadAvatar(instanceid,	SiteType.ROYIDAI, path, "", logo);
		result.setAvatar(instanceid, filename);
      result.setUrl(url);
//
//      String userinfo = doc.select("div.name table tr").text();
//      result.setBorrower(userinfo.split("用户名：")[1].split("信用指数：")[0].trim());

      //讯贷网平台对投资用户实行100%本息保障
      //http://www.royidai.com/callcenter.do?type=true&cid=5
      //讯贷网对VIP客户实现不同产品保本保息、非VIP客户保本承诺。
      //http://www.royidai.com/capitalEnsure.do
      result.setSecurityMode("本金保障");
      //讯贷网合作的全国领先的担保机构
      //ttp://www.royidai.com/capitalEnsure.do
      String ag = doc.select("div[class=agency-logo] a img").attr("alt");
      result.setAgency(ag);
      results.add(result);
      logger.info("Worker [" + Thread.currentThread().getName() + "] --- [RoyidaiParser] end parse from ["+url+"].");
    }
    if (resultMap!=null && !resultMap.isEmpty()){
      resultMap.clear();
      resultMap = null;
      event.remove(Event.results_key);
    }
    return results; 
  }


}
