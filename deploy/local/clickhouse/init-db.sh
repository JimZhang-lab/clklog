#!/bin/sh

set -eu

sed 's/${CLKLOG_LOG_DB}/clklog/g' /bootstrap/init.sql \
  | clickhouse-client --user default --password "${CLICKHOUSE_PASSWORD}" --multiquery

clickhouse-client \
  --user default \
  --password "${CLICKHOUSE_PASSWORD}" \
  --database clklog \
  --multiquery \
  < /bootstrap/seed.sql
