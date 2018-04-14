rm -rf dist
mkdir dist
cp target/console-installer-*-jar-with-dependencies.jar dist/console-installer.jar
cp src/script/run.sh dist/run
chmod +x dist/run
