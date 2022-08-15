# UgCS GeoHammer

UgCS GeoHammer is a simple to use tool to quickly assess and pre-process GPR (Ground Penetrating Radar) data.

For additional information check [user manual](https://github.com/ugcs/UgCS-GeoHammer/wiki).

To download latest version go to [link](https://github.com/ugcs/UgCS-GeoHammer/releases). After downloading ZIP file extract it to any folder.

## Requirements:
To use GeoHammer you need Java installed on the computer.

Oracle JRE [download link](https://www.oracle.com/java/technologies/javase-jre8-downloads.html).

## How to run application:
To run application:
* On Windows - run `geohammer.exe` file or `start.cmd` script;
* On Mac - run `start.sh` script.

## How to build project:
1. Install Launch4J - [download link](https://sourceforge.net/projects/launch4j/files/launch4j-3/3.14/);
2. Set `launch4j.install.path` in `pom.xml`. _Default installation path: C:\Program Files(x86)\Launch4j_;
3. Copy `launch4j.xml.template` file and rename it to `launch4j.xml`;
4. Edit `launch4j.xml` file and set `${PROJECT_DIR}` as actual project path;
5. Run command: `mvn clean install`;
6. The assembled application is located in `target` folder.