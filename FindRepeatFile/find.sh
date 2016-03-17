#!/bin/bash
#
# 该脚本需要传入一个目录地址，查找重复的文件
#

#find $1 -type f -exec md5sum -b '{}' + | sort -k 1 | uniq -w 32 -d | cut -d '*' -f 2-

getMd5Files(){
	md5Files=`find $1 -type f -exec md5sum -b '{}' +`
}

getMd5Files $1

echo $md5Files
