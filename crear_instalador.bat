@echo off
setlocal EnableDelayedExpansion

REM ==========================================
REM  CONFIGURACIÓN
REM ==========================================
set "APP_NAME=Escuela"
set "APP_VERSION=1.0.0"

REM --- MODO CLASSPATH ---
set "MAIN_JAR=escuela-1.0.0.jar"
REM ¡USAMOS LA NUEVA CLASE TRAMPOLÍN!
set "MAIN_CLASS=com.bd.Main"

REM -- RUTAS
set "INNO_SETUP=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"

REM ==========================================
REM  INICIO
REM ==========================================

echo.
echo [1/5] Limpiando...
if exist app-image rmdir /s /q app-image
if exist Output rmdir /s /q Output
if exist target rmdir /s /q target

echo.
echo [2/5] Compilando Maven...
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Fallo Maven.
    pause
    exit /b
)

echo.
echo [3/5] LIMPIANDO JARS DUPLICADOS (CRITICO)...
REM Maven baja versiones linux y windows. JPackage se confunde si hay dos.
REM Borramos las versiones sin sufijo (linux/genericas) y dejamos las -win.
if exist "target\libs\javafx-base-25.jar" del /q "target\libs\javafx-base-25.jar"
if exist "target\libs\javafx-controls-25.jar" del /q "target\libs\javafx-controls-25.jar"
if exist "target\libs\javafx-fxml-25.jar" del /q "target\libs\javafx-fxml-25.jar"
if exist "target\libs\javafx-graphics-25.jar" del /q "target\libs\javafx-graphics-25.jar"
echo Jars limpiados correctamente.

echo.
echo [4/5] Generando Imagen (MODO CLASSPATH)...
REM Usamos --input: Esto mete TODOS los jars de target\libs
REM Usamos --win-console ^ Para ver errores si falla al abrir, abajo de --app-version "%APP_VERSION%" ^

"%JAVA_HOME%\bin\jpackage" ^
  --type app-image ^
  --dest app-image ^
  --name "%APP_NAME%" ^
  --input "target\libs" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --icon "icono.ico" ^
  --vendor "POSS Ventas" ^
  --app-version "%APP_VERSION%" ^
  --java-options "-Dfile.encoding=UTF-8 -Dprism.lcdtext=false"

if %errorlevel% neq 0 (
    echo [ERROR] Fallo jpackage.
    pause
    exit /b
)

echo.
echo [5/5] Configurando Inno Setup...

(
echo [Setup]
echo AppName=%APP_NAME%
echo AppVersion=%APP_VERSION%
echo DefaultDirName={autopf}\%APP_NAME%
REM Esto crea la carpeta "Demo" en el menu inicio  en vez de Default
echo DefaultGroupName=%APP_NAME%
REM Esto evita que el usuario cambie ese nombre durante la instalacion
echo DisableProgramGroupPage=yes
echo OutputDir=Output
echo OutputBaseFilename=%APP_NAME%_Final
echo Compression=lzma2
echo SolidCompression=yes
echo WizardStyle=modern
echo ArchitecturesInstallIn64BitMode=x64compatible
echo PrivilegesRequired=admin
if exist "icono.ico" echo SetupIconFile=icono.ico
REM Poner icono cuando desinstalamos el programa
echo UninstallDisplayIcon={app}\%APP_NAME%.ico
echo UninstallDisplayName=Desinstalar%APP_NAME%

echo.
echo [Files]
REM Copiamos la App
echo Source: "app-image\%APP_NAME%\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
REM Copiamos el icono y le cambiamos el nombre para que coincida con lo que buscan los accesos directos
echo Source: "icono.ico"; DestDir: "{app}"; DestName: "%APP_NAME%.ico"; Flags: ignoreversion

echo.
echo [Icons]
echo Name: "{group}\%APP_NAME%"; Filename: "{app}\%APP_NAME%.exe"; IconFilename: "{app}\%APP_NAME%.ico"
echo Name: "{autodesktop}\%APP_NAME%"; Filename: "{app}\%APP_NAME%.exe"; IconFilename: "{app}\%APP_NAME%.ico"

echo.
echo [Tasks]
echo Name: "desktopicon"; Description: "Create a desktop icon"; GroupDescription: "Additional icons"; Flags: unchecked

echo.
echo [Run]
REM 4. Ejecutamos la App al finalizar la instalacion
echo Filename: "{app}\%APP_NAME%.exe"; Description: "Iniciar %APP_NAME%"; Flags: nowait postinstall skipifsilent

echo.
echo [UninstallDelete]
echo Type: filesandordirs; Name: "{app}"

) > setup.iss

echo.
echo [6/6] Compilando EXE...
"%INNO_SETUP%" "setup.iss"

if %errorlevel% neq 0 (
    echo [ERROR] Fallo Inno Setup.
    pause
    exit /b
)

echo.
echo ========================================================
echo  EXITO.
echo  El instalador esta en Output\
echo ========================================================
pause