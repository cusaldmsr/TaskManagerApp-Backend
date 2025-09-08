# TaskManager

> A Java EE web application for managing tasks, users, and task statuses, using Hibernate ORM and MySQL. This project provides a RESTful API for task management, user authentication, and status tracking, suitable for integration with modern frontend frameworks.

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Folder Structure](#folder-structure)
- [API Endpoints](#api-endpoints)
- [Setup & Installation](#setup--installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Project Overview

TaskManager is a backend application designed to manage tasks, users, and task statuses. It exposes RESTful endpoints for CRUD operations on tasks, user registration/login, and status retrieval. The backend is built with Java Servlets, Hibernate ORM, and MySQL, and is ready to be deployed on a Java EE server (e.g., GlassFish).

### Features

- User registration and login
- Task CRUD (Create, Read, Update, Delete)
- Task status management
- Hibernate ORM for database operations
- RESTful API with JSON responses
- CORS enabled for frontend integration

## Tech Stack

- **Java EE** (Servlets)
- **Hibernate ORM**
- **MySQL** (Database)
- **Gson** (JSON serialization)
- **GlassFish** (or compatible Java EE server)
- **Maven/Ant** (build tools)

## Folder Structure

```
TaskManager/
├── build.xml                # Ant build script
├── README.md                # Project documentation
├── src/
│   ├── conf/
│   │   └── MANIFEST.MF      # Manifest file
│   └── java/
│       ├── hibernate.cfg.xml    # Hibernate configuration
│       ├── controller/          # Servlet controllers (Task, User, TaskStatus)
│       └── hibernate/           # Entity classes (User, Task, TaskStatus, HibernateUtil)
├── web/
│   └── WEB-INF/
│       ├── glassfish-web.xml    # GlassFish deployment descriptor
│       └── ...
├── lib/                    # External libraries (JARs)
├── nbproject/              # NetBeans project files
└── test/                   # Test sources (if any)
```

## API Endpoints

### User

- `POST /api/user/login` — User login
- `POST /api/user/register` — User registration

### Tasks

- `GET /api/tasks` — Get all tasks
- `GET /api/tasks/{id}` — Get task by ID
- `GET /api/tasks/user/{userId}` — Get tasks by user
- `POST /api/tasks` — Create a new task
- `PUT /api/tasks/{id}` — Update a task
- `DELETE /api/tasks/{id}` — Delete a task

### Task Status

- `GET /api/taskstatus` — Get all task statuses

## Setup & Installation

1. **Clone the repository:**
   ```sh
   git clone https://github.com/cusaldmsr/TaskManagerApp-Backend.git
   ```
2. **Configure the database:**
   - Create a MySQL database named `task-manager`.
   - Update `src/java/hibernate.cfg.xml` with your DB credentials if needed.
3. **Build the project:**
   - Use NetBeans, Ant, or your preferred Java IDE to build the project.
4. **Deploy:**
   - Deploy the generated WAR file to GlassFish or another Java EE server.

## Usage

- Use tools like Postman or a frontend app to interact with the API endpoints.
- Ensure the server is running and accessible at the configured base URL.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.
