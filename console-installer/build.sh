mvn clean package assembly:single
echo "Removing dist folder"
rm -rf dist
echo "Creating dist folder"
mkdir dist
echo "Copy $(ls target | grep jar-with-dependencies) to dist"
cp target/console-installer-*-jar-with-dependencies.jar dist/console-installer.jar
echo "Copy run script"
cp src/script/run.sh dist/run
chmod +x dist/run

echo "Done"
