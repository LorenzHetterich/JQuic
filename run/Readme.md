# run
Contains all files of the of the 'released' proxy

## jquic
binary used to run tests, proxy or terminal as `proxy_user` whilst `LD_PRELOAD`ing correct libraries. <br>
Source can be found in `release`.

## IPTables
used to setup `proxy_user` user. <br>
copied from `IPTables` every time the `release` gradle task is run.

## native
contains libraries that need to be `LD_PRELOAD`ed. <br>
copied from `native/libs` every time the `release` gradle task is run.

## jquic.jar
compiled java code packed into a jar.
