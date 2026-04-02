## Personal Task Management System (PTMS)
dangan: Daniel Ganchev - 40315755  Team Lead
<br> soumeyadiop: Souméya Diop - 40197160
<br> Hildthelsta: Dmitrii Cazacu - 40314501
<br>

## System Description
A command line application to help a single user create, organize, and monitor tasks over time! 
<br> The system supports task lifecycle management, project grouping, tagging, subtask tracking, and activity history logging.
<br> The architecure is implemented in an object oriented platform (Java).

## How to Run
Requires Maven and JDK 17+. Verify Maven is in correct PATH

```bash
mvn compile                    # compile
mvn package                    # build fat JAR → target/task-manager-jar-with-dependencies.jar
java -jar target/task-manager-jar-with-dependencies.jar   # run
mvn test                       # run integration tests
```

The app creates a `tasks.db` SQLite file in the working directory on first run.

## Repository Structure
```
/Iteration-1
    /UseCaseDiagram
    /DomainModel
    /SSDs
    /Contracts
    /UseCaseDescriptions
/Iteration-2
    /UpdatedUseCaseDiagram
    /UpdatedDomainModel
    /UpdatedSSDs
    /ClassDiagram
    /InteractionDiagrams
/Iteration-3
/Iteration-4
/src/main/java
```

## Deliverables

### Iteration 1 
* Use Case Diagram 
* Domain Model 
* System Sequence Diagrams (SSDs) for critical use cases
* A Full Use Case Scenario (createTask())
* System operations + operation contracts

### Iteration 2 
* Updated Use-case diagram  
* Updated UML domain model
* Updated SSDs
* Interaction diagrams for critical use-cases
* Class diagram (partial design)
* Prototype: A functional system with features: 
    - Task Search and View
    - Import from CSV
    - Export from CSV
 
### Iteration 3
* Updated Use Case Diagram (iCal export + overload collaborators use cases added)
* Updated UML Class Diagram (Gateway pattern, Catalogue classes, OCL constraints)
* Sequence Diagram for iCal export functionality
* OCL Constraints (embedded in Class Diagram)
* Fully functional system with:
  - iCal (.ics) export: single task, all tasks in a project, or filtered list
  - Gateway pattern: `ICalGateway` interface → `ICalGatewayImpl` → `ICalExportService`
  - Catalogue classes: `TaskCatalogue`, `ProjectCatalogue`, `CollaboratorCatalogue`
  - Overloaded collaborator detection (menu option 6)
  - Persistency layer via CSV import/export

### Iteration 4
* UML [Protocol] State Machine for Task — `Iteration-4/StateMachine/TaskStateMachine.puml`
* Data Model (UML/ER schema) — `Iteration-4/DataModel/DataModel.puml`
* Final Updated UML Class Diagram — `Iteration-4/FinalClassDiagram/ClassDiagram.puml`
* Final Updated Domain Model — `Iteration-4/FinalDomainModel/DomainModel.puml`
* Demo video — see below

> **Final artifacts:** The `Iteration-4/` folder contains final versions of the Class Diagram and Domain Model. 

## Demo Video
TODO: Record and upload 5-minute demo video. Add link here once uploaded.

## Teamwork and Collaboration
All team members participated in requirements analysis and modelling activities. 
<br>Each artifact was collaboratively reviewed and refined before finalization.