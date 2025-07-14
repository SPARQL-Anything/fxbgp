#!/bin/bash


fx -q experiments-tables.sparql -v algorithm=T -v complete=false -o "TopDown_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=T -v complete=true -o "TopDown_SolutionPatterns.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=false -o "BottomUp_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=true -o "BottomUp_SolutionPatterns.csv"