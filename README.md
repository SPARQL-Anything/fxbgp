# Satisfiability of Façade-X BGP 

Implementation of two algorithms for Façade-X BGP satisfiability.

Top down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java) (inefficient)

Bottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java) (efficient)

See [EXPERIMENTS.md](EXPERIMENTS.md)

## Build and run experiments
Building or running tests with maven will also re-generate the file EXPERIMENTS.md

```
mvn clean install
```

or

```
mvn test
```



