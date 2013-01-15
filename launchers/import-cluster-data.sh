#!/bin/sh
HPATH="/user/naward13/traitor-output-testset/*"

DIR="../traitor-data"
TSV=""$DIR/cluster-testset.tsv""
DB=""$DIR/cluster-testset.db""

if [ -f $TSV ];
then
   echo Tab-Separated-File $TSV already exists. Remove it to redownload from HadoopFS.
else
   echo Downloading latest HadoopFS file $HPATH...
   hadoop fs -text $HPATH/* > $TSV
fi


echo Removing old database file $DB...
rm $DB

echo Importing into SQLite3 database $DB...
SQL="DROP TABLE IF EXISTS pairs;
\nCREATE TABLE pairs (a varchar(20), b varchar(20), count int);
\nCREATE INDEX ia ON pairs(a);
\nCREATE INDEX ib ON pairs(b);
\nCREATE INDEX icount ON pairs(count);
\n.separator \\\\t
\n.import $TSV pairs
\n.quit
\n"
echo $SQL | sqlite3 $DB


echo Linking database to presentation application...
ln -fs $DB ../presentation/pairs.db

echo Done.
