# Saitenweise Backend Example

Dies ist eine Version des Saitenweise-Backends als erster Ausgangspunkt für Spring Boot Backends.

Es entspricht der Iteration 7 von main, aber ohne Profil-Konfiguration und ohne Unit-Tests.

Bitte ändern Sie zuerst in der pom.xml die `artifactId` in Zeile 12 auf den Namen Ihres Projekts (alles kleingeschrieben, ggf. mit - getrennt).

Dann starten Sie die Applikation `src/main/java/de/htwg/in/wete/backend/BackendApplication.java` testweise entweder in der IDE oder über die Kommandozeile per

```sh
./mvnw spring-boot:run
```

(Unter Windows das ./ weglassen).

Sie sollten jetzt im Browser bei Aufruf von `http://localhost:8081/api/product` die Liste der Produkte angezeigt bekommen (JSON).

Stoppen Sie die Applikation und öffnen Sie `src/main/resources/application.properties`. Hier können Sie die H2-Konfiguration durch Kommentare (`#` am Anfang der Zeile) deaktivieren und die MariaDB-Konfiguration einstellen (mit den Daten für Ihre Datenbank).