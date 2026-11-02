# FINAL PROJECT REPORT

# CineBook - Movie Ticket Booking System
### A Core Java Object-Oriented Application with MySQL Database Integration
**Course:** Object-Oriented Programming (OOPs) & Database Management Systems (DBMS)  
**Academic Year:** 2026 – 2027  
**Submission Deadline:** 05/11/2026  

---

## PRELIMINARY PAGES

### BONAFIDE CERTIFICATE

Certified that this project report titled **"CineBook - Movie Ticket Booking System"** is the bonafide work of the student(s) who carried out the project under supervision in partial fulfillment of the requirements for the award of the Degree of Bachelor of Engineering in Computer Science and Engineering.

\
**HEAD OF THE DEPARTMENT**  
Department of Computer Science & Engineering  
[College / University Name]  
Chennai, Tamil Nadu, India  

\
**SUPERVISOR / INTERNAL EXAMINER**  
Department of Computer Science & Engineering  
[College / University Name]  
Chennai, Tamil Nadu, India  

\
**EXTERNAL EXAMINER**  
Date: ______________  

---

### STUDENT DECLARATION

I / We hereby declare that the project entitled **"CineBook - Movie Ticket Booking System"** submitted to the Department of Computer Science and Engineering, is a record of original project work done by us under the guidance of our faculty supervisor.

This report represents our authentic work. All concepts of Object-Oriented Programming (OOP) in Java and Database Management Systems (DBMS) using MySQL have been implemented, executed, and verified by our team. Any material cited or borrowed has been referenced appropriately.

**Student Name(s) & Register Number(s):**
1. Name: _______________________ | Register No: __________________
2. Name: _______________________ | Register No: __________________

Place: Chennai  
Date: 02/11/2026  

---

### ACKNOWLEDGEMENT

We express our sincere gratitude and indebtedness to our respected Management and Principal for providing us with the state-of-the-art laboratory infrastructure and environment to carry out this project successfully.

We place on record our heartfelt thanks to our **Head of the Department**, Department of Computer Science and Engineering, for continuous encouragement, valuable suggestions, and moral support throughout this course.

We express our deepest gratitude to our course instructors and **Faculty Supervisors** for their insightful technical guidance, meticulous review, patience, and unwavering support during each development milestone of this OOPs and DBMS project.

Finally, we extend our thanks to our parents, family members, and friends for their continuous encouragement and moral support during the completion of this project work.

---

### ABSTRACT

The **CineBook - Movie Ticket Booking System** is an enterprise-grade console application engineered in Java (JDK 17+) and backed by a relational **MySQL 8.0** database via the Java Database Connectivity (JDBC) API. The project bridges theoretical Object-Oriented Programming (OOP) paradigms with practical Relational Database Management System (DBMS) principles to resolve real-world cinema ticketing bottlenecks, such as paper-based inventory management, concurrent seat conflicts, unverified coupon abuse, and manual refund calculations.

From an Object-Oriented perspective, the system incorporates core principles:
1. **Encapsulation:** Model attributes (`Show`, `Seat`, `Booking`, `Coupon`, `Money`) maintain strict private state access with defensive copying and unmodifiable collections.
2. **Inheritance & Interfaces:** The `Bookable` contract enforces strict execution workflows (`bookSeats()`, `cancelBooking()`), while custom exceptions (`BookingException`, `InvalidCouponException`) inherit from standard Java exception hierarchies.
3. **Polymorphism:** Method overloading is showcased via `calculateTotal()` accepting optional coupon codes, while dynamic method dispatch coordinates booking behavior.
4. **Enums with Behavioral Attributes:** The `SeatType` enumeration dynamically binds row categories (Regular, Premium, VIP) to base prices and display formatting.

From a DBMS perspective, the system implements a normalized 3NF relational schema consisting of `movies`, `shows`, `seats`, `coupons`, `bookings`, and `booking_seats` tables with complete primary key, foreign key, unique key, and check integrity constraints. Database interactions utilize parameter-binding `PreparedStatement` interfaces to guarantee SQL injection immunity. Atomic ticket reservations and cancellations are guarded using ACID database transactions (`setAutoCommit(false)`, `commit()`, and `rollback()`).

The system has been verified through a 20-case automated test suite (covering 15 functional OOP cases and 5 DBMS CRUD transactions) with zero failures. A graceful degradation architecture allows the system to seamlessly toggle between active MySQL persistence and in-memory fallback, ensuring testability across all demonstration environments.

---

## TABLE OF CONTENTS

- Preliminary Pages
  - Bonafide Certificate
  - Student Declaration
  - Acknowledgement
  - Abstract
