# start a terminal as proxy user
sh ip_tables.sh
sudo -H -u proxy_user bash -c 'cd /home/proxy_user&&bash' 