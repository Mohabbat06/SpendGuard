#!/bin/sh
# Gradle start up script for POSIX generated for SpendGuard.
##############################################################################

app_path=$0
while [ -h "$app_path" ]; do
    APP_HOME=${app_path%/*}
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in
      /*) app_path=$link ;;
      *)   app_path=$APP_HOME/$link ;;
    esac
done

APP_HOME=$( cd "${app_path%/*}" && pwd ) || exit
APP_BASE_NAME=${0##*/}

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
