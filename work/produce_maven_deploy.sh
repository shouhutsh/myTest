#!/bin/bash
#此脚本是为了生成
#[WARNING] The POM for com.sun.xml.bind:jaxb-xjc:jar:2.1.13 is missing, no dependency information available
#异常提示所缺失的 jar 包，生成
#mvn deploy:deploy-file -DgroupId=com.sun.xml.bind -DartifactId=jaxb-xjc -Dversion=2.1.13 -Dpackaging=jar -Dfile=D:\jar\jaxb-xjc-2.1.13.jar -Durl=http://192.168.100.102:18081/nexus/content/repositories/thirdparty/ -DrepositoryId=thirdparty
#maven命令

#PS：
#使用前需要配置 repo_path 和 error_log_file
# repo_path maven库的路径
# error_log_file 即异常提示的文件

repo_path=.
error_log_file=test.txt

for i in `sed 's/^.*for \([^ ]*\).*/\1/' $error_log_file`; do
	jar_name=`echo $i | sed 's/\(.*\):\(.*\):\(.*\):\(.*\)/\2-\4\.jar/'`
	jar_path=`find $repo_path -name "$jar_name"`
	echo -n "mvn deploy:deploy-file "
	echo -n `echo $i | sed 's/\(.*\):\(.*\):\(.*\):\(.*\)/-DgroupId=\1 -DartifactId=\2 -Dversion=\4 -Dpackaging=\3 -Dfile=$jar_path/'`
	echo " -Durl=http://192.168.100.102:18081/nexus/content/repositories/thirdparty/ -DrepositoryId=thirdparty"
done;