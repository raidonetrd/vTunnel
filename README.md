# vTunnel 

A websocket protocol based android VPN app.

![image](https://img.shields.io/badge/License-MIT-orange)
![image](https://img.shields.io/badge/License-Anti--996-red)
![image](https://img.shields.io/github/downloads/net-byte/vTunnel/total.svg)

# Requirements
* Android version >= 8.0
* Min SDK version 26

# Screenshot
<p>
	<img src="https://github.com/net-byte/vTunnel/raw/main/assets/screenshot.png" alt="screenshot" width="450">
</p>

# Server setup on linux
[install vtun server with websocket protocol](https://github.com/net-byte/vtun)

# Deploy server  

I recommend you to use caddy2 with automatic https for reverse proxy vtun server.  
1. config your Caddyfile:  
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

# Download
[download](https://github.com/net-byte/vTunnel/releases)

