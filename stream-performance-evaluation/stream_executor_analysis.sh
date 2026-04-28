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

# ./stream_executor_analysis.sh /Users/lgu/workspace/SPARQL-Anything/fxbgp/target/fxbgp-test-executor-1.2.0-SNAPSHOT.jar  /Users/lgu/Desktop/ISWC2026-test/input_csv_json  /Users/lgu/Desktop/ISWC2026-test/stream-performance-csv-json

source functions.sh

JAR=$1
INPUT_FOLDER=$2
RESULT_FOLDER=$3

MEM=100
EXECUTOR="stream"

UCs=(
  # "H_1 1 3 1 100000 csv"
  "H=2_K=1000 2 1 0 100000 json"
)

for UC in "${UCs[@]}"; do
  for RUN in 1; do
    monitor-query-stream ${JAR} ${MEM} ${INPUT_FOLDER} ${UC} ${EXECUTOR} ${RESULT_FOLDER} ${RUN}
  done
done