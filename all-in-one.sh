#!/bin/bash
TEST_INSTALL_DIR=$1
CURRENT_DIR=`pwd`
if [ -z "$1" ]
  then
    echo "No install dir supplied. Using `pwd`/../allInOne"
    TEST_INSTALL_DIR=$CURRENT_DIR/../allInOne
fi
mkdir -p $TEST_INSTALL_DIR 2>/dev/null
INSTALL_DIR=$(cd $TEST_INSTALL_DIR; pwd)
cd $CURRENT_DIR


echo "Installing all in one in $INSTALL_DIR"
echo "building Tank release"
mvn clean install -DskipTests -Prelease

echo "extracting tomcat 6..."
unzip -q -n -d $INSTALL_DIR all-in-one-files/apache-tomcat-6.0.41.zip 2>/dev/null

ln -snf $INSTALL_DIR/apache-tomcat-6.0.41 $INSTALL_DIR/tomcat6 2>/dev/null
chmod 755 $INSTALL_DIR/tomcat6/bin/*.sh
mkdir $INSTALL_DIR/tomcat6/db 2>/dev/null
mkdir $INSTALL_DIR/tomcat6/jars 2>/dev/null

echo "extracting agent-standalone..."
unzip -q -n -d $INSTALL_DIR agent/agent_standalone_pkg/target/agent-standalone-pkg.zip 2>/dev/null

echo "copying support libraries and config..."
cp -n all-in-one-files/*.jar $INSTALL_DIR/tomcat6/lib/
cp -n all-in-one-files/server-all-in-one.xml $INSTALL_DIR/tomcat6/conf/server.xml
cp -n all-in-one-files/settings-all-in-one.xml $INSTALL_DIR/tomcat6/settings.xml
cp -n all-in-one-files/context.xml $INSTALL_DIR/tomcat6/conf/context.xml

echo "installing tank war file..."
rm -fr $INSTALL_DIR/tomcat6/webapps/docs $INSTALL_DIR/tomcat6/webapps/examples $INSTALL_DIR/tomcat6/webapps/ROOT 2>/dev/null
cp web/web_ui/target/tank.war $INSTALL_DIR/tomcat6/webapps/ROOT.war

echo "Creating start script at $INSTALL_DIR/start.sh ..."
echo "#!/bin/bash">$INSTALL_DIR/start.sh
echo "echo \"Starting Tomcat...\"">>$INSTALL_DIR/start.sh
echo "export JAVA_OPTS=\"-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m -Djava.awt.headless=true\"">>$INSTALL_DIR/start.sh
echo "cd $INSTALL_DIR/tomcat6/">>$INSTALL_DIR/start.sh
echo "bin/startup.sh 2> \&1 >/dev/null">>$INSTALL_DIR/start.sh
echo "echo \"Tomcat started.\"">>$INSTALL_DIR/start.sh
echo "cd $INSTALL_DIR/agent-standalone/">>$INSTALL_DIR/start.sh
echo "echo \"Starting Agent...\"">>$INSTALL_DIR/start.sh
echo "./run.sh 2> \&1 >/dev/null">>$INSTALL_DIR/start.sh
echo "echo \"Agent started.\"">>$INSTALL_DIR/start.sh
chmod 755 $INSTALL_DIR/start.sh 2>/dev/null

echo "Creating stop script at $INSTALL_DIR/stop.sh..."
echo "#!/bin/bash">$INSTALL_DIR/stop.sh
echo "echo \"Stopping Tomcat...\"">>$INSTALL_DIR/stop.sh
echo "$INSTALL_DIR/tomcat6/bin/shutdown.sh 2> \&1 >/dev/null">>$INSTALL_DIR/stop.sh
echo "echo \"Stopping Agent...\"">>$INSTALL_DIR/stop.sh
echo "kill \$(ps aux | grep '[a]gent-standalone' | awk '{print \$2}') 2> \&1 >/dev/null">>$INSTALL_DIR/stop.sh
echo "kill \$(ps aux | grep '[a]piharness' | awk '{print \$2}') 2> \&1 >/dev/null">>$INSTALL_DIR/stop.sh
echo "kill \$(ps aux | grep '[t]omcat' | awk '{print \$2}') 2> \&1 >/dev/null">>$INSTALL_DIR/stop.sh
echo "echo \"Tomcat and Agents stopped.\"">>$INSTALL_DIR/stop.sh
chmod 755 $INSTALL_DIR/stop.sh 2>/dev/null

echo "#!/bin/bash">$INSTALL_DIR/agent-standalone/run.sh
echo "controller=\"http://localhost:8080\"">>$INSTALL_DIR/agent-standalone/run.sh
echo "host=\`hostname\`">>$INSTALL_DIR/agent-standalone/run.sh
echo "capacity=400">>$INSTALL_DIR/agent-standalone/run.sh
echo "nohup java -jar agent-standalone-all.jar -controller=\$controller -host=\$host -capacity=\$capacity &">>$INSTALL_DIR/agent-standalone/run.sh



echo "Finished installing all in one in $INSTALL_DIR"
echo "Run $INSTALL_DIR/start.sh to start the all in one server and $INSTALL_DIR/stop.sh to kill it"