- Chapter 1: Introduction
- Chapter 2: Requirements and Feasibility
- Chapter 3: System Analysis and Design
- Chapter 4: Database Design
- Chapter 5: Implementation
- Chapter 6: Verification and Test Plan
- Chapter 7: Security, Limitations, and Maintenance
- Chapter 8: Conclusion and Future Work
- References
- Appendices
  - Appendix A: SQL Schema
  - Appendix B: Output Screenshots

---

## CHAPTER 1: INTRODUCTION

### 1.1 Project Overview
Modern movie entertainment complexes require robust booking infrastructure capable of managing seating availability, multi-tier pricing, promotional campaigns, and transaction cancellations in real time. The **CineBook** system is developed to automate the box-office ticketing desk of a multiplex cinema. Built in core Java and interfaced with MySQL, it provides cinema administrators and ticketing operators with a responsive, menu-driven command-line interface.

The application allows counter agents to:
1. View all active movie schedules and seat availability across multiple screen auditoriums.
2. Interactively inspect a live $5 \times 8$ auditorium grid where available seats are rendered with `O` and reserved seats with `X`.
3. Reserve single or multiple seats in a single atomic transaction.
4. Apply verified promotional coupons (such as `CINE10`) to calculate discounts.
5. Generate formal, itemized booking receipts with uniquely sequenced alphanumeric booking identifiers (e.g., `BK1001`).
6. Cancel confirmed bookings by ID, automatically releasing reserved seats back into the available pool and issuing an exact 80% monetary refund.

### 1.2 Objectives
The primary technical and academic objectives of this project are:
1. **Core OOP Demonstration:** Implement fundamental OOP concepts—Encapsulation, Data Hiding, Inheritance, Polymorphism, Abstraction, Enums, and Interfaces—in accordance with Java industry standards.
2. **Relational Database Management:** Model, normalize, and construct a multi-table relational schema in MySQL 8.0 enforcing referential and domain integrity.
3. **Robust JDBC Data Access:** Implement a modular Data Access Object (DAO) pattern using Java Database Connectivity (JDBC), parameterized queries, and ACID transactional boundaries.
4. **Fault-Tolerant Input Validation:** Guarantee that malformed seat codes, out-of-bounds row/column values, duplicate seat choices, or taken seats fail gracefully with descriptive error prompts and zero system crashes.
5. **Comprehensive Verification:** Deliver a verified, production-ready codebase backed by an automated test suite and exhaustive documentation.

### 1.3 Scope of the Project
The scope of this project encompasses:
- Software development in Java SE 17+ and MySQL Server 8.0+.
- Full CRUD database operations: **C**reate (Insert bookings and reserve seats), **R**ead (Query movies, shows, seat maps, and transaction history), **U**pdate (Modify booking status and free seats upon cancellation), and **D**elete (Cancel booking transaction and clean up test records).
- Dual-mode architecture featuring auto-detection of local MySQL server with in-memory fallback.

---

## CHAPTER 2: REQUIREMENTS AND FEASIBILITY

### 2.1 Problem Definition
Traditional single-screen and independent cinema counters frequently rely on handwritten ledgers or disconnected spreadsheet applications. This introduces severe operational flaws:
- **Double-Booking Vulnerability:** Two ticketing clerks may assign the identical seat simultaneously.
- **Human Accounting Errors:** Manual computation of multi-tier seat prices (Regular vs. Premium vs. VIP) and percentage-based discounts often produces financial discrepancies.
- **Disorganized Refund Audit Trails:** Cancellations handled manually lack timestamps, seat release tracking, and exact audit trails.

CineBook resolves these vulnerabilities by centralizing the seat map, pricing logic, and transaction ledger within an atomic, database-backed application.

### 2.2 Functional Requirements
The system adheres to the following functional specifications:
- **FR-1 (Show Scheduling):** Display a minimum of 3 distinct movie shows with screening times and available seat counts.
- **FR-2 (Live Visual Seat Map):** Present a $5 \times 8$ grid ($40$ seats per screen) indicating available (`O`) and booked (`X`) seats.
- **FR-3 (Multi-Tier Categorized Pricing):** Implement tiered pricing via Java Enums: Rows A–B as Regular (₹150.00), Rows C–D as Premium (₹250.00), and Row E as VIP (₹400.00).
- **FR-4 (Multi-Seat Atomic Booking):** Allow customers to book multiple comma-separated seats (e.g., `C3, C4`). If any seat is taken or invalid, the transaction aborts with all-or-nothing atomicity.
- **FR-5 (Booking Identification & Receipt):** Assign unique, sequential alphanumeric identifiers (`BK1001`, `BK1002`, ...) and display itemized receipts.
- **FR-6 (Coupon Discount Management):** Support promotional codes (e.g., `CINE10` granting 10% discount) through method overloading.
- **FR-7 (Cancellation & Refund Processing):** Allow cancellations by booking ID, restoring seat availability and computing an exact 80% refund.
- **FR-8 (Auditorium Isolation):** Ensure seat allocations for one show do not conflict with or affect any other show.

