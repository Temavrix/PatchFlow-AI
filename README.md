# PatchFlow <img height="20" alt="Image" src="https://private-user-images.githubusercontent.com/69076784/546710126-9e23a75d-a603-4360-a327-749ea0d28b4d.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzA1MzQwMDYsIm5iZiI6MTc3MDUzMzcwNiwicGF0aCI6Ii82OTA3Njc4NC81NDY3MTAxMjYtOWUyM2E3NWQtYTYwMy00MzYwLWEzMjctNzQ5ZWEwZDI4YjRkLnBuZz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNjAyMDglMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjYwMjA4VDA2NTUwNlomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWFkNmY3MjA0NTVmN2NjNTk4YjNiYjgxNTA2MGFmMTg3MDRhMzdiZDcyZTZmODFjMWYxZTFjNDllYWVjZWRmODkmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.N9kFvyb9rv4LOHQsgXL_GbcnmupO_Jkb3w4FKYIUCtU" />

###### An Issue and Bug tracker made easy for solo developers  

[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/Temavrix/NexaView) [![Documentation Status](https://readthedocs.org/projects/ansicolortags/badge/?version=latest)](https://github.com/Temavrix/NexaView) [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/Temavrix/NexaView/issues)

Please consider donating some money to our organization to help fund this project:  
<a href="https://buymeacoffee.com/mahadhevha" target="_blank">
    <img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" height="30"/>
</a>

<img height="200" alt="Image" src="https://github.com/user-attachments/assets/ae016d94-b2b8-4201-889b-9642a436032f" />


## Table Of Contents
- [What's New?](#whats-new)
- [Running PatchFlow On Your Computer](#running-patchflow-on-your-computer)
- [Issues](#issues)
- [License](#license)


## What's New?
Here at Temavrix we are committed in keeping PatchFlow up-to-date and up-to-speed with the growing tech solutions, services and algorithms. Hence this new commit includes:


```
PatchFlow Changelogs:-

ANNOUNCEMENT:- 

1. Removed the ability to open multiple windows by 
   clicking buttons multiple times.

2. UI/UX Improvements

Code Checks Manifest:-
All Checks Status: ✅
-----------------------------------------
UX (User Experience) Checks: ✅
BackEnd Code-FrontEnd UI Integration Checks: ✅
(All evaluations are done by the R&D Department)

Last Updated: 25-February-2026 23:00 HRS (Singapore Standard Time)
Publisher: Temavrix
```
Keep up-to-date with what's happening on this repository by clicking the 'Star' and 'Watch' button on the top right corner of this repository.

## Running PatchFlow On Your Computer

#### Installing Maven and Java
First check if Java and Maven is installed on your system.

##### Install Maven using Chocolatey:  
Open PowerShell as Administrator.
```
choco install maven -y
```
##### Check if Maven and Java is installed:  
```
java --version

mvn -version
```
If both commands work then you're ready.

#### Setting-up Maven Project
Inside project root (where pom.xml is located):
```
mvn clean install
```
Then run Patchflow:
```
mvn javafx:run (OR) mvn clean javafx:run
```

#### Issues when setting up Maven
Check required version in pom.xml:
```
<maven.compiler.source>23</maven.compiler.source>
```
If it says 23 and you’re on 17 then it won’t work and you
must fix by installing correct JDK.


## Issues
As this project is still in constant development, if you run into any issues while operating or have any suggestions or features, please feel free to drop by our [issues](https://github.com/Temavrix/PatchFlow/issues) section and open a issue and we'll respond within 2-4 working days, Thank you for your understanding.


## License
IMPORTANT NOTE: Any User who are willing to Share or Re-Distribute NexaView are kindly advised to:

1. A link to this repository from the user's 'Modified program' README file. 

This will be helpful for us as users will know it's original source and about our startup.
Please also refer to LICENSE file for clarifications.  
Thank you for your kind co-operation :-)


PatchFlow Copyright (C) Temavrix 2026  
All Rights Reserved

Version 0.7.2 (Beta)