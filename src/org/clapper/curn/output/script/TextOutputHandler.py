"""
 $Id$
 ---------------------------------------------------------------------------
 This software is released under a Berkeley-style license:

 Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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

import sys
from org.clapper.curn import CurnException
from org.clapper.util.io import WordWrapWriter

class TextOutputHandler:
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
    HORIZONTAL_RULE = "---------------------------------------" \
                    + "---------------------------------------"


    def __init__ (self):
        """
        Initialize a new TextOutputHandler object.
        """
        self.__channels    = bsf.lookupBean ("channels")
        self.__outputPath  = bsf.lookupBean ("outputPath")
        self.__mimeTypeBuf = bsf.lookupBean ("mimeType")
        self.__config      = bsf.lookupBean ("config")
        self.__sectionName = bsf.lookupBean ("configSection")
        self.__logger      = bsf.lookupBean ("logger");
        self.__version     = bsf.lookupBean ("version")
        self.__message     = None

    def processChannels (self):
        """
        Process the channels passed in through the Bean Scripting Framework.
        """

        # If we didn't care about wrapping the output, we'd just use:
        #
        #     out = open (self.__outputPath, "w")
        #
        # But it'd be nice to wrap long summaries on word boundaries at
        # the end of an 80-character line. For that reason, we use the
        # Java org.clapper.util.io.WordWrapWriter class.

        out = WordWrapWriter (open (self.__outputPath, "w"))
        out.setPrefix ("")

        msg = self.__config.getOptionalStringValue (self.__sectionName,
                                                    "Message",
                                                    None)

        totalNew = 0

        # First, count the total number of new items

        iterator = self.__channels.iterator()
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
            iterator = self.__channels.iterator()
            while iterator.hasNext():
                channel_wrapper = iterator.next()
                channel = channel_wrapper.getChannel()
                feed_info = channel_wrapper.getFeedInfo()
                self.__process_channel (out, channel, feed_info, indentation)

            self.__mimeTypeBuf.append ("text/plain")

            # Output a footer

            self.__indent (out, indentation)
            out.println ()
            out.println (TextOutputHandler.HORIZONTAL_RULE)
            out.println (self.__version)
            out.flush()

    def __process_channel (self, out, channel, feed_info, indentation):
        """
        Process all items within a channel.
        """
        self.__logger.debug ("Processing channel \"" +
                             str (channel.getLink()) + "\"")

        # Print a channel header

        config = self.__config

        self.__indent (out, indentation)
        out.println (TextOutputHandler.HORIZONTAL_RULE)
        out.println (channel.getTitle())
        out.println (channel.getLink().toString())
        out.println (str (channel.getItems().size()) + " item(s)")
        if config.showDates():
            date = channel.getPublicationDate()
            if date != None:
                out.println (str (date))

        if config.showRSSVersion():
            out.println ("(Format: " + channel.getRSSFormat() + ")")

        indentation = indentation + 1
        self.__indent (out, indentation)
        iterator = channel.getItems().iterator()
        while iterator.hasNext():
            # These are RSSItem objects
            item = iterator.next()

            out.println()
            out.println (item.getTitle())
            out.println (str (item.getLink()))

            if config.showDates():
                date = item.getPublicationDate();
                if date != None:
                    out.println (str (date))

            out.println()

            if not feed_info.summarizeOnly():
                summary = item.getSummary()
                if summary != None:
                    self.__indent (out, indentation + 1)
                    out.println (summary)
                    self.__indent (out, indentation)

    def __indent (self, out, indentation):
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

def main():
    """
    Main program entry point. This is what ScriptOutputHandler actually
    ends up invoking.
    """
    handler = TextOutputHandler()
    handler.processChannels()

main()
