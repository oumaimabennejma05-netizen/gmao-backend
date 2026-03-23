#!/bin/bash
# reset_gmao_db_bcrypt.sh - Drops DB, recreates tables, inserts sample data with BCrypt passwords

DB_NAME="gmao_db"
DB_USER="postgres"
DB_PASS="postgres"
DB_HOST="localhost"
DB_PORT="5432"

# Function to run psql commands on postgres database
run_psql() {
    PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d postgres -c "$1"
}

# Function to run psql commands on gmao_db
run_psql_db() {
    PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -c "$1"
}

echo "[INFO] Disconnecting all users from $DB_NAME..."
run_psql "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='$DB_NAME';"

echo "[INFO] Dropping database if exists..."
run_psql "DROP DATABASE IF EXISTS $DB_NAME;"
echo "[SUCCESS] Database dropped."

echo "[INFO] Creating new database $DB_NAME..."
run_psql "CREATE DATABASE $DB_NAME;"
echo "[SUCCESS] Database created."

echo "[INFO] Creating tables..."
run_psql_db "
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    id_number VARCHAR(50),
    profile_picture VARCHAR(255),
    created_at TIMESTAMP
);
CREATE TABLE machines (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    maintenance_date DATE,
    created_at TIMESTAMP
);
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    machine_id BIGINT,
    technician_id BIGINT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    created_at TIMESTAMP,
    CONSTRAINT fk_machine FOREIGN KEY(machine_id) REFERENCES machines(id),
    CONSTRAINT fk_technician FOREIGN KEY(technician_id) REFERENCES users(id)
);
CREATE TABLE task_history (
    id SERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    technician_id BIGINT,
    notes TEXT,
    completed_at TIMESTAMP,
    CONSTRAINT fk_task FOREIGN KEY(task_id) REFERENCES tasks(id),
    CONSTRAINT fk_technician FOREIGN KEY(technician_id) REFERENCES users(id)
);
CREATE TABLE machine_data (
    id SERIAL PRIMARY KEY,
    machine_id BIGINT NOT NULL,
    temperature DOUBLE PRECISION,
    vibration DOUBLE PRECISION,
    runtime DOUBLE PRECISION,
    created_at TIMESTAMP,
    CONSTRAINT fk_machine FOREIGN KEY(machine_id) REFERENCES machines(id)
);
"
echo "[SUCCESS] Tables created."

# --------------------------
# Generate BCrypt passwords using htpasswd
# --------------------------
echo "[INFO] Generating BCrypt hashes for sample users..."
ADMIN_BCRYPT=$(htpasswd -bnBC 10 "" "Admin@123" | tr -d ':\n')
RESP_BCRYPT=$(htpasswd -bnBC 10 "" "Resp@123" | tr -d ':\n')
TECH1_BCRYPT=$(htpasswd -bnBC 10 "" "Tech@123" | tr -d ':\n')

# --------------------------
# Insert users
# --------------------------
echo "[INFO] Inserting sample users..."
run_psql_db "INSERT INTO users (name,email,password,role,address,id_number,created_at) VALUES
('Admin User','admin@gmao.com','$ADMIN_BCRYPT','ADMIN','HQ','1001',NOW()),
('Responsable','responsable@gmao.com','$RESP_BCRYPT','RESPONSABLE','HQ','1002',NOW()),
('Technician 1','tech1@gmao.com','$TECH1_BCRYPT','TECHNICIAN','Site A','1003',NOW());"

# Get IDs
ADMIN_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM users WHERE email='admin@gmao.com';" | xargs)
RESP_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM users WHERE email='responsable@gmao.com';" | xargs)
TECH1_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM users WHERE email='tech1@gmao.com';" | xargs)

# --------------------------
# Insert machines
# --------------------------
echo "[INFO] Inserting sample machines..."
run_psql_db "INSERT INTO machines (name, model, location, status, maintenance_date, created_at) VALUES
('Lathe Machine','LM-200','Workshop A','OPERATIONAL','2026-03-10',NOW()),
('Drill Press','DP-100','Workshop B','OPERATIONAL','2026-03-12',NOW()),
('Milling Machine','MM-500','Workshop A','MAINTENANCE','2026-03-15',NOW());"

# Get machine IDs
M1_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM machines WHERE name='Lathe Machine';" | xargs)
M2_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM machines WHERE name='Drill Press';" | xargs)
M3_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM machines WHERE name='Milling Machine';" | xargs)

# --------------------------
# Insert tasks
# --------------------------
echo "[INFO] Inserting tasks..."
run_psql_db "INSERT INTO tasks (title,description,machine_id,technician_id,priority,status,due_date,created_at) VALUES
('Lubricate Lathe','Lubrication required',$M1_ID,$TECH1_ID,'HIGH','PENDING','2026-03-11',NOW()),
('Check Drill Press','Check spindle and bearings',$M2_ID,$TECH1_ID,'MEDIUM','PENDING','2026-03-12',NOW()),
('Milling Maintenance','Replace worn cutters',$M3_ID,$TECH1_ID,'HIGH','IN_PROGRESS','2026-03-14',NOW());"

# --------------------------
# Insert task history
# --------------------------
T1_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM tasks WHERE title='Lubricate Lathe';" | xargs)
T2_ID=$(PGPASSWORD=$DB_PASS psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -t -c "SELECT id FROM tasks WHERE title='Check Drill Press';" | xargs)

echo "[INFO] Inserting task history..."
run_psql_db "INSERT INTO task_history (task_id,technician_id,notes,completed_at) VALUES
($T1_ID,$TECH1_ID,'Lubricated gears successfully','2026-03-11 10:30:00'),
($T2_ID,$TECH1_ID,'Checked spindle and tightened bolts','2026-03-12 15:00:00');"

# --------------------------
# Insert machine data
# --------------------------
echo "[INFO] Inserting machine data..."
run_psql_db "INSERT INTO machine_data (machine_id,temperature,vibration,runtime,created_at) VALUES
($M1_ID,75.5,0.8,120.0,NOW()),
($M2_ID,68.2,1.2,250.5,NOW()),
($M3_ID,80.0,0.5,500.0,NOW());"

echo "[SUCCESS] Sample data inserted with BCrypt passwords."
echo "[DONE] GMAO database fully reset and ready."
