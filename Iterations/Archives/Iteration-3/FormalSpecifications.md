@startuml ClassDiagram
title Personal Task Management System — Class Diagram
left to right direction

skinparam classAttributeIconSize 0
skinparam class {
    BackgroundColor White
    BorderColor Black
    ArrowColor Black
}
skinparam packageStyle rectangle

' ════════════════════════════════════════
' DOMAIN MODEL
' ════════════════════════════════════════
package "domain.model" {

    enum Priority {
        LOW
        MEDIUM
        HIGH
    }

    enum TaskStatus {
        OPEN
        COMPLETED
        CANCELLED
    }

    enum CollaboratorCategory {
        SENIOR
        INTERMEDIATE
        JUNIOR
    }

    enum RecurrenceType {
        DAILY
        WEEKLY
        MONTHLY
    }

    class Task {
        -id: long
        -title: String
        -description: String
        -creationDate: LocalDateTime
        -priority: Priority
        -status: TaskStatus
        -dueDate: LocalDate
        --
        +complete(): void
        +cancel(): void
        +reopen(): void
    }

    class Project {
        -id: long
        -name: String <<unique>>
        -description: String
    }

    class Subtask {
        -id: long
        -title: String
        -isCompleted: boolean
        --
        +complete(): void
    }

    class Tag {
        -id: long
        -name: String
    }

    class ActivityEntry {
        -id: long
        -timestamp: LocalDateTime
        -description: String
    }

    class Collaborator {
        -id: long
        -name: String
        -category: CollaboratorCategory
        --
        +isOverloaded(): boolean
    }

    class RecurrencePattern {
        -id: long
        -type: RecurrenceType
        -interval: int
        -startDate: LocalDate
        -endDate: LocalDate
        -weekdays: Set<DayOfWeek>
    }
}

' ════════════════════════════════════════
' DOMAIN RELATIONSHIPS
' ════════════════════════════════════════

Task "0..*" --> "0..1" Project
Task "1"    *-- "0..20" Subtask
Task "0..*" --  "0..*"  Tag
Task "1"    *-- "0..*"  ActivityEntry
Task "0..*" --> "0..1"  RecurrencePattern
Project "1" *-- "0..*"  Collaborator
Task "0..*" --  "0..*"  Collaborator
Collaborator "0..1" -- "0..*" Subtask

' ════════════════════════════════════════
' OCL CONSTRAINTS
' ════════════════════════════════════════
note as OCL
  <b>OCL Constraints</b>
  ─────────────────────────────────────────────────
  context Task 
  inv maxSubtasks:
    self.subtasks->size() <= 20

  context Task 
  inv openWithoutDueDate:
    Task.allInstances()
      ->select(t | t.status = TaskStatus::OPEN
               and t.dueDate.oclIsUndefined())->size() <= 50

  context Task 
  inv uniqueOccurrence:
    Task.allInstances()
      ->select(t | t.recurrencePattern <> null)
      ->isUnique(t | Tuple{n = t.title, d = t.dueDate})

  context Collaborator 
  inv noOverload:
    self.assignedTasks->select(t | t.status = TaskStatus::OPEN)
      ->size() <= self.category.getOpenTaskLimit()

  context CollaboratorCategory 
  inv positiveLimit:
    self.getOpenTaskLimit() > 0
end note

' ════════════════════════════════════════
' SERVICES
' ════════════════════════════════════════
package "domain.service" {

    class SearchCriteria {
        -keyword: String
        -status: TaskStatus
        -priority: Priority
        -dateRangeStart: LocalDate
        -dateRangeEnd: LocalDate
        -dayOfWeek: DayOfWeek
        -projectId: long
        -tagId: long
        +isEmpty(): boolean
    }

    class TaskService {
        +createTask(title, priority, dueDate, projectId): Task
        +updateTask(id, ...): Task
        +completeTask(id: long): Task
        +cancelTask(id: long): Task
        +reopenTask(id: long): Task
        +deleteTask(id: long): void
    }

    class ProjectService {
        +createProject(name, description): Project
        +getById(id: long): Project
        +listAll(): List<Project>
    }

    class CollaboratorService {
        +addCollaboratorToProject(name, category, projectId): Collaborator
        +assignCollaboratorToTask(collaboratorId, taskId): void
        +detectOverloaded(): List<Collaborator>
        +changeCategory(id, category: CollaboratorCategory): void
        +setCategoryLimit(category: CollaboratorCategory, limit: int): void
        +getCategoryLimit(category: CollaboratorCategory): int
    }

    class SearchService {
        +search(criteria: SearchCriteria): List<Task>
    }

    class RecurrenceService {
        +previewOccurrenceDates(pattern: RecurrencePattern): List<LocalDate>
        +generateOccurrences(pattern, title, priority): List<Task>
    }

    class CsvService {
        +exportTasks(tasks: List<Task>, out: OutputStream): void
        +importTasks(in: InputStream): ImportResult
    }
}

' ════════════════════════════════════════
' PERSISTENCE
' ════════════════════════════════════════
package "persistence" {

    interface TaskRepository {
        +save(task: Task): Task
        +findById(id: long): Task
        +findAll(): List<Task>
        +countOpenWithoutDueDate(): int
    }

    interface ProjectRepository {
        +save(project: Project): Project
        +findByName(name: String): Project
        +findAll(): List<Project>
    }

    interface CollaboratorRepository {
        +save(c: Collaborator): Collaborator
        +findByProject(projectId: long): List<Collaborator>
    }
}

' ════════════════════════════════════════
' GATEWAY
' ════════════════════════════════════════
package "gateway" {

    interface ICalGateway {
        +exportTask(task: Task, out: OutputStream): void
        +exportTasks(tasks: List<Task>, out: OutputStream): void
    }
}

' ════════════════════════════════════════
' PRESENTATION LAYER
' ════════════════════════════════════════
package "presentation" {

    class App {
        +run(): void
    }

    class TaskMenu {
        +show(): void
    }

    class ProjectMenu {
        +show(): void
    }

    class CollaboratorMenu {
        +show(): void
    }

    class SearchMenu {
        +show(): void
    }

    class RecurrenceMenu {
        +show(): void
    }

    class ExportImportMenu {
        +show(): void
    }

    App --> TaskMenu
    App --> ProjectMenu
    App --> CollaboratorMenu
    App --> SearchMenu
    App --> RecurrenceMenu
    App --> ExportImportMenu
}

' ════════════════════════════════════════
' LAYER DEPENDENCIES
' ════════════════════════════════════════

TaskMenu          --> TaskService
ProjectMenu       --> ProjectService
CollaboratorMenu  --> CollaboratorService
SearchMenu        --> SearchService
RecurrenceMenu    --> RecurrenceService
ExportImportMenu  --> CsvService
ExportImportMenu  --> ICalGateway

TaskService       --> TaskRepository
ProjectService    --> ProjectRepository
CollaboratorService --> CollaboratorRepository
SearchService     --> TaskRepository
CsvService        --> TaskRepository
CsvService        --> ProjectRepository
RecurrenceService --> TaskService

@enduml