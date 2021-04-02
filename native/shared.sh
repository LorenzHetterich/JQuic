gcc -c -Wall -Werror -fpic src/*.c -I src/lsquic -I src -I/lib/jvm/java-14-openjdk-amd64/include -I/lib/jvm/java-14-openjdk-amd64/include/linux -Llibs -lpthread -lssl -ldecrepit -lcrypto -llsquic -lm
gcc -shared -o libs/libusercode.so *.o -Llibs -lpthread -lssl -ldecrepit -lcrypto -llsquic -lm
cp libs/libusercode.so ../src/main/resources/linux-x86-64/libusercode.so
rm *.o
