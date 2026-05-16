# Library Management System

Welcome to my Library Management System! I built this project to manage everyday library operations like keeping track of books, registering members, and handling book loans. 

It's a full-stack Java application that bundles both a backend REST API and a frontend web interface into a single executable file.

## What it does
* 📚 **Manage Books**: Add new books to the catalog, track ISBNs, and monitor how many copies are available versus checked out.
* 👥 **Manage Members**: Register new library members with their contact info.
* 🔄 **Track Loans**: Issue books to members, calculate when they are due back, and process returns.

## Tech Stack
* **Backend**: Java 17, Spark Java Framework, Maven
* **Database**: MySQL (via standard JDBC)
* **Frontend**: HTML, CSS, and JavaScript

## How to run it locally

### 1. Database Setup
First, make sure you have MySQL running locally on port 3306.
1. Open your MySQL client and execute the `src/main/resources/schema.sql` file to create the database and tables.
2. Update `src/main/resources/config.properties` with your MySQL username and password (it defaults to `root`/`root`).

### 2. Build the project
Open your terminal in the project folder and run Maven to build the application:
```bash
.\mvnw.cmd clean package
```

### 3. Run the application
To start the web server, run the following command:
```bash
java -jar target\library-management-1.0.0-shaded.jar web
```
*(Note: If port 8080 is already in use on your machine, you can specify a different port by adding it to the end, like this: `java -jar target\library-management-1.0.0-shaded.jar web 8081`)*

Finally, open your browser and go to `http://localhost:8080` to use the application!
