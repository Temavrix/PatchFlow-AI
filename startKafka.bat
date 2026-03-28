start cmd /k C:\kafka\bin\windows\zookeeper-server-start.bat C:\kafka\config\zookeeper.properties
timeout /t 5
start cmd /k C:\kafka\bin\windows\kafka-server-start.bat C:\kafka\config\server.properties