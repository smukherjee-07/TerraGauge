# TerraGauge
### An Offline-First Operational Carbon Accounting Ledger
**Core Java · MySQL · Command-Line Interface**

---

## 1. Problem Statement

Operational greenhouse gas (GHG) accounting has moved from a voluntary exercise to an expected discipline for academic institutions, enterprise branches, and environmentally conscious individuals seeking to baseline and curtail their carbon output. Yet the prevailing generation of carbon management software suffers from recurring systemic shortcomings:

| Shortcoming | Practical Consequence |
| --- | --- |
| Proprietary, subscription-based cloud ecosystems | Recurring overhead for what is fundamentally deterministic arithmetic |
| Mandatory web dependencies and opaque SaaS microservices | Friction for local auditing; unusable without connectivity |
| Closed-source computational pipelines | Emission factors and conversion arithmetic hidden from the end user |
| Multi-tenant data residency | Consumption data — a proxy for operational behaviour — leaves the premises |

The net result is that small, localized entities have no private, transparent, offline-first auditing engine capable of evaluating internal activity data deterministically against authentic public conversion standards.

**TerraGauge resolves this.** It is an auditable, desktop-grade operational ledger implemented in Core Java and backed by a relational MySQL database. The system ingests verifiable local consumption metrics, enforces rigorous domain-layer validation, resolves each record against an authenticated public conversion index, and computes equivalent carbon output — entirely without proprietary third-party cloud infrastructure. Every number the system reports can be traced by hand, from raw input to final kilogram.

---

## 2. Project Scope

TerraGauge addresses the systematic ingestion, validation, processing, and historical analysis of operational sustainability data across four primary resource domains.

| Domain | Coverage |
| --- | --- |
| **Transportation** | Direct fleet mileage, private passenger commutes, and public transit passenger-distance across fuel variants |
| **Electricity Consumption** | Scope 2 utility electrical energy drawn from regional power grids |
| **Solid Waste Management** | Municipal solid waste output, distinguishing landfill burden from material recovery and recycling offsets |
| **Water Supply Utilities** | Municipal water conveyance, supply, and treatment overhead |

### 2.1 Within Scope

- **Reference factor ingestion** — parsing a dedicated CSV dataset of public conversion factors sourced from official regulatory agencies, principally **DEFRA** (UK Department for Environment, Food & Rural Affairs) and the **CEA** (Central Electricity Authority of India).
- **Full activity lifecycle management** — creation, inspection, in-place mutation, and removal of activity records through a structured command-line interface.
- **Domain-layer business validation** — rejecting corrupted, anomalous, zero, or negative metrics *before* relational commitment, so invalid state never reaches persistence.
- **Concurrent batch computation** — parallel processing of uncalculated activity backlogs using Java concurrency primitives (`ExecutorService`, `Callable`, `Future`).
- **ACID-compliant relational persistence** — MySQL storage with declarative foreign-key constraints and cascading rules that guarantee referential integrity.
- **Structured analytical reporting** — cumulative emissions totals, categorical distributions, and ranked emission drivers computed via optimized SQL aggregation.
- **Automated verification** — a dedicated, framework-free test suite covering validation rules, lookup mechanisms, calculation accuracy, and concurrency contracts.

### 2.2 Out of Scope

- Physical IoT hardware probes, microcontrollers, or real-time atmospheric telemetry.
- Speculative algorithmic forecasting, probabilistic simulation, or deep-learning regression.
- Cloud orchestration, web frameworks, distributed microservices, or multi-tenant frontends.
- Financial transaction reconciliation or carbon credit marketplace trading.

> **Design rationale:** each exclusion is deliberate. Forecasting would introduce estimation into a system whose entire value proposition is deterministic auditability; cloud infrastructure would reintroduce the dependency the project exists to eliminate.

---

## 3. Target Users

**Academic Institutions and University Laboratories**
Departmental administrators, researchers, and campus facility officers who need an auditable desktop tool to track classroom, server room, and laboratory utility footprints against institutional sustainability targets.

**Small and Medium-Sized Enterprises (SMEs)**
Operations managers evaluating monthly grid consumption, waste output, and logistics footprints without incurring recurring SaaS fees or exposing operational data externally.

**Environmental Auditing Students and Researchers**
Learners investigating applied environmental engineering methodology, who require direct visibility into raw conversion factors and the mathematical reconciliation behind every result.

**Eco-Conscious Individuals**
Users seeking an offline, privacy-preserving platform to record, audit, and analyze personal carbon trends over time.

---

## 4. High-Level System Features

