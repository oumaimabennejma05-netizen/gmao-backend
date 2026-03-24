#!/usr/bin/env bash
set -euo pipefail

# ─── Configuration (override via environment variables) ───
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-postgres}"
PGPASSWORD="${PGPASSWORD:-postgres}"
PGDATABASE="${PGDATABASE:-gmao_db}"
export PGPASSWORD

BACKUP_DIR="${BACKUP_DIR:-$HOME/GMAO/BD}"
TIMESTAMP=$(date +"%Y%m%d_%H%M")
BACKUP_FILE="${BACKUP_DIR}/gmao_backup_${TIMESTAMP}.sql"

# ─── Helpers ───
log()   { echo "[$(date '+%H:%M:%S')] $*"; }
error() { echo "[$(date '+%H:%M:%S')] ERROR: $*" >&2; }

# ─── Pre-flight checks ───
if ! command -v pg_dump &>/dev/null; then
    error "pg_dump not found. Install postgresql-client."
    exit 1
fi

mkdir -p "$BACKUP_DIR"

# Test DB connection
if ! psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -c "SELECT 1;" &>/dev/null; then
    error "Cannot connect to database '$PGDATABASE' at $PGHOST:$PGPORT"
    exit 1
fi

# ─── Backup ───
log "Starting backup of '$PGDATABASE' ..."

if pg_dump -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
    --no-owner --no-privileges \
    -F p -f "$BACKUP_FILE"; then
    SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    log "Backup completed: $BACKUP_FILE ($SIZE)"
else
    error "pg_dump failed"
    rm -f "$BACKUP_FILE"
    exit 1
fi

# ─── Cleanup old backups (keep last 10) ───
KEEP=10
COUNT=$(find "$BACKUP_DIR" -maxdepth 1 -name "gmao_backup_*.sql" -type f | wc -l)
if [ "$COUNT" -gt "$KEEP" ]; then
    REMOVE=$((COUNT - KEEP))
    find "$BACKUP_DIR" -maxdepth 1 -name "gmao_backup_*.sql" -type f -printf '%T+ %p\n' \
        | sort | head -n "$REMOVE" | cut -d' ' -f2- \
        | xargs rm -f
    log "Cleaned up $REMOVE old backup(s), keeping last $KEEP"
fi
