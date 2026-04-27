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

source functions.sh

# ./find_OOMs.sh /Users/lgu/workspace/SPARQL-Anything/fxbgp/target/fxbgp-test-executor-1.2.0-SNAPSHOT.jar  /Users/lgu/Desktop/ISWC2026-test/input  /Users/lgu/Desktop/ISWC2026-test/results_2026-04-15

JAR=$1
INPUT_FOLDER=$2
RESULT_FOLDER=$3

MEM=4096
FORMAT="csv"
EXECUTOR="materialisation"

UCs=(
  # "H_1 10 11 0 2000000" OOM
  # "H_1 9 10 0 2000000" OOM
  # "H_1 8 9 0 2000000" OOM
  # "H_1 7 8 0 2000000" OOM
  # "H_1 6 7 0 2000000" OOM
  # "H_1 5 6 0 2000000" # OOM
  # "H_1 4 5 0 2000000" OK
  # "H_1 3 4 0 2000000" OK
  # "H_1 2 3 0 2000000" OK
  # "H_1 1 2 0 2000000" OK
  # "H_2 1 3 1 2000000" # OOM
  # "H_1 1 3 1 2000000" # OOM
  # "H_1 1 3 1 1000000" OOM
 "H_1 10 11 0 1000000" # OK
  # "H_1 10 11 1 1000000" OOM
  # "H_2 10 11 0 1000000" # OK
  # "H_2 10 12 1 1000000" # OOM
  # "H_2 9 10 0 1000000" # OK
)

for UC in "${UCs[@]}"; do
  echo "Execute ${UC}"
  monitor-query-stream ${JAR} ${MEM} ${INPUT_FOLDER} ${UC} ${FORMAT} ${EXECUTOR} ${RESULT_FOLDER} 1
done


