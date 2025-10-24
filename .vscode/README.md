# FirstJavaFX Project

This project is a JavaFX application integrated with OpenCV, built and run using Visual Studio Code.

---

## Prerequisites

- **Java JDK** 17 or 21 installed and configured in VS Code
- **JavaFX SDK** downloaded and placed in a stable location (e.g., `C:/javafx-sdk-21.0.8/lib`)
- **OpenCV** Java bindings JAR (included in `lib/opencv-4120.jar`)
- VS Code with Java Extension Pack installed

---

## Project Structure

FirstJavaFX/
├── lib/
│ ├── javafx-sdk-21.0.8/lib/ # Your JavaFX SDK JAR files here
│ ├── opencv-4120.jar # OpenCV Jar
├── src/
│ ├── application/
│ │ ├── Main.java # Main application launcher
│ │ ├── FXController.java # Controller for JavaFX
│ │ ├── camtest.java
│ │ ├── FirstJFX.fxml # FXML UI layout
│ │ └── application.css # Stylesheet
│ └── utils/
│ └── Utils.java
├── .vscode/
│ ├── launch.json # Launch configuration with module path and VM args
│ └── settings.json # Referenced libraries setup
└── README.md


---
## Editing FXML with Scene Builder

Install Scene Builder
Download from Gluon Scene Builder and install it.

Associate .fxml files with Scene Builder in VS Code

Right-click any .fxml file (e.g., FirstJFX.fxml).

Select Open With → Scene Builder.

If not available, configure VS Code to recognize Scene Builder:

Go to File → Preferences → Settings (or Ctrl+,).

Search for Scene Builder and set the executable path to your Scene Builder installation (e.g., C:/Program Files/SceneBuilder/SceneBuilder.exe).

Design your UI

Open FirstJFX.fxml in Scene Builder.

Drag and drop controls, panes, and components.

Save changes — they will update the FXML file directly.

Link with Controller

Set fx:controller to application.FXController.

Assign fx:id values to UI components.

Match those IDs with fields in your FXController.java.

## Running the Application

1. **Configure `launch.json`**

[
{
"type": "java",
"name": "Launch JavaFX Application",
"request": "launch",
"mainClass": "application.Main",
"vmArgs": "--module-path {path to javafxSDK lib} --add-modules javafx.controls,javafx.fxml,javafx.swing -Djava.library.path={path to opencv.java.dll}"
}
]


2. **Open VS Code**
3. **Run the project using Run and Debug (`F5`)**  
   Make sure you use the Run and Debug sidebar or `F5` to launch, not the in-source CodeLens "Run" links, as those ignore VM arguments.

---

## Notes

- The `--module-path` must point to the **directory containing JavaFX JAR libraries**, not just a single JAR.
- `-Djava.library.path` should point to the location of **native libraries**, like OpenCV native DLLs/so files if required.
- Make sure your VS Code is using the correct JDK that matches JavaFX SDK compatibility (JDK 17 or 21 recommended).
- For more complex dependency management, consider migrating to Maven or Gradle.

---

## Useful Links

- [Official JavaFX Documentation](https://openjfx.io/openjfx-docs/)
- [OpenCV Java](https://opencv.org/)
- [VS Code Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

---

## Troubleshooting

- If `JavaFX runtime components are missing`, verify the `vmArgs` paths and ensure you launch with `Run and Debug`, not CodeLens Run.
- If OpenCV libraries fail to load, check your `-Djava.library.path` setting points to the right native DLL or SO files folder.

---

Created by Dzaki