### 4.1 Interactive Command-Line Console
A presentation terminal featuring ANSI styling, aligned data tables, guided entry wizards, and structured menu navigation covering all daily tracking workflows. The CLI is treated as a first-class interface rather than a fallback — clarity of output is a functional requirement, not decoration.

### 4.2 Comprehensive Activity Lifecycle Management
Complete CRUD capability allowing operators to record consumption events, browse structured historical tables, revise activity metrics, and purge stale records — with cascading relational cleanup of dependent impact records handled automatically by the database.

### 4.3 Transparent Factor Resolution Engine
An in-memory lookup cache mapping activity queries against validated conversion factors drawn from the local reference dataset. Supports case-insensitive matching and partial-string resolution, so operators are not required to memorise exact dataset nomenclature.

### 4.4 Deterministic Carbon Footprint Calculation
Explicit mathematical modelling producing mass-equivalent carbon output with no hidden estimation:

```
Estimated Impact (kg CO₂e) = Activity Quantity × Emission Factor
```

Each computed record retains the factor value and source applied at calculation time, so historical results remain reproducible even if the reference dataset is later revised.

### 4.5 Multi-Threaded Batch Processing Pipeline
A dedicated concurrent calculation module that pulls uncalculated or modified activities from MySQL, partitions the workload across an `ExecutorService` thread pool, resolves factors inside parallel worker threads, and commits results back to the database within a single transaction — ensuring the batch either lands whole or not at all.

### 4.6 Relational Persistence Layer
A normalized MySQL schema implementing `users`, `activities`, and `impact_records` tables with primary/foreign-key relationships, indexing on frequent query paths, positive-quantity `CHECK` constraints, and `ON DELETE CASCADE` rules.

```
users (1) ──< activities (1) ──< impact_records
```

### 4.7 Analytical Intelligence Dashboard
A reporting engine executing optimized SQL aggregations to calculate cumulative carbon output, summarize proportional contribution by category, and surface peak emission drivers — converting a raw ledger into an actionable reduction narrative.

### 4.8 Standalone Automated Test Suite
An integrated verification suite (`TerraGaugeTest.java`) validating activity creation, bounds checking, factor ingestion, calculation accuracy, and multithreaded batch behaviour, implemented without any external testing framework dependency.

---

## 5. Technology Stack

| Layer | Technology | Rationale |
| --- | --- | --- |
| Language | Core Java (JDK 25+) | Strong typing, mature concurrency model, no runtime beyond the JVM |
| Persistence | MySQL 8.x | ACID guarantees, declarative constraints, portable SQL |
| Connectivity | JDBC with `PreparedStatement` | Parameterized queries; injection-safe by construction |
| Concurrency | `ExecutorService`, `Callable`, `Future` | Bounded parallelism over an I/O-and-CPU mixed workload |
| Reference Data | Local CSV (DEFRA, CEA) | Human-readable, independently verifiable, version-controllable |
| Testing | Custom assertion harness | Zero external dependencies; the suite ships with the source |

---

## 6. Architectural Approach

TerraGauge follows a layered separation of concerns:

| Layer | Responsibility |
| --- | --- |
| **Presentation** | CLI rendering, menu routing, input capture |
| **Service** | Orchestration, batch scheduling, transaction boundaries |
| **Domain** | Entity models and business validation rules |
| **Persistence (DAO)** | JDBC access, SQL statements, result mapping |
| **Reference** | CSV parsing and the in-memory factor lookup cache |

Validation lives in the domain layer rather than at the CLI, so every entry path — interactive, batch, or test — is subject to the same rules. Persistence code holds no business logic, keeping the DAO layer replaceable without touching calculation behaviour.

---

## 7. Success Criteria

The project is considered complete when:

1. An operator can record, view, amend, and delete activities across all four domains through the CLI without encountering an unhandled exception.
2. Invalid input — zero, negative, non-numeric, or unrecognised category — is rejected with a clear diagnostic message and never reaches the database.
3. A batch of uncalculated activities is processed concurrently and produces results identical to sequential computation.
4. Reports reconcile: the sum of per-category totals equals the reported cumulative total.
5. Every value in the analytical dashboard can be traced back to a specific activity record and its applied emission factor.
6. The full test suite executes and passes from a clean checkout with no external dependencies installed.

---

## 8. Deliverables

- Complete Core Java source tree with layered package structure.
- MySQL schema definition script (`schema.sql`) including constraints and indexes.
- Reference emission factor dataset (`emission_factors.csv`) with documented provenance.
- Standalone test suite (`TerraGaugeTest.java`).
- Setup and usage documentation covering database configuration and first run.