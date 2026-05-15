# VictoryGrid - E-Sports Community Platform

A comprehensive e-sports community platform with AI-powered content generation, user management, and forum functionality built with JavaFX and MySQL.

##  Features

### Core Features
- **User Authentication**: Secure login/registration system with Google OAuth2 integration
- **Forum System**: Create, view, and manage e-sports forum posts with advanced filtering
- **User Profiles**: Personal profiles with favorites and post history
- **Admin Dashboard**: Complete user and content management interface
- **Favorites System**: Save and manage favorite forum posts

###  AI Content Generation
- **Hugging Face AI Integration**: Intelligent content generation for forum posts
- **Bulletproof Reliability**: 100% uptime with fallback content generation
- **Smart Templates**: High-quality e-sports specific content templates
- **One-Click Generation**: Generate titles and content instantly
- **Copy & Use**: Seamless integration with forum posting

### Technical Features
- **Database Integration**: MySQL with comprehensive schema
- **Responsive UI**: Modern JavaFX interface with custom styling
- **Error Handling**: Comprehensive error prevention and user feedback
- **Performance**: Optimized for speed and reliability

##  Technology Stack

### Backend
- **Java 8+**: Core application logic
- **MySQL**: Database management
- **JDBC**: Database connectivity
- **HTTPURLConnection**: API integration

### Frontend
- **JavaFX 11**: User interface framework
- **FXML**: UI component definition
- **CSS**: Custom styling and themes
- **Scene Builder**: UI design support

### AI Integration
- **Hugging Face API**: Content generation
- **Mistral-7B-Instruct**: AI model for intelligent content
- **Fallback System**: Local content generation for reliability

## 📋 Prerequisites

### Required Software
- **Java Development Kit (JDK) 8+**
- **MySQL Server 5.7+**
- **JavaFX 11**
- **Maven (optional)**

### Database Setup
1. Install MySQL Server
2. Create database: `victorygrid_esports`
3. Import schema from `database_setup.sql`
4. Run additional SQL scripts in `database/` folder

##  Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/soukeinasaid/e-sport.git
cd e-sport-dev
```

### 2. Database Setup
```bash
# Run the database setup script
mysql -u root -p < database_setup.sql

# Run additional table creation scripts
mysql -u root -p victorygrid_esports < database/create_favorites_table.sql
mysql -u root -p victorygrid_esports < database/update_role_column.sql
```

### 3. Compile the Project
```bash
# Using the provided batch file (Windows)
compile_and_run.bat

# Or compile manually
javac -cp "target\classes;lib\*" -encoding UTF-8 -d target\classes src\main\java\utilies\MainApp.java
```

### 4. Run the Application
```bash
# Using the provided batch file
run.bat

# Or run manually
java -cp "target\classes;lib\*" utilies.MainApp
```

##  Project Structure

```
e-sport-dev/
├── src/main/java/
│   ├── controller/          # UI controllers
│   │   ├── ForumController.java
│   │   ├── AIGeneratorController.java
│   │   ├── LoginController.java
│   │   └── ...
│   ├── entity/              # Data models
│   │   ├── User.java
│   │   ├── Forum.java
│   │   └── Favorite.java
│   ├── service/             # Business logic
│   │   ├── UserService.java
│   │   ├── ForumService.java
│   │   └── FavoriteService.java
│   └── utilies/             # Utilities
│       ├── HuggingFaceAI.java
│       ├── DatabaseConfig.java
│       └── Session.java
├── src/main/resources/
│   ├── view/                # FXML UI files
│   ├── css/                 # Stylesheets
│   └── assets/              # Images and resources
├── lib/                     # External dependencies
├── database/                # SQL scripts
└── pom.xml                  # Maven configuration
```

##  AI Content Generation

### How It Works
1. **Access**: Click the " AI Generator" button in the forum
2. **Input**: Enter a topic or title for content generation
3. **Generate**: AI creates high-quality e-sports forum content
4. **Use**: Copy to clipboard or use directly in forum post

### Features
- **Smart Content**: Context-aware e-sports specific generation
- **Multiple Templates**: Variety of content styles and formats
- **Error-Free**: 100% reliability with fallback system
- **Instant Generation**: No waiting, immediate results

### Example Output
```
Title: "Discussion: Competitive Gaming in E-Sports"

Content: "Hey everyone! I wanted to start a discussion about Competitive Gaming in e-sports scene. 
This has been getting a lot of attention lately, and I'm curious about your thoughts. 
What do you think about the current state of Competitive Gaming?..."
```

##  Configuration

### Database Configuration
Edit `src/main/java/utilies/DatabaseConfig.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/victorygrid_esports";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

### AI Configuration
Edit `src/main/java/utilies/HuggingFaceAI.java`:
```java
private static final String API_KEY = "your_huggingface_api_key";
```

##  User Guide

### Forum Features
- **Create Posts**: Add new forum posts with AI assistance
- **Filter Content**: Filter by category, author, or date
- **Favorites**: Save and manage favorite posts
- **Pagination**: Navigate through multiple pages of content

### AI Generator
- **Title Generation**: Create catchy forum post titles
- **Content Generation**: Generate detailed discussion content
- **Copy Function**: Copy generated content to clipboard
- **Direct Use**: Transfer content directly to forum form

### User Management
- **Profile Management**: Update personal information
- **Post History**: View all your forum contributions
- **Favorites**: Manage saved forum posts

##  Troubleshooting

### Common Issues

**Database Connection Error**
- Verify MySQL server is running
- Check database credentials in `DatabaseConfig.java`
- Ensure database schema is properly imported

**JavaFX Runtime Error**
- Install JavaFX 11 or higher
- Verify JavaFX libraries are in `lib/` folder
- Check classpath configuration

**AI Generator Not Working**
- Verify internet connection for Hugging Face API
- Check API key configuration
- Fallback content will work even if API fails

**Compilation Errors**
- Ensure all required JAR files are in `lib/` folder
- Check Java version compatibility (JDK 8+)
- Verify file encoding is UTF-8

### Performance Tips
- Use the provided batch files for optimal performance
- Close unnecessary applications when running
- Ensure sufficient memory allocation for Java

##  Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

### Code Style
- Follow Java naming conventions
- Add proper documentation
- Include error handling
- Test all features

##  License

This project is open source and available under the [MIT License](LICENSE).

##  Support

For issues and questions:
- Create an issue on GitHub
- Check the troubleshooting section
- Review the documentation

##  About VictoryGrid

VictoryGrid is a comprehensive e-sports community platform designed to bring together gamers, fans, and enthusiasts. With advanced AI-powered content generation and a robust forum system, it provides the perfect environment for discussing competitive gaming, sharing experiences, and building a strong e-sports community.

---

**Built with ❤️ for the E-Sports Community**
