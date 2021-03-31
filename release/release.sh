
gcc -o ../run/jquic -I jquic jquic/*.c
rm ../run/IPTables/*
cp ../IPTables/* ../run/IPTables
