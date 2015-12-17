#!/bin/bash
#变更 svn 地址脚本

echo -n "please input new svn URL: "
read new_svn

old_svn=`svn info | grep -m 1 -o 'svn.*$'`

svn switch --relocate $old_svn $new_svn
