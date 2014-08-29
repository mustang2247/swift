package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.fetch.site.CommonLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.EloancnParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;
import com.ganqiang.swift.util.StringUtil;

public final class EloancnSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(EloancnSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.eloancn.com/loan/loadAllTender.action?index=1&sidx=publisheddate&sord=desc");
    Document doc = Jsoup.parse(html);  
    Elements elements = doc.getElementsByClass("pagert");
    String pageinfo = elements.text();
    if (StringUtil.isNullOrBlank(pageinfo)) {
      logger.info("Loading [eloancn] list page number is [1]");
      return 1;
    }
    String[] pageArray = pageinfo.split(" ");
    int totalpage = Integer.valueOf(pageArray[pageArray.length -1].replaceAll("共", "").replaceAll("页", ""));
    logger.info("Loading [eloancn] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.eloancn.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.eloancn.com/loan/loadAllTender.action?index=1&sidx=publisheddate&sord=desc&page=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.eloancn.com/loan/loandetail.action?tenderid=";
  }

  @Override
  public String getLogo()
  {
    return "http://www.eloancn.com:80/commons/images/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "翼龙贷";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new CommonLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new EloancnParser(); 
  }

  @Override
  public String getDetailLinkMark()
  {
    return "loan/loandetail.action?tenderid=";
  }


}
