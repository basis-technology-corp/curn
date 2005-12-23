#!/bin/sh
#
# Front-end Unix shell script for the curn RSS reader
#
# $Id$
# ---------------------------------------------------------------------------

vm_opts=
while [ $# -gt 0 ]
do
    case "$1" in
        -D*|-X*)
            vm_opts="$vm_opts $1"
	    shift
	    ;;
        *)
	    break
	    ;;
    esac
done

exec $JAVA_HOME/bin/java \
-ea \
-client \
$vm_opts \
-classpath \
$INSTALL_PATH/lib/ocutil.jar:\
$INSTALL_PATH/lib/xerces.jar:\
$INSTALL_PATH/lib/activation.jar:\
$INSTALL_PATH/lib/mail.jar:\
$INSTALL_PATH/lib/freemarker.jar:\
$INSTALL_PATH/lib/curn.jar:\
$CLASSPATH
org.clapper.curn.Tool "${@}"
