package com.ganqiang.swift.conf;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.thrift.GlobalConfig;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.util.StringUtil;

public class RemoteConfigHandler extends AbstractConfig implements Visitable {
	private static final Logger logger = Logger
			.getLogger(RemoteConfigHandler.class);

	private final String remote_xml = System.getProperty("user.dir")
			+ "/conf/swift-remote.xml";
	private final String remote_xsd = System.getProperty("user.dir")
			+ "/conf/swift-remote.xsd";

	@Override
	String getConfigFile() {
		return remote_xml;
	}

	@Override
	String getValidateFile() {
		return remote_xsd;
	}

	@Override
	public void loading() {
		RemoteConfig remoteConfig = new RemoteConfig();
		Element swiftNode = XmlHelper.getElement(getDocument(), swift_node);
		Node threadNumNode = swiftNode.selectSingleNode(thread_num_node);
		if (threadNumNode != null && threadNumNode.hasContent()) {
			String threadNum = threadNumNode.getText().trim();
			remoteConfig.setThreadNum(Integer.valueOf(threadNum));
			Constants.thread_num = remoteConfig.getThreadNum();
		}
		Element thriftNode = XmlHelper.getElement(swiftNode, thrift_node);
		if (thriftNode != null) {
			String port = thriftNode.attributeValue(port_node);
			if (!StringUtil.isNullOrBlank(port)) {
				remoteConfig.setPort(Integer.valueOf(port));
			} else {
				logger.error(getConfigFile()
						+ " file configuration error : <swift>-<thrift> has not configuration port attribute.");
				System.exit(1);
			}
			String server = thriftNode.attributeValue(server_node);
			if (!StringUtil.isNullOrBlank(server)) {
				remoteConfig.setServer(server);
			} else {
				remoteConfig.setServer(threadpool_server_value);
			}

			String protocal = thriftNode.attributeValue(protocal_node);
			if (!StringUtil.isNullOrBlank(protocal)) {
				if (remoteConfig.getServer().equalsIgnoreCase(
						nonblocking_server_value)
						&& !protocal.equalsIgnoreCase(compact_protocal_value)) {
					logger.error(getConfigFile()
							+ " file configuration error : <swift>-<thrift> server must use the [compact] protocol.");
					System.exit(1);
				}
				remoteConfig.setProtocal(protocal);
			} else {
				if (remoteConfig.getServer().equalsIgnoreCase(
						nonblocking_server_value)) {
					remoteConfig.setProtocal(compact_protocal_value);
				} else {
					remoteConfig.setProtocal(binary_protocal_value);
				}
			}
		} else {
			logger.error(getConfigFile()
					+ " file configuration error : <swift> has not children element <thrift>.");
			System.exit(1);
		}
		Element zkNode = XmlHelper.getElement(swiftNode, zookeeper_node);
		if (zkNode != null) {
			Node seqidNode = zkNode.selectSingleNode(seq_id_node);
			if (seqidNode == null) {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <seq_id> element.");
				System.exit(1);
			}
			Node addressNode = zkNode.selectSingleNode(address_node);
			if (addressNode == null) {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <address> element.");
				System.exit(1);
			}
			Node totalnodesNode = zkNode.selectSingleNode(total_nodes_node);
			if (totalnodesNode == null) {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <total_nodes> element.");
				System.exit(1);
			}
			String seqid = seqidNode.getText();
			String address = addressNode.getText();
			String totalNodes = totalnodesNode.getText();
			if (!StringUtil.isNullOrBlank(seqid)) {
				remoteConfig.setSeqId(Integer.valueOf(seqid));;
			} else {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <seq_id> element value.");
				System.exit(1);
			}
			if (!StringUtil.isNullOrBlank(address)) {
				remoteConfig.setAddress(address);;
			} else {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <address> element value.");
				System.exit(1);
			}
			if (!StringUtil.isNullOrBlank(totalNodes)) {
				remoteConfig.setTotalNodes(Integer.valueOf(totalNodes));;
			} else {
				logger.error("swift-local.xml file configuration error : <zookeeper> has not <total_nodes> element value.");
				System.exit(1);
			}
		}
		Element storageNode = XmlHelper.getElement(swiftNode, storage_node);
		if (storageNode == null) {
			logger.error("swift-remote.xml file configuration error : <swift> has not child element <storage>.");
			System.exit(1);
		}
		Node disk = storageNode.selectSingleNode(disk_node);
		if (disk == null || StringUtil.isNullOrBlank(disk.getStringValue())) {
			remoteConfig.setDisk(default_disk_path);
		} else {
			remoteConfig.setDisk(disk.getStringValue().trim());
		}

		Element inele = (Element) disk;
		String isSync = inele.attributeValue(is_sync_node);
		if (!StringUtil.isNullOrBlank(isSync)) {
			remoteConfig.setSync(Boolean.valueOf(isSync));
		}

		String syncDomain = inele.attributeValue(sync_domain_node);
		if (!StringUtil.isNullOrBlank(syncDomain)) {
			remoteConfig.setSyncDomain(syncDomain);
		}

		Node node = storageNode.selectSingleNode(thrift_server_node);
		if (node != null && node.hasContent()) {
			remoteConfig.setThriftServer(node.getText().trim());
			Constants.remote_ts = remoteConfig.getThriftServer();
		}

		Node dbNode = storageNode.selectSingleNode(db_node);
		if (dbNode != null) {
			remoteConfig.setDbDriver(dbNode.selectSingleNode(driver_node)
					.getText().trim());
			remoteConfig.setDbUrl(dbNode.selectSingleNode(url_node).getText()
					.trim());
			remoteConfig.setDbUsername(dbNode.selectSingleNode(user_name_node)
					.getText().trim());
			remoteConfig.setDbPassword(dbNode.selectSingleNode(password_node)
					.getText().trim());
			remoteConfig.setDbPoolSize(Integer.valueOf(dbNode
					.selectSingleNode(pool_size_node).getText().trim()));
		}
		
		Node hbaseNode = storageNode.selectSingleNode(hbase_node);
        if (hbaseNode != null) {
            remoteConfig.setHmaster(hbaseNode.selectSingleNode(hbase_master_node) .getText());
            remoteConfig.setZkclientport(Integer.valueOf(hbaseNode.selectSingleNode(zk_client_port_node).getText()));
            remoteConfig.setZkquorum(hbaseNode.selectSingleNode(zk_quorum_node).getText().trim());
        }

		Node index = storageNode.selectSingleNode(index_node);
		if (index != null) {
			if (StringUtil.isNullOrBlank(index.getStringValue())) {
				remoteConfig.setIndex(default_index_path);
			} else {
				remoteConfig.setIndex(index.getStringValue().trim());
			}
		}

		Constants.remote_config = remoteConfig;
	}

