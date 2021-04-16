#!/bin/bash

#RemoteServer.sh
# User-defined variables
# Find explanation of TIMEOUT and BROWER_TIMEOUT from https://seleniumhq.github.io/docs/remote.html
# Currently, we set them to 0 so that the browser will not go away until we stop it.
TIMEOUT=0
BROWER_TIMEOUT=0

if [ `uname -m` == 'x86_64' ]; then
	GECKO_DRIVER=geckodriver_64
else
	GECKO_DRIVER=geckodriver
fi

SELENIUM_SERVER_JAR_LOC=`ls -t $SELENIUM_PLUS/libs/selenium-server-standalone*`
echo "The slenium jar is ${SELENIUM_SERVER_JAR_LOC}"

"$SELENIUM_PLUS/Java64/jre/bin/java" -Xms512m -Xmx2g -Dwebdriver.chrome.driver="$SELENIUM_PLUS/extra/chromedriver" -Dwebdriver.gecko.driver="$SELENIUM_PLUS/extra/$GECKO_DRIVER" -jar "$SELENIUM_SERVER_JAR_LOC" -timeout $TIMEOUT -browserTimeout $BROWER_TIMEOUT
