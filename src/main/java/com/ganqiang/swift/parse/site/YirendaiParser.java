package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public final class YirendaiParser implements Parsable
{
  private static final Logger logger = Logger.getLogger(YirendaiParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String instanceid = seed.getId();
    String key = seed.getKey();
    SiteType type = seed.getType();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for (FetchedPage fetchedPage : fplist) {
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [YirendaiParser] begin parse from [" + url + "].");
        if (fetchedPage.getContent().contains("很抱歉，此页面正在维护中") || fetchedPage.getContent().contains("系统升级中")) {
          continue;
        			}
        Document doc = Jsoup.parse(fetchedPage.getContent());

        Result result = new Result(type);
        result.setUrl(url);

        // http://www.yirendai.com/safty/slbz/
        // 宜人贷为出借资金的客户提供业内最佳的第三方担保公司本息担保制度，避免客户出现资金损失。
        result.setAgency("第三方担保公司");
        result.setSecurityMode("本息担保");
        
        
        String filename = FileUtil.downloadAvatar(instanceid,SiteType.YIRENDAI, path, "", logo);
        result.setAvatar(instanceid, filename);
            
        Elements names = doc.select("strong[class=l]");
        result.setName(names.text());   
        
        Elements detail = doc.select("div[class*=elite_left]");
        for(Element e : detail){
        		if(e.text().contains("借款描述")){
        			result.setDetailDesc(e.select("p").get(1).text().trim());
        					}
        			}
        
        
        Elements up = doc.select("table[class=elite_table] tbody tr");
        String moneystr = up.get(1).select("td").get(0).text().replaceAll(",", "");
        result.setMoney(Double.valueOf(moneystr.trim()));
        String yearrate = up.get(1).select("td").get(1).text().replaceAll("%", "");
        result.setYearRate(Double.valueOf(yearrate.trim()));
        String repayLimitTime = up.get(1).select("td").get(2).text().trim();
        result.setRepayLimitTime(repayLimitTime+"个月");
        
        
        String progressstr = up.get(2).select("td").get(0).select("strong").text().replaceAll("投标完成", "").replaceAll("%", "");
        result.setProgress(Double.valueOf(progressstr.trim()));
        String remaintimestr = up.get(2).select("td").get(1).select("span").text().trim();
        result.setRemainTime(remaintimestr);
        
        String bstr = up.get(3).select("td").get(0).select("span").text().trim();
        result.setBorrower(bstr);
        
        String repaymodestr = up.get(3).select("td").get(1).text().replaceAll("还款方式：", "").trim();
        result.setRepayMode(repaymodestr);
        
        String secstr = up.get(3).select("td").get(2).text().replaceAll("保障计划：", "").trim();
        result.setSecurityMode(secstr);

        Elements totalnum = doc.select("table[class=table_gray] tbody tr");
					result.setTotalNum(totalnum.size());

					String remainMoney = doc.select("p[class=surplus_money]").text().replaceAll(",", "");
					result.setRemainMoney(Double.valueOf(remainMoney.trim()));
					
					String state = doc.select("p[class=distance_value] a").get(0).text();
					if(state.contains("立即投标")){
						result.setStatus(Constants.status_tbz);
					}
					
        results.add(result);
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [YirendaiParser] end parse from [" + url + "].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName()
          + "] execute [YirendaiParser] failure. ", e);
    }

    return results;

  }


}
