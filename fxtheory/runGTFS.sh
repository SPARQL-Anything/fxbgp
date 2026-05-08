#!/bin/bash

for i in {1..10}
do
    echo "Welcome $i times"
    rm GTFS-QUERIES.md GTFS-QUERIES.csv
    mvn clean test -Dtest=GTFSQueriesTest
    cp -rf GTFS-QUERIES.md GTFS-QUERIES-run-$i.md
    cp -rf GTFS-QUERIES.csv GTFS-QUERIES-run-$i.csv
done


