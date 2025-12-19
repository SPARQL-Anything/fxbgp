#!/bin/bash

for i in {1..10}
do
    echo "Welcome $i times"
    rm EXPERIMENTS.md EXPERIMENTS.csv
    rm REAL-WORLD-QUERIES.md REAL-WORLD-QUERIES.csv
    mvn clean install
    cp -rf EXPERIMENTS.md EXPERIMENTS-run-$i.md
    cp -rf EXPERIMENTS.csv EXPERIMENTS-run-$i.csv
    cp -rf REAL-WORLD-QUERIES.md REAL-WORLD-QUERIES-run-$i.md
    cp -rf REAL-WORLD-QUERIES.csv REAL-WORLD-QUERIES-run-$i.csv
done


