<div align="center">

# 🌍 TerraGauge

**An offline-first environmental activity ledger and carbon audit platform — built with Core Java, JDBC, and MySQL.**

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Build](https://img.shields.io/badge/build-javac-informational)]()
[![Tests](https://img.shields.io/badge/tests-framework--free-success)]()
[![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)]()

</div>

---

## 🧭 Project Overview

Tracking greenhouse gas emissions has become essential for organizations, academic institutions, and eco-conscious individuals aiming to baseline and reduce their environmental impact. Existing commercial platforms, however, typically demand heavy web interfaces, proprietary cloud subscriptions, or external IoT sensor networks — and hide their conversion arithmetic behind closed-source pipelines.

**TerraGauge** takes the opposite approach: a lightweight, transparent, **offline-first command-line system** that calculates carbon footprints using deterministic, documented standards.

Rather than simulating pollution sensors, TerraGauge accepts verified user activity values, validates them at the domain layer, resolves them against an authenticated local dataset of baseline emission factors, and computes the resulting equivalent impact:

```
Estimated Impact (kg CO₂e)  =  Activity Quantity  ×  Emission Factor
```

Persistent transactional data is managed through a normalized MySQL database, while a multithreaded batch pipeline processes uncalculated activities in parallel. Every figure the system reports can be traced by hand — from raw input, through the applied factor, to the final kilogram.

**Tracked domains:** Transport · Electricity · Solid Waste · Water Supply

---

## ✨ Features

- 📝 **Activity CRUD Management** — log consumption events, inspect audit trails, modify quantities, and purge records with automatic cascading relational cleanup.
- 📊 **Reference Dataset Lookup** — fast, case-insensitive resolution of emission factors from a local CSV built on standard international conversion metrics.
- ⚡ **Concurrency & Multithreading** — parallel batch evaluation via Java `ExecutorService`, `Callable`, and `Future`, distributing activity batches across multiple CPU threads.
- 📈 **Analytical Dashboards** — SQL aggregation engine producing total footprint summaries, categorical breakdowns, and top pollution drivers.
- 🛡️ **Robust Error Handling & Data Integrity** — input validation, foreign-key constraints (`ON DELETE CASCADE`), transactional commits, and dynamic fallback credential handling.
- 🖥️ **Polished CLI Presentation** — ANSI-styled menus, aligned data tables, and guided entry wizards.
- 🔌 **Fully Offline** — no cloud account, no API keys, no network dependency at runtime.
- 🧪 **Zero-Dependency Test Suite** — automated verification of validation rules, factor lookup, calculation accuracy, and concurrency contracts with no external framework.

---

## 🧰 Technologies & Tools Used

| Category | Technology |
|---|---|
| **Language** | Java 17+ (Core Java, Concurrency, OOP) |
| **Database** | MySQL Server 8.0+ |
| **Connectivity** | JDBC via MySQL Connector/J (`mysql-connector-j-*.jar`) |
| **Data Format** | CSV — reference emission factor dataset |
| **Build** | `javac` / `java` (no build tool required) |
| **Version Control** | Git & GitHub |
| **Environment** | Visual Studio Code / Terminal (PowerShell, CMD, Bash) |

---

## 🗂️ Project Structure

Compiled binaries (`bin/`) are decoupled from source files and excluded via `.gitignore`:

```text
TerraGauge/
│                 
├── data/
│   └── emission_factors.csv     # Documented emission baseline dataset
│
├── database/
│   └── schema.sql               # Relational DDL definitions and constraints
│
├── lib/
│   └── mysql-connector-j-*.jar  # MySQL JDBC connector driver
│
├── src/
│   ├── Activity.java            # Activity domain entity
│   ├── ActivityCategory.java    # Categorical enumeration
│   ├── ActivityManager.java     # CRUD controller and business workflows
│   ├── DatabaseManager.java     # JDBC singleton connection manager
│   ├── EmissionFactor.java      # Reference emission factor entity
│   ├── ImpactCalculator.java    # Factor resolver and concurrent batch engine
│   ├── ImpactRecord.java        # Calculated carbon footprint entity
│   ├── Main.java                # Interactive CLI presentation entry point
│   ├── ReportManager.java       # Relational analytical query manager
│   └── User.java                # Application user entity
│
├── test/
│   └── TerraGaugeTest.java      # Validation and business logic test suite
│
├── .env.example                 # Environment variable reference template
├── .gitignore                   # Exclusion configuration for build binaries
├── README.md                    # Repository documentation
└── statement.md                 # Scope and problem statement document
```

### Layered Design

```text
Main.java  ──────────────►  Presentation layer (CLI rendering, menu routing)
     │
ActivityManager ─ ReportManager  ──►  Service layer (workflows, transactions)
     │
Activity ─ ImpactRecord ─ EmissionFactor  ──►  Domain layer (entities, validation)
     │
DatabaseManager  ──────────────►  Persistence layer (JDBC, SQL)
```

Validation lives in the **domain** layer rather than the CLI, so every entry path — interactive, batch, or test — is subject to identical rules.

---

## 🚀 Installation & Setup

### 1. Prerequisites

| Requirement | Version | Purpose |
|---|---|---|
| [JDK](https://adoptium.net/) | 17 or later | Compiles and runs the application |
| [MySQL Server](https://dev.mysql.com/downloads/mysql/) | 8.0 or later | Relational persistence |
| [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) | 8.x | JDBC driver (place in `lib/`) |
| [Git](https://git-scm.com/) | any | Clones the repository |

Verify your toolchain:

```bash
java -version
javac -version
mysql --version
```

---

### 2. Clone the Repository

```bash
git clone https://github.com/smukherjee-07/TerraGauge.git
cd TerraGauge
```

---

### 3. Install the JDBC Driver

Download the **Platform Independent** Connector/J archive from the [official MySQL site](https://dev.mysql.com/downloads/connector/j/), extract it, and place the `.jar` inside the `lib/` directory:

```text
lib/
└── mysql-connector-j-8.4.0.jar
```

> The application will not connect without this driver on the classpath.

---

### 4. Set Up the Database

Ensure your local MySQL instance is running on port **3306**, then execute the schema script.

<details>
<summary><strong>Windows — PowerShell</strong></summary>

```powershell
Get-Content database/schema.sql | mysql -u root -p
```
</details>

<details>
<summary><strong>Windows — Command Prompt</strong></summary>

```bat
mysql -u root -p < database\schema.sql
```
</details>

<details>
<summary><strong>Linux / macOS</strong></summary>

```bash
mysql -u root -p < database/schema.sql
```
</details>

<details>
<summary><strong>Inside the MySQL Client</strong></summary>

```sql
source database/schema.sql;
```
</details>

The script initializes **`terragauge_db`** with three normalized entities:

| Table | Purpose |
|---|---|
| `users` | Identity and user profiles |
| `activities` | Consumption entries, linked by foreign key, with positive-quantity constraints |
| `impact_records` | Calculated kg CO₂e impacts, linked to activities with cascade deletion |

```text
users (1) ──< activities (1) ──< impact_records
```

Confirm the schema loaded:

```sql
USE terragauge_db;
SHOW TABLES;
```

---

### 5. Configure Credentials

Copy the environment template and fill in your local MySQL credentials:

```bash
cp .env.example .env
```

```env
DB_URL=jdbc:mysql://localhost:3306/terragauge_db
DB_USER=root
DB_PASSWORD=your_password_here
```

> If the default connection fails at startup, TerraGauge falls back to an interactive prompt for your MySQL username and password — so the application remains usable even without a configured `.env`.

---

## ▶️ Running the Application

### Compile

Compile source and test files into `bin/` from the repository root.

<details>
<summary><strong>Windows (PowerShell / Command Prompt)</strong></summary>

```powershell
javac -cp ".;lib/*" -d bin src/*.java test/*.java
```
</details>

<details>
<summary><strong>Linux / macOS</strong></summary>

```bash
javac -cp ".:lib/*" -d bin src/*.java test/*.java
```
</details>

### Launch

<details>
<summary><strong>Windows (PowerShell / Command Prompt)</strong></summary>

```powershell
java -cp "bin;lib/*" Main
```
</details>

<details>
<summary><strong>Linux / macOS</strong></summary>

```bash
java -cp "bin:lib/*" Main
```
</details>

> **Classpath separator:** Windows uses `;`, Linux and macOS use `:`. This is the single most common cause of a failed run.

### Typical Workflow

```text
[1] Log Consumption Event   →   record an activity
[5] Batch Compute Footprint →   calculate all pending impacts in parallel
[6] Analytical Intelligence →   review totals, categories, and top drivers
```

---

## 📊 Emission Factor Dataset

Located at `data/emission_factors.csv`. All figures derive from official public publications:

- **DEFRA** — UK Department for Environment, Food & Rural Affairs: *Greenhouse Gas Reporting Conversion Factors (2023)*
- **CEA** — Central Electricity Authority of India: *CO₂ Baseline Database for the Indian Power Sector (2023)*

| Category | Activity | Factor | Unit | Source |
|---|---|---:|---|---|
| Transport | Petrol Car | 0.170 | km | DEFRA 2023 |
| Transport | Diesel Car | 0.171 | km | DEFRA 2023 |
| Transport | Bus | 0.096 | km | DEFRA 2023 |
| Electricity | Grid Electricity | 0.820 | kWh | CEA India 2023 |
| Waste | Landfill Waste | 0.446 | kg | DEFRA 2023 |
| Waste | Recycled Paper | 0.021 | kg | DEFRA 2023 |
| Water | Tap Water Supply | 0.149 | m³ | DEFRA 2023 |

> **Extending the dataset:** append a row in the same `category,activity,factor,unit,source` format. The lookup cache reloads on startup — no recompilation needed.

---

## 🧪 Testing

TerraGauge ships with an automated suite verifying unit logic, factor matching, and concurrency guarantees **without any external framework overhead**.

<details>
<summary><strong>Windows</strong></summary>

```powershell
java -cp "bin;lib/*" TerraGaugeTest
```
</details>

<details>
<summary><strong>Linux / macOS</strong></summary>

```bash
java -cp "bin:lib/*" TerraGaugeTest
```
</details>

### Verified Test Cases

| # | Test Case | Verifies |
|---|---|---|
| 1 | Valid activity initialization | Field encapsulation and object construction |
| 2 | Zero / negative quantity rejection | Domain-layer bounds checking |
| 3 | Case-insensitive category resolution | Enum parsing robustness |
| 4 | CSV reference dataset parsing | Ingestion and in-memory lookup cache |
| 5 | Non-existent factor reference | Exception handling on failed lookup |
| 6 | Emission computation accuracy | `Quantity × Factor` arithmetic correctness |
| 7 | Multi-threaded batch execution | Worker pool aggregation and result integrity |

> A passing run confirms that concurrent batch results are **identical** to sequential computation — the core correctness guarantee of the threading layer.

---

## 💻 Sample CLI Session

```text
  ╔═══════════════════════════════════════════════════════════════╗
  ║ T E R R A G A U G E                                           ║
  ║ Environmental Accounting and CO2e Footprint Engine            ║
  ╚═══════════════════════════════════════════════════════════════╝

  [System    ] Loading benchmark factors from dataset...
  ✔ Verified 7 environmental baseline factors.
  [Database  ] Testing MySQL connection parameters...
  ✔ Relational link operational on port 3306.

┌─────────────────────────────────────────────────────────────────┐
│  TerraGauge | Environmental Activity Ledger & Audit Platform    │
└─────────────────────────────────────────────────────────────────┘
  OPERATIONAL CONTROLS
  [1] Log Consumption Event       Record transport, energy, or waste
  [2] Audit Activity Register     View entire user activity log
  [3] Modify Event Metric         Update quantity of logged activity
  [4] Purge Event Record          Delete activity and its linked records
  [5] Batch Compute Footprint     Multithreaded background factor matching
  [6] Analytical Intelligence     Aggregations, categories, and top drivers
  [7] Terminate Session
───────────────────────────────────────────────────────────────────
 terra@gauge :> 2
┌─ RECORDED ENVIRONMENTAL LOG
└──────────────────────────────────────────────────────────────────
┌─────┬──────────────┬──────────────────────┬─────────────┬────────────┐
│ ID  │ CATEGORY     │ ACTIVITY             │ QUANTITY    │ DATE       │
├─────┼──────────────┼──────────────────────┼─────────────┼────────────┤
│ 1   │ ELECTRICITY  │ Grid Electricity     │ 450.00 kWh  │ 2026-09-10 │
│ 2   │ TRANSPORT    │ Bus                  │ 120.00 km   │ 2026-09-12 │
│ 3   │ TRANSPORT    │ Petrol Car           │ 85.00 km    │ 2026-09-14 │
│ 4   │ WASTE        │ Landfill Waste       │ 60.00 kg    │ 2026-09-16 │
└─────┴──────────────┴──────────────────────┴─────────────┴────────────┘
  Total Ledger Count: 4 entries

 terra@gauge :> 5
┌─ PARALLEL BATCH PROCESSOR
└──────────────────────────────────────────────────────────────────
  Identified 4 uncalculated records. Dispatching to thread pool...
  [Thread Worker] Activity #1 calculated -> 369.000 kg CO2e
  [Thread Worker] Activity #2 calculated -> 11.520 kg CO2e
  [Thread Worker] Activity #3 calculated -> 14.450 kg CO2e
  [Thread Worker] Activity #4 calculated -> 26.760 kg CO2e

  ✔ Committed 4 calculated impacts to MySQL.

 terra@gauge :> 6
┌─ ANALYTICAL INTELLIGENCE DASHBOARD
└──────────────────────────────────────────────────────────────────
AGGREGATE AUDIT METRICS
  Tracked Events: 4     | Total Footprint: 421.730 kg CO2e

CATEGORY EMISSION PROFILE
┌──────────────┬───────┬──────────────────┐
│ CATEGORY     │ COUNT │ TOTAL (kg CO2e)  │
├──────────────┼───────┼──────────────────┤
│ ELECTRICITY  │ 1     │ 369.000          │
│ WASTE        │ 1     │ 26.760           │
│ TRANSPORT    │ 2     │ 25.970           │
└──────────────┴───────┴──────────────────┘

TOP POLLUTION DRIVERS
┌──────────────────────┬─────────────┬────────────┬──────────────────┐
│ ACTIVITY             │ METRIC      │ DATE       │ IMPACT (kg CO2e) │
├──────────────────────┼─────────────┼────────────┼──────────────────┤
│ Grid Electricity     │ 450.00 kWh  │ 2026-09-10 │ 369.000          │
│ Landfill Waste       │ 60.00 kg    │ 2026-09-16 │ 26.760           │
│ Petrol Car           │ 85.00 km    │ 2026-09-14 │ 14.450           │
│ Bus                  │ 120.00 km   │ 2026-09-12 │ 11.520           │
└──────────────────────┴─────────────┴────────────┴──────────────────┘
```

**Worked verification:** `450 kWh × 0.820 = 369.000` · `120 km × 0.096 = 11.520` · `85 km × 0.170 = 14.450` · `60 kg × 0.446 = 26.760` — summing to **421.730 kg CO₂e**.

---

## 🛠️ Troubleshooting

| Issue | Likely Fix |
|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Connector/J `.jar` is missing from `lib/` or absent from the classpath. |
| `Access denied for user 'root'@'localhost'` | Wrong credentials in `.env` — or use the interactive fallback prompt at startup. |
| `Communications link failure` | MySQL service isn't running, or is not listening on port 3306. |
| `Unknown database 'terragauge_db'` | The schema script hasn't been executed. Re-run step 4. |
| `error: class Main is public, should be declared in a file named Main.java` | Compiling from the wrong directory — run `javac` from the repository root. |
| Compiles but won't launch | Classpath separator mismatch: `;` on Windows, `:` on Linux/macOS. |
| Garbled box characters in the CLI | Terminal isn't UTF-8. On Windows run `chcp 65001` before launching. |
| Batch processor finds 0 records | All activities already have impact records. Log a new activity first. |

---

## 📚 Data Sources & Attribution

Emission factors are reproduced from publicly available government publications and are **not** original estimates:

- UK Department for Environment, Food & Rural Affairs (DEFRA) — *Greenhouse Gas Reporting: Conversion Factors 2023*
- Central Electricity Authority of India (CEA) — *CO₂ Baseline Database for the Indian Power Sector, 2023*

Factors are regional and time-bound. Results should be interpreted as **indicative operational estimates**, not certified regulatory disclosures.
