#!/bin/bash

for i in {1..10}
do
    echo "Welcome $i times"
    rm EXPERIMENTS.md EXPERIMENTS.csv
    mvn clean install
    cp EXPERIMENTS.md EXPERIMENTS-run-$i.md
    cp EXPERIMENTS.csv EXPERIMENTS-run-$i.csv
done


