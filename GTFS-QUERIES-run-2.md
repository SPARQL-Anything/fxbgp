# Experiments with queries from the GTFS madrid benchmark
## Algorithms

Top down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java)

Bottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java)

## Files

Each file may include one or more BGPs


## Bottom up / CSP
### Bottom up, only satisfiability
The algorithm stops when 1 satisfiable annotation is found

| name | bgpx | found | varSize | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| q1-csv-strategy1-no_slice.sparql | 1 | 1 | 5 | 4 | 0 | 16 |
| q10-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q10-csv-strategy1-no_slice.sparql | 2 | 1 | 3 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 1 | 1 | 3 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 2 | 1 | 4 | 3 | 0 | 8 |
| q11-csv-strategy1-no_slice.sparql | 3 | 1 | 1 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 4 | 1 | 2 | 1 | 0 | 2 |
| q12-csv-strategy1-no_slice.sparql | 1 | 1 | 3 | 2 | 0 | 4 |
| q12-csv-strategy1-no_slice.sparql | 2 | 1 | 3 | 2 | 1 | 4 |
| q12-csv-strategy1-no_slice.sparql | 3 | 1 | 3 | 2 | 0 | 4 |
| q12-csv-strategy1-no_slice.sparql | 4 | 1 | 3 | 3 | 0 | 8 |
| q13-csv-strategy1-no_slice.sparql | 1 | 1 | 3 | 2 | 0 | 4 |
| q13-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q13-csv-strategy1-no_slice.sparql | 3 | 1 | 4 | 3 | 0 | 8 |
| q14-csv-strategy1-no_slice.sparql | 1 | 1 | 5 | 4 | 0 | 16 |
| q14-csv-strategy1-no_slice.sparql | 2 | 1 | 3 | 2 | 0 | 4 |
| q14-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q14-csv-strategy1-no_slice.sparql | 4 | 1 | 2 | 1 | 0 | 2 |
| q15-csv-strategy1-no_slice.sparql | 1 | 1 | 4 | 2 | 0 | 24 |
| q16-csv-strategy1-no_slice.sparql | 1 | 1 | 4 | 3 | 0 | 8 |
| q16-csv-strategy1-no_slice.sparql | 2 | 1 | 3 | 3 | 0 | 8 |
| q17-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q17-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q17-csv-strategy1-no_slice.sparql | 3 | 1 | 4 | 3 | 0 | 8 |
| q17-csv-strategy1-no_slice.sparql | 4 | 1 | 4 | 3 | 0 | 8 |
| q18-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 2 | 0 | 4 |
| q18-csv-strategy1-no_slice.sparql | 2 | 1 | 4 | 3 | 0 | 8 |
| q18-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q18-csv-strategy1-no_slice.sparql | 4 | 1 | 2 | 1 | 0 | 2 |
| q18-csv-strategy1-no_slice.sparql | 5 | 1 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 4 | 1 | 3 | 2 | 0 | 4 |
| q3-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 4 | 1 | 3 | 2 | 0 | 4 |
| q3-csv-strategy1-no_slice.sparql | 5 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 4 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 5 | 1 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 6 | 1 | 4 | 3 | 0 | 8 |
| q4-csv-strategy1-no_slice.sparql | 7 | 1 | 2 | 1 | 0 | 2 |
| q5-csv-strategy1-no_slice.sparql | 1 | 1 | 4 | 3 | 0 | 8 |
| q6-csv-strategy1-no_slice.sparql | 1 | 1 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 4 | 1 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 5 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 6 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 7 | 1 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 8 | 1 | 2 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 9 | 1 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 10 | 1 | 3 | 2 | 0 | 4 |
| q8-csv-strategy1-no_slice.sparql | 1 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 3 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 4 | 1 | 3 | 2 | 0 | 4 |
| q8-csv-strategy1-no_slice.sparql | 5 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 6 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 7 | 1 | 4 | 3 | 0 | 8 |
| q8-csv-strategy1-no_slice.sparql | 8 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 9 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 10 | 1 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 11 | 1 | 3 | 2 | 0 | 4 |
| q9-csv-strategy1-no_slice.sparql | 1 | 1 | 5 | 4 | 0 | 16 |
| q9-csv-strategy1-no_slice.sparql | 2 | 1 | 2 | 1 | 0 | 2 |
| q9-csv-strategy1-no_slice.sparql | 3 | 1 | 4 | 3 | 0 | 8 |

### Bottom up, all annotations (real queries should have all bgps satisfiable)
The algorithm proceeds to find all possible satisfiable annotations

