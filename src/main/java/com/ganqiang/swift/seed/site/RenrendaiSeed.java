package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ganqiang.swift.fetch.site.RenrendaiLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.RenrendaiParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public final class RenrendaiSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(RenrendaiSeed.class);

  @Override
  public int loadPageSize()
  {
    try {
      String html = HttpHelper.getContentFromUrl("http://www.renrendai.com/lend/loanList.action");
      Document doc = Jsoup.parse(html);
      Element as = doc.getElementById("loan-list-rsp");
      if (as == null) {
        return 0;
      }
      String json = as.html();
      JSONObject jsonObject = new JSONObject(json);
      JSONObject data = jsonObject.getJSONObject("data");
      int totalpage = data.getInt("totalPage");
      logger.info("Loading [renrendai] list total page number is ["+ totalpage +"]");
      return totalpage;
    } catch (Exception e){
      e.printStackTrace();
      logger.error("It obtain renrendai pagesize failure.", e);
    }
    return 0;
  }

  @Override
  public String getHomePage()
  {
    return "http://www.renrendai.com/";
  }

  @Override
  public String getPreListLink()
  {
    return "http://www.renrendai.com/lend/loanList.action?pageIndex=";
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://www.renrendai.com/lend/detailPage.action?loanId=";
  }

  @Override
  public String getLogo()
  {
    return "http://www.renrendai.com/static/img/logo.png";
  }

  @Override
  public String getPlatform()
  {
    return "人人贷";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new RenrendaiLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new RenrendaiParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    return null;
  }


}
