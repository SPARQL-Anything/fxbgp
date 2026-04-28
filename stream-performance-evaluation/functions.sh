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


function monitor-query-stream {

  JAR=$1
  MEM=$2
  INPUT_FOLDER=$3
  D=$4
  TPs=$5
  VARs=$6
  PVARS=$7
  SIZE=$8
  FORMAT=$9
  EXECUTOR=${10}
  RESULT_FOLDER=${11}
  RUN=${12}

  TIME_FILE="${RESULT_FOLDER}/time.tsv"
  MEM_FILE="$RESULT_FOLDER/MEM_${D}_${TPs}_${VARs}_${PVARS}_${SIZE}_${FORMAT}_${EXECUTOR}${RUN}.tsv"
  ERR_FILE="$RESULT_FOLDER/ERR_${D}_${TPs}_${VARs}_${PVARS}_${SIZE}_${FORMAT}_${EXECUTOR}${RUN}.tsv"
  MEM_RECORDS="MemoryLimit\tPID\t%cpu\t%mem\tvsz\trss\n"

  # echo "$INPUT_FOLDER $D $TPs $VARs $PVARS $SIZE $FORMAT $EXECUTOR"

  java "-Xmx${MEM}m" -jar $JAR $INPUT_FOLDER $D $TPs $VARs $PVARS $SIZE $FORMAT $EXECUTOR 360000  2>"$ERR_FILE" >> $TIME_FILE &

  MPID=$!

  while kill -0 $MPID 2>/dev/null; do
    PS_RECORD="$MEM $(ps -p $MPID -o pid,%cpu,%mem,vsz,rss | sed 1d)\n"
    PS_RECORD=$(echo "$PS_RECORD" | sed -E 's/ +/\t/g')
    PS_RECORD_TRIM=$(echo "$PS_RECORD" | sed -E 's/^ +| +$//g')
    if [ -n "$PS_RECORD_TRIM" ]; then
        MEM_RECORDS+=$PS_RECORD_TRIM
    fi

    sleep 0.2
  done

  echo -n -e "$MEM_RECORDS" | tr ' ' '\t' > $MEM_FILE

}
