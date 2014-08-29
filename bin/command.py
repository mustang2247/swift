#!/usr/bin/env python

from subprocess import call
from ftplib import FTP
import ftplib
import os
import logging
import sys

swift_path = os.getcwd()

class Command:

    def send(self, local_file):
        pass

class Rsync(Command):
    def __init__(self, cache):
        self.logger = logging.getLogger("Rsync")
        self.remote_module = cache.get("remote_module").replace(" ","")
        self.remote_host = cache.get("remote_host")
        self.remote_user = cache.get("remote_user")
        self.remote_pwd = cache.get("remote_pwd").replace(" ","")
        self.remote_path = cache.get("remote_path").replace(" ","")
        self.local_pwd_file = cache.get("local_pwd_file").replace(" ","")
        if self.local_pwd_file != "":
            self.__create_client_pwd_file()

    def __create_client_pwd_file(self):
        f = open(self.local_pwd_file, "w+")
        f.write(self.remote_pwd)
        os.system("chmod 600 %s" % self.local_pwd_file)
        f.close()

    def send(self, local_root_data_path, local_sub_data_path):
        local_dir = local_sub_data_path.replace(local_root_data_path, ".")
        cmd = "cd " + local_root_data_path + " && rsync -azlWR "
        if self.local_pwd_file == "":
            if self.remote_module == "":
                cmd = cmd + "%s %s@%s:%s" % (local_dir, self.remote_user, self.remote_host, self.remote_path)
            else:
                cmd = cmd + "%s %s@%s::%s" % (local_dir, self.remote_user, self.remote_host, self.remote_module)
        else:
            cmd = cmd + " --password-file=%s "
            if self.remote_module == "":
                cmd = cmd + "%s %s@%s:%s" % (self.local_pwd_file, local_dir, self.remote_user, self.remote_host, self.remote_path)
            else:
                cmd = cmd + "%s %s@%s::%s" % (self.local_pwd_file, local_dir, self.remote_user, self.remote_host, self.remote_module)
        ret = call(cmd, shell=True)
        if ret == 5:
            self.logger.error('Unknown module backup when using [rsync] mode to send data. command=[' + cmd + ']')
        elif ret == 10:
            self.logger.error('Connection timed out when using [rsync] mode to send data. command=[' + cmd + ']')
        elif ret == 23:
            self.logger.error(
                'Did not find local file when using [rsync] mode to send data. command=[' + cmd + ']')
        elif ret != 0:
            self.logger.error('Exception occurred when using [rsync] mode to send data. command=[' + cmd + ']')
        else:
            self.logger.info("Send data was successful on the [rsync] mode. command=[" + cmd + "]'")

class Scp(Command):
    def __init__(self, cache):
        self.logger = logging.getLogger("Scp")
        self.remote_dir = cache.get("remote_dir")
        self.remote_host = cache.get("remote_host")
        self.remote_user = cache.get("remote_user")
        self.remote_pwd = cache.get("remote_pwd")
        self.remote_port = cache.get("remote_port")

    def send(self, local_root_data_path, local_sub_data_path):
        cmd = ""
        try:
            remote_dir = self.remote_dir.rstrip("/") + local_sub_data_path.replace(local_root_data_path, "")
            sub = remote_dir.rstrip("/").rsplit("/")[-1:][0]
            remote_parent_dir = remote_dir.rstrip("/").replace(sub, "")
            cmd = '%s %s %s %s %s %s %s' % (self.remote_port, local_sub_data_path, self.remote_user, 
											self.remote_host, remote_parent_dir, self.remote_pwd, remote_dir)
            os.system(swift_path + '/bin/auto_input.exp ' + cmd)
            self.logger.info("Send data was successful on the [scp] mode. command=[" + cmd + "]'")
        except:
            self.logger.error('Exception occurred when using [scp] mode to send data. command=[' + cmd + ']')

class Ftp(Command):
    def __init__(self, cache):
        self.logger = logging.getLogger("Ftp")
        self.remote_dir = cache.get("remote_dir")
        self.remote_host = cache.get("remote_host")
        self.remote_user = cache.get("remote_user")
        self.remote_pwd = cache.get("remote_pwd")
        self.remote_port = cache.get("remote_port")

    def check_dir(self, li, inner):
        for l in li:
            source = l.strip("/").split("/")[0]
            if inner in source:
                return True
        return False

    def send(self, local_root_data_path, local_sub_data_path):
        try:
            ftp = FTP()
            ftp.set_debuglevel(0)
            ftp.connect(self.remote_host, self.remote_port)
            ftp.login(self.remote_user, self.remote_pwd)
            remote_parent_dir = local_sub_data_path.replace(local_root_data_path, "").strip("/")
            array = remote_parent_dir.split("/")
            i = 0
            length = len(array)
            while i <= length:
                fl = []
                if i == 0:
                   fl = ftp.nlst()
                else:
                   fl = ftp.nlst(array[i - 1])
                   flag = self.check_dir(fl, array[i - 1])
                   if not flag:
                       ftp.mkd(array[i - 1])
                   ftp.cwd(array[i - 1])
                i += 1
            local_files = os.listdir(local_sub_data_path)
            for item in local_files:
                file_handler = open(local_sub_data_path + "/" +item, 'rb')
                ftp.storbinary('STOR %s' % os.path.basename(item), file_handler, 4096)
                ftp.set_debuglevel(0)
                file_handler.close()
            ftp.quit()
            self.logger.info("Send data was successful on the [ftp] mode. remote=[" + self.remote_host + "]'")
        except ftplib.all_errors:
            self.logger.error(
                'Exception occurred when using [ftp] mode to send data. remote=[' + self.remote_host + '] ')
      	else:
            self.logger.error(
                'Exception occurred when using [ftp] mode to send data. remote=[' + self.remote_host + ']')


