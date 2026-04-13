## Personal Task Management System (PTMS)

dangan: Daniel Ganchev - 40315755 - Team Lead
<br> soumeyadiop: Souméya Diop - 40197160
<br> Hildthelsta: Dmitrii Cazacu - 40314501

## System Description

A command-line application that helps a single user create, organize, and monitor tasks over time.
Supports task lifecycle management, project grouping, collaborator assignment, recurring tasks,
tagging, subtask tracking, activity history, CSV import/export, and iCal (.ics) export.
Implemented in Java with SQLite persistence.

---

## Setup

**Requirements:** JDK 17+, Maven 3.x

```bash
mvn package          # compiles and builds target/task-manager-jar-with-dependencies.jar
mvn test             # runs the integration test suite (in-memory DB, no side effects)
```

**Run:**
```bash
java -jar target/task-manager-jar-with-dependencies.jar
```

The app creates `tasks.db` (SQLite) in the working directory on first launch and auto-seeds
it with demo data (see below).

**Custom DB path:**
```bash
java -jar target/task-manager-jar-with-dependencies.jar mydata.db
```

---

## Demo / Mockup Database

On first launch with an empty database the app automatically populates a representative
dataset covering every feature and OCL edge case.

To reset and re-seed at any time:
```bash
del tasks.db                   # Windows
java -jar target/task-manager-jar-with-dependencies.jar
```

**What the seed data contains:**

| # | Data | Purpose |
|---|---|---|
| 2 | Projects (Course Assignments, Home Renovation) | Test project-scoped search and iCal export by project |
| 4 | Tags (school, work, urgent, personal) | Test search by tag |
| ~25 | Tasks across all statuses and priorities | Test search filters, status transitions |
| 1 | Task with **19/20 subtasks** | Adding one more subtask triggers OCL violation |
| 3 | Open tasks **without due date** | Visible toward the 50-task OCL limit |
| 1 | Overdue open task (due last week) | Test iCal export, search by date range |
| 1 | Completed task + 1 cancelled task | Test reopen, search by status |
| 4 | Collaborators (Alice, Bob, Carol, Dave) | See table below |
| 5 | Daily recurring tasks (standup series) | Test recurrence + duplicate detection |
| 4 | Weekly recurring tasks (Mon+Wed × 2 weeks) | Test weekly recurrence |

**Collaborator edge cases:**

| Collaborator | Category | Open tasks | Limit | State |
|---|---|---|---|---|
| Alice | SENIOR | 1 | 2 | Can take 1 more |
| Bob | SENIOR | 2 | 2 | **AT LIMIT** — next assignment throws |
| Carol | INTERMEDIATE | 1 | 5 | Well below limit |
| Dave | JUNIOR | 2 | 1 (lowered) | **OVERLOADED** — appears in overload menu |

---

## Main Menu

```
1. Tasks          — create, view, update, status changes, subtasks, activity history
2. Projects       — create and manage projects
3. Collaborators  — add, assign, change category, set category limit
4. Overloaded     — list collaborators currently over their open-task limit
5. Search         — filter by keyword / date / priority / status / project / tag / day of week
                    export search results to CSV or iCal
6. Recurring      — create recurring task series (daily / weekly / monthly)
7. Export/Import  — CSV import/export, iCal export (all / by project / open / single task)
0. Exit
```

---

## OCL Constraints (enforced at runtime)

| Constraint | Enforced in |
|---|---|
| A task cannot have more than 20 subtasks | `SubtaskService.addSubtask()` |
| Open tasks without a due date ≤ 50 | `TaskService.createTask()`, `updateDueDate()`, `reopenTask()` |
| Category limit must be a positive integer | `CollaboratorCategory.setLimit()` |
| No collaborator must be overloaded (preventive) | `CollaboratorService.assignCollaboratorToTask()` |

---

## Repository Structure

```
/Iterations
    /Iteration-1    — Use case diagram, domain model, SSDs, contracts
    /Iteration-2    — Updated diagrams, interaction diagrams, class diagram
    /Iteration-3    — Updated use case diagram, sequence diagram (iCal export)
    /Iteration-4    — Final class diagram, domain model, state machine, data model  ← final artifacts
/src/main/java/com/taskmanager
    /cli            — Menu classes (App, TaskMenu, CollaboratorMenu, SearchMenu, ...)
    /domain/model   — Domain entities (Task, Project, Collaborator, ...)
    /domain/service — Business logic services (TaskService, CollaboratorService, ...)
    /domain/gateway — iCal Gateway pattern (ICalGateway, ICalGatewayImpl)
    /persistence    — DatabaseManager + repository interfaces and SQLite implementations
    /exception      — Typed exceptions (ValidationException, CollaboratorOverloadException, ...)
/src/test/java      — IntegrationTest (14 tests, in-memory SQLite)
```

> **Final graded artifacts** are in `Iterations/Iteration-4/`.

---

## Deliverables

### Iteration 1
- Use Case Diagram
- Domain Model
- System Sequence Diagrams (SSDs) for critical use cases
- Full Use Case Scenario (createTask)
- System operations + operation contracts

### Iteration 2
- Updated Use Case Diagram
- Updated Domain Model, SSDs, Interaction Diagrams
- Class diagram (partial design)
- Prototype: task search, CSV import/export

### Iteration 3
- Updated Use Case Diagram (iCal + overload use cases)
- Updated Class Diagram (Gateway pattern, OCL constraints)
- Sequence Diagram for iCal export
- OCL constraints
- Fully functional system (all features implemented, SQLite persistence)

### Iteration 4
- UML [Protocol] State Machine for Task — `Iterations/Iteration-4/UmlStateMachine/`
- Data Model (UML/ER) — `Iterations/Iteration-4/UmlDataModel/`
- Final Class Diagram — `Iterations/Iteration-4/FinalClassDiagram/`
- Final Domain Model — `Iterations/Iteration-4/FinalDomainModel/`
- Final OCL Contract — `Iterations/Iteration-4/FinalOclContract/`
- Demo video — see below

## Demo Video

TODO: Record and upload 5-minute demo video. Add link here once uploaded.

---

## Teamwork and Collaboration

All team members participated in requirements analysis, design, implementation, and testing.
Each artifact was collaboratively reviewed and refined before finalization.
