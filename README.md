# Face-Detector

A small Java / JavaFX example that demonstrates face detection using OpenCV.

This project contains Java source code (in `src/`), OpenCV Java wrapper JAR (`lib/opencv-4120.jar`),
and a bundled JavaFX SDK (`lib/javafx-sdk-21.0.8`). It requires the native OpenCV binaries (platform-specific) to be available
when running.

## Prerequisites

- JDK 17 or later installed and `java`/`javac` on your PATH.
- JavaFX SDK (this repo includes `lib/javafx-sdk-21.0.8` but you may use a system installation).
- OpenCV Java JAR: `lib/opencv-4120.jar` (included).
- OpenCV native binaries (DLLs on Windows) — NOT included. Download the matching OpenCV release for your platform
  (for example OpenCV 4.x) and note the folder that contains the native library (e.g. `opencv_java412.dll`).

Note: The repository currently contains the JavaFX SDK binaries (large files). GitHub may warn about large files;
consider using Git LFS or installing JavaFX separately instead of committing the binary SDK into the repo.

## Quick run (Windows PowerShell)

Open PowerShell and run the commands below from the project root (where this `README.md` lives).

1) Compile all Java sources into `out/` (the one-liner uses PowerShell to compile every `.java` file):

```powershell
Set-Location -LiteralPath 'C:\path\to\firstjavafx'  # change to your project folder
mkdir out -ErrorAction SilentlyContinue
Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { javac -cp "lib\opencv-4120.jar" -d out $_.FullName }
```

2) Run the application. Replace the `-Djava.library.path` value with the folder that contains your OpenCV native DLLs
(for example `C:\opencv\build\java\x64` or wherever `opencv_java412.dll` is located):

```powershell
java --module-path "lib\javafx-sdk-21.0.8\lib" --add-modules javafx.controls,javafx.fxml \
  -cp "out;lib\opencv-4120.jar" -Djava.library.path="C:\path\to\opencv\build\java\x64" application.Main
```

Alternative: set the native library folder on your PATH instead of using `-Djava.library.path`:

```powershell
$env:PATH = "C:\path\to\opencv\build\java\x64;" + $env:PATH
java --module-path "lib\javafx-sdk-21.0.8\lib" --add-modules javafx.controls,javafx.fxml -cp "out;lib\opencv-4120.jar" application.Main
```

## Running from an IDE

- In IntelliJ or VS Code (with Java extensions), add `lib/opencv-4120.jar` to the project libraries.
- Add VM options when running the application:
  --module-path <path-to-javafx-lib> --add-modules javafx.controls,javafx.fxml
  and set VM arg `-Djava.library.path=<path-to-opencv-native>` so the native OpenCV library can be loaded.

## Common issues

- UnsatisfiedLinkError: If you see errors like `java.lang.UnsatisfiedLinkError: no opencv_java412 in java.library.path`,
  make sure the native OpenCV DLL (for Windows) is downloaded and that you passed the correct folder via
  `-Djava.library.path` or added it to `PATH`.
- JavaFX errors (module not found): ensure you are passing `--module-path` and `--add-modules` pointing to the JavaFX
  SDK `lib` folder.
- Large files warning on push: the JavaFX SDK contains big DLLs (for example `jfxwebkit.dll` > 50MB). Consider
  removing the SDK from the repo and add installation instructions instead, or enable Git LFS for those files.

## Notes

- Main class: `application.Main` (the app loads FXML from `src/application/FirstJFX.fxml`).
- Haar cascades and other detector models are in the `resources/` folder.

If you'd like, I can:
- add a short PowerShell script to automate compile+run,
- add a `.gitattributes` and example `.lfsconfig` for large files,
- or remove the JavaFX binaries from the repo and add installation instructions instead.

Enjoy exploring the face-detector example!

## References / Learned from

- I learned and adapted ideas from the `opencv-java/face-detection` repository:
  https://github.com/opencv-java/face-detection


