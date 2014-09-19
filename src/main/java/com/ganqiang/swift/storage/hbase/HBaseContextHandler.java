package com.ganqiang.swift.storage.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.util.StringUtil;

public class HBaseContextHandler implements Visitable {

	private static final Logger logger = Logger.getLogger(HBaseContextHandler.class);

	private void initTable(HConnectionController hcc) {
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(hcc.getConfig());
			if (admin.tableExists(Constants.hbase_table_name)) {
				logger.info("hbase table " + Constants.hbase_table_name
						+ " already exists!");
			} else {
				HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(Constants.hbase_table_name));
				HColumnDescriptor hcol = new HColumnDescriptor(Constants.hbase_column_family);
				hcol.setMaxVersions(1);
				// hcol.setInMemory(true);
				tableDesc.addFamily(hcol);
				tableDesc.setDurability(Durability.ASYNC_WAL);
				admin.createTable(tableDesc);
			}
		} catch (MasterNotRunningException e) {
			logger.error("hbase init failed. ", e);
		} catch (ZooKeeperConnectionException e) {
			logger.error("hbase init failed. ", e);
		} catch (IOException e) {
			logger.error("hbase init failed. ", e);
		} finally {
			if (admin != null) {
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void localInit(Instance instance) {
		Configuration conf = HBaseConfiguration.create();
		Integer zkclientport = instance.getZkclientport();
		String zkquorum = instance.getZkquorum();
		String hmaster = instance.getHmaster();
		if (!StringUtil.isNullOrBlank(zkquorum)) {
			conf.set("hbase.zookeeper.quorum", zkquorum);
		} else {
			return;
		}
		if (!StringUtil.isNullOrBlank(hmaster)) {
			conf.set("hbase.master", hmaster);
		}
		if (zkclientport != null) {
			conf.set("hbase.zookeeper.property.clientPort",
					String.valueOf(zkclientport));
		}
		HConnectionController cc = new HConnectionController();
		cc.setConfig(conf);
		Constants.local_hbase_map.put(instance.getId(), cc);
		initTable(cc);
	}

	public void remoteInit() {
		RemoteConfig config = Constants.remote_config;
		Configuration conf = HBaseConfiguration.create();
		Integer zkclientport = config.getZkclientport();
		String zkquorum = config.getZkquorum();
		String hmaster = config.getHmaster();
		if (!StringUtil.isNullOrBlank(zkquorum)) {
			conf.set("hbase.zookeeper.quorum", zkquorum);
		} else {
			return;
		}
		if (!StringUtil.isNullOrBlank(hmaster)) {
			conf.set("hbase.master", hmaster);
		}
		if (zkclientport != null) {
			conf.set("hbase.zookeeper.property.clientPort",
					String.valueOf(zkclientport));
		}
		HConnectionController cc = new HConnectionController();
		cc.setConfig(conf);
		Constants.remote_hbase = cc;
		initTable(cc);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitHBaseContext(this);
	}

}