### 2.3 Non-Functional Requirements
- **Performance:** Seat map rendering and booking queries execute in under 100 milliseconds.
- **Data Integrity & Consistency:** Database tables maintain referential integrity via Foreign Keys with cascading updates and restricted deletes.
- **Reliability:** The application handles all unexpected inputs gracefully without runtime crashes.
- **Portability:** Built on standard Java SE and cross-platform MySQL 8.0, allowing execution across Windows, Linux, and macOS.

### 2.4 Hardware & Software Requirements
- **Operating System:** Windows 10/11, Ubuntu 22.04 LTS, or macOS 12+
- **Processor:** Intel Core i3 / AMD Ryzen 3 or higher
- **RAM:** 4 GB minimum (8 GB recommended)
- **Disk Space:** 500 MB free space
- **Java Development Kit (JDK):** JDK 17 or JDK 21+ (Tested on OpenJDK 25 HotSpot)
- **Database Server:** MySQL Server 8.0 or MariaDB 10.5+
- **Database Driver:** `mysql-connector-j-8.4.0.jar`
- **IDE / Editor:** Visual Studio Code, Eclipse, IntelliJ IDEA, or Command-line Terminal

### 2.5 Feasibility Analysis
- **Technical Feasibility:** The project employs proven, standard technologies: Java SE and MySQL. Java’s strict typing, object model, and native JDBC API provide complete technical capability.
- **Operational Feasibility:** The console interface requires zero graphical overhead or external browser dependencies. Operators can learn the 9-option menu in minutes.
- **Economic Feasibility:** The entire stack is constructed using open-source, zero-cost tools (Adoptium OpenJDK, MySQL Community Server, and Visual Studio Code).

---

## CHAPTER 3: SYSTEM ANALYSIS AND DESIGN

### 3.1 Existing System vs. Proposed System

| Parameter | Existing System (Manual / Paper) | Proposed System (CineBook) |
| :--- | :--- | :--- |
| **Seat Map Visibility** | Static paper charts / mental count | Dynamic live $5 \times 8$ console grid with real-time updates |
| **Concurrency & Double Booking**| High risk of duplicate seat assignments | Zero risk; atomic transactions prevent double booking |
| **Pricing Calculation** | Mental arithmetic or manual calculators | Automated calculations based on `SeatType` enum prices |
| **Discount Handling** | Prone to unverified or unauthorized discounts | Validated via `Coupon` parser with fixed percentage rules |
| **Audit & Record Keeping** | Physical receipts, missing records | Relational MySQL ledger tracking timestamps and status |
| **Cancellation & Refund** | Manual, inconsistent refund policies | Automated 80% refund computation with instant seat release |

### 3.2 System Architecture
CineBook is designed following a **3-Tier Layered Architecture**:
1. **Presentation Layer (`com.cinebook.app`):** `CineBook.java` handles console I/O, user prompts, menu loops, and formatted tabular rendering.
2. **Business Logic / Service Layer (`com.cinebook.service` & `com.cinebook.model`):**
   - `Theatre.java`: Coordinates showtimes, seat selections, and ticket requests.
   - Domain models: `Show`, `Seat`, `Booking`, `Coupon`, `Money`, `SeatType`, `BookingStatus`, and `Bookable`.
3. **Data Access Layer (`com.cinebook.dao`):**
   - `DBConnection.java`: Manages the MySQL database connection pool.
   - `CineBookDAO.java`: Implements JDBC CRUD operations via parameter-bound SQL statements.

```
+-------------------------------------------------------------------+
|               PRESENTATION LAYER (Console UI)                     |
|                      com.cinebook.app.CineBook                    |
+-------------------------------------------------------------------+
                                 |
                                 v
+-------------------------------------------------------------------+
|                   BUSINESS LOGIC & SERVICE LAYER                  |
|  com.cinebook.service.Theatre   |   com.cinebook.model.*          |
|  - Show Coordination            |   - Booking, Show, Seat         |
|  - Pricing & Coupon Valuation   |   - Bookable Interface          |
+-------------------------------------------------------------------+
                                 |
                                 v
+-------------------------------------------------------------------+
|                     DATA ACCESS LAYER (DAO)                       |
|                 com.cinebook.dao.CineBookDAO                      |
|                 com.cinebook.dao.DBConnection                     |
+-------------------------------------------------------------------+
                                 |
                                 v
+-------------------------------------------------------------------+
|                     PERSISTENCE LAYER (DBMS)                      |
|                   MySQL 8.0 Server (cinebook_db)                  |
|    Tables: movies | shows | seats | coupons | bookings | seats    |
+-------------------------------------------------------------------+
```

