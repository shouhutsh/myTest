#!/bin/bash
# 将各系统自动编译并上传到服务器指定目录

TARGET="user@XXX.XXX.XXX.XXX:/tmp/"

deploy(){
	(cd `ls -d *[Cc]ommon*`/trunk; bash install.sh)

	for d in `ls -d */ | grep -o -P "\w+"`; do
	    if ! [ `echo $d | grep -P "[Cc]ommon"` ]; then
            (cd $d/trunk;
            mvn clean;
            mvn install -Dmaven.test.skip=true;
            tar zcvf $d.tar target/latest*.gz;
            scp -P 310 $d.tar $TARGET &);
        fi
	done;
}

(cd system1; deploy)
(cd system2; deploy)