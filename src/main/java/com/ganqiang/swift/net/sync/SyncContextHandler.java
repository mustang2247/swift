package com.ganqiang.swift.net.sync;

import org.python.util.PythonInterpreter;

import com.ganqiang.swift.conf.SyncConfig;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;

public final class SyncContextHandler implements Visitable
{
  private static final String script_path = System.getProperty("user.dir")+"/bin/";
  public static String dict = "";
  public static PythonInterpreter interpreter = new PythonInterpreter();

  public void init(SyncConfig conf){
    StringBuilder str = new StringBuilder("map = {");
    str.append("'selector':'" + conf.getSelector()+"', ");
    str.append("'remote_host':'" + conf.getRemoteHost()+"', ");
    str.append("'remote_user':'" + conf.getRemoteUser()+"', ");
    str.append("'remote_pwd':'" + conf.getRemotePwd()+"', ");
    str.append("'remote_module':'" + conf.getRemoteModule()+"', ");
    str.append("'local_pwd_file':'" + conf.getLocalPwdFile()+"', ");
    str.append("'remote_dir':'" + conf.getRemoteDir()+"', ");
    str.append("'remote_port':'" + conf.getRemotePort()+"', ");
    str.append("'remote_path':'" + conf.getRemotePath()+"' ");
    str.append("}");
    dict = str.toString();
    interpreter.exec("import sys");
    interpreter.exec("sys.path.append('"+script_path+"')");
    interpreter.exec("import bootstrap");
    interpreter.exec(dict);
  }
  
  public void close(){
    interpreter.cleanup();
    dict = null;
  }

  @Override
  public void accept(Visitor visitor)
  {
    visitor.visitSyncContext(this);
  }
}