### 3.3 Use Case Analysis
The system identifies two primary user roles:
1. **Customer / Ticket Buyer:**
   - Browse movie catalog and schedules (Option 1).
   - Select movie and preferred showtime (Option 2).
   - View visual seat map (Option 3).
   - Book seats (Option 4).
   - Apply discount coupon (Option 5).
   - View booking confirmation receipt (Option 6).
   - Request cancellation and receive refund (Option 7).
2. **Cinema Administrator / Box Office Manager:**
   - Audit all current and cancelled bookings (Option 8).
   - Verify transaction status and revenue receipts.

### 3.4 Object-Oriented Design Principles
- **Encapsulation:** All model instance variables are declared `private final`. State mutation is governed strictly through validated methods (e.g., `reserveSeats()` in `Show`).
- **Data Hiding & Defensive Copying:** `getSeats()` returns `Collections.unmodifiableList(seats)`, preventing external modification of internal collections.
- **Inheritance & Polymorphism:**
  - `Booking` implements the `Bookable` interface, providing concrete implementations for `bookSeats()` and `cancelBooking()`.
  - Overloaded methods: `calculateTotal()` computes base price, while `calculateTotal(String couponCode)` computes discounted pricing.
- **Enums with State & Behavior:** `SeatType` defines `REGULAR(150.0)`, `PREMIUM(250.0)`, and `VIP(400.0)` with immutable price fields and static row-mapping methods (`forRow(char letter)`).

---

## CHAPTER 4: DATABASE DESIGN

### 4.1 Database Overview
The database layer is implemented on **MySQL 8.0 Server** using the InnoDB storage engine, which provides full ACID (Atomicity, Consistency, Isolation, Durability) transaction support and foreign key constraints. The database name is **`cinebook_db`**.

### 4.2 Entity-Relationship (ER) Model
The schema comprises five primary relational entities and one associative bridge table:
1. **`movies` (Strong Entity):** Stores master movie catalog records.
2. **`shows` (Weak Entity depending on `movies`):** Represents specific auditorium screenings.
3. **`seats` (Entity):** Stores all 40 individual seats per show with real-time boolean availability flags.
4. **`coupons` (Entity):** Stores authorized promotional voucher codes and percentages.
5. **`bookings` (Entity):** Master audit table for all completed ticketing transactions.
6. **`booking_seats` (Associative Bridge Entity):** Maps multi-seat reservations to parent booking records in accordance with First Normal Form.

### 4.3 Data Dictionary & Relational Tables

#### 1. Table: `movies`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `movie_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for movie |
| `title` | VARCHAR(150) | NOT NULL, UNIQUE | Title of the movie |
| `duration_mins` | INT | NOT NULL | Movie runtime in minutes |
| `genre` | VARCHAR(50) | NOT NULL | Classification genre |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record insertion timestamp |

#### 2. Table: `shows`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `show_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for screening |
| `movie_id` | INT | FOREIGN KEY (`movies.movie_id`) | Referenced movie identifier |
| `show_time` | TIME | NOT NULL | Screening start time |
| `theatre_name` | VARCHAR(100)| NOT NULL | Cinema venue name |

#### 3. Table: `seats`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `seat_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique seat record identifier |
| `show_id` | INT | FOREIGN KEY (`shows.show_id`) | Show association |
| `seat_code` | VARCHAR(5) | NOT NULL | Alphanumeric seat code (e.g., C3) |
| `row_letter` | CHAR(1) | CHECK IN ('A','B','C','D','E') | Row letter |
| `column_number` | INT | CHECK (1 TO 8) | Column number |
| `seat_type` | VARCHAR(20) | NOT NULL | REGULAR, PREMIUM, or VIP |
| `price` | DECIMAL(8,2) | NOT NULL | Seat price in INR |
| `is_booked` | BOOLEAN | NOT NULL DEFAULT FALSE | Availability flag (0: Free, 1: Booked)|

#### 4. Table: `coupons`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `coupon_code` | VARCHAR(20) | PRIMARY KEY | Voucher code (e.g., CINE10) |
| `discount_percent`| INT | CHECK (1 TO 100) | Percentage discount value |
| `description` | VARCHAR(150)| NOT NULL | User explanation text |

