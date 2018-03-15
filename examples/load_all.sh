#!/bin/bash -ue
# usage: $0 [database_name] [map_reduce_queue]

DATABASE=$1
QUEUE=$2

for SCRIPT in "simple.hql" "names.hql"
do
  hive -f ${SCRIPT} --database ${DATABASE} --hiveconf mapred.job.queue.name=${QUEUE}
done
