# ladder

## General info
A clone of the [Todoist](https://todoist.com/) web-app written in Java + Spring (backend) and Typescript + Angular (frontend).

![gif](readme_files/gifs/main.gif)

## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)
	* [Local Setup](#local-setup)
	* [Docker](#docker)
* [Features](#features/planned-features)

## Technologies
Project is created with:
* Java 11
* Spring Boot 2.51
* Typescript
* Angular
* PostgreSQL

Additional libraries (backend):
* Lombok
* Jjwt library for JWT tokens
* RestAssured for testing

Additional libraries (frontend):
* ngx-markdown
* ngx-drag-drop

## Setup
### Local Setup
In order to run project locally you need to clone this repository:
```
$ git clone https://github.com/xpakx/ladder.git
```

Navigate to backend folder in the command line and start server:
```
$ mvn spring-boot:run
```

Optionally, start notification server:
```
$ mvn spring-boot:run
```

Finally, navigate to frontend folder, build and run frontend:
```
$ npm i
$ ng serve
```

### Docker
In order to run project with Docker you need to clone this repository and build project with Docker Compose:
```
$ git clone https://github.com/xpakx/ladder.git
$ cd ladder
$ docker-compose up --build -d
```

To stop:
```
$ docker-compose stop
```

## Features/planned features
- [x] Projects
	- [x] Displaying project list
	- [x] Creating projects
	- [x] Editing projects
	- [x] Deleting projects
	- [x] Adding projects above/below another project
	- [x] Nested projects
	- [x] Collapsing projects
	- [x] Re-ordering projects with drag'n'drop
	- [x] Viewing projects' tasks
	- [x] Duplicating projects
	- [x] Archiving projects
- [x] Tasks
	- [x] Creating tasks
	- [x] Editing tasks
	- [x] Deleting tasks
	- [x] Adding tasks above/below another task
	- [x] Nested tasks
	- [x] Collapsing tasks
	- [x] Re-ordering tasks with drag'n'drop
	- [x] Changing tasks' project
	- [x] Due dates
	- [x] Priorities
	- [x] Duplicating tasks
	- [x] Detailed task view
- [x] Labels
	- [x] Displaying label list
	- [x] Creating labels
	- [x] Editing labels
	- [x] Deleting labels
	- [x] Adding labels above/below another label
	- [x] Re-ordering labels with drag'n'drop
	- [x] Viewing label' tasks
	- [x] Assigning labels to tasks
- [ ] Synchronization
	- [x] Synchronizing data between clients
	- [ ] Offline changes
- [x] Filters
	- [x] Search
	- [x] Save searches as filters
	- [x] Displaying filter list 
	- [x] Editing filters
	- [x] Deleting filters
	- [x] Adding filters above/below another filter
	- [x] Re-ordering filters with drag'n'drop
	- [x] Viewing filters' search results
- [x] Habits
	- [x] Creating habits
	- [x] Editing habits
	- [x] Deleting habits
	- [x] Adding habits above/below another habits
- [x] Shared projects
	- [x] Adding collaborators to project
	- [x] Invitations
	- [x] Different permissions for viewing, completing and editing collaborative tasks
	- [x] Assigning tasks to collaborator
- [x] Keyboard shortcuts
- [ ] Enhancement
	- [ ] Gamification
	- [ ] Tasks' stats
	- [ ] Enhance inline task editor with NLP
- [ ] UI
	- [ ] Themes
	- [ ] Sections for tasks

