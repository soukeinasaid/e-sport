# Database Setup Instructions

## Problem
The application is failing to connect to MySQL database with error: "Communications link failure"

## Solution

### 1. Install MySQL Server
If you don't have MySQL installed, download and install it from:
- [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)
- Or use XAMPP/WAMP which includes MySQL

### 2. Start MySQL Service
Make sure MySQL service is running:
- **Windows**: Open Services, find "MySQL" and start it
- **Or via command line**: `net start mysql`

### 3. Create Database
Run the provided SQL script:
```bash
mysql -u root -p < database_setup.sql
```

Or manually execute these commands in MySQL:
```sql
CREATE DATABASE smoka CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smoka;

CREATE TABLE utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    motDePasse VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE forum (
    idForum INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idUser INT NOT NULL,
    FOREIGN KEY (idUser) REFERENCES utilisateur(idUser) ON DELETE CASCADE
);
```

### 4. Test Connection
Run the application again. You should see:
- "Connected to database successfully" in console
- Login page should work with test credentials

### 5. Test Credentials
After running the setup script, you can use:
- **Email**: admin@smoka.com, **Password**: admin123
- **Email**: john@example.com, **Password**: password123
- **Email**: jane@example.com, **Password**: password123

### 6. Troubleshooting
If still getting connection errors:

1. **Check MySQL is running**: `sc query mysql` or check services
2. **Check port**: Default is 3306, ensure it's not blocked by firewall
3. **Check credentials**: DatabaseConfig.java uses user "root" with empty password
4. **Check database name**: Should be "smoka"
5. **Check MySQL config**: Ensure MySQL allows connections from 127.0.0.1

### 7. Alternative: Change Database Config
If using different MySQL settings, update `DatabaseConfig.java`:
```java
final String URL = "jdbc:mysql://localhost:3306/your_database";
final String USER = "your_username";
final String PASSWORD = "your_password";
```

Once database is set up, the forum search and filter functionality will work perfectly!