#### 5. Table: `bookings`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `booking_id` | VARCHAR(15) | PRIMARY KEY | Unique booking ID (e.g., BK1001) |
| `show_id` | INT | FOREIGN KEY (`shows.show_id`) | Associated screening |
| `movie_title` | VARCHAR(150)| NOT NULL | Cached movie title |
| `show_time_str`| VARCHAR(20) | NOT NULL | Formatted time text |
| `seat_codes` | VARCHAR(100)| NOT NULL | Comma-separated booked seats |
| `base_amount` | DECIMAL(8,2)| NOT NULL | Gross total price before discount |
| `coupon_code` | VARCHAR(20) | FOREIGN KEY (`coupons.coupon_code`)| Optional applied coupon |
| `discount_amount`| DECIMAL(8,2)| NOT NULL DEFAULT 0.00 | Monetary discount applied |
| `amount_paid` | DECIMAL(8,2)| NOT NULL | Final net amount collected |
| `status` | VARCHAR(20) | CHECK IN ('CONFIRMED','CANCELLED')| Transaction lifecycle state |
| `refund_amount`| DECIMAL(8,2)| NOT NULL DEFAULT 0.00 | Amount refunded on cancellation |
| `booking_time` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Audit timestamp |

#### 6. Table: `booking_seats`
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `booking_id` | VARCHAR(15) | FOREIGN KEY (`bookings.booking_id`)| Associated booking |
| `seat_code` | VARCHAR(5) | NOT NULL | Individual seat code |
| *Composite PK*| (`booking_id`, `seat_code`) | PRIMARY KEY | Uniqueness across reservation |

### 4.4 Normalization Analysis
1. **First Normal Form (1NF):** All attributes are atomic. Multi-value seat codes are decomposed via the relational bridge table `booking_seats`.
2. **Second Normal Form (2NF):** Every non-key attribute is fully functionally dependent on the entire primary key, eliminating partial dependencies.
3. **Third Normal Form (3NF):** Transitive dependencies have been eliminated by isolating movie properties into `movies` and pricing/categories into `seats`.

---

## CHAPTER 5: IMPLEMENTATION

### 5.1 Technology Stack
- **Programming Language:** Java SE 17+ (Compiled on OpenJDK 25)
- **Database Engine:** MySQL Server 8.0 Community Edition
- **Database Driver:** MySQL Connector/J 8.4.0 (`mysql-connector-j-8.4.0.jar`)
- **Build & Execution Scripts:** Windows Command Scripts (`run.bat`, `test.bat`)

### 5.2 Package and File Structure
The project is organized in modular Java packages:
```
CineBook_Final_Submission/
├── src/
│   └── com/cinebook/
│       ├── app/
│       │   └── CineBook.java             (Menu UI & Controller)
│       ├── dao/
│       │   ├── DBConnection.java         (JDBC Connection Manager)
│       │   └── CineBookDAO.java          (CRUD JDBC Operations)
│       ├── exception/
│       │   ├── BookingException.java     (Domain Validation Exception)
│       │   └── InvalidCouponException.java (Coupon Validation Exception)
│       ├── model/
│       │   ├── Bookable.java             (Interface for Booking lifecycle)
│       │   ├── Booking.java              (Booking transaction entity)
│       │   ├── BookingStatus.java        (Enum: PENDING, CONFIRMED, CANCELLED)
│       │   ├── Coupon.java               (Coupon Enum: CINE10)
│       │   ├── Money.java                (Currency rounding utility)
│       │   ├── Seat.java                 (Immutable Grid Coordinate entity)
│       │   ├── SeatType.java             (Enum: REGULAR, PREMIUM, VIP)
│       │   └── Show.java                 (Auditorium & Seat Grid Model)
│       └── service/
│           └── Theatre.java              (Business Logic & Dispatcher)
├── test/
│   └── com/cinebook/test/
│       ├── CineBookTest.java             (15 Functional OOP Test Cases)
│       └── DatabaseCRUDTest.java         (5 DBMS & CRUD Test Cases)
├── lib/
│   └── mysql-connector-j-8.4.0.jar       (Official MySQL JDBC Driver)
├── database/
│   └── schema.sql                        (Complete MySQL DDL & DML Script)
├── docs/                                 (Project Documentation & Diagrams)
├── screenshots/                          (Output Demonstration Captures)
├── db.properties                         (Database Configuration Settings)
├── run.bat                               (One-click Compile & Run Script)
└── test.bat                              (One-click Automated Test Script)
```

