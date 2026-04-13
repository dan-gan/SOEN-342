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
| Dave | JUNIOR | 2 | 1 (lowered) | **OVERLOADED** — appears in overload list |

---

## Main Menu

```
1. Tasks          — create, view, update, status changes, subtasks, activity history, delete
2. Projects       — create, list (with task/collaborator counts), view details, update, delete
3. Collaborators  — create (with project), list, view details, update (name/category/project),
                    assign to task, set category limit, delete, list overloaded
4. Search         — see Search menu below
5. Recurring      — create recurring task series (daily / weekly / monthly)
6. Export/Import  — CSV import/export, iCal export (all / by project / open / single task)
0. Exit
```

### Search Menu

```
1. Search with all criteria    — combine any/all filters in one pass
2. Search by keyword           — matches title or description
3. Search by status            — OPEN / COMPLETED / CANCELLED
4. Search by priority          — LOW / MEDIUM / HIGH
5. Search by project           — lists available projects, search by name
6. Search by tag               — lists available tags, search by name
7. Search by date →
     1. Exact due date
     2. Due date range (from / to)
     3. Day of week (MON–SUN)
8. List all open tasks         — sorted by due date ascending (default view)
```

All search results offer CSV or iCal export immediately after display.

---

## Collaborator Menu

```
1. Create collaborator         — name, category, project (one step)
2. List all collaborators      — shows open/limit load and project per collaborator
3. View collaborator details   — identity, load, project, assigned tasks + subtask status
4. Update collaborator         — change name, category, and/or project; shows overload warnings
5. Assign collaborator to task — enforces category limit (throws if at limit)
6. Set category limit          — change open-task limit for SENIOR / INTERMEDIATE / JUNIOR
7. Delete collaborator
8. List overloaded collaborators — shows full detail: load, project, tasks, subtask status
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
    /Archives       — Iterations 1–3 artifacts (archived)
    /Iteration-4    — Final class diagram, domain model, state machine, data model  ← final artifacts
/src/main/java/com/taskmanager
    /cli            — Menu classes (App, TaskMenu, ProjectMenu, CollaboratorMenu, SearchMenu, ...)
    /domain/model   — Domain entities (Task, Project, Collaborator, Tag, Subtask, ...)
    /domain/service — Business logic + OCL enforcement (TaskService, CollaboratorService, ...)
    /domain/gateway — iCal Gateway pattern (ICalGateway, ICalGatewayImpl)
    /persistence    — DatabaseManager + repository interfaces and SQLite implementations
    /exception      — Typed exceptions (ValidationException, CollaboratorOverloadException, ...)
/src/test/java      — IntegrationTest (in-memory SQLite, no side effects)
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
- OCL constraints (4 constraints, formally specified)
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