	public GlobalConfig get() {
		GlobalConfig conf = new GlobalConfig();
		Element swiftNode = XmlHelper.getElement(getDocument(), swift_node);
		Node threadNumNode = swiftNode.selectSingleNode(thread_num_node);
		if (threadNumNode != null && threadNumNode.hasContent()) {
			Integer threadNum = Integer.valueOf(threadNumNode.getText().trim());
			conf.setThreadNum(threadNum);
		}
		Node httpProxysNode = swiftNode.selectSingleNode(http_proxy_node);
		if (httpProxysNode != null && httpProxysNode.hasContent()) {
			String sproxys = httpProxysNode.getText().trim();
			conf.setHttpProxys(StringUtil.StringToList(sproxys));
		}
		Node thriftNode = swiftNode.selectSingleNode(thrift_node);
		if (thriftNode != null) {
			String port = ((Element) thriftNode).attributeValue(port_node);
			if (!StringUtil.isNullOrBlank(port)) {
				conf.setPort(Integer.valueOf(port));
			}
			String server = ((Element) thriftNode).attributeValue(server_node);
			if (!StringUtil.isNullOrBlank(server)) {
				conf.setServer(server);
			}
			String protocal = ((Element) thriftNode)
					.attributeValue(protocal_node);
			if (!StringUtil.isNullOrBlank(protocal)) {
				conf.setProtocal(protocal);
			}
		}
		
		Node zkNode = swiftNode.selectSingleNode(zookeeper_node);
        if (zkNode != null) {
            Node addressNode = zkNode.selectSingleNode(address_node);
            if (addressNode != null && addressNode.hasContent()) {
                conf.setAddress(addressNode.getText().trim());
            }
            Node seqidNode = zkNode.selectSingleNode(seq_id_node);
            if (seqidNode != null && seqidNode.hasContent()) {
                conf.setSeqId(Integer.valueOf(seqidNode.getText().trim()));
            }
            Node totalNodesNode = zkNode.selectSingleNode(total_nodes_node);
            if (totalNodesNode != null && totalNodesNode.hasContent()) {
                conf.setTotalNodes(Integer.valueOf(totalNodesNode.getText().toString()));
            }
        }

		Node storageNode = swiftNode.selectSingleNode(storage_node);
		if (storageNode == null) {
			return conf;
		}
		Node dbNode = storageNode.selectSingleNode(db_node);
		if (dbNode != null) {
			Node driverNode = dbNode.selectSingleNode(driver_node);
			if (driverNode != null && driverNode.hasContent()) {
				conf.setDbDriver(driverNode.getText().trim());
			}
			Node urlNode = dbNode.selectSingleNode(url_node);
			if (urlNode != null && urlNode.hasContent()) {
				conf.setDbUrl(urlNode.getText().trim());
			}
			Node usernameNode = dbNode.selectSingleNode(user_name_node);
			if (usernameNode != null && usernameNode.hasContent()) {
				conf.setDbUsername(usernameNode.getText().trim());
			}
			Node passwordNode = dbNode.selectSingleNode(password_node);
			if (passwordNode != null && passwordNode.hasContent()) {
				conf.setDbPassword(passwordNode.getText().trim());
			}
			Node poolsizeNode = dbNode.selectSingleNode(pool_size_node);
			if (poolsizeNode != null && poolsizeNode.hasContent()) {
				conf.setDbPoolSize(Integer.valueOf(poolsizeNode.getText()
						.trim()));
			}
		}

		Node hbaseNode = storageNode.selectSingleNode(hbase_node);
		if (hbaseNode != null) {
		    Node hmasterNode = dbNode.selectSingleNode(hbase_master_node);
            if (hmasterNode != null && hmasterNode.hasContent()) {
                conf.setHbaseMaster(hmasterNode.getText().trim());
            }
            Node zkquorumNode = dbNode.selectSingleNode(zk_quorum_node);
            if (zkquorumNode != null && zkquorumNode.hasContent()) {
                conf.setZkQuorum(zkquorumNode.getText().trim());
            }
            Node zkclientportNode = dbNode.selectSingleNode(zk_client_port_node);
            if (zkclientportNode != null && zkclientportNode.hasContent()) {
                conf.setZkClientPort(Integer.valueOf(zkclientportNode.getText()));
            }
		}

		Node diskNode = storageNode.selectSingleNode(disk_node);
		if (diskNode != null && diskNode.hasContent()) {
			String disk = diskNode.getText().trim();
			String issync = ((Element) diskNode).attributeValue(is_sync_node);
			String syncdomain = ((Element) diskNode)
					.attributeValue(sync_domain_node);
			if (StringUtil.isNullOrBlank(disk)) {
				conf.setDisk(default_disk_path);
			} else {
				conf.setDisk(disk);
			}
			if (StringUtil.isNullOrBlank(issync)) {
				conf.setIsSync(true);
			} else {
				conf.setIsSync(Boolean.valueOf(issync));
			}
			conf.setSyncDomain(syncdomain);
		}

		Node indexNode = storageNode.selectSingleNode(index_node);
		if (indexNode != null && indexNode.hasContent()) {
			String index = indexNode.getText();
			if (StringUtil.isNullOrBlank(index)) {
				conf.setIndex(default_index_path);
			} else {
				conf.setIndex(indexNode.getText().trim());
			}
		}
		return conf;
	}

