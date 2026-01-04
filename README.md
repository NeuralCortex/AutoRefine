# AutoRefine

**AutoRefine** is a JavaFX desktop application designed to help Java developers maintain clean and efficient codebases by identifying and removing unused resources.

The tool focuses on two common sources of technical debt:

- Unused keys in localization `.properties` files
- Unused public fields and enum constants in Java source files (e.g., obsolete global configuration parameters)

AutoRefine performs static analysis across project directories, presents results in an intuitive interface, and enables safe removal of dead code with automatic backups.

## Screenshots

![AutoRefine Main Interface](https://github.com/NeuralCortex/AutoRefine/blob/main/auto.png)

## Features

- Directory scanning for `.properties` and `.java` files
- Detection of unused property keys through cross-reference analysis with Java source code
- Identification of unused public fields and enum constants
- Tree-view navigation of analyzed files and detected items
- Selective removal with visual preview
- Optional alphabetical sorting of properties files
- Automatic backup creation (`.bak`) before any file modification
- Context-menu-driven save operations

## Requirements

- Java 25 (JDK recommended)

## Usage

### Properties Tab – Localization Cleanup

1. Select the root source directory to analyze.
2. Click **Check**. The application scans for all `.properties` files, analyzes key usage across the codebase, and highlights scanned files in orange.
3. Review the results. Optionally remove unused keys or enforce alphabetical sorting.
4. Right-click the desired file in the tree view and select **Save**. A backup file is created automatically.

### Fields & Enums Tab – Java Source Cleanup

1. Select the root source directory to analyze.
2. Choose a specific `.java` file from the tree view.
3. Click **Check** to detect unused public fields and enum constants.
4. Select the items you wish to remove.
5. Right-click the file in the tree view and select **Save**. A backup file is created automatically.

**Recommendation**: Use version control (e.g., Git) and review changes carefully before applying modifications in production codebases.

## Technologies

- **IDE**: Apache NetBeans 28
- **Java Version**: JDK 25
- **UI Framework**: JavaFX 25
- **UI Design Tool**: Gluon Scene Builder

## Building a Windows Installer (.exe)

AutoRefine includes an Inno Setup script (`AutoRefine.iss`) to create a native Windows installer.

### Prerequisites

- Inno Setup 6 or later (download from https://jrsoftware.org/isinfo.php)

### Steps

1. Build the application within NetBeans.
2. Adjust paths in `AutoRefine.iss`.
3. Open `AutoRefine.iss` in Inno Setup Compiler.
4. Compile the script (Run → Compile or press F9).

This will generate `AutoRefine x.x.x Setup.exe` – a single-file installer that:
- Installs the application with libs
- Creates desktop and Start Menu shortcuts
- Allows users to run AutoRefine without NetBeans

## Attribution

<a href="https://www.flaticon.com/free-icons/cleaning-browser-history">Cleaning icons created by Dewi Sari - Flaticon</a>