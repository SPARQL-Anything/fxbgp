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
| S_2J | true | - | J | 2 | - 	(-) |
| N_3P_C | false | - | P | 3 | - 	(-) |
| N_2T | false | - | T | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| N_5T | false | - | T | 5 | - 	(-) |
| N_4J | false | - | J | 4 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| N_3J | false | - | J | 3 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| N_3T | false | - | T | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| N_2P_R | false | - | P | 2 | - 	(-) |
| N_5J | false | - | J | 5 | - 	(-) |
| N_2J | false | - | J | 2 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| N_1T | false | - | T | 1 | - 	(-) |
| N_5P_C | false | - | P | 5 | - 	(-) |
| N_3P_R | false | - | P | 3 | - 	(-) |
| N_4P_C | false | - | P | 4 | - 	(-) |
| N_4T | false | - | T | 4 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |
| S_2J | true | - | J | 2 | - 	(-) |
| N_3P_C | false | - | P | 3 | - 	(-) |
| N_2T | false | - | T | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| N_5T | false | - | T | 5 | - 	(-) |
| N_4J | false | - | J | 4 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| N_3J | false | - | J | 3 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| N_3T | false | - | T | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| N_2P_R | false | - | P | 2 | - 	(-) |
| N_5J | false | - | J | 5 | - 	(-) |
| N_2J | false | - | J | 2 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| N_1T | false | - | T | 1 | - 	(-) |
| N_5P_C | false | - | P | 5 | - 	(-) |
| N_3P_R | false | - | P | 3 | - 	(-) |
| N_4P_C | false | - | P | 4 | - 	(-) |
| N_4T | false | - | T | 4 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |

### Bottom up, all annotations (only satisfiable bgps)
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_2J | true | - | J | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |


## Top down / Search
### Top down, only satisfiability
The algorithm stops when 1 satisfiable annotation is found

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_2J | true | - | J | 2 | - 	(-) |
| N_3P_C | false | - | P | 3 | - 	(-) |
| N_2T | false | - | T | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| N_5T | false | - | T | 5 | - 	(-) |
| N_4J | false | - | J | 4 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| N_3J | false | - | J | 3 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| N_3T | false | - | T | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| N_2P_R | false | - | P | 2 | - 	(-) |
| N_5J | false | - | J | 5 | - 	(-) |
| N_2J | false | - | J | 2 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| N_1T | false | - | T | 1 | - 	(-) |
| N_5P_C | false | - | P | 5 | - 	(-) |
| N_3P_R | false | - | P | 3 | - 	(-) |
| N_4P_C | false | - | P | 4 | - 	(-) |
| N_4T | false | - | T | 4 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |
| S_2J | true | - | J | 2 | - 	(-) |
| N_3P_C | false | - | P | 3 | - 	(-) |
| N_2T | false | - | T | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| N_5T | false | - | T | 5 | - 	(-) |
| N_4J | false | - | J | 4 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| N_3J | false | - | J | 3 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| N_3T | false | - | T | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| N_2P_R | false | - | P | 2 | - 	(-) |
| N_5J | false | - | J | 5 | - 	(-) |
| N_2J | false | - | J | 2 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| N_1T | false | - | T | 1 | - 	(-) |
| N_5P_C | false | - | P | 5 | - 	(-) |
| N_3P_R | false | - | P | 3 | - 	(-) |
| N_4P_C | false | - | P | 4 | - 	(-) |
| N_4T | false | - | T | 4 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |

### Top down, all satisfiable annotations
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | annotations found | type | size | ms |
| ---- | ------------ | ----------------- | ---- | ---- | -- |
| S_2J | true | - | J | 2 | - 	(-) |
| S_3T | true | - | T | 3 | - 	(-) |
| S_4P_C | true | - | P | 4 | - 	(-) |
| S_4J | true | - | J | 4 | - 	(-) |
| S_4T | true | - | T | 4 | - 	(-) |
| S_2P_R | true | - | P | 2 | - 	(-) |
| S_3P_C | true | - | P | 3 | - 	(-) |
| S_1T | true | - | T | 1 | - 	(-) |
| S_5T | true | - | T | 5 | - 	(-) |
| S_5P_C | true | - | P | 5 | - 	(-) |
| S_2T | true | - | T | 2 | - 	(-) |
| S_3J | true | - | J | 3 | - 	(-) |