### 5.3 Implementation of Core Java OOP Concepts
1. **Encapsulation:** Model classes keep fields strictly private. Accessors expose immutable values or defensive copies.
2. **Polymorphism:** Method overloading is implemented in `calculateTotal()` within `Booking.java`:
   ```java
   public double calculateTotal() {
       double total = 0;
       for (Seat seat : seats) {
           total += seat.getSeatType().getPrice();
       }
       return Money.round(total);
   }

   public double calculateTotal(String couponCode) throws InvalidCouponException {
       return applyDiscount(calculateTotal(), Coupon.parseOptional(couponCode));
   }
   ```
3. **Abstraction & Interfaces:** The `Bookable` interface declares contract methods:
   ```java
   public interface Bookable {
       void bookSeats() throws BookingException;
       double cancelBooking() throws BookingException;
   }
   ```
4. **Enum Types with Associated Values:** The `SeatType` enumeration encapsulates price rules:
   ```java
   public enum SeatType {
       REGULAR(150.00, "Regular"),
       PREMIUM(250.00, "Premium"),
       VIP(400.00, "VIP");

       private final double price;
       private final String displayName;
       // ... Constructor & Getters
   }
   ```

### 5.4 Implementation of DBMS & JDBC CRUD Operations
In `com.cinebook.dao.CineBookDAO`, JDBC operations are implemented with `PreparedStatement` and transactional control:

- **CREATE (Insert Booking & Reserve Seats):**
  ```java
  conn.setAutoCommit(false); // Begin Transaction
  try (PreparedStatement ps = conn.prepareStatement(insertBookingSql)) {
      ps.setString(1, booking.getBookingId());
      // ... bind parameters
      ps.executeUpdate();
  }
  try (PreparedStatement psUpdate = conn.prepareStatement(updateSeatSql)) {
      for (Seat seat : booking.getSeats()) {
          psUpdate.setInt(1, showId);
          psUpdate.setString(2, seat.getCode());
          psUpdate.executeUpdate();
      }
  }
  conn.commit(); // Commit Transaction
  ```
- **READ (Query Shows, Seat Availability, and Records):**
  Queries data from `shows`, `seats`, and `bookings` to populate live displays and receipts.
- **UPDATE (Cancellation & Seat Release):**
  Updates the booking status to `CANCELLED`, computes an 80% refund, and resets `is_booked = FALSE` in `seats`.
- **DELETE (Record Cleanup):**
  Provides administrative deletion methods for test cleanup while preserving historical integrity in production via soft deletion.

---

## CHAPTER 6: VERIFICATION AND TEST PLAN

### 6.1 Testing Methodology
The CineBook application was evaluated using a two-tier verification architecture:
1. **Unit & Functional OOP Testing (`CineBookTest.java`):** Automated testing of business logic, grid boundaries, exception handling, and coupon discounts across 15 automated test cases.
2. **Database & CRUD Transaction Testing (`DatabaseCRUDTest.java`):** Automated verification of MySQL connectivity, query execution, transaction atomicity, and rollback integrity across 5 test cases.

### 6.2 Test Cases and Results

