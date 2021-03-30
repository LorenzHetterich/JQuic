% Quic-Proxy(1) proxy 1.0
% Lorenz Hetterich, Sophie Wenning
% 2021

# NAME
jquic - runs JQuic in different configurations

# SYNOPSIS
**jquic** [OPTIONS]

# DESCRIPTION
**jquic** runs a HTTP3/QUIC proxy which can modify traffic as
specified by a java class or by a JSON file containing modification rules.

# OPTIONS
**-t**
: type of configuration used

**-l**
: optional, log level for JQuic loggers, default INFO

**-L**
: optional, log level for lsquic library, default NONE

## java
**-f**
: input file 

## json_http
**-f**
: input file 

**-c**
: certificate file for SSL

**-k**
: key file for SSL

**-v**
: optional, quic version to use, default h3-34

**-dp**
: optional, destination port for proxy to server requests, default 443

**-sp**
: optional, port of proxy server, default 4001

# EXAMPLE
**jquic -t java -f ../test/cli/java/LogPlaintext.java -c ../testcert/cert.crt -k ../testcert/key.key**
: starts a QUIC proxy with rules as specified in example class on default ports

**jquic -t json_http -f ../test/cli/json_http/modify_requests.json -c ../testcert/cert.crt -k ../testcert/key.key**
: starts a QUIC proxy with rules specified in JSON file on default ports

**jquic tests**
: runs all tests

**jquic terminal**
: runs proxy as dedicated proxy user in terminal





