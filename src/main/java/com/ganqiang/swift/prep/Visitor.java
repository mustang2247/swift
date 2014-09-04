package com.ganqiang.swift.prep;

import com.ganqiang.swift.conf.LocalConfigHandler;
import com.ganqiang.swift.conf.RemoteConfigHandler;
import com.ganqiang.swift.conf.SyncConfigHandler;
import com.ganqiang.swift.net.http.HttpContextHandler;
import com.ganqiang.swift.net.http.HttpProxyHandler;
import com.ganqiang.swift.net.sync.SyncContextHandler;
import com.ganqiang.swift.net.zk.ZKContextHandler;
import com.ganqiang.swift.seed.PageSizeSettingHandler;
import com.ganqiang.swift.seed.SeedContextHandler;
import com.ganqiang.swift.storage.db.DBContextHandler;
import com.ganqiang.swift.storage.disk.DiskContextHandler;
import com.ganqiang.swift.storage.index.LuceneContextHandler;
import com.ganqiang.swift.timer.TimerContextHandler;

public interface Visitor
{
  void visitLocalConfig(LocalConfigHandler handler);

  void visitRemoteConfig(RemoteConfigHandler handler);

  void visitSyncConfig(SyncConfigHandler handler);

  void visitTimerContext(TimerContextHandler handler);

  void visitHttpContext(HttpContextHandler handler);

  void visitHttpProxy(HttpProxyHandler handler);

  void visitInsideSeed(SeedContextHandler handler);

  void visitDiskContext(DiskContextHandler handler);

  void visitDBContext(DBContextHandler handler);

  void visitLuceneContext(LuceneContextHandler handler);

  void visitDelayAccessContext(DelayContextHandler handler);

  void visitSyncContext(SyncContextHandler handler);

  void visitLifeCycleSetting(LifeCycleSettingHandler handler);
  
  void visitInsideSeedPageSize(PageSizeSettingHandler handler);
  
  void visitZKContext(ZKContextHandler handler);

  void visitAll();
}
