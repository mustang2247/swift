package com.ganqiang.swift.parse.site;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.fetch.FetchedPage;
import com.ganqiang.swift.parse.Parsable;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.FileUtil;

public class EloancnParser implements Parsable
{

  private static final Logger logger = Logger.getLogger(EloancnParser.class);

  @SuppressWarnings("unchecked")
  public List<Result> parse(Event event){
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    String instanceid = seed.getId();
    String key = seed.getKey();
    List<FetchedPage> fplist = (List<FetchedPage>) event.get(Event.fetchedPages_key);
    List<Result> results = new ArrayList<Result>();
    String path = Constants.inside_avatar_path_map.get(key);
    String logo = Constants.seed_map.get(type).getLogo();
    try {
      for(FetchedPage fetchedPage : fplist){
        String url = fetchedPage.getUrl();
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [EloancnParser] begin parse from ["+url+"].");
        String pageid = url.split("=")[1];
        Result result = new Result(type);
        result.setUrl(url);
        Document doc = Jsoup.parse(fetchedPage.getContent());

        Elements lup= doc.select(".ld_user").select(".fl").select(".mt20");
        String borrower = lup.select("p a").text();
        result.setBorrower(borrower);

        Elements mup = doc.select(".ld_info").select(".fl");
        String na = mup.select("h2").text();
        result.setName(na.trim());
        Elements ee = mup.select("ul li[class=wd270]").select("span[class*=font22]");
        Element me = ee.get(0);
        Element re = ee.get(1);
        if (me != null){
          result.setMoney(Double.valueOf(me.text().replaceAll(",", "")));
        }
        if (re != null) {
          result.setCreditRating(re.text());
        }

        String yr = mup.select("ul li[class=wd300]").select("span[id*=interestrate_]").text();
        result.setYearRate(Double.valueOf(yr.replaceAll("%", "")));

        Elements qe = mup.select("ul li[class=wd180]");
        Element re1 = qe.get(0);
        Element re2 = qe.get(1);
        if (re1 != null) {
          result.setRepayLimitTime(re1.text().replaceAll("期限：", ""));
        }
        if (re2 != null) {
          result.setRepayMode(re2.text().replaceAll("还款方式：", "").trim());
        }

        String progress = mup.select("ul li[class=wd300]").select("span[class=plan]").text().replaceAll("%", "");
        result.setProgress(Double.valueOf(progress));

        String desce = doc.select("div[class=record]").select("dl dd").text();
        result.setDetailDesc(desce.trim());

        String cate = doc.select("div[id=record0] table tr").select("td[class*=wd339]").select(".pdl15").first().text();
        result.setCategory(cate.trim());

        Elements time = doc.select("div[id=showNomalTenderMsg] script");
        if (!time.isEmpty()){
          String timestr = time.html().replaceAll("tenderDetailCountDown\\(", "").replaceAll("\\);", "");
          Long t = System.currentTimeMillis() + Long.valueOf(timestr) * 1000;
          Date endt = new Date(t);
          result.setEndTime(DateUtil.dateToStr(endt));
          result.setRemainTime(DateUtil.getRemainTime(endt));
        }

        Elements tt = doc.select("p[class*=ld_status_list0]").select(".ml15").select(".pdt5").first().children();
        if (!tt.isEmpty()){
          String st = tt.get(0).text().trim();
          result.setStartTime(st);
          String et = "";
          if (tt.size() == 1){
            result.setStatus(Constants.status_tbz);
          } else if (tt.size() == 2 || tt.size() == 3) {
            et = tt.get(1).text().trim();
            result.setEndTime(et);
            result.setStatus(Constants.status_ymb);
          } else if (tt.size() == 4) {
            et = tt.get(1).text().trim();
            result.setEndTime(et);
            result.setStatus(Constants.status_hkz);
          } else if (tt.size() == 5) {
            et = tt.get(1).text().trim();
            result.setEndTime(et);
            result.setStatus(Constants.status_yhw);
          }
        }

        String avatar = doc.select("div[class*=ld_user] a[target=_blank] img").attr("src");
        String filename = FileUtil.downloadAvatar(instanceid, type, path, pageid, logo, avatar);
        result.setAvatar(instanceid, filename);

//        result.setSecurityMode("本息保障");

        results.add(result);
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- [EloancnParser] end parse from ["+url+"].");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Worker [" + Thread.currentThread().getName() + "] --- [EloancnParser] execute failure. ",e);
    }
    
    return results; 
  }
  
  public static void main(String... args){
//    EloancnParser pp = new EloancnParser();
//    Event event = new Event();
//    List<FetchedPage> fplist = new ArrayList<FetchedPage>();
//    //http://www.jimubox.com/Project/Index/602
//    String url = "http://www.eloancn.com/loan/loandetail.action?tenderid=12764";
//    String content = HttpUtil.getContentFromUrl(url);
//    fplist.add(new FetchedPage(url, content, 200));
//    event.put(Event.fetchedPages, fplist);
//    event.put(Event.siteType, SiteType.ELOANCN);
//    event.put(Event.results, new HashMap<String,Result>());
//    pp.parse(event);
//    String str = "9%+3%";
//    String[] array = str.replaceAll("%", "").replaceAll("\\+", " ").split(" ");
//    System.out.println(array[0]);
  }


}
