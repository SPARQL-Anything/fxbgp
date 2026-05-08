# Bounded-Memory Basic Graph Pattern Evaluation over Façade-X Resources (Implementation)

## Compile
Compile without tests.

```bash
mvn clean install -DskipTests
```
## Generate executable jar

```bash
mvn install -DskipTests -Pgenerate-jar
```
Produces `target/fxbgp-test-executor-1.2.0-SNAPSHOT.jar`

## Generate Data

To generate the data used in the experiments run 

```bash
mvn test -Dtest=io.github.sparqlanything.fxbgp.stream.performance.PerformanceTest
```

The generated data will be under `target/test-classes/io/github/sparqlanything/fxbgp/stream/performance/performance-test/input`.


## Execution traces

The traces of the experiments reported in the paper for the research questions RQ1 and RQ2 are contained in `execution_traces.zip`. More about the execution traces and how to reproduce the experiments can be found in  README file of  the archive. 
