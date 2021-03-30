# IPTables
This directory contains scripts used to setup a user whose traffic is proxied and to run a terminal as this user. <br>

## create_user.sh
Creates a user named `proxy_user` and sets up the `~/.bashrc` file for this user to use the local display.

## ip_tables.sh
Configures the iptables of `proxy_user` such that all udp traffic to port `443` is redirected to `localhost:4000`. <br>
Any other traffic is untouched

## term.sh
Allows launching a terminal as `proxy_user`. <br>
GUI applications should`tm` work, only tested on `Ubuntu 20.04`
