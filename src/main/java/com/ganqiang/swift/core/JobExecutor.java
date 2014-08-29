package com.ganqiang.swift.core;

public interface JobExecutor extends Executor
{

  void pause();

  void continues();

  void destory(String jobid);

}
