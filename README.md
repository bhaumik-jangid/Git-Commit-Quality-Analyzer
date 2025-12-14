# Git Commit Quality Analyzer (Java + Maven + JavaFX)

This project analyzes Git commit messages from a local repository and scores them  
based on length, clarity, and meaningful keywords. It then classifies commits into  
quality levels and (later) visualizes results using a JavaFX interface.

---

## Tech Stack

- **Java 17+**  
- **Maven**  
- **JavaFX**  
- **Git** (commit history extraction)

---

## Development Progress (Daily Log)

### **Day 1 — Project Setup**
- Maven project initialized  
- Basic package structure created (`model`, `core`, `ui`)  
- Placeholder classes added (`CommitRecord`, `CommitScore`, `CommitAnalyzer`, `MainApp`)  
- Initial README and `.gitignore` configured  
- Verified Maven + JavaFX compile and run

---

### **Day 2 — Git Log Reader Implementation**
- Implemented `GitLogReader` to execute `git log` and fetch commit history  
- Parsed commit data into `CommitRecord` objects  
- Added testing runner (`DebugRunner`) to print commits to console  
- Successfully retrieved commit logs from the local repository  
- Added safe error-handling and output parsing

---

### Day 3:
- Commit scoring engine implemented
- Classifies commit messages into Good / Average / Poor
- Basic console output for analyzed commits

---

### Day 4:
- Created JavaFX main window using FXML
- Added table for commits, summary labels, and pie chart placeholder
- Wired UI to MainController with dummy data

---

### Day 5:
- Connected Git log reader and commit analyzer to the JavaFX UI
- Table now displays real commit history with scores and categories
- Summary labels and pie chart use live data from the repository
- Added a Refresh button to reload analysis

---

### Day 6:
- Improved error handling for git log execution (exit codes, stderr messages)
- UI now displays status messages and shows an error dialog on failures
- Shortened hashes safely without risking substring errors
- Displayed the active repository path in the toolbar for clarity

