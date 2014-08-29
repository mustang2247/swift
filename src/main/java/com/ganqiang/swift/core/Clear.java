package com.ganqiang.swift.core;

public final class Clear implements Process
{
  
  @Override
  public void execute(Event event)
  {
    event.clear();
    event = null;
  }

}
