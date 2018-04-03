# Startup Servlet for AEM

This project meant to be a pair of:
- servlet that shares data on AEM startup level and specific packages (are they installed?)
- command line, executable jar that install and WAITS until package is not installed (which is crucial thing in terms of automating builds and controlling set up of system)

Project structure
- console-installer - regular executable JAR file
- core - OSGi bundle installable through `ui.apps`
- ui.apps - FileVault package project (includes `core` during packaging)

## Contribution

Any help is warm welcomed. Don't be shy just create a PR!
