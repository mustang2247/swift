package com.ganqiang.swift.net.http;

import java.util.concurrent.locks.ReentrantLock;

import com.ganqiang.swift.util.ConcurrentQueue;

public final class HttpProxyLooper extends AbstractLoop implements Loop
{

  private static volatile int front = 0;
  private static final ReentrantLock lock = new ReentrantLock(true);
  private static final ConcurrentQueue<HttpProxy> usefulHosts = new ConcurrentQueue<HttpProxy>();

  @Override
  public int getFront()
  {
    return front;
  }

  @Override
  public ReentrantLock getLock()
  {
    return lock;
  }

  public void addQueue(HttpProxy httpproxy)
  {
    usefulHosts.add(httpproxy);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConcurrentQueue<T> getQueue()
  {
    return (ConcurrentQueue<T>) usefulHosts;
  }

  @Override
  Loop getLooper()
  {
    return this;
  }

  @Override
  public int resetFront()
  {
    front = 0;
    return front;
  }

  @Override
  public void createFront()
  {
    front += 1;
  }

}