| Test ID | Test Description | Input / Action | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Successful Single Booking | Show 1, Seat `C3`, No Coupon | Booking confirmed, ID generated, seat marked booked | Confirmed, `BK1001`, `C3` booked | **PASS** |
| **TC-02** | Booking Multiple Seats | Show 1, Seats `C3, C4` | Total ₹500, both seats booked | Confirmed, Total ₹500.00 | **PASS** |
| **TC-03** | Occupied Seat Rejection | Book `C3` when already booked | `BookingException` thrown, no seat reserved | Exception caught, transaction rejected | **PASS** |
| **TC-04** | Invalid Seat Coordinate | Seat code `Z9` or `A9` | `BookingException` with descriptive message | Rejected with validation error | **PASS** |
| **TC-05** | Valid Coupon Application | Coupon `CINE10` on ₹500 booking | 10% discount applied, net payable ₹450 | ₹50 discount, ₹450 collected | **PASS** |
| **TC-06** | Invalid Coupon Code | Coupon code `INVALID99` | `InvalidCouponException` thrown | Rejected with error prompt | **PASS** |
| **TC-07** | Successful Cancellation | Cancel confirmed `BK1001` | Status `CANCELLED`, 80% refund issued, seats freed | 80% refund calculated, seats available | **PASS** |
| **TC-08** | Malformed Booking ID | Lookup ID `BK9999` or `XYZ` | `BookingException` indicating not found | Descriptive error displayed | **PASS** |
| **TC-09** | Double Cancellation Block | Cancel already cancelled booking | Second cancellation rejected, zero double refund | Rejected: "Already cancelled" | **PASS** |
| **TC-10** | Show Isolation Verification | Book `C3` in Show 1; check Show 2 | `C3` in Show 2 remains completely available | Show 2 grid unimpacted | **PASS** |
| **TC-11** | Duplicate Seat in Request | Input `C3, C3` in one request | Rejected; no duplicate reservation allowed | Aborted before processing | **PASS** |
| **TC-12** | Tiered Price Verification | Check Row A (Regular), C (Prem), E (VIP) | ₹150, ₹250, ₹400 respectively | Accurate price mapping verified | **PASS** |
| **TC-13** | Sequential Unique IDs | Book consecutive tickets | IDs follow `BK1001`, `BK1002`, `BK1003` | Sequentially incremented IDs | **PASS** |
| **TC-14** | Empty Input Handling | Press Enter without input | Prompt for re-entry without crashing | User re-prompted cleanly | **PASS** |
| **TC-15** | Method Overloading Test | Test `calculateTotal()` variants | Base price and discounted price match | Overloaded methods return correct totals | **PASS** |
| **TC-16** | DBMS Connection Test | Ping MySQL via `DBConnection` | Valid connection or graceful memory fallback | Verified and connected | **PASS** |
| **TC-17** | DBMS READ Operations | Query shows and 40 seats via JDBC | All 4 shows and 40 seats loaded | Loaded correctly via ResultSet | **PASS** |
| **TC-18** | DBMS CREATE Transaction | Execute atomic multi-seat booking | Records written to `bookings` & `seats` | Transaction committed successfully | **PASS** |
| **TC-19** | DBMS UPDATE Operation | Cancel booking via JDBC | Status updated to `CANCELLED`, seats freed | Updated with ACID commit | **PASS** |
| **TC-20** | Transaction Rollback Check| Fail half-way through booking | Rollback restores original state | Rollback verified, 0 phantom seats | **PASS** |

**Summary: 20 Test Cases Executed | 20 Passed | 0 Failed (100% Success Rate).**

---

## CHAPTER 7: SECURITY, LIMITATIONS, AND MAINTENANCE

### 7.1 Security Measures
1. **SQL Injection Immunity:** All database communication strictly uses parameterized `PreparedStatement` objects with typed setters (`setInt`, `setString`, `setDouble`). Zero string concatenation is employed for SQL execution.
2. **Encapsulation & Immutability:** Sensitive business logic and pricing fields are protected from unauthorized external modification via immutable classes and unmodifiable collection wrappers.
3. **Transaction Safety:** Financial and seat updates are executed within atomic database transactions with automatic rollback in the event of any runtime failure.

### 7.2 System Limitations
1. **Console Interface:** The system currently operates via a command-line interface, which lacks graphical seat selection controls.
2. **Single-Node Execution:** The application is architected for single-terminal box-office use. Distributed multi-terminal deployment would require centralized connection pooling and distributed row locks.
3. **Payment Gateway Integration:** Payment settlement is currently simulated; real-world deployment requires integration with UPI or credit card payment gateways.

### 7.3 Maintenance and Support Plan
- **Database Backup:** Periodic logical exports using `mysqldump`:
  ```bash
  mysqldump -u root -p cinebook_db > cinebook_backup.sql
  ```
- **Configuration Management:** Database credentials, ports, and URLs are isolated in `db.properties`, allowing environment changes without recompilation.

---

## CHAPTER 8: CONCLUSION AND FUTURE WORK

### 8.1 Conclusion
The **CineBook - Movie Ticket Booking System** satisfies all academic requirements outlined for the OOPs and DBMS Final Project. It demonstrates mastery of core Java Object-Oriented principles (Encapsulation, Inheritance, Polymorphism, Abstraction, Enums, and Custom Exceptions) alongside practical relational database design and management (MySQL 8.0, 3NF Normalization, Referential Integrity, JDBC Driver Integration, and ACID Transactions).

The software is fully functional, verified through 20 automated test cases, and packaged with build and execution scripts ready for college laboratory demonstration and final evaluation.

### 8.2 Future Enhancements
1. **Web and Mobile Frontend:** Develop a responsive web UI using React or JavaFX to render an interactive graphical seat layout.
2. **Digital Payment Gateway:** Integrate third-party payment APIs (Razorpay, Stripe) to support real-time digital ticket purchasing.
3. **Automated Notification Services:** Implement JavaMail API and SMS/WhatsApp APIs to dispatch PDF e-tickets and QR-code boarding passes directly to patrons.

---

## REFERENCES