	public void update(GlobalConfig globalconfig) {
		Document doc = getDocument();
		Element swiftNode = XmlHelper.getElement(doc, swift_node);
		if (swiftNode == null) {
			doc.addElement(swift_node);
			logger.error(getConfigFile() + " have not root node.");
		}
		Integer gthreadnum = globalconfig.getThreadNum();
		if (gthreadnum != null && gthreadnum != 0) {
			Node threadNumNode = swiftNode.selectSingleNode(thread_num_node);
			if (threadNumNode != null && threadNumNode.hasContent()) {
				Integer threadNum = Integer.valueOf(threadNumNode.getText()
						.trim());
				if (!threadNum.equals(gthreadnum)) {
					threadNumNode.setText(gthreadnum.toString());
				}
			} else {
				Element ne = swiftNode.addElement(thread_num_node);
				ne.addText(gthreadnum.toString());
			}
		}
		Integer tport = globalconfig.getPort();
		String tserver = globalconfig.getServer();
		String tprotocal = globalconfig.getProtocal();
		if (tport != null || !StringUtil.isNullOrBlank(tserver)
				|| !StringUtil.isNullOrBlank(tprotocal)) {
			Node thriftNode = swiftNode.selectSingleNode(thrift_node);
			if (thriftNode != null) {
				Integer sport = Integer.valueOf(((Element) thriftNode)
						.attributeValue(port_node));
				if (!sport.equals(tport)) {
					((Element) thriftNode).attributeValue(port_node,
							tport.toString());
				}
				String sserver = ((Element) thriftNode)
						.attributeValue(server_node);
				if (!sserver.equalsIgnoreCase(tserver)) {
					((Element) thriftNode).attributeValue(server_node, tserver);
				}
				String sprotocal = ((Element) thriftNode)
						.attributeValue(protocal_node);
				if (!sprotocal.equalsIgnoreCase(tprotocal)) {
					((Element) thriftNode).attributeValue(protocal_node,
							tprotocal);
				}
			} else {
				Element ne = swiftNode.addElement(thrift_node);
				ne.addAttribute(port_node, tport.toString());
				ne.addAttribute(server_node, tserver);
				ne.addAttribute(protocal_node, tprotocal);
			}
		}
		
		Integer seqid = globalconfig.getSeqId();
		String address = globalconfig.getAddress();
		Integer totalnodes = globalconfig.getTotalNodes();
		Node zkNode = swiftNode.selectSingleNode(zookeeper_node);
		if(zkNode != null){
			Node seqidNode = zkNode.selectSingleNode(seq_id_node);
			Node addressNode = zkNode.selectSingleNode(address_node);
			Node totalnodesNode = zkNode.selectSingleNode(total_nodes_node);
			seqidNode.setText(seqid.toString());;
			addressNode.setText(address);
			totalnodesNode.setText(totalnodes.toString());
		}
		
		List<String> httpProxys = globalconfig.getHttpProxys();
		String tproxys = StringUtil.ListToString(httpProxys);
		if (httpProxys != null && !httpProxys.isEmpty()) {
			Node httpProxysNode = swiftNode.selectSingleNode(http_proxy_node);
			if (httpProxysNode != null && httpProxysNode.hasContent()) {
				String sproxys = httpProxysNode.getText().trim();
				if (!tproxys.equals(sproxys)) {
					httpProxysNode.setText(tproxys);
				}
			} else {
				Element ne = swiftNode.addElement(http_proxy_node);
				ne.addText(tproxys);
			}
		}
		Node storageNode = swiftNode.selectSingleNode(storage_node);
		if (storageNode == null) {
			swiftNode.addElement(storage_node);
			logger.error(getConfigFile() + " have not storage node.");
		}
		Node dbNode = storageNode.selectSingleNode(db_node);
		String tdbdriver = globalconfig.getDbDriver();
		String tdburl = globalconfig.getDbUrl();
		String tdbusername = globalconfig.getDbUsername();
		String tdbpassword = globalconfig.getDbPassword();
		Integer tdbpoolsize = globalconfig.getDbPoolSize();
		if (!StringUtil.isNullOrBlank(tdbdriver)) {
			Node driverNode = dbNode.selectSingleNode(driver_node);
			if (driverNode != null && driverNode.hasContent()) {
				String sdriver = driverNode.getText().trim();
				if (!tdbdriver.equals(sdriver)) {
					driverNode.setText(tdbdriver);
				}
			} else {
				Element ne = ((Element) dbNode).addElement(driver_node);
				ne.addText(tdbdriver);
			}
		}
		if (!StringUtil.isNullOrBlank(tdburl)) {
			Node urlNode = dbNode.selectSingleNode(url_node);
			if (urlNode != null && urlNode.hasContent()) {
				String surl = urlNode.getText().trim();
				if (!tdburl.equals(surl)) {
					urlNode.setText(tdburl);
				}
			} else {
				Element ne = ((Element) dbNode).addElement(url_node);
				ne.addText(tdburl);
			}
		}
		if (!StringUtil.isNullOrBlank(tdbusername)) {
			Node usernameNode = dbNode.selectSingleNode(user_name_node);
			if (usernameNode != null && usernameNode.hasContent()) {
				String susername = usernameNode.getText().trim();
				if (!tdbusername.equals(susername)) {
					usernameNode.setText(tdbusername);
				}
			} else {
				Element ne = ((Element) dbNode).addElement(user_name_node);
				ne.addText(tdbusername);
			}
		}
		if (!StringUtil.isNullOrBlank(tdbpassword)) {
			Node passwordNode = dbNode.selectSingleNode(password_node);
			if (passwordNode != null && passwordNode.hasContent()) {
				String spassword = passwordNode.getText().trim();
				if (!tdbpassword.equals(spassword)) {
					passwordNode.setText(tdbpassword);
				}
			} else {
				Element ne = ((Element) dbNode).addElement(password_node);
				ne.addText(tdbpassword);
			}
		}
		if (tdbpoolsize != null) {
			Node poolsizeNode = dbNode.selectSingleNode(pool_size_node);
			if (poolsizeNode != null && poolsizeNode.hasContent()) {
				Integer spoolsize = Integer.valueOf(poolsizeNode.getText());
				if (!tdbpoolsize.equals(spoolsize)) {
					poolsizeNode.setText(tdbpoolsize.toString());
				}
			} else {
				Element ne = ((Element) dbNode).addElement(pool_size_node);
				ne.addText(tdbpoolsize.toString());
			}
		}
		
		
		Node hbaseNode = storageNode.selectSingleNode(hbase_node);
        String remoteHMaster = globalconfig.getHbaseMaster();
        String remoteZkQuorum = globalconfig.getZkQuorum();
        Integer remoteZkClientPort = globalconfig.getZkClientPort();
        if (!StringUtil.isNullOrBlank(remoteHMaster)) {
            Node hmasterNode = hbaseNode.selectSingleNode(hbase_master_node);
            if (hmasterNode != null && hmasterNode.hasContent()) {
                String hbasemaster = hmasterNode.getText().trim();
                if (!remoteHMaster.equals(hbasemaster)) {
                    hmasterNode.setText(remoteHMaster);
                }
            } else {
                Element ne = ((Element) hbaseNode).addElement(hbase_master_node);
                ne.addText(tdbdriver);
            }
        }
        if (!StringUtil.isNullOrBlank(remoteZkQuorum)) {
            Node zkQuorumNode = hbaseNode.selectSingleNode(zk_quorum_node);
            if (zkQuorumNode != null && zkQuorumNode.hasContent()) {
                String zkquorum = zkQuorumNode.getText().trim();
                if (!remoteZkQuorum.equals(zkquorum)) {
                    zkQuorumNode.setText(remoteZkQuorum);
                }
            } else {
                Element ne = ((Element) hbaseNode).addElement(zk_quorum_node);
                ne.addText(remoteZkQuorum);
            }
        }
        if (remoteZkClientPort != null) {
            Node zkClientPort = hbaseNode.selectSingleNode(zk_client_port_node);
            if (zkClientPort != null && zkClientPort.hasContent()) {
                Integer zkclientport = Integer.valueOf(zkClientPort.getText());
                if (!remoteZkClientPort.equals(zkclientport)) {
                    zkClientPort.setText(remoteZkClientPort.toString());
                }
            } else {
                Element ne = ((Element) hbaseNode).addElement(zk_client_port_node);
                ne.addText(remoteZkClientPort.toString());
            }
        }

		Node diskNode = storageNode.selectSingleNode(disk_node);
		String tdisk = globalconfig.getDisk();
		if (!StringUtil.isNullOrBlank(tdisk)) {
			if (diskNode != null && diskNode.hasContent()) {
				String sdiskvalue = diskNode.getText();
				if (!sdiskvalue.equals(tdisk)) {
					diskNode.setText(tdisk);
				}
			} else {
				Element ne = ((Element) storageNode).addElement(disk_node);
				ne.addText(tdisk);
			}
		}

		Boolean tissync = globalconfig.isIsSync();
		String tsyncdomain = globalconfig.getSyncDomain();
		if (tissync != null || !StringUtil.isNullOrBlank(tsyncdomain)) {
			if (diskNode != null && diskNode.hasContent()) {
				String sissync = ((Element) diskNode)
						.attributeValue(is_sync_node);
				Boolean sisync = true;
				if (!StringUtil.isNullOrBlank(sissync)) {
					sisync = Boolean.valueOf(sissync);
				}
				if (!sisync.equals(tissync)) {
					((Element) diskNode).attributeValue(is_sync_node,
							tissync.toString());
				}
				String ssyncdomain = ((Element) diskNode)
						.attributeValue(sync_domain_node);
				if (!ssyncdomain.equals(tsyncdomain)) {
					((Element) diskNode).attributeValue(sync_domain_node,
							tsyncdomain);
				}
			} else {
				Element ne = ((Element) storageNode).addElement(disk_node);
				ne.addText(tdisk);
				ne.addAttribute(is_sync_node, tissync.toString());
				ne.addAttribute(sync_domain_node, tsyncdomain);
			}
		}

		String tindex = globalconfig.getIndex();
		if (!StringUtil.isNullOrBlank(tindex)) {
			Node sindexNode = storageNode.selectSingleNode(index_node);
			if (sindexNode != null && sindexNode.hasContent()) {
				String sindex = sindexNode.getText();
				if (!sindex.equals(tindex)) {
					sindexNode.setText(tindex);
				}
			} else {
				Element ne = ((Element) storageNode).addElement(index_node);
				ne.addText(tindex);
			}
		}

		XmlHelper.saveXML(doc, remote_xml);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitRemoteConfig(this);
	}

	public void updateTime(String instanceid) {
		// TODO Auto-generated method stub

	}

}
