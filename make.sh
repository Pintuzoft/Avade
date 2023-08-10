#!/bin/bash                                                                                                                               
          
export JAVA_HOME=/usr/lib/jvm/java17
                                                                                                                                
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
   ant ${1} ${2} 2>&1 | while read line; do
      if [ -z "$1" ]; then
         echo $line | grep -i 'warning\|error';
      else
         echo $line;
      fi
   done
}

function install {      
   echo "Installing to ~/avade/";
   mkdir -p ~/avade
   cp -v dist/Avade.jar ~/avade/avade.jar
   cp -Rv dist/lib ~/avade/
   cp template.conf ~/avade/
   cp reference.conf ~/avade/
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


