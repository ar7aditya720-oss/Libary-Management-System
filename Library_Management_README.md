# 📚 Library Management System

A desktop-based **Library Management System built with Java Swing** for managing books, members, book circulation, and overdue fines through a graphical user interface.

> **🤖 AI-Assisted Project:** This project was developed with assistance from AI tools for coding support, debugging, implementation guidance, and documentation. The developer was responsible for the project requirements, integration, testing, debugging, and final implementation decisions.

## ✨ Features

### 🔐 Admin Login
- Login screen for accessing the application.
- Default testing credentials:
```text
Username: admin
Password: admin
```

### 📊 Dashboard
Provides an overview of:
- Total books
- Total members
- Issued books
- Overdue books

### 📚 Book Management
- Add books
- Edit book details
- Delete books
- Search books
- Manage multiple copies

### 👥 Member Management
- Add members
- Edit member details
- Delete members
- Search members

### 🔄 Issue & Return Books
- Issue books to members
- 14-day loan period
- Return issued books
- Track book availability
- Detect overdue returns

### 💰 Overdue Fine
The system calculates overdue fines at:

```text
₹5 per overdue day
```

Example:
```text
5 overdue days × ₹5 = ₹25 fine
```

### 💾 Automatic Data Persistence
Data is automatically stored locally in:

```text
data\library.dat
```

This allows records to persist between application sessions.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core application development |
| Java Swing | Desktop graphical interface |
| Object-Oriented Programming | Application structure |
| File I/O / Serialization | Local data persistence |
| PowerShell | Compilation and execution |

## 🧠 Java Concepts Demonstrated

- Classes and Objects
- Encapsulation
- Methods and Constructors
- Collections
- GUI/Event-driven programming
- Date handling
- File handling
- Data persistence
- CRUD operations
- Application state management

## 📁 Project Location

```text
Desktop\java mini projects\library management
```

## ▶️ How to Run

### 1. Open the Project Folder

```powershell
cd "Desktop\java mini projects\library management"
```

### 2. Compile the Project

```powershell
javac -d out (Get-ChildItem -Recurse src\library\*.java).FullName
```

### 3. Run the Application

```powershell
java -cp out library.Main
```

## 🔑 Login Credentials

```text
Username: admin
Password: admin
```

## 📋 Library Rules

| Rule | Value |
|---|---:|
| Loan duration | 14 days |
| Overdue fine | ₹5/day |
| Data storage | `data\library.dat` |

## 🎯 Project Objectives

The project was created to apply core Java concepts to a practical real-world application.

Main objectives:
- Practice Java programming
- Apply Object-Oriented Programming
- Build a GUI using Java Swing
- Implement CRUD operations
- Manage books and members
- Implement an issue/return workflow
- Handle dates and overdue calculations
- Learn local data persistence
- Understand how multiple Java classes work together

## 🤖 AI-Assisted Development

AI tools were used as a **development assistant** during this project.

AI assistance was used for:
- Understanding Java concepts and implementation approaches
- Generating and refining code
- Debugging and identifying errors
- Suggesting improvements to application structure
- Assisting with GUI implementation
- Improving code readability
- Creating and refining documentation

The developer remained responsible for:
- Defining project requirements
- Choosing the features
- Integrating assisted code
- Testing the application
- Debugging issues
- Making implementation decisions
- Reviewing the final project

**AI was used as a development aid, not as a replacement for understanding and testing the project.**

## 🚀 Future Improvements

- 🔐 Improved authentication and password security
- 👤 Multiple user roles such as Admin and Librarian
- 🔎 Advanced search, filtering, and sorting
- 📊 Detailed reports and analytics
- 🧾 Printable issue/return receipts
- 📧 Overdue email notifications
- 🗄️ MySQL/PostgreSQL database integration
- 📈 Library usage statistics
- 🔄 Automatic backup and restore
- 🖼️ Book cover/image support

## 📌 Project Status

**Status: Completed / Functional**

Current version includes:

- ✅ Admin login
- ✅ Dashboard
- ✅ Book management
- ✅ Member management
- ✅ Book issuing
- ✅ Book returns
- ✅ Overdue tracking
- ✅ Fine calculation
- ✅ Local data persistence

## 👨‍💻 Author

**Aditya**

A Java project created to strengthen practical programming, Object-Oriented Programming, GUI development, and application-building skills.

## 📄 License

This project is intended for educational and portfolio purposes.
