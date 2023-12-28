UgCS GeoHammer is a simple to use tool to quickly assess and pre-process GPR (ground penetrating radar) data.

User manual - https://github.com/ugcs/UgCS-GeoHammer/wiki

To download latest release please navigate here - https://github.com/ugcs/UgCS-GeoHammer/releases - and download ZIP file and extract it to any folder.

To run application:

On Windows - run geohammer.exe or start.cmd

On Mac or linux - run start.sh


Cool guys willing to build project run

mvn clean install -P windows-build

Select profile to build for specific platform. Windows version may be built only with MS Windows environment.

Available profiles are: windows-build, linux-build, macos-build

How to build with specific JDK:

This version is compiled for Java 21 and packaged with JRE 21 for your platform.
If your default installation is less then JDK15, this version will not be compiled by default.

Use this command to run maven build with non-default JDK installation:

mvn -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/path/to/jdk21/bin/javac clean install -P linux-build