| name | bgpx | found | varSize | size | ms | tested |
| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |
| q1-csv-strategy1-no_slice.sparql | 1 | 16 | 5 | 4 | 0 | 16 |
| q10-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q10-csv-strategy1-no_slice.sparql | 2 | 4 | 3 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 1 | 4 | 3 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 2 | 8 | 4 | 3 | 0 | 8 |
| q11-csv-strategy1-no_slice.sparql | 3 | 1 | 1 | 2 | 0 | 4 |
| q11-csv-strategy1-no_slice.sparql | 4 | 2 | 2 | 1 | 0 | 2 |
| q12-csv-strategy1-no_slice.sparql | 1 | 4 | 3 | 2 | 0 | 4 |
| q12-csv-strategy1-no_slice.sparql | 2 | 4 | 3 | 2 | 0 | 4 |
| q12-csv-strategy1-no_slice.sparql | 3 | 4 | 3 | 2 | 0 | 4 |
| q12-csv-strategy1-no_slice.sparql | 4 | 4 | 3 | 3 | 0 | 8 |
| q13-csv-strategy1-no_slice.sparql | 1 | 4 | 3 | 2 | 0 | 4 |
| q13-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q13-csv-strategy1-no_slice.sparql | 3 | 4 | 4 | 3 | 0 | 8 |
| q14-csv-strategy1-no_slice.sparql | 1 | 16 | 5 | 4 | 0 | 16 |
| q14-csv-strategy1-no_slice.sparql | 2 | 4 | 3 | 2 | 0 | 4 |
| q14-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q14-csv-strategy1-no_slice.sparql | 4 | 2 | 2 | 1 | 0 | 2 |
| q15-csv-strategy1-no_slice.sparql | 1 | 12 | 4 | 2 | 0 | 24 |
| q16-csv-strategy1-no_slice.sparql | 1 | 8 | 4 | 3 | 0 | 8 |
| q16-csv-strategy1-no_slice.sparql | 2 | 4 | 3 | 3 | 0 | 8 |
| q17-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q17-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q17-csv-strategy1-no_slice.sparql | 3 | 8 | 4 | 3 | 0 | 8 |
| q17-csv-strategy1-no_slice.sparql | 4 | 8 | 4 | 3 | 0 | 8 |
| q18-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 2 | 0 | 4 |
| q18-csv-strategy1-no_slice.sparql | 2 | 8 | 4 | 3 | 0 | 8 |
| q18-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q18-csv-strategy1-no_slice.sparql | 4 | 2 | 2 | 1 | 0 | 2 |
| q18-csv-strategy1-no_slice.sparql | 5 | 2 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q2-csv-strategy1-no_slice.sparql | 4 | 4 | 3 | 2 | 0 | 4 |
| q3-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q3-csv-strategy1-no_slice.sparql | 4 | 4 | 3 | 2 | 0 | 4 |
| q3-csv-strategy1-no_slice.sparql | 5 | 2 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 1 | 2 |
| q4-csv-strategy1-no_slice.sparql | 4 | 2 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 5 | 2 | 2 | 1 | 0 | 2 |
| q4-csv-strategy1-no_slice.sparql | 6 | 8 | 4 | 3 | 0 | 8 |
| q4-csv-strategy1-no_slice.sparql | 7 | 2 | 2 | 1 | 0 | 2 |
| q5-csv-strategy1-no_slice.sparql | 1 | 8 | 4 | 3 | 0 | 8 |
| q6-csv-strategy1-no_slice.sparql | 1 | 4 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 1 | 2 |
| q7-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 4 | 4 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 5 | 2 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 6 | 2 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 7 | 4 | 3 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 8 | 2 | 2 | 2 | 0 | 4 |
| q7-csv-strategy1-no_slice.sparql | 9 | 2 | 2 | 1 | 0 | 2 |
| q7-csv-strategy1-no_slice.sparql | 10 | 4 | 3 | 2 | 1 | 4 |
| q8-csv-strategy1-no_slice.sparql | 1 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 3 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 4 | 4 | 3 | 2 | 0 | 4 |
| q8-csv-strategy1-no_slice.sparql | 5 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 6 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 7 | 8 | 4 | 3 | 0 | 8 |
| q8-csv-strategy1-no_slice.sparql | 8 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 9 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 10 | 2 | 2 | 1 | 0 | 2 |
| q8-csv-strategy1-no_slice.sparql | 11 | 4 | 3 | 2 | 0 | 4 |
| q9-csv-strategy1-no_slice.sparql | 1 | 16 | 5 | 4 | 12 | 16 |
| q9-csv-strategy1-no_slice.sparql | 2 | 2 | 2 | 1 | 0 | 2 |
| q9-csv-strategy1-no_slice.sparql | 3 | 8 | 4 | 3 | 1 | 8 |

