package com.ganqiang.swift.storage.disk;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.Storable;

public class DiskStore implements Storable, Process
{
  private static final Logger logger = Logger.getLogger(DiskStore.class);
  

  @Override
  public void execute(Event event)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <Store> Result readOne(Store... s)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <Store> void writeBatch(Store... s)
  {
    // TODO Auto-generated method stub
    
  }

}
