# CineBook - Movie Ticket Booking System & Live Website
### Core Java Object-Oriented Programming (OOP), MySQL DBMS & Responsive Web Application

[![Live Demo](https://img.shields.io/badge/Live%20Website-Demo-brightgreen?style=for-the-badge&logo=google-chrome)](https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/website/index.html?v=3.0)
[![GitHub Pages](https://img.shields.io/badge/GitHub%20Pages-Active-blue?style=for-the-badge&logo=github)](https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/)
[![Tests](https://img.shields.io/badge/Automated%20Tests-20%2F20%20PASS-success?style=for-the-badge)](test.bat)

---

### 🌐 Live Website & Demo Links
* **Live Web Application:** [https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/website/index.html?v=3.0](https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/website/index.html?v=3.0)
* **GitHub Repository:** [https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking](https://github.com/kaviyab2025cse-cmd/Movie-Ticket-Booking)
* **College Submission Deadline:** 05/11/2026

---

## 📌 Project Overview
**CineBook** is an enterprise-grade cinema ticket booking platform featuring both an interactive modern web application and a robust Core Java console back-office system:

1. **🌐 Interactive Modern Web Application (GitHub Pages):**
   - **5 Tamil Cinema Releases:** *Baththa*, *Dorothy*, *Meesaya Murukku 2*, *Jailer 2*, and *Mandaadi*.
   - **Official Poster Art:** Integrated high-definition movie posters.
   - **Interactive Seat Selection:** 5x8 auditorium grid categorized into Regular (₹120), Premium (₹180), and VIP (₹250).
   - **Discount Engine:** Promo coupon code support (`CINE20` for 20% off, `STUDENT10` for 10% off).
   - **Instant Booking Confirmation:** Interactive ticket receipt with booking ID, date, time, screen, seat tags, and print/download feature.
   - **Active Bookings Ledger:** Dynamic management ledger with real-time status and ticket cancellation.

2. **☕ Core Java OOP Paradigms:**
   - **Encapsulation:** Private domain models with immutable invariants.
   - **Polymorphism:** Method overloading for single and multi-seat allocations.
   - **Inheritance & Interfaces:** `Bookable` contract defining lifecycle operations.
   - **Enums with State/Behavior:** `SeatType` (with pricing logic) and `BookingStatus`.
   - **Custom Exceptions:** Domain exceptions (`BookingException`, `InvalidCouponException`).

3. **🗄️ MySQL Database Management (DBMS):**
   - 3NF relational schema (`movies`, `shows`, `seats`, `coupons`, `bookings`, `booking_seats`).
   - Transactional safety with ACID boundaries (`setAutoCommit(false)`, `commit()`, `rollback()`).
   - Parameterized SQL via `PreparedStatement` preventing SQL injection.
   - **Graceful In-Memory Fallback:** Automatically switches to in-memory mode if MySQL is offline during college viva presentations.

---

## 📂 Project Structure
```
CineBook_Final_Submission/
├── website/                              # Complete Responsive Web Application
│   ├── images/                           # High-Resolution Movie Posters
│   │   ├── baththa.jpeg                  # Baththa official poster
│   │   ├── dorothy.jpeg                  # Dorothy official poster
│   │   ├── meesaya_murukku_2.jpeg        # Meesaya Murukku 2 official poster
│   │   ├── jailer_2.jpeg                 # Jailer 2 official poster
│   │   └── mandaadi.jpeg                 # Mandaadi official poster
│   ├── index.html                        # Website Single Page Application
│   ├── styles.css                        # Cinema Dark Theme Stylesheet
│   └── app.js                            # Frontend Booking Engine & Modal Logic
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
├── index.html                            # Root GitHub Pages portal redirect
├── run.bat                               # Double-click to compile & run console app on Windows
├── test.bat                              # Double-click to run all 20 automated tests
└── README.md                             # Comprehensive project documentation
```

---

## 🎬 Featured Movies on Live Website
| # | Movie Title | Language | Genre | Poster |
|---|-------------|----------|-------|--------|
| 1 | **Baththa** | Tamil | Action / Drama | `website/images/baththa.jpeg` |
| 2 | **Dorothy** | Tamil | Crime / Thriller | `website/images/dorothy.jpeg` |
| 3 | **Meesaya Murukku 2** | Tamil | Musical / Comedy | `website/images/meesaya_murukku_2.jpeg` |
| 4 | **Jailer 2** | Tamil | Action / Mass Thriller | `website/images/jailer_2.jpeg` |
| 5 | **Mandaadi** | Tamil | Period / Coastal Adventure | `website/images/mandaadi.jpeg` |

---

## 🚀 How to Run the Project

### Option 1: Open the Live Website (Zero Setup)
Simply open the live URL on any browser, laptop, or mobile:
👉 **[https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/website/index.html?v=3.0](https://kaviyab2025cse-cmd.github.io/Movie-Ticket-Booking/website/index.html?v=3.0)**

---

### Option 2: Java Console Application (Local Desktop)
1. **Prerequisites:** JDK 17+ installed.
2. Double-click `run.bat` (or run in terminal):
   ```bash
   javac -cp "lib/mysql-connector-j-8.4.0.jar;src" -d bin src/com/cinebook/model/*.java src/com/cinebook/exception/*.java src/com/cinebook/dao/*.java src/com/cinebook/service/*.java src/com/cinebook/app/*.java
   java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.app.CineBook
   ```

---

### Option 3: Automated Test Execution
Double-click `test.bat` (or run in terminal):
```bash
javac -cp "lib/mysql-connector-j-8.4.0.jar;src;test" -d bin src/com/cinebook/model/*.java src/com/cinebook/exception/*.java src/com/cinebook/dao/*.java src/com/cinebook/service/*.java src/com/cinebook/app/*.java test/com/cinebook/test/*.java
java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.test.CineBookTest
java -cp "bin;lib/mysql-connector-j-8.4.0.jar" com.cinebook.test.DatabaseCRUDTest
```
**Results:** **20 Test Cases Executed | 20 Passed | 0 Failed (100% Pass Rate).**

---

### Option 4: MySQL Database Setup (Optional)
1. Open MySQL CLI or MySQL Workbench:
   ```sql
   source database/schema.sql;
   ```
2. Configure password in `db.properties`:
   ```properties
   db.password=your_mysql_password
   ```
