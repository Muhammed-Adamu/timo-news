#!/bin/sh
# ============================================================
# Rewrites Tomcat's HTTP connector to listen on $PORT, then
# starts Tomcat in the foreground (required for containers).
#
# Render assigns a PORT (default 10000) and routes only to that
# port, but Tomcat's stock server.xml hardcodes 8080.
# ============================================================
set -e

PORT="${PORT:-8080}"

sed -i "s/port=\"8080\"/port=\"${PORT}\"/g" "$CATALINA_HOME/conf/server.xml"

echo "[entrypoint] Tomcat HTTP connector listening on port ${PORT}"

exec catalina.sh run
