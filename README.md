# PatchFlow <img height="20" alt="Image" src="https://github.com/user-attachments/assets/f23b203a-9e13-432f-906c-29c0c70e5d56" />

###### An Issue and Bug tracker made easy for solo developers  

[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/Temavrix/NexaView) [![Documentation Status](https://readthedocs.org/projects/ansicolortags/badge/?version=latest)](https://github.com/Temavrix/NexaView) [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/Temavrix/NexaView/issues)

Please consider donating some money to our organization to help fund this project:  
<a href="https://buymeacoffee.com/mahadhevha" target="_blank">
    <img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" height="30"/>
</a>

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/f49a9290-b664-494d-b2c5-766a92f581b0" />  <img height="150" alt="Image" src="https://github.com/user-attachments/assets/5d6e7c48-476f-4b71-966a-1f47b5e86631" />



## Table Of Contents
- [What's New?](#whats-new)
- [Introducing PatchFlow](#introducing-patchflow)
- [Running PatchFlow On Your Computer](#running-patchflow-on-your-computer)
- [Features Of PatchFlow](#features)
  * [Github Issues](#github-issues)
  * [Apache Kafka](#apache-kafka)
  * [AI](#ai)
- [Issues](#issues)
- [License](#license)


## What's New?
Here at Temavrix we are committed in keeping PatchFlow up-to-date and up-to-speed with the growing tech solutions, services and algorithms. Hence this new commit includes:


```
PatchFlow Changelogs:-

ANNOUNCEMENT:- 

1. New Kanban Board For Developers:-
   Users can now map workflow stages into columns to manage tasks. 
   By organising issues based on completion status they can plan 
   tasks more efficiently.

2. Users can now disable kafka from running by disabling it in 
   settings.

Code Checks Manifest:-
All Checks Status: ✅
-----------------------------------------
UX (User Experience) Checks: ✅
BackEnd Code-FrontEnd UI Integration Checks: ✅
(All evaluations are done by the R&D Department)

Last Updated: 02-April-2026 22:50 HRS (Singapore Standard Time)
Publisher: Temavrix
```
Keep up-to-date with what's happening on this repository by clicking the 'Star' and 'Watch' button on the top right corner of this repository.


## Introducing PatchFlow
PatchFlow is a desktop-based issue tracking and developer productivity tool built using JavaFX and SQLite, designed to help developers manage bugs, track issues, and improve workflow efficiency.

It's Simplicity, Speed, Clean UI and Developer-first features make this suitable and lightweight for solo-developers.


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


## Features

#### Github Issues
With Patchflow not only are you able to keep track of unfinished tasks and feature requests on your local system but you can also import issues from Github.

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/e61d41df-c4a9-48a0-94d6-e5fbeb88e11c" />

By serching for your requested repository you can view the list of issues and select them to add in your local database.

You will need to create a Github token [here](https://github.com/settings/tokens) and paste it in settings under `Github Token`.

#### Apache Kafka
NOTE: Don't want to use Apache Kafka? you can disable 'kafka' in settings.

Kafka assists users in collecting details of issues that have been created and is stored in a JSON 
(Subject to further review) file and for sending emails to user's email if it's severity is `Critical`.

First Users will have to open current `patchflow` and `patcher` folder in a another window which will 
act as the backend (consumer). 

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/fa8a439a-a051-4859-a783-c8aca8bbb020" />

Upon running `patchflow`, 2 terminal screens will pop-up and start running. You can minimize it and 
continue working.  
These terminals screens are for the `zookeeper` and `kafka server` to run in the background.

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/9173cf37-7e9a-42f4-ab69-98d5b0fd9247" />

Once `patchflow` and the 2 terminal screens are running, you can start the consumer in the `patcher` 
folder.  
Upon creating a new issue it will display here and store the issue in a JSON file and if any issue 
is 'Critical' then it will send an email to your email address.

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/fa6287d6-0050-46aa-904e-349bf803177c" />

NOTE: To send an email, you will need to register in your [Google Account](https://myaccount.google.com/apppasswords) and paste your app password in line 35 of IssueConsumer.java.

#### AI
Patchflow allows you to ask assistance on an issue if you have any problems solving it by using our in-built AI window.

You can choose the model that you want to chat with: 

<img height="100" alt="Image" src="https://github.com/user-attachments/assets/bdcb6571-85db-43d3-b991-3ea6deed8bdf" />

Select the model and you can get assistance to your questions: 

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/6a2ee934-4a13-4580-8d20-a022b85f09de" />

You will need to create a Gemini token [here](https://aistudio.google.com/app/api-keys?project=gen-lang-client-0531172755) and a OpenRouter token [here](https://openrouter.ai/workspaces/default/keys) then paste it in settings under `Gemini API` and `OpenRouter API` repectively to access the models.


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

Version 0.9.3 (Beta)