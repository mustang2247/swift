package com.ganqiang.swift.core;

public interface EventExecutor extends Executor
{

  void execute(Event event);

}
