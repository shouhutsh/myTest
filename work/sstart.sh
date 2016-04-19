#!/bin/bash
# 该脚本是为了方便启动shadowsocks，自动找到免费的代理，配置，启动

Config=/etc/shadowsocks/config.json
Html=`curl -s http://www.ishadowsocks.net/`

Val=`echo $Html | grep -o -P "<section id=\"free\">.*?</section>" | grep -o -P "<div class=\"col-lg-4 text-center\">.*?</div>" | head -n 1`

Addr=`echo $Val | grep -o -P "服务器地址:[^<]+" | sed "s/服务器地址:\(\)/\1/"`
Port=`echo $Val | grep -o -P "端口:[^<]+" | sed "s/端口:\(\)/\1/"`
Pass=`echo $Val | grep -o -P "密码:[^<]+" | sed "s/密码:\(\)/\1/"`
Method=`echo $Val | grep -o -P "加密方式:[^<]+" | sed "s/加密方式:\(\)/\1/"`

cat << EOF > $Config
{
    "server":"$Addr",
    "server_port":$Port,
    "local_address": "127.0.0.1",
    "local_port":1080,
    "password":"$Pass",
    "timeout":300,
    "method":"$Method",
    "fast_open": false,
    "workers": 1
}
EOF

killall -q sslocal
sslocal -c $Config &
