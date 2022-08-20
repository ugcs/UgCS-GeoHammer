#!/bin/sh
SCRIPTPATH=$(dirname "$0")
$SCRIPTPATH/jre17/bin/java -Xmx2g -cp $SCRIPTPATH/geohammer-jar-with-dependencies.jar com.ugcs.gprvisualizer.app.MainGeoHammer
