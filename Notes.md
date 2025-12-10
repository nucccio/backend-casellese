# Notes
- Achte auf H2 Configuration and MariaDB-Configuration in application.properties

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