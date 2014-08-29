package com.ganqiang.swift.core;

import java.util.ArrayList;
import java.util.List;

public class EventChain implements Chain
{
  List<Process> list = new ArrayList<Process>();

  @Override
  public void execute(Event event)
  {
    for(Process p : list){
      p.execute(event);
    }
  }

  @Override
  public Chain addProcess(Process p)
  {
    list.add(p);
    return this;
  }

}
