#!/bin/bash
#这个脚本是用来将 base64 加密的 xml格式报文格式化输出

echo "-----------------------------"
echo -n $1 | base64 -d | sed "s|</\([^>]*\)>|</\1>\n|g"
echo "-----------------------------"
