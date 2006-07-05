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
-classpath \
$INSTALL_PATH/lib/curnboot.jar \
-ea \
-client \
$vm_opts \
-Dcurn.home=$INSTALL_PATH \
org.clapper.curn.Bootstrap \
$INSTALL_PATH/lib \
$INSTALL_PATH/plugins \
@user.home/curn/plugins \
@user.home/.curn/plugins \
@user.home/curn/lib \
@user.home/.curn/lib \
-- \
org.clapper.curn.Tool "${@}"
