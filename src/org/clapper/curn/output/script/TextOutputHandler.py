"""
 $Id$
 ---------------------------------------------------------------------------
 This software is released under a Berkeley-style license:

 Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

 Redistribution and use in source and binary forms are permitted provided
 that: (1) source distributions retain this entire copyright notice and
 comment; and (2) modifications made to the software are prominently
 mentioned, and a copy of the original software (or a pointer to its
 location) are included. The name of the author may not be used to endorse
 or promote products derived from this software without specific prior
 written permission.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

 Effectively, this means you can do what you want with the software except
 remove this notice or take advantage of the author's name. If you modify
 the software and redistribute your modified version, you must indicate that
 your version is a modification of the original, and you must provide either
 a pointer to or a copy of the original.
"""

"""
Python/Jython version of the curn-supplied C{TextOutputHandler}
(org.clapper.curn.output.TextOutputHandler). This script must be used
in conjunction with curn's ScriptOutputHandler class
(org.clapper.curn.output.script.ScriptOutputHandler), which requires
the presence of the Jakarta version of the Bean Scripting Framework (BSF).

This script is intended primarily as a demonstration, though it is a
fully-functioning script-based output handler.

In addition to the configuration parameters supported by the
ScriptOutputHandler class and its parent FileOutputHandler class, this
script also honors an optional Message parameter. If present in the
configuration section, the Message parameter defines an arbitrary
string to be displayed at the top of the output.
"""

import sys
from org.clapper.curn import CurnException
from org.clapper.util.io import WordWrapWriter

HORIZONTAL_RULE = "---------------------------------------" \
                + "---------------------------------------"


def processChannels():
    """
    Process the channels passed in through the Bean Scripting Framework.
    """

    # If we didn't care about wrapping the output, we'd just use:
    #
    #     out = open (self.outputPath, "w")
    #
    # But it'd be nice to wrap long summaries on word boundaries at
    # the end of an 80-character line. For that reason, we use the
    # Java org.clapper.util.io.WordWrapWriter class.

    out = WordWrapWriter (open (outputPath, "w"))
    out.setPrefix ("")

    msg = config.getOptionalStringValue (sectionName, "Message", None)

    totalNew = 0

    # First, count the total number of new items

    iterator = channels.iterator()
    while iterator.hasNext():
        channel_wrapper = iterator.next()
        channel = channel_wrapper.getChannel()
        totalNew = totalNew + channel.getItems().size()

    if totalNew > 0:
        # If the config file specifies a message for this handler,
        # display it.

        if msg != None:
            out.println (msg)
            out.println ()

        # Now, process the items

        indentation = 0
        iterator = channels.iterator()
        while iterator.hasNext():
            channel_wrapper = iterator.next()
            channel = channel_wrapper.getChannel()
            feed_info = channel_wrapper.getFeedInfo()
            process_channel (out, channel, feed_info, indentation)

        mimeTypeOut.print ("text/plain")

        # Output a footer

        indent (out, indentation)
        out.println ()
        out.println (HORIZONTAL_RULE)
        out.println (version)
        out.flush()

def process_channel (out, channel, feed_info, indentation):
    """
    Process all items within a channel.
    """
    logger.debug ("Processing channel \"" + str (channel.getTitle()) + "\"")

    # Print a channel header

    indent (out, indentation)
    out.println (HORIZONTAL_RULE)
    out.println (channel.getTitle())
    out.println (channel.getLinks().iterator().next().toString())
    out.println (str (channel.getItems().size()) + " item(s)")
    if config.showDates():
        date = channel.getPublicationDate()
        if date != None:
            out.println (str (date))

    if config.showRSSVersion():
        out.println ("(Format: " + channel.getRSSFormat() + ")")

    indentation = indentation + 1
    indent (out, indentation)
    iterator = channel.getItems().iterator()
    while iterator.hasNext():
        # These are RSSItem objects
        item = iterator.next()

        out.println()
        out.println (item.getTitle())
        out.println (str (item.getLinks().iterator().next()))

        if config.showDates():
            date = item.getPublicationDate();
            if date != None:
                out.println (str (date))

        out.println()

        if not feed_info.summarizeOnly():
            summary = item.getSummary()
            if summary != None:
                indent (out, indentation + 1)
                out.println (summary)
                indent (out, indentation)

def indent (out, indentation):
    """
    Apply a level of indentation to a WordWrapWriter, by changing
    the WordWrapWriter's prefix string.

    out         - the org.clapper.util.io.WordWrapWriter
    indentation - the numeric indentation level
    """

    prefix = ""
    for i in range (indentation):
        prefix = prefix + "    "

    out.setPrefix (prefix)

# ---------------------------------------------------------------------------

channels    = bsf.lookupBean ("channels")
outputPath  = bsf.lookupBean ("outputPath")
mimeTypeOut = bsf.lookupBean ("mimeType")
config      = bsf.lookupBean ("config")
sectionName = bsf.lookupBean ("configSection")
logger      = bsf.lookupBean ("logger");
version     = bsf.lookupBean ("version")
message     = None

processChannels()
