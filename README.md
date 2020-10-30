### Running Flyway migrations
Execute `flyway.sh` (you may want to add execute rights to the file first with
 `chmod +x flyway.sh`).  
 
 The script accepts these optional parameters:  
 `-n <username>` - database username (`postgres` by default),   
 `-p <password>` - database password (`postgres` by default),  
 `-u <URL>` - JDBC database url `jdbc:postgresql://localhost:5432/rubduk` by default),   
 `-d <name>` - JDBC database name on default url `jdbc:postgresql://localhost:5432/<name>` by default),   
 `-c` - run `flywayClean` instead of `flywayMigrate`.
 
 
