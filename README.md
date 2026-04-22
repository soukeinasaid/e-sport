# VictoryGrid Login Application

A JavaFX application that replicates the VictoryGrid login screen with a modern dark theme and teal accent.

## Features

- **Dark Theme**: Modern dark background with teal accent header
- **VictoryGrid Branding**: Logo and tagline matching the original design
- **Login Form**: Email and password input fields with validation
- **Responsive Design**: Clean, centered layout with proper spacing
- **Interactive Elements**: Hover effects and button interactions

## Project Structure

```
src/main/java/com/victorygrid/
    VictoryGridApp.java      # Main application class
    LoginController.java     # Controller for login functionality
    login-view.fxml          # FXML layout file
    styles.css              # CSS styling for dark theme
```

## Requirements

- Java 17 or higher
- JavaFX 17.0.2
- Maven 3.6 or higher

## How to Run

### Option 1: Using Maven (Recommended)

1. Make sure you have Java 17 and Maven installed
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn clean javafx:run
   ```

### Option 2: Using IDE

1. Import the project as a Maven project in your IDE (IntelliJ, Eclipse, etc.)
2. Make sure JavaFX SDK is configured
3. Run the `VictoryGridApp.java` main class

### Option 3: Manual Compilation

If Maven is not available, you can compile manually:

1. Download JavaFX SDK 17.0.2
2. Compile with JavaFX modules:
   ```bash
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml src/main/java/com/victorygrid/*.java
   ```
3. Run the application:
   ```bash
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml com.victorygrid.VictoryGridApp
   ```

## Application Features

### Login Validation
- Email format validation (checks for @ and .)
- Password requirement validation
- Console output for login attempts

### UI Components
- **Header**: Teal background with VictoryGrid logo and tagline
- **Login Form**: Dark background with rounded corners
- **Input Fields**: Modern styling with focus states
- **Login Button**: Blue button with hover effects
- **Sign Up Link**: Clickable link at the bottom

### Styling
- Dark theme (#1a1a1a background)
- Teal accent color (#00897b)
- Blue primary button (#2196f3)
- Smooth hover transitions
- Drop shadow effects

## Future Enhancements

- [ ] Connect to actual user database
- [ ] Add password strength validation
- [ ] Implement remember me functionality
- [ ] Add forgot password feature
- [ ] Create sign up screen
- [ ] Add user dashboard after login
- [ ] Implement session management

## Troubleshooting

### Common Issues

1. **JavaFX not found**: Make sure JavaFX SDK is properly configured
2. **Module not found**: Ensure all required JavaFX modules are included
3. **CSS not loading**: Check that styles.css is in the correct location
4. **FXML not loading**: Verify the FXML file path in VictoryGridApp.java

### IDE Configuration

**IntelliJ IDEA:**
1. File > Project Structure > Modules
2. Add JavaFX SDK to module dependencies
3. Go to Run > Edit Configurations
4. In VM options, add:
   ```
   --module-path "C:\Users\saids\.m2\repository\org\openjfx\javafx-controls\17.0.2;C:\Users\saids\.m2\repository\org\openjfx\javafx-fxml\17.0.2;C:\Users\saids\.m2\repository\org\openjfx\javafx-graphics\17.0.2;C:\Users\saids\.m2\repository\org\openjfx\javafx-base\17.0.2" --add-modules javafx.controls,javafx.fxml
   ```
5. Apply and run the application

**Eclipse:**
1. Right-click project > Build Path > Configure Build Path
2. Add JavaFX libraries to the build path
3. Run with VM arguments: `--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml`

## License

This project is for educational purposes to demonstrate JavaFX UI development.
