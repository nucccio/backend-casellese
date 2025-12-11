# Notes
- Achte auf H2 Configuration and MariaDB-Configuration in application.properties
- CTRL + C beendet Prozess
- lsof -i :8081 | grep LISTEN (PID graben)
- kill -9 <PID> (Prozess killen)
- ./mvnw spring-boot:run (BackEnd starten)

# Support-Fragen Herr Schneider
- Was ist der Unterschied zu mvn spring-boot:run und starten der BackEnd-Application?
- 

# Iteration 1:
./mvnw spring-boot:run , !WICHTIG! Das Backend erst starten, bevor das Terminal funktioniert
curl http://localhost:8081/api/product in neuem Bash Terminal

# Iteration 2: 
## Lokal mit H2 (erst Java-Prozesse beenden!)
pkill -f java  # oder: rm -f ./target/saitenweise-db.mv.db
mvn spring-boot:run -Dspring-boot.run.profiles=local

## Mit Test-Datenbank (MariaDB)
mvn spring-boot:run -Dspring-boot.run.profiles=test

## Mit Produktions-Datenbank (MariaDB)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Iteration 3:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
- JDBC URL: jdbc:h2:file:./target/saitenweise-db;AUTO_SERVER=TRUE
- Username: sa
- unter http://localhost:8081/h2-console
![alt text](image-1.png)

# Iteration 4: CORS Configuration
Ohne diese Konfiguration würde der Browser Anfragen vom Frontend (z.B. http://localhost:5173) an das Backend (z.B. http://localhost:8081) blockieren, da sie von unterschiedlichen Origins kommen. Die CORS-Konfiguration erlaubt diese Cross-Origin-Anfragen.

# Iteration 5: Added GitHub Actions workflow for Maven build verification
Der GitHub Actions Workflow macht Folgendes:
Schritt : Beschreibung 
______________________
Trigger : Läuft bei jedem push und pull_request

Checkout: Klont das Repository

JDK 21 Setup: Installiert Java 21 (Temurin Distribution) mit Maven-Caching

Maven Verify: Führt mvn verify aus (kompiliert, testet, verifiziert)

![alt text](image.png)