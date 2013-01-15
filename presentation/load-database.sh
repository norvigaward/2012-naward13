#!/bin/sh
TSV=""full""
DB=""pairs.db""

echo Removing old database file $DB...
rm $DB

echo Importing into SQLite3 database $DB...
SQL="DROP TABLE IF EXISTS pairs;
\nCREATE TABLE pairs (a varchar(20), b varchar(20), count int);
\n.separator \\\\t
\n.import $TSV pairs
\nINSERT INTO pairs(a,b,count) SELECT b AS a, a AS b, count FROM pairs;
\nCREATE INDEX ia ON pairs(a,count);
\n.quit
\n"
echo $SQL | sqlite3 $DB

echo Done.

# \nCREATE INDEX ia ON pairs(a);
