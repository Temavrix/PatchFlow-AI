# PatchFlow <img height="20" alt="Image" src="https://github.com/user-attachments/assets/f23b203a-9e13-432f-906c-29c0c70e5d56" />

###### An AI Bug tracker for an easier workflow for developers  

[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/Temavrix/PatchFlow) [![Documentation Status](https://readthedocs.org/projects/ansicolortags/badge/?version=latest)](https://github.com/Temavrix/PatchFlow) [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/Temavrix/PatchFlow/issues)

Please consider donating some money to our organization to help fund this project:  
<a href="https://buymeacoffee.com/mahadhevha" target="_blank">
    <img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" height="30"/>
</a>

Head to [Releases](https://github.com/Temavrix/PatchFlow/releases/tag/v1.0.0) to download the exe, which is pre-bundled and ready to use.

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/51062f14-87ad-429e-ac76-85370d0a8af7" /> <img height="150" alt="Image" src="https://github.com/user-attachments/assets/a0a54ed3-33c7-471b-86f6-574da2314a32" />



## Table Of Contents
- [What's New?](#whats-new)
- [Introducing PatchFlow](#introducing-patchflow)
- [Running PatchFlow On Your Computer](#running-patchflow-on-your-computer)
- [Features Of PatchFlow](#features)
  * [Teams](#teams)
  * [AI](#ai)
  * [Github Issues](#github-issues)
- [Issues](#issues)
- [License](#license)


## What's New?
Here at Temavrix we are committed in keeping PatchFlow up-to-date and up-to-speed with the growing tech solutions, services and algorithms. Hence this new commit includes:


```
PatchFlow Changelogs:-

ANNOUNCEMENT:- 
For the project's future: Our resources at Temavrix are 
currently being diverted to other new projects and endeavours 
hence PatchFlow will be currently recieving important 
Security patches and urgent patches if needed.

1. Due to unfeasibility concerns we have removed email 
   notifications for critical emails done by apache kafka.
   Hence there will be no future plans of using kafka in 
   this project.

2. AI assistant and teams is now out of beta testing.

Code Checks Manifest:-
All Checks Status: ✅
-----------------------------------------
UX (User Experience) Checks: ✅
BackEnd Code-FrontEnd UI Integration Checks: ✅
(All evaluations are done by the R&D Department)

Last Updated: 27-May-2026 19:00 HRS (Singapore Standard Time)
Publisher: Temavrix
```
Keep up-to-date with what's happening on this repository by clicking the 'Star' and 'Watch' button on the top right corner of this repository.


## Introducing PatchFlow
PatchFlow is a desktop-based issue tracking and developer productivity tool built using JavaFX and SQLite (for local storage) and Firebase (for cloud based storage) designed to help developers manage bugs, track issues, and improve workflow efficiency.

It's Simplicity, Speed, Clean UI and Developer-first features make this suitable and lightweight for developers.


## Running PatchFlow On Your Computer

#### Installing Maven and Java:
Install Maven using Chocolatey using PowerShell as Administrator:
```
choco install maven -y
```

#### Setting-up Maven Project:
Inside project root (where pom.xml is located) and then run Patchflow:
```
mvn clean install

mvn javafx:run (OR) mvn clean javafx:run
```

#### Issues when setting up Maven:
Check required version in pom.xml:
```
<maven.compiler.source>21</maven.compiler.source>
```
If it says 21 and you’re on 17 then it won’t work and you
must fix by installing correct JDK.


## Features

### Teams
A new way introduced to delegate and assign tasks. 

To use this feature you will need to go settings and enter your 'email' and 'password' to register for accessing the database.

Patchflow will store your issues on our cloud-based storage and allow you to share the issue with others. Head to any of your local stored issues and select the 'assign task' button to be prompted with which email you want to assign it to.

<img height="50" alt="Image" src="https://github.com/user-attachments/assets/c947aca3-fd18-4827-860d-1b19fb5529db" />

By assigning a task to a team member, they will be able to view the issue in their 'team issue' page and then add to their workflow. The team member would also need to register with their unique Email for accessing Firebase from their side.

<img height="130" alt="Image" src="https://github.com/user-attachments/assets/701e5dab-ce33-4ced-ba20-1949a56f6cdd" />

NOTE: For developers who want to use their custom firebase instance, you can head too line 18 of FirebaseService.java and add your Firebase API Key.



### AI
#### 1. Auto-Filling Capabilities
When creating a new issue you can now ask AI to assist you in filling issue's description and issue's code snippet.  
User will need to fill project's language and issue's title for the AI to provide description and code snippet.

Before: 

<img height="100" alt="Image" src="https://github.com/user-attachments/assets/ad1c79de-bfd8-40a4-80c2-1b332ee0bffc" />  

After with AI assistance:

<img height="100" alt="Image" src="https://github.com/user-attachments/assets/f0f45030-daf7-4942-a430-5ea276c3b166" />

You will need to create a Gemini token [here](https://aistudio.google.com/app/api-keys?project=gen-lang-client-0531172755) and paste it in settings under `Gemini API` to ask for assistance.

#### 2. AI Assistance
Patchflow allows you to ask assistance on an issue if you have any problems solving it by using our in-built AI window.

You can choose the model that you want to chat with: 

<img height="100" alt="Image" src="https://github.com/user-attachments/assets/bdcb6571-85db-43d3-b991-3ea6deed8bdf" />

Select the model and you can get assistance to your questions: 

<img height="150" alt="Image" src="https://github.com/user-attachments/assets/6a2ee934-4a13-4580-8d20-a022b85f09de" />

You will need to create a Gemini token [here](https://aistudio.google.com/app/api-keys?project=gen-lang-client-0531172755) and a OpenRouter token [here](https://openrouter.ai/workspaces/default/keys) then paste it in settings under `Gemini API` and `OpenRouter API` repectively to access the models.



### Github Issues
With Patchflow not only are you able to keep track of unfinished tasks and feature requests on your local system but you can also import issues from Github.

<img height="130" alt="Image" src="https://github.com/user-attachments/assets/d51bc9f9-c583-4d44-b3f4-f44fd562cb63" />

By serching for your requested repository you can view the list of issues and select them to add in your local database.

You will need to create a Github token [here](https://github.com/settings/tokens) and paste it in settings under `Github Token`.



## Issues
As this project is still in constant development, if you run into any issues while operating or have any suggestions or features, please feel free to drop by our [issues](https://github.com/Temavrix/PatchFlow/issues) section and open a issue and we'll respond within 2-4 working days, Thank you for your understanding.


## License
IMPORTANT NOTE: Any User who are willing to Share or Re-Distribute PatchFlow are kindly advised to:

1. A link to this repository from the user's 'Modified program' README file. 

This will be helpful for us as users will know it's original source and about our startup.
Please also refer to LICENSE file for clarifications.  
Thank you for your kind co-operation :-)


PatchFlow Copyright (C) Temavrix 2026  
All Rights Reserved

Version 1.1.0