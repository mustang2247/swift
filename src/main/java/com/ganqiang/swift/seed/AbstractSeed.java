package com.ganqiang.swift.seed;

import com.ganqiang.swift.fetch.Fetchable;
import com.ganqiang.swift.parse.Parsable;

public abstract class AbstractSeed
{
  protected int pageSize = 1;
  protected Fetchable fetcher;
  protected Parsable parser;

  public AbstractSeed(){
    this.setFetcher();
    this.setParser();
  }

  public int getPageSize()
  {
    return pageSize;
  }

  public void setPageSize()
  {
    this.pageSize = loadPageSize();
  }

  public Fetchable getFetcher()
  {
    return fetcher;
  }

  protected abstract void setFetcher();

  public Parsable getParser()
  {
    return parser;
  }

  protected abstract void setParser();

  protected abstract int loadPageSize();

}
