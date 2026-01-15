# UM_VISA_APP_FINAL

This repository contains the rebuilt UM Visa Application project. It has been configured to resolve previous build and runtime issues, including namespace conflicts, Gradle script errors, and resource duplication.

## Project Structure
This is a multi-module Android project with the following modules:
- `app` (main application module)
- `student_module`
- `UMVISAMATE`
- `UMVISAMATE_M3`
- `UMVisaMate1`
- `UMVISAMATE2_M4`
- `UMVISAMATEM2-Faculty`

## Setup and Build Instructions
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/cyberjaya101/UM_VISA_APP_FINAL.git
    ```
2.  **Open in Android Studio**: Open the cloned project in Android Studio.
3.  **Sync Gradle**: Android Studio should automatically prompt you to sync Gradle. If not, click the "Sync Project with Gradle Files" button.
4.  **Clean and Rebuild**: Go to `Build > Clean Project` and then `Build > Rebuild Project`.
5.  **Run**: You should now be able to run the application on an emulator or a physical device.

## Troubleshooting
-   **"Unresolved reference" errors in Kotlin files**: If you see red errors related to `R` imports or other classes, use Android Studio's quick fix (Alt + Enter on Windows/Linux, Option + Return on Mac) to import the correct classes.
-   **Gradle Sync Issues**: Ensure your Android Studio is up-to-date and that you have the necessary SDK components installed (Platform 34, Build-Tools 34.0.0 or 35.0.0).

Feel free to contribute or report any issues!
