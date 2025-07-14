# Satisfiability of Façade-X BGP 

Implementation of two algorithms for Façade-X BGP satisfiability.

Top down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java) (inefficient)

Bottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java) (efficient)

See [EXPERIMENTS.md](EXPERIMENTS.md) for an example of output.

## Build and run experiments
Building or running tests with maven will also re-generate the file EXPERIMENTS.md

```
mvn clean install
```

or

```
mvn test
```

## Run the experiments 10 times

Use the script runAll.sh to generate 10 execution files EXPERIMENTS-run-`N`.md/.csv

The script was tested only on a MacBook Pro environment.

## Build tables

We use SPARQL Anything, `fx` stands for `java -jar -Xmx=4g sparql-anything-v1.0.0.jar`:

```bash
fx -q experiments-tables.sparql -v algorithm=T -v complete=false -o "TopDown_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=T -v complete=true -o "TopDown_SolutionPatterns.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=false -o "BottomUp_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=true -o "BottomUp_SolutionPatterns.csv"
```


