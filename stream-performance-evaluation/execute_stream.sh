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

# ./execute_stream.sh /Users/lgu/workspace/SPARQL-Anything/fxbgp/target/fxbgp-test-executor-1.2.0-SNAPSHOT.jar  /Users/lgu/Desktop/ISWC2026-test/input_xml  /Users/lgu/Desktop/ISWC2026-test/stream-performance-xml

source functions.sh

JAR=$1
INPUT_FOLDER=$2
RESULT_FOLDER=$3

MEM=50
FORMAT="xml"
EXECUTOR="stream"

UCs=(
  # "1 3 1 1000000" # OK
  # "2 4 1 1000000" # OK
  # "3 5 1 1000000" # OK
  # "4 6 1 1000000" # OK
  # "5 7 1 1000000" #ok
  # "10 11 0 1000000" # T
  # "5 6 0 2000000" # T
  # "10 11 1 1000000" # T
  # "2 3 0 1000000" # OK
  # "8 3 1 1000000" # OK
  # "2 3 1 1000000" # OK
  # "8 4 1 1000000" # OK 8 tree patterns
  #"6 7 0 2000000"
  #"7 8 0 2000000"
  #"8 9 0 2000000"
  #"9 10 0 2000000"
  # "H_1 1 3 1 1000000" # OK
  # "H_1 1 3 1 2000000" # OK
  # "H_1 10 11 0 2000000" # OK
  # "H_1 10 11 1 1000000" # OK
  # "H_1 5 6 0 2000000" # OK
  # "H_1 6 7 0 2000000" # OK
  # "H_1 7 8 0 2000000" # OK
  # "H_1 8 9 0 2000000" # OK
  # "H_1 9 10 0 2000000" # OK
  "H=2_K=1000T 1 1 0 100000" #
  # "H_2 1 3 1 2000000" # OK
)


for UC in "${UCs[@]}"; do
  echo "$(date '+%Y-%m-%d %H:%M:%S') Execute ${UC} RUN #${RUN}"
  monitor-query-stream ${JAR} ${MEM} ${INPUT_FOLDER} ${UC} ${FORMAT} ${EXECUTOR} ${RESULT_FOLDER} ${RUN} 10
done