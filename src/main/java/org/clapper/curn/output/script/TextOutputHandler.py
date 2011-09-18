# ---------------------------------------------------------------------------
# Jython output handler for curn.
#
# This software is released under a BSD license, adapted from
# <http://opensource.org/licenses/bsd-license.php>
#
# Copyright (c) 2004-2010 Brian M. Clapper.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
#
# * Neither the name "clapper.org", "curn", nor the names of the project's
#   contributors may be used to endorse or promote products derived from
#   this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
# IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
# THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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


def process_channels():
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

    out = WordWrapWriter (open (curn.outputPath, "w"))
    out.setPrefix ("")

    msg = curn.config.getOptionalStringValue (curn.configSection,
                                              "Message",
                                              None)

    totalNew = 0

    # First, count the total number of new items

    for channel_wrapper in curn.channels:
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
        for channel_wrapper in curn.channels:
            channel = channel_wrapper.getChannel()
            feed_info = channel_wrapper.getFeedInfo()
            process_channel (out, channel, feed_info, indentation)

        curn.setMIMEType ("text/plain")

        # Output a footer

        indent (out, indentation)
        out.println ()
        out.println (HORIZONTAL_RULE)
        out.println (curn.getVersion())
        out.flush()

def process_channel (out, channel, feed_info, indentation):
    """
    Process all items within a channel.
    """
    curn.logger.debug ("Processing channel \"" + str (channel.getTitle()) + "\"")

    # Print a channel header

    indent (out, indentation)
    out.println (HORIZONTAL_RULE)
    out.println (channel.getTitle())
    out.println (channel.getLinks()[0].toString())
    out.println (str (channel.getItems().size()) + " item(s)")

    date = channel.getPublicationDate()
    if date != None:
        out.println (str (date))

    if curn.config.showRSSVersion():
        out.println ("(Format: " + channel.getRSSFormat() + ")")

    indentation = indentation + 1
    indent (out, indentation)
    for item in channel.getItems():

        # These are RSSItem objects

        out.println()
        out.println (item.getTitle())
        author = item.getAuthor()
        if author != None:
            out.println (author)

        out.println (str (item.getLinks()[0]))

        date = item.getPublicationDate();
        if date != None:
            out.println (str (date))

        out.println()

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

process_channels()