1. Herbert Schildt, *"Java: The Complete Reference"*, 12th Edition, McGraw-Hill Education, 2021.
2. Abraham Silberschatz, Henry F. Korth, and S. Sudarshan, *"Database System Concepts"*, 7th Edition, McGraw-Hill, 2019.
3. Oracle Corporation, *"Java SE 17 & 21 Platform Documentation"*, https://docs.oracle.com/en/java/javase/
4. MySQL AB / Oracle Corporation, *"MySQL 8.0 Reference Manual"*, https://dev.mysql.com/doc/refman/8.0/en/
5. Joshua Bloch, *"Effective Java"*, 3rd Edition, Addison-Wesley Professional, 2018.

---

## APPENDICES

### APPENDIX A: SQL SCHEMA DDL & DML
*(See `database/schema.sql` for the full executable script)*

```sql
CREATE DATABASE IF NOT EXISTS cinebook_db;
USE cinebook_db;

CREATE TABLE movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL UNIQUE,
    duration_mins INT NOT NULL,
    genre VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE shows (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    show_time TIME NOT NULL,
    theatre_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_code VARCHAR(5) NOT NULL,
    row_letter CHAR(1) NOT NULL,
    column_number INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    price DECIMAL(8, 2) NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE CASCADE,
    UNIQUE KEY uk_show_seat (show_id, seat_code)
) ENGINE=InnoDB;

CREATE TABLE coupons (
    coupon_code VARCHAR(20) PRIMARY KEY,
    discount_percent INT NOT NULL,
    description VARCHAR(150) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE bookings (
    booking_id VARCHAR(15) PRIMARY KEY,
    show_id INT NOT NULL,
    movie_title VARCHAR(150) NOT NULL,
    show_time_str VARCHAR(20) NOT NULL,
    seat_codes VARCHAR(100) NOT NULL,
    base_amount DECIMAL(8, 2) NOT NULL,
    coupon_code VARCHAR(20) NULL,
    discount_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(8, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    refund_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(show_id)
) ENGINE=InnoDB;
```

---

### APPENDIX B: OUTPUT SCREENSHOTS & DESCRIPTIONS

#### Figure B.1: System Architecture Diagram
*Illustrates the 3-Tier Layered Architecture decoupling Console Presentation, Business Domain Services, and MySQL Persistence.*  
*(Refer to `docs/diagrams/architecture_diagram.png`)*

#### Figure B.2: Class & Interface Diagram
*Detailed UML diagram showing `Show`, `Seat`, `Booking`, `Coupon`, and the `Bookable` interface.*  
*(Refer to `docs/diagrams/class_diagram.png`)*

#### Figure B.3: Use Case Diagram
*Actor interactions for Customer browsing, booking, coupon redemption, and cancellation.*  
*(Refer to `docs/diagrams/use_case_diagram.png`)*

#### Figure B.4: Booking & Cancellation Workflow
*Activity flow detailing input validation, seat checks, and refund computation.*  
*(Refer to `docs/diagrams/booking_workflow.png`)*

#### Figure B.5: Main Menu & Active Movie Schedules (READ)
*Console demonstration of Option 1 and Option 2 showing available movie listings and showtimes.*  
*(Refer to `screenshots/01_Main_Menu_And_Shows.png`)*

#### Figure B.6: Live 5x8 Seat Map & Multi-Seat Reservation (CREATE)
*Interactive $5 \times 8$ grid display and generation of confirmed booking receipt `BK1001`.*  
*(Refer to `screenshots/02_Seat_Map_And_Booking.png`)*

#### Figure B.7: Coupon Discount Validation
*Validation and 10% discount deduction using promotional code `CINE10`.*  
*(Refer to `screenshots/03_Coupon_Discount.png`)*

#### Figure B.8: Booking Cancellation & 80% Refund Calculation (UPDATE)
*Cancellation of `BK1001` with immediate seat release and 80% monetary refund computation.*  
*(Refer to `screenshots/04_Cancellation_And_Refund.png`)*

#### Figure B.9: Audit Log & All Bookings History (READ)
*Tabular report of all confirmed and cancelled bookings with database synchronization.*  
*(Refer to `screenshots/05_All_Bookings_History.png`)*

#### Figure B.10: Fault-Tolerant Input Validation
*Handling of invalid seat codes, duplicate inputs, and already-booked seats without system failure.*  
*(Refer to `screenshots/06_Input_Validation_Errors.png`)*

#### Figure B.11: Automated Test Suite Execution (20 Test Cases Passing)
*Full execution of OOP and DBMS automated verification test suites demonstrating 100% pass rate.*  
*(Refer to `screenshots/07_Automated_Test_Results.png`)*

---
*End of Report*
