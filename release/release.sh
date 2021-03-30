gcc -o ../run/jquic -I jquic jquic/*.c
rm ../run/native/libs/*
cp ../native/libs/* ../run/native/libs/
rm ../run/IPTables/*
cp ../IPTables/* ../run/IPTables
