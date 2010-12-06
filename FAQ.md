---
title: curn FAQ
layout: withTOC
---

# Why?

## Why a command-line utility?

I wanted something that was simpler than the existing graphical RSS
readers, something that could run unattended and notify me periodically,
but unobtrusively, when new items were posted to RSS sites of interest. For
me, email is a good notification mechanism. *curn* sends me email every so
often, with updated information from a configured set of RSS feeds; the
resulting snapshot information sits unchanged in my mailbox until I'm ready
to read it.

## Why Java?

A few reasons.

* At the time I wrote *curn*, I'd been using Java professionally for a long
  time. I was comfortable with it (still am) and was familiar with the
  various Java tools for handling XML, generating HTML, etc.
* Java is portable. For this kind of application, "Write Once, Run a Lot of
  Places" is an easily realized goal.
* *curn* was a good application to help shake out my
  [Java utility library][ocutil]. It's also a good testbed for
  experimenting with various Java APIs.
* I can live with the slight performance hit associated with interpreted
  byte-code. *curn* spends most of its time doing network I/O; using a
  native compiled language wouldn't improve its performance dramatically
  enough to be worth having to compile it individually for every supported
  platform. Plus, I typically run it in the background, via *cron*(8), so I
  don't notice that it's a little slower than, say, a native C application
  would be.

## Why doesn't your code follow the [standard Java coding conventions][]?

Because I don't like them. Seriously, though, you're probably referring to
my curly brace style, which is definitely at odds with the recommended Java
style (which, in turns, borrows from one of the more popular C language
coding styles). Put simply, I like white space, so I prefer the so-called
Allman style. Putting braces on their own lines (as opposed to on the same
lines as other code) leads to more readable code. In my opinion of course.
You're free to differ--in your own code.

---

# How?

## What scripting languages can I use to write a script output handler?

