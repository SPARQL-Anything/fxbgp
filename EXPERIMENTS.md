# Experiments
## Algorithms

Top down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java)

Bottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java)

## Files

Each file tests a single BGP

File names are informative:

 - S: the bgp is satisfiable
 - N: the bgp is not satisfiable
 - [number]: the number of triples in the bgp
 - T: the bgp contains only variables without joins
 - J: the bgp contains one or more joins
 - P: the bgp contains a join on an object and must satisfy the single-path-to-root condition
 - R: the bgp contains a join on an object that is fx:root
 - C: the bgp contains a join on an object that is a container


Examples:

 - S_5T: a satisfiable bgp of 5 triples with only variables without joins
 - E.g. N_3P_C: a not satisfiable bgp of 3 triples that has multiple paths to an object container

### N_1T

The bgp is not satisfiable.

The bgp has 1 triples
```
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
### N_2J

The bgp is not satisfiable.

The bgp has 2 triples
```
?q ?r ?o
?s ?p ?r
```
### N_2P_R

The bgp is not satisfiable.

The bgp has 2 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to fx:root
```
<iri1> ?p <http://sparql.xyz/facade-x/ns/root>
<iri2> ?p1 <http://sparql.xyz/facade-x/ns/root>

```
### N_2T

The bgp is not satisfiable.

The bgp has 2 triples
```
?x ?y ?z
?s <iri> <http://sparql.xyz/facade-x/ns/root>

```
### N_3J

The bgp is not satisfiable.

The bgp has 3 triples
```
?q ?r ?o
?a ?b ?c
?s ?p ?r
```
### N_3P_C

The bgp is not satisfiable.

The bgp has 3 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
<iri1> ?p ?o
<iri2> ?p1 ?o
?o a <Person>
```
### N_3P_R

The bgp is not satisfiable.

The bgp has 3 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to fx:root
```
<iri1> ?p ?o
?o ?p <http://sparql.xyz/facade-x/ns/root>
?s ?p1 <http://sparql.xyz/facade-x/ns/root>

```
### N_3T

The bgp is not satisfiable.

The bgp has 3 triples
```
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
### N_4J

The bgp is not satisfiable.

The bgp has 4 triples
```
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s ?p ?r
```
### N_4P_C

The bgp is not satisfiable.

The bgp has 4 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
?a ?b <iri2>
<iri1> ?p ?o
<iri2> ?p1 ?o
?o a <Person>
```
### N_4T

The bgp is not satisfiable.

The bgp has 4 triples
```
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
### N_5J

The bgp is not satisfiable.

The bgp has 5 triples
```
?q1 ?r1 ?o1
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s ?p ?r

```
### N_5P_C

The bgp is not satisfiable.

The bgp has 5 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
<iri1> ?b ?s
?s ?p ?o
<iri2> ?b1 ?s1
?s1 ?p1 ?o
?o a <Person>

```
### N_5T

The bgp is not satisfiable.

The bgp has 5 triples
```
?q1 ?r1 ?o1
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
### S_1T

The bgp is satisfiable.

The bgp has 1 triples
```
?s ?p ?o
```
### S_2J

The bgp is satisfiable.

The bgp has 2 triples
```
?s ?p ?o
?s ?p1 ?o1
```
### S_2P_R

The bgp is satisfiable.

The bgp has 2 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to fx:root
```
?s ?p <http://sparql.xyz/facade-x/ns/root>
?s1 ?p1 <http://sparql.xyz/facade-x/ns/root>

```
### S_2T

The bgp is satisfiable.

The bgp has 2 triples
```
?s ?p ?o
?x ?y ?z
```
### S_3J

The bgp is satisfiable.

The bgp has 3 triples
```
?s ?p ?o
?s ?p1 ?o1
?o ?p2 ?o2
```
### S_3P_C

The bgp is satisfiable.

The bgp has 3 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
?s ?p ?o
?s1 ?p1 ?o
?o a <Person>
```
### S_3T

