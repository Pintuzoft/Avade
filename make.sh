#!/bin/bash                                                                                                                               
          
# Set JAVA_HOME yourself to use another java: JAVA_HOME=/path/to/jdk ./make.sh
if [ -z "$JAVA_HOME" ]; then
   for DIR in /usr/lib/jvm/java-17-openjdk /usr/lib/jvm/java-17 /usr/lib/jvm/java17; do
      if [ -x "$DIR/bin/javac" ]; then
         export JAVA_HOME=$DIR
         break
      fi
   done
fi
                                                                                                                                
MISSING="";                                                                                                                               

# Functions

function addMissing {
   MISSING+="${1} ";
}

function sendError {
   echo "Error. The following softwares is missing:";
   YUMSTR=" sudo yum install";
   for NAME in $MISSING; do
      YUMSTR+=" ${NAME}";
      echo " - ${NAME}";
   done
   echo "Install the missing software. On centos use:";
   echo "  ${YUMSTR}";
   exit 1;
}

# Check User

USERNAME=$(whoami);
if [ "$USERNAME" == "root" ]; then
   echo "Error: dont install or run this software as root.";
   exit 1;
fi


# Dependencies

#DEPENDENCIES="java:java javac:openjdk-devel ant:ant";
#IFS=':' read -ra DATA <<< "$SOFTWARE";
#for SOFTWARE in $DEPENDENCIES; do
#   FILE=${DATA[0]};
#   RPM=${DATA[1]};
#   echo -n "Checking for ${FILE^} - ";
#   FILEINFO=$(which ${FILE} 2>&1 | cut -d ' ' -f 1);
#   if [ -f "$FILEINFO" ]; then
#      echo "OK!";
#   else
#      echo "Fail!";
#      addMissing $RPM;
#   fi
#done

#if [ "$MISSING" != "" ]; then
#  sendError
#fi


# Compile / Install

function compile {
   if ! command -v ant > /dev/null 2>&1; then
      echo "Note: ant is not installed, Avade is not compiled. Using the dist/Avade.jar that came with the source.";
   else
      ant ${1} ${2} 2>&1 | while read line; do
         if [ -z "$1" ]; then
            echo $line | grep -i 'warning\|error';
         else
            echo $line;
         fi
      done
   fi
   # The mailer needs only javac
   if [ "$1" != "-q" ] && command -v javac > /dev/null 2>&1; then
      mailer/build.sh;
   else
      echo "Note: the mailer is not compiled. Using the dist/AvadeMailer.jar that came with the source.";
   fi
}

function install {      
   echo "Installing to ~/avade/";
   mkdir -p ~/avade
   cp -v dist/Avade.jar ~/avade/avade.jar
   cp -v dist/AvadeMailer.jar ~/avade/mailer.jar
   # Libraries from an older version are not used anymore, remove them
   for JAR in ~/avade/lib/*.jar ~/avade/lib/mail/*.jar; do
      [ -e "$JAR" ] && [ ! -e "dist/${JAR#$HOME/avade/}" ] && rm -v "$JAR";
   done
   cp -Rv dist/lib ~/avade/
   cp template.conf ~/avade/
   cp reference.conf ~/avade/
   cp mailer-template.conf ~/avade/
   cp avade.sh ~/avade/
   chmod +x ~/avade/avade.sh
   echo "Installed: $(ls -l ~/avade/avade.jar)";
}

if [ -z "$1" ]; then
   TYPE="COMPILE";
else
   TYPE="$1";
fi


case ${TYPE^^} in
   "COMPILE")
       echo "=== COMPILE ===";
       compile;
       echo "Done.";
       ;;
   
   "DEBUG")
       echo "=== COMPILE (debug) ===";
       compile -d;
       echo "Done.";
       ;;
   
   "INSTALL")
       echo "=== COMPILE ===";
       compile;
       echo "=== INSTALL ===";
       install;
       echo "Done.";
       ;;

   "CLEAN")
       echo "=== CLEAN ===";
       compile -q clean
       echo "Done.";
       ;;

   *)
       echo "Error: No matching action..";
       echo "Syntax: $0 <COMPILE|DEBUG|INSTALL|CLEAN>";
       ;;
esac


