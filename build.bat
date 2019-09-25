dir /s /B src\*.java > build\sources.txt
javac -Xlint:unchecked -target 8 -d build @build\sources.txt
cd build
jar cmfv spmanifest.mf SandPile.jar com *.class
cd ..