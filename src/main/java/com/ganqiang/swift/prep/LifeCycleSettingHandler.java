package com.ganqiang.swift.prep;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Chain;
import com.ganqiang.swift.core.Clear;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.EventChain;
import com.ganqiang.swift.fetch.LinkFetchDispatcher;
import com.ganqiang.swift.fetch.PageFetch;
import com.ganqiang.swift.net.sync.FileSync;
import com.ganqiang.swift.net.zk.ZKClient;
import com.ganqiang.swift.parse.ParserDispatcher;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.db.DBStorage;
import com.ganqiang.swift.storage.index.LuceneIndexStorage;
import com.ganqiang.swift.util.StringUtil;

public final class LifeCycleSettingHandler implements Visitable {

	public void setting(Instance instance) {
		String[] inseeds = instance.getInSeeds();
		if (inseeds != null) {
			for (String site : inseeds) {
				String key = Seed.getKey(instance.getId(), site);
				Chain chain = new EventChain();
				chain.addProcess(new LinkFetchDispatcher());
				chain.addProcess(new PageFetch());
				chain.addProcess(new ParserDispatcher());
				if (!StringUtil.isNullOrBlank(instance.getIndex())) {
					chain.addProcess(new LuceneIndexStorage());
				}
				if (instance.isSync()) {
					chain.addProcess(new FileSync());
				}
				chain.addProcess(new DBStorage());
				if (!StringUtil.isNullOrBlank(instance.getAddress())) {
						chain.addProcess(new ZKClient());
				}
				chain.addProcess(new Clear());
				Constants.chain_map.put(key, chain);
			}
		}

		String[] outseeds = instance.getOutSeeds();
		if (outseeds != null) {
			for (String site : outseeds) {
				String key = Seed.getKey(instance.getId(), site);
				Chain chain = new EventChain();
				chain.addProcess(new PageFetch());
				if (instance.isCascade()) {
					chain.addProcess(new ParserDispatcher());
				}
				if (instance.isSync()) {
					chain.addProcess(new FileSync());
				}
				if (!StringUtil.isNullOrBlank(instance.getAddress())) {
						chain.addProcess(new ZKClient());
				}
				chain.addProcess(new Clear());
				Constants.chain_map.put(key, chain);
			}
		}

	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitLifeCycleSetting(this);
	}

}
