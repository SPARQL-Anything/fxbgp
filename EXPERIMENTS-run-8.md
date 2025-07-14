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

| name | satisfiable? | found | type | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| N_1T | false | 0 | T | 1 | 17 | 12 |
| N_2J | false | 0 | J | 2 | 0 | 12 |
| N_2P_R | false | 0 | P | 2 | 5 | 36 |
| N_2T | false | 0 | T | 2 | 12 | 144 |
| N_3J | false | 0 | J | 3 | 0 | 144 |
| N_3P_C | false | 0 | P | 3 | 11 | 432 |
| N_3P_R | false | 0 | P | 3 | 6 | 144 |
| N_3T | false | 0 | T | 3 | 37 | 1728 |
| N_4J | false | 0 | J | 4 | 0 | 1728 |
| N_4P_C | false | 0 | P | 4 | 69 | 5184 |
| N_4T | false | 0 | T | 4 | 202 | 20736 |
| N_5J | false | 0 | J | 5 | 0 | 20736 |
| N_5P_C | false | 0 | P | 5 | 456 | 62208 |
| N_5T | false | 0 | T | 5 | 1919 | 248832 |
| S_1T | true | 1 | T | 1 | 0 | 12 |
| S_2J | true | 1 | J | 2 | 0 | 144 |
| S_2P_R | true | 1 | P | 2 | 1 | 36 |
| S_2T | true | 1 | T | 2 | 0 | 144 |
| S_3J | true | 1 | J | 3 | 0 | 1728 |
| S_3P_C | true | 1 | P | 3 | 0 | 432 |
| S_3T | true | 1 | T | 3 | 0 | 1728 |
| S_4J | true | 1 | J | 4 | 33 | 20736 |
| S_4P_C | true | 1 | P | 4 | 1 | 5184 |
| S_4T | true | 1 | T | 4 | 5 | 20736 |
| S_5P_C | true | 1 | P | 5 | 34 | 62208 |
| S_5T | true | 1 | T | 5 | 179 | 248832 |

### Bottom up, all annotations (only satisfiable bgps)
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | found | type | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| S_1T | true | 6 | T | 1 | 0 | 12 |
| S_2J | true | 36 | J | 2 | 1 | 144 |
| S_2P_R | true | 1 | P | 2 | 1 | 36 |
| S_2T | true | 36 | T | 2 | 1 | 144 |
| S_3J | true | 60 | J | 3 | 6 | 1728 |
| S_3P_C | true | 4 | P | 3 | 2 | 432 |
| S_3T | true | 216 | T | 3 | 9 | 1728 |
| S_4J | true | 300 | J | 4 | 75 | 20736 |
| S_4P_C | true | 8 | P | 4 | 23 | 5184 |
| S_4T | true | 1296 | T | 4 | 152 | 20736 |
| S_5P_C | true | 16 | P | 5 | 283 | 62208 |
| S_5T | true | 7776 | T | 5 | 1505 | 248832 |


## Top down / Search
### Top down, only satisfiability
The algorithm stops when 1 satisfiable annotation is found

| name | satisfiable? | found | type | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| N_1T | false | 0 | T | 1 | 1 | 12 |
| N_2J | false | 0 | J | 2 | 0 | 12 |
| N_2P_R | false | 0 | P | 2 | 56 | 1957 |
| N_2T | false | 0 | T | 2 | 451 | 16364 |
| N_3J | false | 0 | J | 3 | 0 | 16364 |
| N_3P_C | false | 0 | P | 3 | 2572 | 109601 |
| N_3P_R | false | 0 | P | 3 | 247 | 12330 |
| N_3T | false | -1 | T | 3 | -1 | -1 |
| N_4J | false | 0 | J | 4 | 0 | 136661 |
| N_4P_C | false | -1 | P | 4 | -1 | -1 |
| N_4T | false | -1 | T | 4 | -1 | -1 |
| N_5J | false | 0 | J | 5 | 0 | 505037 |
| N_5P_C | false | -1 | P | 5 | -1 | -1 |
| N_5T | false | -1 | T | 5 | -1 | -1 |
| S_1T | true | 1 | T | 1 | 0 | 22 |
| S_2J | true | 1 | J | 2 | 0 | 19 |
| S_2P_R | true | 1 | P | 2 | 47 | 1500 |
| S_2T | true | 1 | T | 2 | 0 | 16 |
| S_3J | true | 1 | J | 3 | 136 | 12588 |
| S_3P_C | true | 1 | P | 3 | 0 | 44 |
| S_3T | true | 1 | T | 3 | 1 | 45 |
| S_4J | true | 1 | J | 4 | 40 | 7498 |
| S_4P_C | true | 1 | P | 4 | 4218 | 812489 |
| S_4T | true | 1 | T | 4 | 1 | 52 |
| S_5P_C | true | 1 | P | 5 | 1 | 82 |
| S_5T | true | 1 | T | 5 | 2 | 93 |

### Top down, all satisfiable annotations
The algorithm proceeds to find all possible satisfiable annotations

| name | satisfiable? | found | type | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| S_1T | true | 6 | T | 1 | 4 | 151 |
| S_2J | true | 36 | J | 2 | 593 | 62196 |
| S_2P_R | true | 1 | P | 2 | 24 | 5737 |
| S_2T | true | 36 | T | 2 | 2741 | 596242 |
| S_3J | true | -1 | J | 3 | -1 | -1 |
| S_3P_C | true | -1 | P | 3 | -1 | -1 |
| S_3T | true | -1 | T | 3 | -1 | -1 |
| S_4J | true | -1 | J | 4 | -1 | -1 |
| S_4P_C | true | -1 | P | 4 | -1 | -1 |
| S_4T | true | -1 | T | 4 | -1 | -1 |
| S_5P_C | true | -1 | P | 5 | -1 | -1 |
| S_5T | true | -1 | T | 5 | -1 | -1 |

