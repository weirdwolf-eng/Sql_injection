# Sql_injection

# SQL Injection Demo (Java Swing + H2)

**A small Java Swing application that demonstrates the difference between a vulnerable SQL query built using string concatenation and a secure query using `PreparedStatement`.**

---

## Table of Contents

* [Overview](#overview)
* [Features](#features)
* [Prerequisites](#prerequisites)
* [Project Structure](#project-structure)
* [Setup & Run](#setup--run)
* [How the Demo Works](#how-the-demo-works)

  * [Database Initialization](#database-initialization)
  * [Vulnerable Mode (String Concatenation)](#vulnerable-mode-string-concatenation)
  * [Secure Mode (Prepared Statement)](#secure-mode-prepared-statement)
* [Try the SQL Injection](#try-the-sql-injection)
* [Expected Behaviour / Output](#expected-behaviour--output)
* [Security Notes & Mitigations](#security-notes--mitigations)
* [Extending the Demo](#extending-the-demo)
* [Testing Ideas](#testing-ideas)
* [License](#license)
* [Credits](#credits)

---

## Overview

This project is a small desktop application built with Java Swing that demonstrates how an insecure method of building SQL queries (string concatenation) can be exploited using SQL injection, and how using `PreparedStatement` prevents that attack. It uses an in-memory H2 database for simplicity.

The GUI allows you to switch between **Vulnerable** and **Secure** modes and attempt to log in with sample credentials.

## Features

* Simple Swing-based login form
* Toggle between vulnerable and secure login handling
* In-memory H2 database with example users
* Detailed result panel showing executed SQL (for educational purposes)

## Prerequisites

* Java Development Kit (JDK) 8 or later
* H2 Database JAR (`h2.jar`) on the classpath when running the program

  * If using an IDE (Eclipse/IntelliJ), add the H2 dependency to the project.
  * If running from command line, include `-cp path/to/h2.jar`.

Optional (recommended for building/running):

* Maven or Gradle if you want to add dependencies and build automatically (not required for this simple demo).

## Project Structure

```
SQLInjectionDemo.java        # Main Swing application (single-file demo)
README.md                    # This file
lib/h2.jar                   # (not checked in) H2 JDBC driver — add to classpath
```

> Note: The provided sample is a single Java file that initializes an in-memory H2 DB and populates it with two users: `admin/admin123` and `user/user123`.

## Setup & Run

### Using an IDE

1. Create a new Java project and copy `SQLInjectionDemo.java` into the `src` folder.
2. Add the H2 JDBC driver (h2.jar) to the project's libraries.
3. Run the `main` method of `SQLInjectionDemo`.

### Using command line (simple)

1. Place `SQLInjectionDemo.java` and `h2.jar` in the same folder (or adjust paths).
2. Compile:

```bash
javac -cp .:h2.jar SQLInjectionDemo.java
```

*(On Windows replace `:` with `;` in classpath.)*

3. Run:

```bash
java -cp .:h2.jar SQLInjectionDemo
```

The application window should open and the result panel will show `Database initialized successfully!`.

## How the Demo Works

### Database Initialization

When the application starts it calls `initDatabase()` which:

* Loads the H2 JDBC driver.
* Creates an in-memory H2 database (`jdbc:h2:mem:testdb`).
* Creates a `users` table and inserts two sample users:

  * `admin` / `admin123`
  * `user`  / `user123`

This is intentionally kept in-memory so the DB resets when the application exits.

### Vulnerable Mode (String Concatenation)

When **Vulnerable** mode is selected, the login code builds the SQL query by concatenating the raw `username` and `password` strings into the query:

```java
String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
```

Because user input is placed directly into the SQL statement, an attacker can inject SQL tokens (for example: `' OR '1'='1' --`) and alter the query logic.

The application prints the exact SQL that was executed to the result panel so you can see the effect.

### Secure Mode (Prepared Statement)

When **Secure** mode is selected, the app uses a `PreparedStatement` with placeholders and binds the user input via `setString()`:

```java
String query = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement pstmt = connection.prepareStatement(query);
pstmt.setString(1, username);
pstmt.setString(2, password);
```

This prevents user input from being interpreted as SQL code and safely treats it as data.

## Try the SQL Injection

Open the GUI, select **Vulnerable** mode, and enter the following:

* **Username:** `' OR '1'='1' --`
* **Password:** (anything)

Explanation:

* The injected username terminates the username string, adds a condition that is always true (`'1'='1'`), and the `--` comments out the rest of the SQL (the password check).

This will typically result in a query
