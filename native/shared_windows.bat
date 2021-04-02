@ECHO OFF
gcc -c -fpic -I src -I src/lsquic  src/*.c -Llibs -lssl -ldecrepit -lcrypto -llsquic -lm -lkernel32
gcc -shared -o libs/usercode.dll *.o  -Llibs -lssl -ldecrepit -lcrypto -llsquic -lm -lkernel32
REM rm *.o