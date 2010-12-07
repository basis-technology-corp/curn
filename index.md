---
title: curn, a customizable, utilitarian RSS notifier
layout: withTOC
---

# Introduction

*curn* is an RSS reader. It scans a configured set of URLs, each one
representing an RSS feed, and summarizes the results. By default, curn
keeps track of individual items within each RSS feed, using an on-disk
cache; when using the cache, it will suppress displaying information for
items it has already processed (though that behavior can be disabled).

Unlike many RSS readers, *curn* does not use a graphical user interface. It
is a command-line utility, intended to be run periodically in the
background by a command scheduler such as [cron][](8) (on Unix-like systems) or
the Windows Scheduler Service (on Windows).

*curn* is written entirely in [Java][] and can read RSS feeds from any URL
that's supported by Java's runtime. When querying HTTP sites, *curn* uses
the HTTP `If-Modified-Since` and `Last-Modified` headers to suppress
retrieving and processing feeds that haven't changed. By default, it also
requests that the remote HTTP server gzip the XML before sending it. (Some
HTTP servers honor the request; some don't.) These measures both minimize
network bandwidth and ensure that *curn* is as kind as possible to the
remote RSS servers.

# Extensibility

*curn* can be extended in a number of ways.

## Choice of Output Formats

*curn* supports several output formats; you can configure one or more
output handlers in *curn*'s [configuration file][]. A sample of *curn*'s
HTML output is [here][html-sample-output]. A sample of *curn*'s plain text
output is [here][text-sample-output].

*curn* supports, and uses internally, the [FreeMarker][] template engine;
you can easily generate another output format by
[writing your own FreeMarker template][write-freemarker-template]. In
addition, you can write your own output handlers, in Java or in any
scripting language supported by the `javax.script` API. See
[Writing Your Own Output Handler][write-output-handler] in the
[*curn* User's Guide][users-guide] for more details.

## Plug-ins

*curn* supports custom Java plug-ins that can intercept various phases of
*curn* processing and can enhance or modify *curn*'s behavior. See
[Plug-ins][] in the [*curn* User's Guide][users-guide] for more details.

## Use an RSS Parser of your Choice

*curn* can be adapted to use any Java RSS parser. By default, it uses the
[ROME][] parser, which can handle RSS feeds in [Atom][] format and RSS
formats [0.91][rss-0.91], 0.92, [1.0][rss-1.0] and [2.0][rss-2.0]. *curn*
can easily be adapted to work with other Java-based RSS parser. Adding an
adapter for a new underlying RSS parser technology requires implementing
several interfaces and providing concrete implementations for a small
number of abstract classes.

# Installation

Previous versions of *curn* could be installed manually or via an
[IzPack][]-generated installer. With the 3.0 release of *curn*, only the
graphical installer is supported. That's because the installer creates a
custom front-end shell script (for Unix and Mac OS X) or command script
(for Windows) that sets up the proper runtime environment before running
curn. Replicating that work manually is enough of a pain that it is no
longer officially supported.

[IzPack][] installers support both a graphical installation mode (the
default) and a command-line installation mode (by specifying a `-console`
parameter to the invocation).

To install *curn*:

* Download the installer jar from the [downloads area][].
* Run the installer jar: `java -jar install-curn-xxxxx.jar`
* Follow the instructions in the graphical installation screens.

Once you've installed the *curn* via the graphical installer, you should
run it via the *curn* shell script (for Unix systems) or the *curn.bat* DOS
script (for Windows systems), located in the bin directory where you
installed *curn*.

# Using *curn*

For complete instructions on configuring and using *curn*, please see the
[*curn* User's Guide][users-guide].

# Building *curn*

*curn* does not currently build with [Maven][], so building it from source
is a bit of a pain.

## Third-party Software

Before building *curn*, you'll need

1. [Jakarta Ant][Ant], version 1.6.5 or better.
2. The [clapper.org Java Utility Library][].
3. The [prerequisite jar files][ocutil-jars] for the
   [clapper.org Java Utility Library][].
4. The [Jakarta Commons Logging][jcl] jar.
5. The [JavaMail][] jar.
6. The [JavaBeans Activation Framework][jaf] (JAF), if you're using a 1.5 JDK.
   (JAF is bundled with Java 1.6.)
7. A SAX2 XML Parser, such as [Apache Xerces][].
8. The [Jakarta Bean Scripting Framework][bsf] jar file.
9. The `izpack-compiler.jar` file from the [IzPack][] distribution. This is
   only necessary if you're going to build the installer.
   
The easiest way to get everything except IzPack and Ant is to install *curn*
via the installer. Be sure to install the source, too.

[FreeBSD][] users will find ports for many of the third-party libraries.
Linux users may find packages (RPMs, DEBs, etc.) for those libraries.

## Prepare the Build Environment


1. Once you've downloaded the various third-party jar files, place them in
   a directory somewhere.
2. Download the source from the [downloads area][] and unzip it, or
   get the code from the [GitHub repository][].
3. Change your working directory to the top-level `curn` source directory.
4. In the topmost source directory (i.e., the directory containing the
   `build.xml` file), create a file called `build.properties` containing the
   following line:
   
    third.party.jar.dir: /path/to/directory/containing/jars

## Building

* Type `ant build` to compile the code and create the jar file. The jar file
  ends up in the `build/lib` subdirectory.
* To create the Javadocs, type `ant javadocs`. (This step is optional.)
* To create version-stamped release files, type `ant release`. The
  resulting files end up in the `build/release` directory.
* To create the installer, type `ant release installer`. The installer jar
  file will end up in the `build/release` directory.

# Keeping Up to Date

To be notified automatically of new releases of *curn*, please join the
(low-volume) `curn-users` mailing list, at
[http://groups.google.com/group/curn-users/](http://groups.google.com/group/curn-users/).

To track the *curn* source code, please see the *curn* [GitHub repository][].

# Author

Brian M. Clapper, [bmc@clapper.org][]

# Acknowledgements

My friend, former co-worker, and (to our mutual surprise) fourth cousin,
Steve Sapovits, suggested the name *curn*.

Portions of *curn* were developed by, and funded by, [ArdenTex, Inc][]. and
donated back to Brian M. Clapper and the *curn* project.

Portions of *curn* were developed by [ArdenTex, Inc][]., under contract
with [Dow Jones & Co.][dow-jones]. Dow Jones has graciously donated the
source code back to the *curn* project.

# Frequently Asked Questions

Please see the [FAQ][].

# Related Links

* [CHANGELOG][]: Change log for the current release
* [*curn* User's Guide][users-guide]
* [clapper.org Java Utility Library][]
* [FreeMarker][]
* [What is RSS?][], an introduction suited to programmers.
* [All About RSS][], if you're not as technically minded.
* [RSS 0.91 specification][rss-0.91]
* [RSS 1.0 specification][rss-1.0]
* [RSS 2.0 specification][rss-2.0]
* [Atom specification][Atom]
* The [ROME][] RSS parser library
* [*rawdog*][rawdog], a Python-based RSS reader, is similar to *curn*, in
  spirit, features and invocation. I had no idea *rawdog* existed when I
  wrote *curn*.

# Miscellaneous

I *use* curn myself, every day, to poll a variety of RSS feeds. I develop,
test and run curn on [Mac OS X][], [FreeBSD][] and [Ubuntu][] Linux.
have successfully built it on:

* Mac OS X, using the Apple-supplied 1.6 JDK
* FreeBSD, using the native FreeBSD 1.6 JDK
* Ubuntu 9 and 10, using the Sun Linux 1.6.0 JDK

# Copyright and License

*curn* is copyright &copy; 2004-2010 Brian M. Clapper and is released under
a [BSD License][].

# Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [GitHub repository][] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the *curn* project
  under a [BSD License][].

[BSD License]: license.html
[GitHub repository]: http://github.com/bmc/curn
[GitHub]: http://github.com/bmc/
[downloads area]: http://github.com/bmc/curn/downloads
[bmc@clapper.org]: mailto:bmc@clapper.org
[cron]: http://www.freebsd.org/cgi/man.cgi?query=cron&apropos=0&sektion=0&manpath=FreeBSD+5.2-RELEASE+and+Ports&format=html
[Java]: http://www.oracle.com/technetwork/java/index.html
[configuration file]: users-guide/index.html#ConfigFile
[html-sample-output]: users-guide/output/HTMLOutput.html
[text-sample-output]: users-guide/output/TextOutput.txt
[FreeMarker]: http://www.freemarker.org/
[write-freemarker-template]: users-guide/index.html#WriteFreeMarkerTemplate
[write-output-handler]: users-guide/index.html#NewOutputHandler
[users-guide]: users-guide/index.html
[Plug-ins]: users-guide/index.html#Plug-ins
[ROME]: https://rome.dev.java.net/
[Atom]: http://www.atomenabled.org/developers/
[rss-0.91]: http://backend.userland.com/rss091
[rss-1.0]: http://web.resource.org/rss/1.0/
[rss-2.0]: http://cyber.law.harvard.edu/rss/rss.html
[IzPack]: http://www.izforge.com/izpack/
[ArdenTex, Inc.]: http://www.ardentex.com/
[Down Jones & Co.]: http://www.dowjones.com/
[Ubuntu]: http://www.ubuntu.com/
[FreeBSD]: http://www.freebsd.org/
[Mac OS X]: http://www.apple.com/macosx/
[clapper.org Java Utility Library]: http://bmc.github.com/javautil/
[What is RSS?]: http://www.xml.com/pub/a/2002/12/18/dive-into-xml.html
[All About RSS]: http://www.faganfinder.com/search/rss.php
[rawdog]: http://offog.org/code/rawdog.html
[BSD License]: license.html
[GitHub repository]: http://github.com/bmc/curn
[GitHub]: http://github.com/bmc/
[downloads area]: http://github.com/bmc/curn/downloads
[bmc@clapper.org]: mailto:bmc@clapper.org
[cron]: http://www.freebsd.org/cgi/man.cgi?query=cron&apropos=0&sektion=0&manpath=FreeBSD+5.2-RELEASE+and+Ports&format=html
[Java]: http://www.oracle.com/technetwork/java/index.html
[configuration file]: users-guide/index.html#ConfigFile
[html-sample-output]: users-guide/output/HTMLOutput.html
[text-sample-output]: users-guide/output/TextOutput.txt
[FreeMarker]: http://www.freemarker.org/
[write-freemarker-template]: users-guide/index.html#WriteFreeMarkerTemplate
[write-output-handler]: users-guide/index.html#NewOutputHandler
[users-guide]: users-guide/index.html
[Plug-ins]: users-guide/index.html#Plug-ins
[ROME]: https://rome.dev.java.net/
[Atom]: http://www.atomenabled.org/developers/
[rss-0.91]: http://backend.userland.com/rss091
[rss-1.0]: http://web.resource.org/rss/1.0/
[rss-2.0]: http://cyber.law.harvard.edu/rss/rss.html
[IzPack]: http://www.izforge.com/izpack/
[ArdenTex, Inc.]: http://www.ardentex.com/
[Down Jones & Co.]: http://www.dowjones.com/
[Ubuntu]: http://www.ubuntu.com/
[FreeBSD]: http://www.freebsd.org/
[Mac OS X]: http://www.apple.com/macosx/
[clapper.org Java Utility Library]: http://bmc.github.com/javautil/
[What is RSS?]: http://www.xml.com/pub/a/2002/12/18/dive-into-xml.html
[All About RSS]: http://www.faganfinder.com/search/rss.php
[BSD License]: license.html
[GitHub repository]: http://github.com/bmc/curn
[GitHub]: http://github.com/bmc/
[downloads area]: http://github.com/bmc/curn/downloads
[bmc@clapper.org]: mailto:bmc@clapper.org
[cron]: http://www.freebsd.org/cgi/man.cgi?query=cron&apropos=0&sektion=0&manpath=FreeBSD+5.2-RELEASE+and+Ports&format=html
[Java]: http://www.oracle.com/technetwork/java/index.html
[configuration file]: users-guide/index.html#ConfigFile
[html-sample-output]: users-guide/output/HTMLOutput.html
[text-sample-output]: users-guide/output/TextOutput.txt
[FreeMarker]: http://www.freemarker.org/
[write-freemarker-template]: users-guide/index.html#WriteFreeMarkerTemplate
[write-output-handler]: users-guide/index.html#NewOutputHandler
[users-guide]: users-guide/index.html
[Plug-ins]: users-guide/index.html#Plug-ins
[ROME]: https://rome.dev.java.net/
[Atom]: http://www.atomenabled.org/developers/
[rss-0.91]: http://backend.userland.com/rss091
[rss-1.0]: http://web.resource.org/rss/1.0/
[rss-2.0]: http://cyber.law.harvard.edu/rss/rss.html
[IzPack]: http://www.izforge.com/izpack/
[ArdenTex, Inc.]: http://www.ardentex.com/
[Down Jones & Co.]: http://www.dowjones.com/
[Ubuntu]: http://www.ubuntu.com/
[FreeBSD]: http://www.freebsd.org/
[Mac OS X]: http://www.apple.com/macosx/
[clapper.org Java Utility Library]: http://bmc.github.com/javautil/
[What is RSS?]: http://www.xml.com/pub/a/2002/12/18/dive-into-xml.html
[All About RSS]: http://www.faganfinder.com/search/rss.php
[rawdog]: http://offog.org/code/rawdog.html
[CHANGELOG]: CHANGELOG.txt
[FAQ]: FAQ.html
[Ant]: http://ant.apache.org/
[ocutil-jars]: http://www.clapper.org/software/java/util/install.shtml#Jars
[jcl]: http://jakarta.apache.org/commons/logging/
[JavaMail]: http://www.oracle.com/technetwork/java/index-jsp-139225.html
[jaf]: http://java.sun.com/products/archive/javabeans/jaf102.html
[Apache Xerces]: http://xerces.apache.org/
[bsf]: http://jakarta.apache.org/bsf/
[Maven]: http://maven.apache.org/
[dow-jones]: http://www.dowjones.com/
