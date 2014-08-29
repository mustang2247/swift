package com.ganqiang.swift.seed;

public class Seed
{
  private String id;// jobid or instanceid
  private SiteType type;
  private String seedName;// inside seed name or outside site url
  private String listLink;

  public String getKey(){
    return id + seedName;
  }

  /**
   * id : jobid or instanceid
   * seed : inside seed name or outside site url
   */
  public static String getKey(String id, String seed){
    return id + seed;
  }

//  public static String getInsideKey(String id, SiteType type){
//    return id + type.getInseed();
//  }
//
//  public static String getOutsideKey(String id, String url){
//    return id + url;
//  }

  public static String[] getAllInsites(){
    SiteType[] types = SiteType.values();
    String[] array = new String[types.length - 1];
    for (int i=0; i<types.length; i++) {
      SiteType type = types[i];
      if (type.equals(SiteType.OUTSIDE)) {
        continue;
      }
      array[i - 1] = type.toString().toLowerCase();
    }
    return array;
  }

  public String getSeedName()
  {
    return seedName;
  }

  public void setSeedName(String inseed)
  {
    this.seedName = inseed;
    setType(SiteType.getType(inseed));
  }

  private void setType(SiteType type)
  {
    this.type = type;
  }

  public String getListUrl()
  {
    return listLink;
  }

  public void setListUrl(String listUrl)
  {
    this.listLink = listUrl;
  }

  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }
  public SiteType getType()
  {
    return type;
  }


  public enum SiteType
  {
    OUTSIDE("outside"),
    RENRENDAI("renrendai"),
    YIRENDAI("yirendai"),
    PPDAI("ppdai"),
    JIMUBOX("jimubox"),
    LUFAX("lufax"),
    PPMONEY("ppmoney"),
    ELOANCN("eloancn"),
    MY089("my089"),
    ROYIDAI("royidai"),
    EDAI365("edai365"),
    WZDAI("wzdai"),
    YOOLI("yooli"),
    NIWODAI("niwodai"),
    FIRSTP2P("firstp2p");
    
    private String inseed;
    
    SiteType(String inseed){
      this.inseed = inseed;
    }
    
    public String getInseed(){
      return this.inseed;
    }

    public static SiteType getType(String inseed){
      for(SiteType p2p : SiteType.values()){
        if(p2p.getInseed().equals(inseed)){
          return p2p;
        }
      }
      return OUTSIDE;
    }
    
    
  }
}
