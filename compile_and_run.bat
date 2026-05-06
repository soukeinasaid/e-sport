@echo off
echo =================================
echo   VictoryGrid E-Sports Forum
echo   Java 8 + JavaFX 11 Setup
echo =================================

echo.
echo Checking Java installation...
java -version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Setting up directories...
if not exist target\classes mkdir target\classes
if not exist lib mkdir lib

echo.
echo Downloading JavaFX dependencies...
echo Note: This requires internet connection
echo.

REM Download JavaFX 11 JARs (these are the minimal required JARs)
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/openjfx/javafx-base/11/javafx-base-11.jar' -OutFile 'lib\javafx-base-11.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/openjfx/javafx-controls/11/javafx-controls-11.jar' -OutFile 'lib\javafx-controls-11.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/11/javafx-fxml-11.jar' -OutFile 'lib\javafx-fxml-11.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/11/javafx-graphics-11.jar' -OutFile 'lib\javafx-graphics-11.jar'}"

echo.
echo Downloading MySQL connector...
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile 'lib\mysql-connector-j-8.0.33.jar'}"

echo.
echo Downloading Google API dependencies...
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/api-client/google-api-client/1.33.1/google-api-client-1.33.1.jar' -OutFile 'lib\google-api-client-1.33.1.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/0.27.0/google-auth-library-oauth2-http-0.27.0.jar' -OutFile 'lib\google-auth-library-oauth2-http-0.27.0.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client-jetty/1.32.1/google-oauth-client-jetty-1.32.1.jar' -OutFile 'lib\google-oauth-client-jetty-1.32.1.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/http-client/google-http-client-gson/1.39.2/google-http-client-gson-1.39.2.jar' -OutFile 'lib\google-http-client-gson-1.39.2.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client-java6/1.32.1/google-oauth-client-java6-1.32.1.jar' -OutFile 'lib\google-oauth-client-java6-1.32.1.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/apis/google-api-services-oauth2/v2-rev157-1.25.0/google-api-services-oauth2-v2-rev157-1.25.0.jar' -OutFile 'lib\google-api-services-oauth2-v2-rev157-1.25.0.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.39.2/google-http-client-1.39.2.jar' -OutFile 'lib\google-http-client-1.39.2.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/0.27.0/google-auth-library-credentials-0.27.0.jar' -OutFile 'lib\google-auth-library-credentials-0.27.0.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar' -OutFile 'lib\jsr305-3.0.2.jar'}"

echo Downloading JSON library for Hugging Face API...
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar' -OutFile 'lib\json-20231013.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client/1.32.1/google-oauth-client-1.32.1.jar' -OutFile 'lib\google-oauth-client-1.32.1.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar' -OutFile 'lib\httpclient-4.5.13.jar'}"
powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.15/httpcore-4.4.15.jar' -OutFile 'lib\httpcore-4.4.15.jar'}"

echo.
echo Compiling the project...
set CLASSPATH=lib\javafx-base-11.jar;lib\javafx-controls-11.jar;lib\javafx-fxml-11.jar;lib\javafx-graphics-11.jar;lib\mysql-connector-j-8.0.33.jar;lib\google-api-client-1.33.1.jar;lib\google-auth-library-oauth2-http-0.27.0.jar;lib\google-oauth-client-jetty-1.32.1.jar;lib\google-http-client-gson-1.39.2.jar;lib\google-oauth-client-java6-1.32.1.jar;lib\google-api-services-oauth2-v2-rev157-1.25.0.jar;lib\google-http-client-1.39.2.jar;lib\google-auth-library-credentials-0.27.0.jar;lib\jsr305-3.0.2.jar;lib\google-oauth-client-1.32.1.jar;lib\httpclient-4.5.13.jar;lib\httpcore-4.4.15.jar

"C:\Program Files (x86)\Java\jdk1.8.0_202\bin\javac.exe" -cp "%CLASSPATH%" -encoding UTF-8 -d target\classes ^
    src\main\java\utilies\*.java ^
    src\main\java\entity\*.java ^
    src\main\java\service\*.java ^
    src\main\java\controller\*.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed
    echo Check the error messages above
    pause
    exit /b 1
)

echo.
echo Copying resources...
xcopy /E /I /Y "src\main\resources" "target\classes" >nul 2>&1

echo.
echo Compilation successful!
echo Starting the application...
java -cp "target\classes;%CLASSPATH%" utilies.MainApp

pause
