# CineBook - Movie Ticket Booking System
### Core Java Object-Oriented Programming (OOP) & MySQL Database Management (DBMS) College Project

**GitHub Repository:** [https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking](https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking)  
**Submission Deadline:** 05/11/2026  

---

## 📌 Project Overview
**CineBook** is an enterprise-grade console movie ticket booking application developed for cinema box-office ticketing. It demonstrates the seamless integration of:
- **Core Java OOP Paradigms:** Encapsulation, Polymorphism (method overloading), Inheritance & Interfaces (`Bookable`), Enums with state/behavior (`SeatType`), and Custom Exception Handling (`BookingException`, `InvalidCouponException`).
- **MySQL Database Management (DBMS):** 3NF Relational Schema (`movies`, `shows`, `seats`, `coupons`, `bookings`, `booking_seats`), JDBC Driver integration, Parameterized `PreparedStatement` interfaces (SQL injection immunity), and ACID transactional boundaries (`setAutoCommit(false)`, `commit()`, `rollback()`).
- **Graceful Degradation:** Auto-detects local MySQL server (`cinebook_db`). If MySQL is offline, it seamlessly falls back to in-memory mode without crashing, ensuring smooth viva evaluation in any environment.

---

## 📂 Project Structure
```
CineBook_Final_Submission/
├── src/                                  # Java source code
│   └── com/cinebook/
│       ├── app/
│       │   └── CineBook.java             # Console User Interface & Controller
│       ├── dao/
│       │   ├── DBConnection.java         # JDBC Connection Pool Manager
│       │   └── CineBookDAO.java          # CRUD Operations with Transactions
│       ├── exception/
│       │   ├── BookingException.java     # Domain validation exception
│       │   └── InvalidCouponException.java # Coupon validation exception
│       ├── model/
│       │   ├── Bookable.java             # Interface for booking lifecycle
│       │   ├── Booking.java              # Booking entity & pricing calculator
│       │   ├── BookingStatus.java        # Enum (PENDING, CONFIRMED, CANCELLED)
│       │   ├── Coupon.java               # Coupon enum with discount rules
│       │   ├── Money.java                # Financial rounding utility
│       │   ├── Seat.java                 # Coordinate & seat parser entity
│       │   ├── SeatType.java             # Enum (REGULAR, PREMIUM, VIP)
│       │   └── Show.java                 # Auditorium & seat grid model
│       └── service/
│           └── Theatre.java              # Cinema service layer
├── test/                                 # Automated test suites
│   └── com/cinebook/test/
│       ├── CineBookTest.java             # 15 Core OOP functional test cases
│       └── DatabaseCRUDTest.java         # 5 DBMS & CRUD transaction test cases
├── lib/
│   └── mysql-connector-j-8.4.0.jar       # Official MySQL JDBC Driver
├── database/
│   └── schema.sql                        # Complete MySQL DDL & DML initialization script
├── docs/                                 # Full college project report & diagrams
│   ├── CineBook_Final_Project_Report.docx # Word Report Document
│   ├── CineBook_Final_Project_Report.html # Styled HTML Report (Print to PDF via browser)
│   ├── CineBook_Final_Project_Report.md   # Markdown Report Source
│   └── diagrams/
│       ├── architecture_diagram.png      # 3-Tier Layered Architecture
│       ├── class_diagram.png             # UML Class & Interface Diagram
│       ├── use_case_diagram.png          # Use Case Diagram
│       └── booking_workflow.png          # Activity & Sequence Workflow
├── screenshots/                          # 7 Verification Screenshots
│   ├── 01_Main_Menu_And_Shows.png        # Option 1 & 2 (Shows & Movie selection)
│   ├── 02_Seat_Map_And_Booking.png       # Option 3 & 4 (5x8 Seat Map & Booking)
│   ├── 03_Coupon_Discount.png            # Option 5 (Coupon application)
│   ├── 04_Cancellation_And_Refund.png    # Option 7 (Cancellation & 80% refund)
│   ├── 05_All_Bookings_History.png       # Option 8 (Database synchronized ledger)
│   ├── 06_Input_Validation_Errors.png    # Input boundary & error checks
│   └── 07_Automated_Test_Results.png     # Full automated test run (20/20 PASS)
├── db.properties                         # MySQL connection configuration
├── run.bat                               # Double-click to compile & run on Windows
├── test.bat                              # Double-click to run all 20 tests
└── README.md                             # This documentation guide
```

---

## 🚀 How to Run the Project

### Prerequisites
1. **Java Development Kit (JDK 17 or higher):** Ensure `java` and `javac` are available in your system path.
2. **MySQL Server 8.0+ (Optional for DB Mode):** If installed, start MySQL and run the database script.

### Option 1: Quick Run (One-Click)
Simply double-click:
* `run.bat` to compile and launch the CineBook Console application.
* `test.bat` to run all 20 automated tests.

### Option 2: Command Line Run
Open PowerShell or Command Prompt inside the folder and run:
```bash
# 1. Compile with MySQL JDBC Driver
javac -cp "lib/mysql-connector-j-8.4.0.jar;src" -d bin src/com/cinebook/model/*.java src/com/cinebook/exception/*.java src/com/cinebook/dao/*.java src/com/cinebook/service/*.java src/com/cinebook/app/*.java

# 2. Run Application
java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.app.CineBook
```

### Option 3: Setting Up MySQL Database
1. Open MySQL Command Line or MySQL Workbench.
2. Execute the initialization script:
   ```sql
   source database/schema.sql;
   ```
3. If your MySQL root password is not empty, open `db.properties` and update:
   ```properties
   db.password=your_mysql_password
   ```

---

## 🧪 Automated Testing
Run `test.bat` or the following command:
```bash
javac -cp "lib/mysql-connector-j-8.4.0.jar;src;test" -d bin src/com/cinebook/model/*.java src/com/cinebook/exception/*.java src/com/cinebook/dao/*.java src/com/cinebook/service/*.java src/com/cinebook/app/*.java test/com/cinebook/test/*.java

java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.test.CineBookTest
java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.test.DatabaseCRUDTest
```
**Results:** 20 Test Cases Executed | 20 Passed | 0 Failed (100% Pass Rate).

---

## 📤 How to Upload to GitHub
To upload all project files to your repository (`https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking`):

### Method A: Upload Directly via GitHub Web (Easiest for Beginners)
1. Open [https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking](https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking) in your web browser.
2. Click **"Add file"** ➔ **"Upload files"**.
3. Select all files and folders inside `CineBook_Final_Submission` and drag them into the browser.
4. Type a commit message: `Initial commit - Complete CineBook OOPs and DBMS Project`.
5. Click **"Commit changes"**.

### Method B: Upload Using Git Command Line
Open PowerShell or Command Prompt inside `CineBook_Final_Submission`:
```bash
git init
git add .
git commit -m "Complete CineBook OOPs and DBMS Project ready for submission"
git branch -M main
git remote add origin https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking.git
git push -u origin main
```
*(Enter your GitHub username and Personal Access Token when prompted)*.
