package com.ganqiang.swift.conf;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.util.FileUtil;
import com.ganqiang.swift.util.StringUtil;

public class SyncConfigHandler extends AbstractConfig implements Visitable
{
  
  private final String sync_file = System.getProperty("user.dir")+"/conf/swift-sync.cfg";
  private static SyncConfig syncConfig;
  
  @Override
  String getConfigFile()
  {
    return sync_file;
  }

  @Override
  String getValidateFile()
  {
    return null;
  }

  @Override
  public void loading()
  {
    IniHelper.loadIni(sync_file);
    syncConfig = new SyncConfig();
    String selector = IniHelper.getValue("protocal","selector");
    syncConfig.setSelector(selector);
    if ("rsync".equalsIgnoreCase(selector)) {
      String localpwdfile = IniHelper.getValue(selector,"local_pwd_file");
      String remotePwd = IniHelper.getValue(selector,"remote_pwd");
      if (!StringUtil.isNullOrBlank(localpwdfile)){
        FileUtil.createFile(localpwdfile, remotePwd);
      }
      syncConfig.setRemotePath(IniHelper.getValue(selector,"remote_path"));
      syncConfig.setLocalPwdFile(localpwdfile);
      syncConfig.setRemotePwd(remotePwd);
      syncConfig.setRemoteHost(IniHelper.getValue(selector,"remote_host"));
      syncConfig.setRemoteModule(IniHelper.getValue(selector,"remote_module"));
      syncConfig.setRemoteUser(IniHelper.getValue(selector,"remote_user"));
    } else {
      syncConfig.setRemoteHost(IniHelper.getValue(selector,"remote_host"));
      syncConfig.setRemoteUser(IniHelper.getValue(selector,"remote_user"));
      syncConfig.setRemotePwd(IniHelper.getValue(selector,"remote_pwd"));
      syncConfig.setRemoteDir(IniHelper.getValue(selector,"remote_dir"));
      syncConfig.setRemotePort(IniHelper.getValue(selector,"remote_port"));
    }
    Constants.sync_config = syncConfig;
  }


  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitSyncConfig(this);
  }

 

}
