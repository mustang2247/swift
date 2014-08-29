package com.ganqiang.swift.seed.site;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.fetch.site.LufaxLinkFetcher;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.parse.site.LufaxParser;
import com.ganqiang.swift.seed.AbstractSeed;
import com.ganqiang.swift.seed.InsideSeed;

public class LufaxSeed extends AbstractSeed implements InsideSeed
{
  private static final Logger logger = Logger.getLogger(LufaxSeed.class);

  @Override
  public int loadPageSize()
  {
    int totalPageNum = 0;
    try{  
      String html = HttpHelper.getContentFromUrl("http://list.lufax.com/list/service/product/listing/1?minAmount=0&maxAmount=100000000" +
          "&minInstalments=1&maxInstalments=240&order=asc&isDefault=true&pageLimit=20");
      JSONObject jsonObject = new JSONObject(html);
      totalPageNum = jsonObject.getInt("totalPage");
      logger.info("Loading [lufax] list page number is ["+ totalPageNum +"]");
    }catch(Exception e){
      e.printStackTrace();
      logger.error("Loading [lufax] page size failure.", e);
    }
    return totalPageNum;
  }

  @Override
  public String getHomePage()
  {
    return "http://www.lufax.com/";
  }

  @Override
  public String getPreListLink()
  {
    String lufax_pre_seed = "http://list.lufax.com/list/service/product/listing/";
    String lufax_suf_seed = "?minAmount=0&maxAmount=100000000&minInstalments=1&maxInstalments=240&order=asc&isDefault=true&pageLimit=20";
    return lufax_pre_seed + Constants.split_str + lufax_suf_seed;
  }

  @Override
  public String getPreDetailLink()
  {
    return "http://list.lufax.com/list/productDetail?productId=";
  }

  @Override
  public String getLogo()
  {
    return "https://static.lufax.com/config/images/logo_festival.png";
  }

  @Override
  public String getPlatform()
  {
    return "陆金所";
  }

  @Override
  protected void setFetcher()
  {
    this.fetcher = new LufaxLinkFetcher();
  }

  @Override
  protected void setParser()
  {
    this.parser = new LufaxParser();
  }

  @Override
  public String getDetailLinkMark()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
