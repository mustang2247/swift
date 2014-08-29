package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.fetch.site.Firstp2pLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.CommonParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class Firstp2pSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(Edai365Seed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.firstp2p.com/deals");
    Document doc = Jsoup.parse(html);  
    Elements elements = doc.select(".pages").select(".pl10").select(".pr10");
    String pageinfo = elements.text().split("/")[1].split("页")[0];
    int totalpage = Integer.valueOf(pageinfo.trim());
    logger.info("Loading [firstp2p] list page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.firstp2p.com/";
  }
  
  @Override
  public String getPreListLink()
  {
    return "http://api.firstp2p.com/deals/info?count=10&offset=" + Constants.split_str + "0";
  }

//  @Override
//  public String getPreListLink()
//  {
//    return "http://www.firstp2p.com/deals?cate=0&p=";
//  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.firstp2p.com/deal/";
  }

  @Override
  public String getLogo()
  {
    return "http://www.ucfgroup.com/uploadfile/2014/0303/20140303034943621.png";
  }

  @Override
  public String getPlatform()
  {
    return "第一P2P";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new Firstp2pLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new CommonParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "deal/";
  }



}
