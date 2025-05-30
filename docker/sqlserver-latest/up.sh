#!/bin/bash

set -eu
scriptdir=$(dirname "$(readlink -f "$0")")
cd "$scriptdir/"

docker compose up -d

network=$(docker ps --format "{{.Names}} {{.Networks}}" | awk '( $1 ~ /^sqlserver/ ) { print $2 }')

# Also setup the database for use with the sample configs.
# See Also: .github/workflows/maven.yml

# Wait until ready
for i in {1..60}; do
    if /usr/bin/docker inspect --format="{{print .State.Health.Status}}" sqlserver | grep -q -x healthy; then
        break
    else
        sleep 5
    fi
done

function run_sqlcmd_in_docker() {
    set -x
    docker run --rm --network=$network --entrypoint /opt/mssql-tools/bin/sqlcmd mcr.microsoft.com/mssql-tools:latest \
        -U sa -P SApassword1 -S sqlserver -b "$@"
    set +x
}

# Cleanup database
run_sqlcmd_in_docker -Q "DROP DATABASE IF EXISTS benchbase;"

# Setup database
run_sqlcmd_in_docker -Q "CREATE DATABASE benchbase;"

# Setup login
run_sqlcmd_in_docker -Q "CREATE LOGIN benchuser01 WITH PASSWORD='P@ssw0rd';" || true

# Setup access
run_sqlcmd_in_docker -Q "USE benchbase; CREATE USER benchuser01 FROM LOGIN benchuser01; EXEC sp_addrolemember 'db_owner', 'benchuser01';" || true

# Setup privileged access for monitoring and session start tests
run_sqlcmd_in_docker -Q "USE master; GRANT ALTER SERVER STATE, VIEW SERVER PERFORMANCE STATE TO benchuser01;" || true
