#!/bin/bash
sudo useradd proxy_user
sudo sh -c 'echo "export DISPLAY=:0.0">/home/proxy_user/.bashrc'
sudo chown proxy_user:proxy_user /home/proxy_user/.bashrc 
