package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.fetch.site.CommonLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.YooliParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class YooliSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(YooliSeed.class);

  @Override
  public int loadPageSize()
  {
    String html = HttpHelper.getContentFromUrl("http://www.yooli.com/yuexitong/");
    Document doc = Jsoup.parse(html);   
    Elements elements = doc.select(".yPager").select(".inner");
    String[] pageinfo = elements.text().replaceAll(">", "").split(" ");
    int totalpage = Integer.valueOf(pageinfo[pageinfo.length-1]);
    logger.info("Loading [yooli] list total page number is ["+ totalpage +"]");
    return Integer.valueOf(totalpage);
  }

  @Override
  public String getHomePage()
  {
    return "http://www.yooli.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.yooli.com/yuexitong/page/" + Constants.split_str + ".html";
  }

  @Override
  public String getPreDetailLink()
  {
    return "yuexitong/zhuan/" + Constants.split_str + "yuexitong/detail/";
  }

  @Override
  public String getLogo()
  {
    return "http://www.yooli.com/v2/local/img/common/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "有利网";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new CommonLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new YooliParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return "yuexitong/zhuan/" + Constants.split_str + "yuexitong/detail/";
  }


}