The bgp is satisfiable.

The bgp has 3 triples
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
```
### S_4J

The bgp is satisfiable.

The bgp has 4 triples
```
?s ?p ?o
?s ?p1 ?o1
?o ?p2 ?o2
?o ?p3 ?o3
```
### S_4P_C

The bgp is satisfiable.

The bgp has 4 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
?a ?b ?s1
?s ?p ?o
?s1 ?p1 ?o
?o a <Person>
```
### S_4T

The bgp is satisfiable.

The bgp has 4 triples
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
?u ?i ?m
```
### S_5P_C

The bgp is satisfiable.

The bgp has 5 triples

The bgp has  multiple paths to an object

The bgp has multiple paths to a container
```
?a ?b ?s
?s ?p ?o
?a1 ?b1 ?s1
?s1 ?p1 ?o
?o a <Person>

```
### S_5T

The bgp is satisfiable.

The bgp has 5 triples
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
?u ?i ?m
?a ?b ?c

```

## Bottom up / CSP
### Bottom up, only satisfiability
The algorithm stops when 1 satisfiable annotation is found

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_1T | true | (1) | T | 1 | 14 |
| S_2T | true | (1) | T | 2 | 1 |
| S_3T | true | (1) | T | 3 | 1 |
| S_4T | true | (1) | T | 4 | 31 |
| S_5T | true | (1) | T | 5 | 228 |
| N_1T | false | (0) | T | 1 | 1 |
| N_2T | false | (0) | T | 2 | 7 |
| N_3T | false | (0) | T | 3 | 61 |
| N_4T | false | (0) | T | 4 | 207 |
| N_5T | false | (0) | T | 5 | 1494 |
| S_2J | true | (1) | J | 2 | 0 |
| S_3J | true | (1) | J | 3 | 1 |
| S_4J | true | (1) | J | 4 | 29 |
| N_2J | false | (0) | J | 2 | 0 |
| N_3J | false | (0) | J | 3 | 0 |
| N_4J | false | (0) | J | 4 | 0 |
| N_5J | false | (0) | J | 5 | 0 |
| S_3P_C | true | (1) | P | 3 | 2 |
| S_4P_C | true | (1) | P | 4 | 1 |
| S_5P_C | true | (1) | P | 5 | 46 |
| N_3P_C | false | (0) | P | 3 | 3 |
| N_3P_R | false | (0) | P | 3 | 6 |
| N_4P_C | false | (0) | P | 4 | 36 |
| N_5P_C | false | (0) | P | 5 | 202 |

### Bottom up, all annotations (only satisfiable bgps)
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_1T | true | (6) | T | 1 | 0 |
| S_2T | true | (36) | T | 2 | 2 |
| S_3T | true | (216) | T | 3 | 10 |
| S_4T | true | (1296) | T | 4 | 96 |
| S_5T | true | (7776) | T | 5 | 1190 |
| S_2J | true | (36) | J | 2 | 1 |
| S_3J | true | (60) | J | 3 | 4 |
| S_4J | true | (300) | J | 4 | 47 |
| S_3P_C | true | (4) | P | 3 | 1 |
| S_4P_C | true | (8) | P | 4 | 17 |
| S_5P_C | true | (16) | P | 5 | 273 |


## Top down / Search
### Top down, only satisfiability
The algorithm stops when 1 satisfiable annotation is found

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_1T | true | (1) | T | 1 | 1	(4) |
| S_2T | true | (1) | T | 2 | 0	(6) |
| S_3T | true | (1) | T | 3 | 1	(10) |
| S_4T | true | (1) | T | 4 | 3	(14) |
| S_5T | true | (1) | T | 5 | 2	(18) |
| N_1T | false | (0) | T | 1 | 1	(12) |
| N_2T | false | (0) | T | 2 | 449	(16364) |

### Top down, all satisfiable annotations
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_1T | true | (6) | T | 1 | 3	(60) |
| S_2T | true | (36) | T | 2 | 2265	(193237) |

