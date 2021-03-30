# forward all UDP traffic on port 443 to localhost:4000
sudo xhost +
sudo iptables -t nat -A OUTPUT -m owner --uid-owner 1001 -p udp --dport 443 -j DNAT --to-destination 127.0.0.1:4000
