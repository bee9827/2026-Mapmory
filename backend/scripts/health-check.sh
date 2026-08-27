#!/bin/bash
for i in $(seq 1 30); do
  if curl -sf http://127.0.0.1:8080/health > /dev/null; then
    echo "healthy after $((i * 5))s"
    exit 0
  fi
  sleep 5
done
echo "health check failed"
journalctl -u mapmory -n 50 --no-pager
exit 1
