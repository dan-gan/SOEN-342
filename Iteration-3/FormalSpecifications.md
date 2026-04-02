# Formal Specifications (OCL)

This document specifies business rules using Object Constraint Language (OCL) that cannot be fully captured in UML diagrams.

## 1. Task Subtask Constraint

**Business Rule:** "A task cannot have more than 20 sub-tasks."

```ocl
context Task
inv MaxSubtasks: self.subtasks->size() <= 20
```

**Explanation:** For any Task instance, the number of subtasks associated with it must not exceed 20.

---

## 2. Open Tasks Without Due Date Constraint

**Business Rule:** "The number of open tasks without a due date should not exceed 50."

```ocl
context TaskManager
inv MaxOpenTasksWithoutDueDate: 
    self.tasks->select(status = Status::OPEN and dueDate = null)->size() <= 50
```

**Explanation:** Across all tasks managed by the TaskManager, the count of tasks with OPEN status and no due date assigned must not exceed 50.

---

## 3. Collaborator Category Open Tasks Limit

**Business Rule:** "The limit for open tasks for each collaborator category is a positive integer."

```ocl
context Collaborator
inv CollaboratorCategoryOpenTaskLimit:
    if self.category = CollaboratorCategory::SENIOR then
        self.assignedTasks->select(status = Status::OPEN)->size() <= 2
    else if self.category = CollaboratorCategory::INTERMEDIATE then
        self.assignedTasks->select(status = Status::OPEN)->size() <= 5
    else if self.category = CollaboratorCategory::JUNIOR then
        self.assignedTasks->select(status = Status::OPEN)->size() <= 10
    else
        true
    endif endif endif
```
OR 

```ocl
context Collaborator
let openTasks : Integer = self.assignedTasks->select(status = Status::OPEN)->size()
    let categoryLimit : Integer = self.category.getOpenTaskLimit()
    in openTasks <= categoryLimit
```

**Generic Constraint:** The above uses a generic approach referencing `category.getOpenTaskLimit()` which allows limits to be configured dynamically. If these limits are modified, this specification document should be updated accordingly. 

**Configuration (from Collaborator.java):**
- SENIOR: max 2 open tasks
- INTERMEDIATE: max 5 open tasks
- JUNIOR: max 10 open tasks

---

## 4. No Collaborator Overload Constraint

**Business Rule:** "No collaborator must be overloaded. The number of assigned tasks that are open should not exceed the limit."

```ocl
context Collaborator
inv NoOverload:
    self.assignedTasks->select(status = Status::OPEN)->size() <= 
    self.category.getOpenTaskLimit()
```

**Alternative (Combined with Constraint 3):**

```ocl
context Collaborator
inv NoOverloadConstraint:
    let openTaskCount : Integer = 
        self.assignedTasks->select(status = Status::OPEN)->size()
    let categoryLimit : Integer = 
        self.category.getOpenTaskLimit()
    in openTaskCount <= categoryLimit
```

**Explanation:** For every Collaborator, the count of their assigned tasks with OPEN status must not exceed the configured limit for their category.

---


These constraints ensure data integrity and enforce business logic that cannot be represented in UML class diagrams alone.
