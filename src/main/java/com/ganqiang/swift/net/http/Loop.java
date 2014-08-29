package com.ganqiang.swift.net.http;

import java.util.concurrent.locks.ReentrantLock;

import com.ganqiang.swift.util.ConcurrentQueue;

public interface Loop
{

  int getFront();

  void createFront();

  int resetFront();

  ReentrantLock getLock();

  <L> ConcurrentQueue<L> getQueue();

}