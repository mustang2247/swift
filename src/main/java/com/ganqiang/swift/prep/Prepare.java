package com.ganqiang.swift.prep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.conf.LocalConfig;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.conf.LocalConfigHandler;
import com.ganqiang.swift.conf.RemoteConfigHandler;
import com.ganqiang.swift.conf.SyncConfig;
import com.ganqiang.swift.conf.SyncConfigHandler;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.http.HttpContextHandler;
import com.ganqiang.swift.net.http.HttpProxyHandler;
import com.ganqiang.swift.net.sync.SyncContextHandler;
import com.ganqiang.swift.net.zk.ZKContextHandler;
import com.ganqiang.swift.seed.PageSizeSettingHandler;
import com.ganqiang.swift.seed.SeedContextHandler;
import com.ganqiang.swift.storage.db.DBContextHandler;
import com.ganqiang.swift.storage.disk.DiskContextHandler;
import com.ganqiang.swift.storage.hbase.HBaseContextHandler;
import com.ganqiang.swift.storage.index.LuceneContextHandler;
import com.ganqiang.swift.timer.TimerContextHandler;

public class Prepare implements Visitor {
	private static final Logger logger = Logger.getLogger(Prepare.class);

	private boolean is_validate = false;
	private static Collection<Visitable> list = new ArrayList<Visitable>();

	private StartupMode mode;

	public Prepare(StartupMode mode) {
		this.mode = mode;
		switch (mode) {
		case local:
			list.add(new LocalConfigHandler());
			list.add(new SyncConfigHandler());
			list.add(new SeedContextHandler());
			list.add(new ZKContextHandler());
			list.add(new HttpContextHandler());
			list.add(new HttpProxyHandler());
			list.add(new PageSizeSettingHandler());
			list.add(new TimerContextHandler());
			list.add(new DBContextHandler());
			list.add(new HBaseContextHandler());
			list.add(new DiskContextHandler());
			list.add(new LuceneContextHandler());
			list.add(new DelayContextHandler());
			list.add(new SyncContextHandler());
			list.add(new LifeCycleSettingHandler());
			break;
		case remote:
			list.add(new RemoteConfigHandler());
			list.add(new SyncConfigHandler());
			list.add(new SeedContextHandler());
			list.add(new ZKContextHandler());
			list.add(new HttpContextHandler());
			list.add(new TimerContextHandler());
			list.add(new DBContextHandler());
			list.add(new HBaseContextHandler());
			list.add(new SyncContextHandler());
			break;
		}
	}

	public static void main(String... args) {
		Visitor visitor = new Prepare(StartupMode.local);
		visitor.visitAll();
	}

	@Override
	public void visitLocalConfig(LocalConfigHandler handler) {
		logger.info("loading local configuration...");
		if (is_validate) {
			handler.validate();
		}
		handler.loading();
		logger.info("loading local configuration finish.");
	}

	@Override
	public void visitRemoteConfig(RemoteConfigHandler handler) {
		logger.info("loading remote configuration...");
		if (is_validate) {
			handler.validate();
		}
		handler.loading();
		logger.info("loading remote configuration finish.");
	}

	@Override
	public void visitSyncConfig(SyncConfigHandler handler) {
		logger.info("loading sync configuration...");
		handler.loading();
		logger.info("loading sync configuration finish.");
	}

	@Override
	public void visitTimerContext(TimerContextHandler handler) {
		logger.info("setting timer context...");
		handler.init();
		logger.info("setting timer context finish.");
	}

	@Override
	public void visitHttpContext(HttpContextHandler handler) {
		logger.info("setting http context...");
		handler.init();
		logger.info("setting http context finish.");
	}

	@Override
	public void visitHttpProxy(HttpProxyHandler handler) {
		logger.info("checking local http proxy...");
		handler.localCheck();
		logger.info("checking local http proxy finish.");
	}

	@Override
	public void visitInsideSeed(SeedContextHandler handler) {
		logger.info("loading seed context...");
		handler.register();
		logger.info("loading seed context finish.");
	}

	@Override
	public void visitDBContext(DBContextHandler handler) {
		logger.info("setting db context...");
		switch (mode) {
		case local:
			LocalConfig lc = Constants.local_config;
			List<Instance> list = lc.getInstances();
			for (Instance instance : list) {
				handler.localInit(instance);
			}
			logger.info("setting local db context finish.");
			break;
		case remote:
			handler.remoteInit();
			logger.info("setting remote db context finish.");
		}
	}
	
	@Override
    public void visitHBaseContext(HBaseContextHandler handler) {
	    logger.info("setting hbase context...");
        switch (mode) {
        case local:
            LocalConfig lc = Constants.local_config;
            List<Instance> list = lc.getInstances();
            for (Instance instance : list) {
                handler.localInit(instance);
            }
            logger.info("setting local hbase context finish.");
            break;
        case remote:
            handler.remoteInit();
            logger.info("setting remote hbase context finish.");
        }
    }

	@Override
	public void visitAll() {
		Iterator<Visitable> iterator = list.iterator();
		while (iterator.hasNext()) {
			Visitable o = iterator.next();
			o.accept(this);
		}
	}

	@Override
	public void visitLuceneContext(LuceneContextHandler handler) {
		logger.info("setting lucnene index context...");
		LocalConfig lc = Constants.local_config;
		List<Instance> list = lc.getInstances();
		for (Instance instance : list) {
			handler.init(instance);
		}
		logger.info("setting lucnene index context finish.");
	}

	@Override
	public void visitDiskContext(DiskContextHandler handler) {
		logger.info("setting disk context...");
		LocalConfig lc = Constants.local_config;
		List<Instance> list = lc.getInstances();
		for (Instance instance : list) {
			handler.init(instance);
		}
		logger.info("setting disk context finish.");
	}

	@Override
	public void visitDelayAccessContext(DelayContextHandler handler) {
		logger.info("setting delay context...");
		LocalConfig lc = Constants.local_config;
		List<Instance> list = lc.getInstances();
		for (Instance instance : list) {
			handler.init(instance);
		}
		logger.info("setting delay context finish.");
	}

	@Override
	public void visitSyncContext(SyncContextHandler handler) {
		logger.info("setting sync context...");
		SyncConfig sc = Constants.sync_config;
		handler.init(sc);
		logger.info("setting sync context finish.");
	}

	@Override
	public void visitLifeCycleSetting(LifeCycleSettingHandler handler) {
		logger.info("setting local lifecycle...");
		LocalConfig lc = Constants.local_config;
		List<Instance> list = lc.getInstances();
		for (Instance instance : list) {
			handler.setting(instance);
		}
		logger.info("setting local lifecycle finish.");
	}

	@Override
	public void visitInsideSeedPageSize(PageSizeSettingHandler handler) {
		logger.info("setting local inside seed page size...");
		LocalConfig lc = Constants.local_config;
		List<Instance> list = lc.getInstances();
		for (Instance instance : list) {
			handler.init(instance);
		}
		logger.info("setting local inside seed page size finish.");
	}

	@Override
	public void visitZKContext(ZKContextHandler handler) {
		logger.info("setting zookeeper context...");
		switch (mode) {
		case local:
			LocalConfig lc = Constants.local_config;
			List<Instance> list = lc.getInstances();
			for (Instance instance : list) {
				 handler.localInit(instance);
			}
			logger.info("setting local zookeeper context finish.");
			break;
		case remote:
			handler.remoteInit();
			logger.info("setting remote zookeeper context finish.");
		}
	}

    

}
