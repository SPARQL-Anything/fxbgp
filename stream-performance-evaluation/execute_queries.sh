#!/bin/bash
#
# Copyright (c) 2022 SPARQL Anything Contributors @ http://github.com/sparql-anything
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#./execute_queries.sh ../bin/sparql-anything-1.2.0-SNAPSHOT-3aacf72ccc97fee1f66f55d9a23b015a1419c54a.jar "1 10 100 1000" "csv" "2026-03-17-csv-1-10-100-1000" "1 5 6 10 12 15 16"
# ./execute_queries.sh ../bin/sparql-anything-1.2.0-SNAPSHOT-3aacf72ccc97fee1f66f55d9a23b015a1419c54a.jar "1 10 100 1000" "json" "2026-03-19-json-1-10-100-1000" "1 5 6 10 12 15 16"

SPARQL_ANYTHING_JAR=$1
RESULTS_DIR=$(pwd)/$4

if [ ! -d $RESULTS_DIR ]; then
  mkdir $RESULTS_DIR
else
  echo "$RESULTS_DIR already exists!"
fi

source functions.sh

if [ -n "$5" ]; then
  QUERIES_TO_EXECUTE=$5
else
  QUERIES_TO_EXECUTE="1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18"
fi


for format in $3
do
  for size in $2
  do
    for query in $QUERIES_TO_EXECUTE
    do

      echo "Monitoring q$query strategy0 no_slice size $size $format"
      monitor-query $size "q$query" "strategy2" "no_slice" $format
      
    done
  done
done
