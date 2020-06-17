# COMP4111-Project

Please first initialize the database. Run the SQL file called dbInitialize.sql.
Use user = root, password = 1234 on MySQL 5.7. 

After initializing the database, run HttpServer.java under /src/main/java
using Java SDK 11. Then send requests by JMeter. 

In /src/main/java, HttpServer.java controls the server. 
DBTrial connects the database. Please ignore JsonTrial.java. 

In /src/main/java/bean, Book.java, Database.java, User.java are JavaBean. 