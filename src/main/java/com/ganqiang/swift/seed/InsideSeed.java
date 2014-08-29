package com.ganqiang.swift.seed;

import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.parse.Parsable;

public interface InsideSeed
{
  String getHomePage();

  String getPreListLink();

  String getPreDetailLink();

  String getDetailLinkMark();

  String getLogo();

  String getPlatform();

  Fetchable getFetcher();

  Parsable getParser();

  void setPageSize();

  int getPageSize();
}
