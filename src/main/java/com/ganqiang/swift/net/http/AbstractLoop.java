package com.ganqiang.swift.net.http;

import java.util.concurrent.locks.Lock;

import com.ganqiang.swift.util.ConcurrentQueue;

public abstract class AbstractLoop
{

  abstract Loop getLooper();

  @SuppressWarnings("unchecked")
  protected <L> L next(){
    Loop loop = getLooper();
    Lock lock = loop.getLock();
    lock.lock();
    try{
      ConcurrentQueue<?> queue = loop.getQueue();
      int front = loop.getFront();
      int size = queue.size();
      int rear = size - 1;
      if (front > rear) {
        front = loop.resetFront();
      }
      L looper = (L) queue.get(front);
      loop.createFront();
      return looper;
    }finally{
      lock.unlock();
    }
    
  }

}
