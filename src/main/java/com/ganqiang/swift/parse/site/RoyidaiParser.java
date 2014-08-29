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
    String homepage = Constants.seed_map.get(type).getHomePage();
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
      Elements eles = doc.select("div.xqbox > h3");
      String[] namearray = eles.text().split("\\(");
      result.setName(namearray[0].replaceAll(" ", "").trim().replaceAll(" ", ""));
      String cate = namearray[1].split("\\)")[0].trim();
      if (cate.contains("担保")) {
        result.setCategory("担保标");
      } else if (cate.contains("净值")) {
        result.setCategory("净值标");
      } else if (cate.contains("秒还")) {
        result.setCategory("秒还标");
      } else if (cate.contains("信用")) {
        result.setCategory("信用标");
      } else if (cate.contains("实地")) {
        result.setCategory("实地考察标");
      }
      
      Elements up = doc.select("div.money");
      String money = up.text().split("借款金额：")[1].split("借款目的：")[0].replaceAll("￥", "").replaceAll(",", "").trim().replaceAll(" ", "");
      result.setMoney(Double.valueOf(money));
      String detail = doc.select("p.txt strong").text().replaceAll("我的借款描述：", "").trim();
      if (StringUtil.isNullOrBlank(detail) || detail.equals("测试")) {
        detail = up.text().split("借款目的：")[1].split("借款年利率：")[0].trim();
      }
      result.setDetailDesc(detail);
      String yr = up.text().split("借款年利率：")[1].split("%")[0].trim();
      result.setYearRate(Double.valueOf(yr));
      String month = up.text().split("借款期限：")[1].trim();
      result.setRepayLimitTime(month);
      
      String statusimg = doc.select("div.tbbtn img").attr("src");
      if (statusimg.contains("neiye2_07.jpg")) {
        result.setStatus(Constants.status_tbz);
      } else if (statusimg.contains("neiye1_636.jpg")){
        result.setStatus(Constants.status_hkz);
      } else if (statusimg.contains("neiye1_637.jpg")){
        result.setStatus(Constants.status_yhw);
      } else if (statusimg.contains("neiye2_18.jpg")){
        result.setStatus(Constants.status_ylb);
      }

      Elements middleup = doc.select("div.xqbottom table tr");
      String remainmoney = middleup.text().split("还差：")[1].split("投标进度：")[0].replaceAll("￥", "").replaceAll(",", "").trim();
      result.setRemainMoney(Double.valueOf(remainmoney));
      String progress = middleup.text().split("投标进度：")[1].split("%")[0].trim();
      result.setProgress(Double.valueOf(progress));
      String repayMode = middleup.text().split("还款方式：")[1].split("投标奖励：")[0].trim();
      result.setRepayMode(repayMode);
      String totalnum = middleup.text().split("总投标数：")[1].split("浏览量")[0].trim();
      result.setTotalNum(Integer.valueOf(totalnum));
     
      // js 获取剩余时间
//      Element e = doc.getElementById("remainTimeTwo");
//      result.setRemainTime(e.text());

      String userimg = doc.select("div.tx img").attr("src");
      String filename = "";
      if (userimg.contains("images/default-img.jpg")) {
        filename = FileUtil.downloadAvatar(instanceid, SiteType.ROYIDAI, path, "default-img", logo, default_avatar);
      } else {
        filename = FileUtil.downloadAvatar(instanceid, SiteType.ROYIDAI, path, pageid, logo, homepage + userimg);
      }
      result.setAvatar(instanceid, filename);
      result.setUrl(url);

      String userinfo = doc.select("div.name table tr").text();
      result.setBorrower(userinfo.split("用户名：")[1].split("信用指数：")[0].trim());
      String rating = doc.select("div.name table tr img").attr("src");
      if (rating.contains("ico_1.jpg")) {
        result.setCreditRating("HR");
      } else if(rating.contains("ico_2.jpg")){
        result.setCreditRating("E");
      } else if(rating.contains("ico_3.jpg")){
        result.setCreditRating("D");
      } else if(rating.contains("ico_4.jpg")){
        result.setCreditRating("C");
      } else if(rating.contains("ico_5.jpg")){
        result.setCreditRating("B");
      } else if(rating.contains("ico_6.jpg")){
        result.setCreditRating("A");
      } else if(rating.contains("ico_7.jpg")){
        result.setCreditRating("AA");
      }
      //讯贷网平台对投资用户实行100%本息保障
      //http://www.royidai.com/callcenter.do?type=true&cid=5
      //讯贷网对VIP客户实现不同产品保本保息、非VIP客户保本承诺。
      //http://www.royidai.com/capitalEnsure.do
      result.setSecurityMode("本金保障");
      //讯贷网合作的全国领先的担保机构
      //ttp://www.royidai.com/capitalEnsure.do
      result.setAgency("第三方担保机构");
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
