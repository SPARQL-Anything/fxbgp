# Experiment files
## File N_1T
Satisfiable: false
Number of triples: 1
```
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
## File N_2J
Satisfiable: false
Number of triples: 2
```
?q ?r ?o
?s ?p ?r
```
## File N_2P_R
Satisfiable: false
Number of triples: 2
```
<iri1> ?p <http://sparql.xyz/facade-x/ns/root>
<iri2> ?p1 <http://sparql.xyz/facade-x/ns/root>

```
## File N_2T
Satisfiable: false
Number of triples: 2
```
?x ?y ?z
?s <iri> <http://sparql.xyz/facade-x/ns/root>

```
## File N_3J
Satisfiable: false
Number of triples: 3
```
?q ?r ?o
?a ?b ?c
?s ?p ?r
```
## File N_3P_C
Satisfiable: false
Number of triples: 3
```
<iri1> ?p ?o
<iri2> ?p1 ?o
?o a <Person>
```
## File N_3P_R
Satisfiable: false
Number of triples: 3
```
<iri1> ?p ?o
?o ?p <http://sparql.xyz/facade-x/ns/root>
?s ?p1 <http://sparql.xyz/facade-x/ns/root>

```
## File N_3T
Satisfiable: false
Number of triples: 3
```
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
## File N_4J
Satisfiable: false
Number of triples: 4
```
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s ?p ?r
```
## File N_4P_C
Satisfiable: false
Number of triples: 4
```
?a ?b <iri2>
<iri1> ?p ?o
<iri2> ?p1 ?o
?o a <Person>
```
## File N_4T
Satisfiable: false
Number of triples: 4
```
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
## File N_5J
Satisfiable: false
Number of triples: 5
```
?q1 ?r1 ?o1
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s ?p ?r

```
## File N_5P_C
Satisfiable: false
Number of triples: 5
```
<iri1> ?b ?s
?s ?p ?o
<iri2> ?b1 ?s1
?s1 ?p1 ?o
?o a <Person>

```
## File N_5T
Satisfiable: false
Number of triples: 5
```
?q1 ?r1 ?o1
?q ?r ?o
?x ?y ?z
?a ?b ?c
?s <iri> <http://sparql.xyz/facade-x/ns/root>
```
## File S_1T
Satisfiable: true
Number of triples: 1
```
?s ?p ?o
```
## File S_2J
Satisfiable: true
Number of triples: 2
```
?s ?p ?o
?s ?p1 ?o1
```
## File S_2P_R
Satisfiable: true
Number of triples: 2
```
?s ?p <http://sparql.xyz/facade-x/ns/root>
?s1 ?p1 <http://sparql.xyz/facade-x/ns/root>

```
## File S_2T
Satisfiable: true
Number of triples: 2
```
?s ?p ?o
?x ?y ?z
```
## File S_3J
Satisfiable: true
Number of triples: 3
```
?s ?p ?o
?s ?p1 ?o1
?o ?p2 ?o2
```
## File S_3P_C
Satisfiable: true
Number of triples: 3
```
?s ?p ?o
?s1 ?p1 ?o
?o a <Person>
```
## File S_3T
Satisfiable: true
Number of triples: 3
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
```
## File S_4J
Satisfiable: true
Number of triples: 4
```
?s ?p ?o
?s ?p1 ?o1
?o ?p2 ?o2
?o ?p3 ?o3
```
## File S_4P_C
Satisfiable: true
Number of triples: 4
```
?a ?b ?s1
?s ?p ?o
?s1 ?p1 ?o
?o a <Person>
```
## File S_4T
Satisfiable: true
Number of triples: 4
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
?u ?i ?m
```
## File S_5P_C
Satisfiable: true
Number of triples: 5
```
?a ?b ?s
?s ?p ?o
?a1 ?b1 ?s1
?s1 ?p1 ?o
?o a <Person>

```
## File S_5T
Satisfiable: true
Number of triples: 5
```
?s ?p ?o
?x ?y ?z
?q ?r ?t
?u ?i ?m
?a ?b ?c

```

## Bottom up, only satisfiability

| name | sat (ann) | type | size | ms |
| ---- | --------- | ---- | ---- | -- |
| S_1T | true | (1) | T | 1 | 18 |
| S_2T | true | (1) | T | 2 | 1 |
| S_3T | true | (1) | T | 3 | 8 |
| S_4T | true | (1) | T | 4 | 4 |
| S_5T | true | (1) | T | 5 | 14 |
| N_1T | false | (0) | T | 1 | 1 |
| N_2T | false | (0) | T | 2 | 4 |
| N_3T | false | (0) | T | 3 | 46 |
| N_4T | false | (0) | T | 4 | 309 |
| N_5T | false | (0) | T | 5 | 1522 |
| S_2J | true | (1) | J | 2 | 1 |
| S_3J | true | (1) | J | 3 | 3 |
| S_4J | true | (1) | J | 4 | 3 |
| N_2J | false | (0) | J | 2 | 0 |
| N_3J | false | (0) | J | 3 | 0 |
| N_4J | false | (0) | J | 4 | 0 |
| N_5J | false | (0) | J | 5 | 0 |
| S_3P_C | true | (1) | P | 3 | 1 |
| S_4P_C | true | (1) | P | 4 | 3 |
| S_5P_C | true | (1) | P | 5 | 106 |
| N_3P_C | false | (0) | P | 3 | 10 |
| N_3P_R | false | (0) | P | 3 | 12 |
| N_4P_C | false | (0) | P | 4 | 77 |
| N_5P_C | false | (0) | P | 5 | 275 |

## Bottom up, all annotations (only satisfiable bgps)

| name | sat (ann) | type | size | ms |
| ---- | --------- | ---- | ---- | -- |
| S_1T | true | (6) | T | 1 | 0 |
| S_2T | true | (36) | T | 2 | 0 |
| S_3T | true | (216) | T | 3 | 8 |
| S_4T | true | (1296) | T | 4 | 90 |
| S_5T | true | (7776) | T | 5 | 1267 |
| S_2J | true | (36) | J | 2 | 0 |
| S_3J | true | (60) | J | 3 | 5 |
| S_4J | true | (300) | J | 4 | 53 |
| S_3P_C | true | (4) | P | 3 | 2 |
| S_4P_C | true | (8) | P | 4 | 14 |
| S_5P_C | true | (16) | P | 5 | 205 |

## Top down, only satisfiability
| name | sat (ann) | type | size | ms |
| ---- | --------- | ---- | ---- | -- |
| S_1T | true | (1) | T | 1 | 4	(4) |
| S_2T | true | (1) | T | 2 | 0	(6) |
| S_3T | true | (1) | T | 3 | 2	(11) |
| S_4T | true | (1) | T | 4 | 3	(13) |
| S_5T | true | (1) | T | 5 | 2	(19) |
| N_1T | false | (0) | T | 1 | 0	(12) |
| N_2T | false | (0) | T | 2 | 423	(16364) |
## Top down, all satisfiable annotations
| name | sat (ann) | type | size | ms |
| ---- | --------- | ---- | ---- | -- |
| S_1T | true | (6) | T | 1 | 2	(60) |
| S_2T | true | (36) | T | 2 | 2131	(193237) |
