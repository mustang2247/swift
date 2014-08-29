package com.ganqiang.swift.timer;

import java.util.Timer;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;

public final class TimerContextHandler implements Visitable
{

  public void init()
  {
    Timer timer = new Timer();
    Constants.timer = timer;
  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitTimerContext(this);
  }

}
