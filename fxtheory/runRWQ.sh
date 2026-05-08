#!/bin/bash

for i in {1..10}
do
    echo "Welcome $i times"
    rm REAL-WORLD-QUERIES.md REAL-WORLD-QUERIES.csv
    mvn clean test -Dtest=RealWorldQueriesTest
    cp -rf REAL-WORLD-QUERIES.md REAL-WORLD-QUERIES-run-$i.md
    cp -rf REAL-WORLD-QUERIES.csv REAL-WORLD-QUERIES-run-$i.csv
done


