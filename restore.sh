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

# ─── Helpers ───
log()   { echo "[$(date '+%H:%M:%S')] $*"; }
error() { echo "[$(date '+%H:%M:%S')] ERROR: $*" >&2; }

# ─── Pre-flight checks ───
if ! command -v psql &>/dev/null; then
    error "psql not found. Install postgresql-client."
    exit 1
fi

# ─── Parse flags ───
AUTO_YES=false
FILE_ARG=""
for arg in "$@"; do
    case "$arg" in
        --yes|-y) AUTO_YES=true ;;
        *)        FILE_ARG="$arg" ;;
    esac
done

# ─── Determine backup file ───
if [ -n "$FILE_ARG" ]; then
    # Argument provided: use as filename or full path
    if [ -f "$FILE_ARG" ]; then
        BACKUP_FILE="$FILE_ARG"
    elif [ -f "${BACKUP_DIR}/$FILE_ARG" ]; then
        BACKUP_FILE="${BACKUP_DIR}/$FILE_ARG"
    else
        error "Backup file not found: $FILE_ARG"
        echo "Available backups in $BACKUP_DIR:"
        ls -1t "$BACKUP_DIR"/gmao_backup_*.sql 2>/dev/null || echo "  (none)"
        exit 1
    fi
else
    # No argument: use the latest backup
    BACKUP_FILE=$(find "$BACKUP_DIR" -maxdepth 1 -name "gmao_backup_*.sql" -type f -printf '%T+ %p\n' \
        | sort -r | head -1 | cut -d' ' -f2-)
    if [ -z "$BACKUP_FILE" ]; then
        error "No backup files found in $BACKUP_DIR"
        exit 1
    fi
    log "No file specified — using latest: $(basename "$BACKUP_FILE")"
fi

# Validate file is non-empty
if [ ! -s "$BACKUP_FILE" ]; then
    error "Backup file is empty: $BACKUP_FILE"
    exit 1
fi

# Test DB connection
if ! psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "postgres" -c "SELECT 1;" &>/dev/null; then
    error "Cannot connect to PostgreSQL server at $PGHOST:$PGPORT"
    exit 1
fi

# ─── Confirmation ───
SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo ""
echo "┌─────────────────────────────────────────────┐"
echo "│  RESTORE CONFIRMATION                       │"
echo "├─────────────────────────────────────────────┤"
echo "│  File:     $(basename "$BACKUP_FILE")"
echo "│  Size:     $SIZE"
echo "│  Target:   $PGDATABASE @ $PGHOST:$PGPORT"
echo "└─────────────────────────────────────────────┘"
echo ""

# Skip confirmation if --yes flag is passed
if [ "$AUTO_YES" = false ]; then
    read -rp "This will DROP and recreate '$PGDATABASE'. Continue? [y/N] " CONFIRM
    if [[ ! "$CONFIRM" =~ ^[yY]$ ]]; then
        log "Restore cancelled."
        exit 0
    fi
fi

# ─── Restore ───
log "Terminating active connections to '$PGDATABASE' ..."
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "postgres" -c \
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$PGDATABASE' AND pid <> pg_backend_pid();" \
    &>/dev/null || true

log "Dropping and recreating '$PGDATABASE' ..."
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "postgres" <<SQL
DROP DATABASE IF EXISTS ${PGDATABASE};
CREATE DATABASE ${PGDATABASE} OWNER ${PGUSER};
SQL

log "Restoring from $(basename "$BACKUP_FILE") ..."
RESTORE_LOG=$(mktemp)
if psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
    -f "$BACKUP_FILE" 2>"$RESTORE_LOG" 1>/dev/null; then
    log "Restore completed successfully."
else
    error "Restore encountered errors:"
    cat "$RESTORE_LOG" >&2
    rm -f "$RESTORE_LOG"
    exit 1
fi
# Show warnings if any (non-fatal)
if [ -s "$RESTORE_LOG" ]; then
    ERRORS=$(grep -c "ERROR" "$RESTORE_LOG" 2>/dev/null || true)
    if [ "$ERRORS" -gt 0 ]; then
        log "Warning: $ERRORS non-fatal error(s) during restore. Details: $RESTORE_LOG"
    else
        rm -f "$RESTORE_LOG"
    fi
else
    rm -f "$RESTORE_LOG"
fi

# ─── Verify ───
TABLE_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -c \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';")
USER_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -c \
    "SELECT count(*) FROM users;" 2>/dev/null || echo "?")

log "Verification: ${TABLE_COUNT// /} tables, ${USER_COUNT// /} users"
