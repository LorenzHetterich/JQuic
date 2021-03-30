# release
Contains code of the `jquic` binary used to run the proxy, tests or a terminal as `proxy_user` whilst preloading the correct libraries. <br>

## release.sh
Compiles the `jquic` binary and puts it into the `run` directory. <br>
Also copies some other things

## jquic
Contains c code of `jquic` binary (this binary is used to `LD_PRELOAD` all required libraries when launching the java code)

