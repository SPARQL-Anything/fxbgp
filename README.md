# Satisfiability of Façade-X BGP 

Implementation of two algorithms for Façade-X BGP satisfiability.

Top down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java) (inefficient)

Bottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java) (efficient)

See [EXPERIMENTS.md](EXPERIMENTS.md) for an example of output.

We only perform experiments with real world queries with the Bottom up/CSP algorithm.

See [REAL-WORLD-QUERIES.md](REAL-WORLD-QUERIES.md) for an example of output.

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

Use the script runAll.sh to generate 10 execution files EXPERIMENTS-run-`N`.md/.csv and REAL-WORLD-QUERIES-run-`N`.md/.csv

The script was tested only on a MacBook Pro environment.

## Build tables

We use SPARQL Anything, `fx` stands for `java -jar -Xmx=4g sparql-anything-v1.0.0.jar`:

```bash
fx -q experiments-tables.sparql -v algorithm=T -v complete=false -o "TopDown_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=T -v complete=true -o "TopDown_SolutionPatterns.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=false -o "BottomUp_Satisfiability.csv"
fx -q experiments-tables.sparql -v algorithm=B -v complete=true -o "BottomUp_SolutionPatterns.csv"
fx -q rwq-experiments-tables.sparql -v complete=false -o "RWQ_Satisfiability.csv"
fx -q rwq-experiments-tables.sparql -v complete=true -o "RWQ_SolutionPatterns.csv"
```

## Real world queries

We collected queries mentioning the sparql anything service clause on GitHub. The process and data is documented in [this repository](https://github.com/SPARQL-Anything/facade-x-queries-on-github).

Number of queries analysed: 449 (rwq-number-of-queries.sparql)

Number of BGP identified per queries:

query\_n,number\_of\_bgp

| queries | number of bgp|
|---|---|
|228|1|
|79|2|
|37|3|
|32|4|
|22|5|
|11|11|
|9|10|
|6|7|
|4|6|
|4|9|
|2|13|
|2|22|
|2|26|
|2|27|
|1|12|
|1|17|
|1|20|
|1|24|
|1|25|
|1|29|
|1|31|
|1|40|
|1|8|