The short answer: Any scripting language for which a scripting engine
exists. With Java 1.5, you'll need a scripting language supported by the
Apache Jakarta [Bean Shell Framework][bsf] (BSF). If you're using a Java 6
VM, you can also use the `javax.script` API; there are prebuilt scripting
adapters for that API available at
[https://scripting.dev.java.net/](https://scripting.dev.java.net/). For a
longer answer, see the section in the [curn User's Guide][users-guide] that
describes how to configure an instance of the `ScriptOutputHandler` output
handler.

## Can I write a *perl* script output handler?

Can I write a perl script output handler?

No. There is a BSF scripting language engine that purports to work with
*perl* scripts. It's located at
[http://bsfperl.sourceforge.net/](http://bsfperl.sourceforge.net/). It's
very alpha. Among other things, it doesn't appear to support putting
arbitrary Java types into the BSF framework. (Its mapper class,
`net.sourceforge.bsfperl.PerlPrinter`, supports a limited number of
Java-to-Perl type translations.) This restriction makes it unsuitable for
use with *curn*.

## I've written a Java output handler (or RSS parser adapter). Why won't *curn* find it, even when I put its jar file in my CLASSPATH?

As of version 3.0, *curn* uses its own custom class loader which ignores
the `CLASSPATH` setting. (I may change that in a future release.) The custom
class loader is necessary to support plug-ins. The easiest way to install
your custom output handler (or parser adapter) is to copy its jar file into
one of three places:

* `${curn.home}/lib`
* `${user.home}/curn/lib`
* `${user.home}/.curn/lib`

`${curn.home}` refers to the curn installation directory. `${user.home}` is
the home directory of the user running *curn*.

See the [Installing Supporting Software][],
[Writing Your Own Output Handler][], and
[Using an Unsupported RSS Parser][] sections in the
[*curn* User's Guide][users-guide] for more details.

## I've written a plug-in. How do I install it?

Pack the plug-in class(es) in a jar file, and copy the jar file to one of
these directories:

* `${curn.home}/plugins`
* `${user.home}/curn/plugins`
* `${user.home}/.curn/plugins`

`${curn.home}` refers to the curn installation directory. `${user.home}` is
the home directory of the user running *curn*.

For more information, see the [Installing Plug-ins][] and
[Overview of Plug-In Support][] sections in the
[*curn* User's Guide][users-guide].

## How does *curn* know whether an article or feed has changed?

The answer, as with most things, is, "It depends." There are several
techniques *curn* uses to detect whether something remote has changed. Each
has flaws. First, recall that a remote feed is an XML file.

1. When pulling down a feed, curn sets the HTTP `If-Modified-Since` header
   to the date of the last time it pulled down the feed. This header tells
   the remote HTTP server not to deliver the feed at all if it hasn't
   changed since then. Whether the remote HTTP server actual honors that
   header or not, however, is outside *curn*'s control.

2. If *curn* actually gets the feed, then either (a) the remote HTTP server
   doesn't honor the `If-Modified-Since` header, or (b) it told *curn* the
   feed has changed. Of course, this doesn't mean the feed has actually
   changed. In my experience, plenty of sites continue to send old data,
   with updated dates.

3. The next thing *curn* does is check the `Last-Modified` header in the
   response. This header, if present, is supposed to tell the HTTP client
   (browser, *curn*, whatever) the date that the remote document was last
   modified. Again, it isn't always present; worse, when it is present, it
   isn't always right. But *curn* knows when it last saw the feed, so if
   the `Last-Modified` header in the response isn't newer than the last
   time *curn* saw the feed, it skips the feed entirely.

4. If the feed passes those checks, *curn* then moves on to the individual
   items. Each item has a unique ID associated with it. Sometimes, that ID
   is a [GUID][]; sometimes, it's just the URL associated with the
   corresponding article. But, regardless, *curn* keeps that information in
   its cache. If *curn* detects that it has not seen an item before
   (because the item's ID is not in the cache for that feed), *curn* assumes
   the item is new and displays it.

5. If the item is in the cache, *curn* knows it has seen the item before.
   However, the item may have changed since then, so *curn* checks the item
   publication date, if there is one. The various RSS feed formats allow
   publishers to specify publication dates for individual items. This field
   is, of course, optional. If it's present, though, *curn* compares it to
   the last publication date in the cache. If the current publication date
   is newer, *curn* assumes the item has been changed or updated, so it
   display it (again). Otherwise, it skips the item.
   
That's the basic algorithm *curn* uses to decide whether or not to display
a feed and its items. There are, obviously, ways it can fail, including:

* The remote HTTP server doesn't properly set the dates on the feeds.
* The feed itself contains bad dates. For instance, the feed generation
  software (which is typically separate from the HTTP server) may always
  present the current date as the publication date. If that happens, *curn*
  will always think the article has been updated, so it'll always display
  it. I've seen sites that behave this way.

*curn* does the best it can to show you only the new or changed stuff, but
it's not perfect.


## Does the order of the items in a feed affect how curn processes the feed?

This question arose from an email:

> Say for some reason a site changes the order of its RSS feed. Is *curn*
> able to determine it has pulled an article, if it appears in a different
> order then the last time it pulled it? (I know I can use the `SortBy`
> option, but suppose I don't.)

In short: Order of appearance in the actual feed doesn't matter.

*curn* doesn't care about the order the articles appear in the downloaded
feed. Each article has a unique ID of its own, and *curn* evaluates whether
it has seen an article or not based solely on that unique ID. Further, it
determines whether an article has changed based on the algorithm outlined
above, without regard to the order in which the articles actually arrive
within the feed.

`SortBy` is only for display. That is, once *curn* has downloaded a feed,
figured out which articles to display (and which to suppress), and prepares
to create the output, it uses `SortBy` solely to determine the order of
articles in the output. `SortBy` has no effect on the processing, other than
dictating the order of display.

## I think I found a bug. What do I do?

I need as much information as possible to diagnose and reproduce the bug.
Please run *curn* with logging enabled, and mail me the log file. See the
[Logging][] section in the [*curn* User's Guide][users-guide] for details on
how to enable logging.

[users-guide]: users-guide/index.html
[ocutil]: http://bmc.github.com/javautil/
[standard Java coding conventions]: http://www.oracle.com/technetwork/java/codeconvtoc-136057.html
[Allman style]: http://en.wikipedia.org/wiki/Indent_style#Allman_style_.28bsd_in_Emacs.29
[Installing Supporting Software]: users-guide/index.html#InstallingSupportSoftware
[Writing Your Own Output Handler]: users-guide/index.html#NewOutputHandler
[Using an Unsupported RSS Parser]: users-guide/index.html#UnsupportedParser
[Installing Plug-ins]: users-guide/index.html#PlugInInstallation
[Overview of Plug-In Support]: users-guide/index.html#PlugInOverview
[Logging]: users-guide/index.html#Logging
[GUID]: http://en.wikipedia.org/wiki/Guid
[bsf]: http://jakarta.apache.org/bsf/
