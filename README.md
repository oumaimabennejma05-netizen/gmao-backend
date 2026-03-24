# 🔧 GMAO — Guide de démarrage rapide

> **** — Stack : Spring Boot 3 · Angular 17 · PostgreSQL 15  
> Environnements supportés : **WSL2 (Windows)** · **AWS EC2 Ubuntu 24.04+**

---

## 📋 Table des matières

1. [Prérequis](#1-prérequis)
2. [Installation initiale](#2-installation-initiale)
3. [Démarrage PostgreSQL](#3-démarrage-postgresql)
4. [Démarrage du Backend](#4-démarrage-du-backend-spring-boot)
5. [Démarrage du Frontend](#5-démarrage-du-frontend-angular)
6. [Démarrage rapide tout-en-un](#6-démarrage-rapide-tout-en-un)
7. [URLs et accès](#7-urls-et-accès)
8. [Commandes utiles](#8-commandes-utiles)
9. [Résolution de problèmes](#9-résolution-de-problèmes)
10. [Déploiement AWS EC2](#10-déploiement-aws-ec2)

---

## 1. Prérequis

| Composant | Version | Vérification |
|-----------|---------|--------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ (Linux) | `node -v` |
| npm | 10+ | `npm -v` |
| Angular CLI | 17+ | `ng version` |
| PostgreSQL | 15 | `psql --version` |

> ⚠️ **WSL2** : toujours travailler dans un terminal **Ubuntu**, jamais depuis CMD ou PowerShell.  
> Vérifie que `which node` retourne `/usr/bin/node` et **non** `/mnt/c/...`

---

## 2. Installation initiale

```bash
# Télécharger et rendre exécutable
chmod +x setup.sh

# Installation complète
sudo bash setup.sh

# ── Options disponibles ──────────────────────────────────────
sudo bash setup.sh --skip-frontend        # Backend + DB seulement
sudo bash setup.sh --skip-backend         # Frontend + DB seulement
sudo bash setup.sh --skip-db              # Backend + Frontend seulement
sudo bash setup.sh --ec2                  # Mode AWS EC2 (systemctl + UFW)
sudo bash setup.sh --db-pass MonMotDePasse  # Mot de passe PostgreSQL custom
```

> ⚠️ **WSL2** : après le script, exécuter dans PowerShell Windows :
> ```powershell
> wsl --shutdown
> ```
> Puis rouvrir Ubuntu. Cela active l'isolation du PATH Windows.

---

## 3. Démarrage PostgreSQL

### WSL2 (Ubuntu sous Windows)

```bash
sudo service postgresql start

# Vérifier le statut
sudo service postgresql status

# Arrêter
sudo service postgresql stop
```

### AWS EC2 / Ubuntu natif

```bash
sudo systemctl start postgresql

# Activer au démarrage automatique
sudo systemctl enable postgresql

# Vérifier
sudo systemctl status postgresql
```

### Tester la connexion à la base

```bash
PGPASSWORD='GmaoSecure@2024' psql -h 127.0.0.1 -U postgres -d gmao_db -c "SELECT version();"
```

---

## 4. Démarrage du Backend (Spring Boot)

### Vérification préalable

```bash
# S'assurer que PostgreSQL tourne (voir section 3)
# S'assurer que le fichier application.properties est correct
cat ~/gmao-backend/src/main/resources/application.properties
```

### Lancement

```bash
cd ~/gmao-backend

# Mode développement
mvn spring-boot:run

# Mode développement avec logs détaillés
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.root=DEBUG"

# Build + lancement du JAR (plus rapide au 2e lancement)
mvn clean package -DskipTests
java -jar target/*.jar
```

### Vérifier que le backend tourne

```bash
curl http://localhost:8080/actuator/health 2>/dev/null || \
curl http://localhost:8080/api/health 2>/dev/null || \
echo "Backend accessible sur http://localhost:8080"
```

---

## 5. Démarrage du Frontend (Angular)

### Vérification préalable

```bash
# Vérifier que node est le bon (Linux, pas Windows)
which node      # doit afficher /usr/bin/node
node -v         # doit afficher v20.x.x

# Vérifier les dépendances
ls ~/gmao-frontend/node_modules 2>/dev/null || echo "node_modules absent -> npm install requis"
```

### Installer les dépendances (première fois ou après suppression)

```bash
cd ~/gmao-frontend
npm install
```

### Lancement

```bash
cd ~/gmao-frontend

# Mode développement (port 4200)
npm start
# ou
ng serve

# Accessible depuis Windows aussi
ng serve --host 0.0.0.0 --port 4200

# Port personnalisé
ng serve --port 4201
```

### Si node_modules est corrompu (erreurs EPERM / UNC path)

```bash
cd ~/gmao-frontend
rm -rf node_modules package-lock.json .angular
npm install
npm start
```

---

## 6. Démarrage rapide tout-en-un

```bash
# Script généré par setup.sh (lance DB + backend + frontend)
bash ~/start-gmao.sh
```

Ou manuellement dans **deux terminaux séparés** :

**Terminal 1 — Backend :**
```bash
sudo service postgresql start   # WSL2
cd ~/gmao-backend && mvn spring-boot:run
```

**Terminal 2 — Frontend :**
```bash
cd ~/gmao-frontend && npm start
```

---

## 7. URLs et accès

| Service | URL | Notes |
|---------|-----|-------|
| **API REST** | http://localhost:8080 | Backend Spring Boot |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentation API interactive |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs | Spec OpenAPI 3 |
| **Frontend** | http://localhost:4200 | Angular SPA |
| **PostgreSQL** | localhost:5432 | Base : `gmao_db`, User : `postgres` |

> 🌐 **Depuis Windows** (WSL2) : remplacer `localhost` par l'IP WSL2 si nécessaire :
> ```powershell
> # Dans PowerShell Windows
> wsl hostname -I
> ```

---

## 8. Commandes utiles

### PostgreSQL

```bash
# Accès console PostgreSQL
PGPASSWORD='GmaoSecure@2024' psql -h 127.0.0.1 -U postgres -d gmao_db

# Lister les tables
\dt

# Quitter
\q

# Backup de la base
pg_dump -h 127.0.0.1 -U postgres gmao_db > ~/gmao_db_backup_$(date +%Y%m%d).sql

# Restaurer la base
PGPASSWORD='GmaoSecure@2024' psql -h 127.0.0.1 -U postgres -d gmao_db < ~/gmao_db_backup.sql
```

### Maven / Spring Boot

```bash
# Compiler sans tests
mvn clean package -DskipTests

# Lancer les tests
mvn test

# Nettoyer le cache Maven
mvn dependency:purge-local-repository

# Voir les dépendances
mvn dependency:tree
```

### Angular

```bash
# Build production
ng build --configuration production

# Lancer les tests unitaires
ng test

# Analyser la taille du bundle
ng build --stats-json && npx webpack-bundle-analyzer dist/*/stats.json

# Mettre à jour les dépendances Angular
ng update @angular/core @angular/cli
```

---

## 9. Résolution de problèmes

### ❌ `Unit postgresql.service not found` (WSL2)

```bash
# Utiliser service au lieu de systemctl sous WSL2
sudo service postgresql start   # ✅
sudo systemctl start postgresql # ❌ sous WSL2 sans systemd
```

### ❌ `Unable to locate package postgresql-15`

```bash
# Ajouter le dépôt PGDG officiel
sudo install -d /usr/share/postgresql-common/pgdg
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
  | sudo gpg --dearmor -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.gpg
echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.gpg] \
https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
  | sudo tee /etc/apt/sources.list.d/pgdg.list
sudo apt-get update && sudo apt-get install -y postgresql-15
```

### ❌ `CMD.EXE a été démarré` / chemins `\\wsl.localhost\...`

```bash
# Le node/npm de Windows est utilisé au lieu de celui de WSL2
# Vérifier
which node   # si /mnt/c/... → problème

# Fix immédiat
export PATH=/usr/bin:/usr/local/bin:$PATH

# Fix permanent : ajouter dans ~/.bashrc
echo 'export PATH=/usr/bin:/usr/local/bin:$HOME/.npm-global/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Fix définitif : isolation WSL2
echo '[interop]
appendWindowsPath = false' | sudo tee -a /etc/wsl.conf
# Puis dans PowerShell : wsl --shutdown
```

### ❌ `EPERM: operation not permitted` sur node_modules

```bash
# node_modules créé par npm Windows — tout supprimer et réinstaller depuis WSL2
cd ~/gmao-frontend
rm -rf node_modules package-lock.json .angular
npm install
```

### ❌ Spring Boot — `Connection refused` à PostgreSQL

```bash
# 1. Vérifier que PostgreSQL tourne
sudo service postgresql status

# 2. Vérifier la connexion
PGPASSWORD='GmaoSecure@2024' psql -h 127.0.0.1 -U postgres -d gmao_db -c '\l'

# 3. Vérifier application.properties
grep "datasource" ~/gmao-backend/src/main/resources/application.properties
# Doit contenir :
#   spring.datasource.url=jdbc:postgresql://localhost:5432/gmao_db
#   spring.datasource.username=postgres
#   spring.datasource.password=GmaoSecure@2024
```

### ❌ Port 8080 déjà occupé

```bash
# Trouver le processus
ss -tlnp | grep 8080

# Tuer le processus
kill -9 $(lsof -ti:8080)

# Ou changer le port dans application.properties
server.port=8081
```

### ❌ Port 4200 déjà occupé

```bash
ng serve --port 4201
```

---

## 10. Déploiement AWS EC2

### Lancement de l'instance

- **AMI** : Ubuntu Server 24.04 LTS
- **Type** : `t3.medium` minimum recommandé (2 vCPU, 4 GB RAM)
- **Security Group** — ports à ouvrir :

| Port | Protocole | Usage |
|------|-----------|-------|
| 22 | TCP | SSH |
| 8080 | TCP | API Spring Boot |
| 4200 | TCP | Angular (dev) |
| 80 | TCP | HTTP (prod) |
| 443 | TCP | HTTPS (prod) |

### Installation sur EC2

```bash
# Connexion SSH
ssh -i ta-cle.pem ubuntu@<IP-EC2>

# Télécharger et lancer le script
curl -fsSL https://<ton-bucket>/setup.sh -o setup.sh
sudo bash setup.sh --ec2 --db-pass TonMotDePasseSecurise
```

### Démarrage au reboot (EC2)

```bash
# Option 1 — crontab
crontab -e
# Ajouter :
@reboot sleep 30 && bash ~/start-gmao.sh >> ~/gmao-startup.log 2>&1

# Option 2 — service systemd dédié
sudo tee /etc/systemd/system/gmao.service << 'EOF'
[Unit]
Description=GMAO Application
After=network.target postgresql.service

[Service]
Type=forking
User=ubuntu
ExecStart=/home/ubuntu/start-gmao.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl enable gmao
sudo systemctl start gmao
```

### Variables d'environnement pour la production

```bash
# Copier le fichier de référence généré par setup.sh
cp ~/gmao-application.properties \
   ~/gmao-backend/src/main/resources/application.properties

# Modifier les valeurs sensibles AVANT de démarrer
nano ~/gmao-backend/src/main/resources/application.properties
# → Changer : jwt.secret, spring.datasource.password
```

---

## 📁 Structure des répertoires

```
~/
├── gmao-backend/                  # Projet Spring Boot
│   └── src/main/resources/
│       └── application.properties # Configuration principale
├── gmao-frontend/                 # Projet Angular
│   ├── src/
│   ├── node_modules/              # Dépendances npm (ne pas versionner)
│   └── .angular/                  # Cache Angular (ne pas versionner)
├── gmao-application.properties    # Référence config générée par setup.sh
├── start-gmao.sh                  # Script démarrage rapide
└── gmao_db_backup.sql             # Backup PostgreSQL
```

---


