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
    String homepage = Constants.seed_map.get(type).getHomePage();
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for (FetchedPage fetchedPage : fplist) {
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [YirendaiParser] begin parse from [" + url + "].");
        String id = url.split("=")[1];
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
        
        
        Element avatarele = doc.select(".img_temp2").select(".bc").select(".png").select("img").first();
        String imgsrc = homepage + StringUtil.getAbsolutePath(avatarele.attr("src"));
        if (imgsrc.contains("touxiang.jpg")) {
          id = "touxiang";
        }
        String filename = FileUtil.downloadAvatar(instanceid, type, path, id, logo, imgsrc);
        result.setAvatar(instanceid, filename);
            
        Elements names = doc.select(".fb").select(".f14").select(".mt30").select(".mb30").select(".ml20");
        result.setName(names.text());   
        
        Elements detail = doc.select(".pl100").select(".lh200").select(".mw710");
        result.setDetailDesc(detail.text());
        
        Elements up = doc.select(".borrowApplyDetail").select(".borrowBox").select(".br5")
            .select(".bc").select(".pt30").select(".pl15").select(".pr15").select(".pb30").first().children();
        for (Element ele : up) {
          if (ele.tag().getName().equals("span")) {
            if (ele.className().equals("picIconJyb")) {
              result.setCategory("精英标");
            }
          } else {
            String text = up.text().toString().split("借款人信息")[0];
            
            String content = text.replaceAll("\\：", "").replaceAll("\\:", "")
                .replaceAll("借款人", "")
                .replaceAll("借款金额", "").replaceAll("年利率", "").replaceAll("元", "")
                .replaceAll("提示", "").replaceAll("此不等同于收益率", "")
                .replaceAll("（由于采用等额本息法每月还款），若想达到等同于此利率的收益，建议您循环出借。", "")
                .replaceAll("借款期限", "").replaceAll("%", "").replaceAll("借款说明", "")
                .replaceAll("本借款采用", "").replaceAll("还款方式", "")
                .replaceAll("剩余时间", "").replaceAll("投标进度", "")
                .replaceAll("投标完成", "").replaceAll("共", "").replaceAll("笔投资", "")
                .replaceAll("可投金额", "").replaceAll("投标金额", "")
                .replaceAll("加入购物车", "");
            
            StringTokenizer st = new StringTokenizer(content);
            //说明已满标
            if(content.contains("看看其他标")){
              int i = 0;
              while (st.hasMoreElements()) {
                Object obj = st.nextElement();
                String str = obj.toString().trim();
                if (i == 0) {
                  result.setBorrower(str);
                } else if (i == 1) {
                  result.setMoney(Double.valueOf(str));
                } else if (i == 2) {
                  result.setYearRate(Double.valueOf(str));
                } else if (i == 3) {
                  result.setRepayLimitTime(str);
                } else if (i == 4) {
                  result.setRepayMode(str);
                } else if (i == 5) {
                  Double progress = Double.valueOf(str);
                  result.setProgress(progress);
                } else if (i == 6) {
                  result.setTotalNum(Integer.valueOf(str));
                } else if (i == 7) {
                  result.setRemainMoney(Double.valueOf(str));
                } else if (i == 8){
                  if (str.equals("看看其他标")) {
                    result.setStatus(Constants.status_ymb);
                  }
                }
                i++;
              }
            } else { // 投标中
              int i = 0;
              while (st.hasMoreElements()) {
                Object obj = st.nextElement();
                String str = obj.toString().trim();
                if (i == 0) {
                  result.setBorrower(str);
                } else if (i == 1) {
                  result.setMoney(Double.valueOf(str));
                } else if (i == 2) {
                  result.setYearRate(Double.valueOf(str));
                } else if (i == 3) {
                  result.setRepayLimitTime(str);
                } else if (i == 4) {
                  result.setRepayMode(str);
                } else if (i == 5) {
                  result.setRemainTime(str);
                } else if (i == 6) {
                  result.setProgress(Double.valueOf(str));
                } else if (i == 7) {
                  result.setTotalNum(Integer.valueOf(str));
                } else if (i == 8) {
                  result.setRemainMoney(Double.valueOf(str));
                } else if (i == 9){
                  if (str.equals("立即投标")) {
                    result.setStatus(Constants.status_tbz);
                  }
                }
                i++;
              }
            }
            
          }
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
