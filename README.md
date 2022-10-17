# vTunnel 

A simple VPN app for Android.

![image](https://img.shields.io/badge/License-MIT-orange)
![image](https://img.shields.io/badge/License-Anti--996-red)
![image](https://img.shields.io/github/downloads/net-byte/vTunnel/total.svg)
<p>
<a href="https://play.google.com/store/apps/details?id=com.netbyte.vtunnel"><img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="100"></a>
</p>

# Requirements
* Android version >= 8.0
* Min SDK version 26

# Server setup on linux
[install vtun server with websocket protocol](https://github.com/net-byte/vtun)

# Deploy server  

Reverse proxy vtun server via caddy2.  
1. config Caddyfile:  
```
your.domain {
    reverse_proxy localhost:3001
}
```
2. deploy caddy2 on docker  
docker run -d -p 80:80 -p 443:443 --name caddy --restart=always --net=host -v /data/caddy/Caddyfile:/etc/
caddy/Caddyfile -v /data/caddy/data:/data caddy

3. deploy vtun server on docker  
docker run  -d --privileged --restart=always --net=host --name vtun-server netbyte/vtun -S -l=:3001 -c=172.16.0.1/24 -k=123456 -p ws



