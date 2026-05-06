# How to Run VictoryGrid E-Sports Forum Application

## Quick Start

### Option 1: Using the RunApp.java file (Recommended)
```bash
# Navigate to the project directory
cd c:/Users/saids/Downloads/e-sport-form/e-sport-form

# Compile and run with Maven (if Maven is installed)
mvn compile exec:java -Dexec.mainClass="RunApp"

# Or run the batch file
run.bat
```

### Option 2: Using the original MainApp.java
```bash
# Navigate to the project directory
cd c:/Users/saids/Downloads/e-sport-form/e-sport-form

# Compile with Maven
mvn compile

# Run the application
mvn javafx:run
```

## Prerequisites

1. **Java 17 or higher** - Required for JavaFX 21
2. **MySQL Server** - Running on localhost:3306
3. **Maven** - For dependency management (recommended)

## Database Setup

Before running the application, set up the database:

```bash
# Start MySQL service
# On Windows: net start mysql
# Or start via XAMPP/WAMP control panel

# Run the database setup script
mysql -u root -p < database_setup.sql
```

### Default Test Users
- **Email**: admin@smoka.com, **Password**: admin123
- **Email**: john@example.com, **Password**: password123
- **Email**: jane@example.com, **Password**: password123

## What the RunApp.java Does

The `RunApp.java` file I created provides:

1. **Database Connection Testing** - Tests MySQL connection before starting
2. **Clear Error Messages** - Helpful feedback if database is not available
3. **Graceful Fallback** - Starts the application even if database fails
4. **Console Output** - Shows startup progress and status

## Troubleshooting

### Database Connection Issues
If you see database connection errors:

1. **Check MySQL is running**:
   ```bash
   # Windows
   sc query mysql
   
   # Or check in Services
   ```

2. **Verify database exists**:
   ```sql
   SHOW DATABASES;
   USE smoka;
   SHOW TABLES;
   ```

3. **Check credentials** - Default is root with empty password

4. **Port issues** - Ensure port 3306 is not blocked by firewall

### Java/JavaFX Issues
1. **Java version** - Must be Java 17+
2. **JavaFX dependencies** - Ensure they're in Maven dependencies
3. **PATH issues** - Make sure Java is in your system PATH

### Compilation Issues
1. **Maven dependencies** - Run `mvn clean install` to download dependencies
2. **Missing JARs** - Ensure JavaFX and MySQL connector JARs are available

## Application Features

Once running, you can:
- **Login** with existing users or create new accounts
- **Browse Forum** posts
- **Create/Edit/Delete** forum posts
- **Add to Favorites** and manage favorites
- **View Profile** and user information

## Application Flow

1. **Login Screen** - First screen shown
2. **Main Dashboard** - Sidebar navigation with content area
3. **Forum Section** - Main functionality for discussions
4. **Profile Section** - User management
5. **Favorites Section** - Saved forum posts

The application uses a modern dark theme with glass effects and smooth transitions.
