# $Id$
# ---------------------------------------------------------------------------
# JRuby output handler for curn.
#
# This software is released under a BSD-style license:
#
# Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
# 1.  Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# 2.  The end-user documentation included with the redistribution, if any,
#     must include the following acknowlegement:
#
#       "This product includes software developed by Brian M. Clapper
#       (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
#       copyright (c) 2004-2007 Brian M. Clapper."
#
#     Alternately, this acknowlegement may appear in the software itself,
#     if wherever such third-party acknowlegements normally appear.
#
# 3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
#     nor any of the names of the project contributors may be used to
#     endorse or promote products derived from this software without prior
#     written permission. For written permission, please contact
#     bmc@clapper.org.
#
# 4.  Products derived from this software may not be called "clapper.org
#     Java Utility Library", nor may "clapper.org" appear in their names
#     without prior written permission of Brian M. Clapper.
#
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
# NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
# ---------------------------------------------------------------------------

require 'java'

# == Description
#
# TextOutputHandler is a JRuby version of the curn-supplied
# (({org.clapper.curn.output.TextOutputHandler})) class. This script
# script must be used in conjunction with curn's (({ScriptOutputHandler}))
# class, which requires the presence of the Jakarta version of the Bean
# Scripting Framework (BSF). In addition, ((*jruby.jar*)) must be in the
# CLASSPATH.
#
# This script is intended primarily as a demonstration, though it is a
# fully-functioning script-based output handler.
#
# In addition to the configuration parameters supported by the
# (({ScriptOutputHandler})) class and its parent (({FileOutputHandler}))
# class, this script also honors an optional (({Message})) parameter. If
# present in the configuration section, the (({Message})) parameter defines 
# an arbitrary string to be displayed at the top of the output.
#
class TextOutputHandler

    include_package 'org.clapper.curn'
    include_package 'org.clapper.util.io'
    include_package 'org.clapper.util.misc'

    HORIZONTAL_RULE = "------------------------------------------------------------------------------"

    attr_reader :logger

    def initialize()
        #
        # Initialize a new TextOutputHandler object.
        #
        @channels    = $curn.channels
        @outputPath  = $curn.outputPath
        @config      = $curn.config
        @sectionName = $curn.configSection
        @logger      = $curn.logger
        @version     = $curn.getVersion()
        @message     = nil
    end

    def processChannels()
        #
        # Process the channels passed in through the Bean Scripting Framework.
        #

        # If we didn't care about wrapping the output, we'd just use:
        #
        #     @out = open(@outputPath, "w")
        #
        # But it'd be nice to wrap long summaries on word boundaries at
        # the end of an 80-character line. For that reason, we use the
        # Java org.clapper.util.io.WordWrapWriter class.

        #out = WordWrapWriter.new(FileWriter.new(@outputPath))
        out = WordWrapper.new(File.new(@outputPath, "w"))

        out.setPrefix("")

        msg = @config.getOptionalStringValue(@sectionName, "Message", nil)

        totalNew = 0

        # First, count the total number of new items

        iterator = @channels.iterator()
        while iterator.hasNext()
            channel_wrapper = iterator.next()
            channel = channel_wrapper.getChannel()
            totalNew = totalNew + channel.getItems().size()
        end

        if totalNew > 0
            # If the config file specifies a message for this handler,
            # display it.

            if(msg != nil)
                out.println(msg)
                out.newline()
            end

            # Now, process the items

            indentation = 0
            iterator = @channels.iterator()
            while iterator.hasNext()
                channel_wrapper = iterator.next()
                channel = channel_wrapper.getChannel()
                feed_info = channel_wrapper.getFeedInfo()
                process_channel(out, channel, feed_info, indentation)
            end

            $curn.setMIMEType("text/plain")
            # Output a footer

            indent(out, indentation)
            out.newline()
            out.println(HORIZONTAL_RULE)
            out.println(@version)
            out.flush()
        end
    end

    def process_channel(out, channel, feed_info, indentation)
        #
        # Process all items within a channel.
        #
        urlString = channel.getLinks().iterator().next().toString()
        @logger.debug("Processing channel \"" + urlString + "\"")

        # Print a channel header

        config = @config

        indent(out, indentation)
        out.println(HORIZONTAL_RULE)
        out.println(channel.getTitle())
        out.println(urlString)
        out.println(channel.getItems().size().to_s() + " item(s)")
        date = channel.getPublicationDate()
        if date
            out.println(date.toString())
        end

        indentation = indentation + 1
        indent(out, indentation)
        iterator = channel.getItems().iterator()
        while iterator.hasNext()
            # These are RSSItem objects
            item = iterator.next()

            out.newline()
            out.println(item.getTitle())
            author = item.getAuthor()
            if author
                out.println(author)
            end

            out.println(item.getLinks().iterator().next().toString())

            date = item.getPublicationDate()
            if date
                out.println(date.toString())
            end

            out.newline()

            summary = item.getSummary()
            if summary
                indent(out, indentation + 1)
                out.println(summary)
                indent(out, indentation)
            end
        end
    end

    def indent(out, indentation)
        #
        # Apply a level of indentation to a WordWrapWriter, by changing
        # the WordWrapWriter's prefix string.
        #
        # out         - the org.clapper.util.io.WordWrapWriter
        # indentation - the numeric indentation level
        #

        prefix = ""
        for i in 0 .. indentation - 1
            prefix = prefix + "    "
        end

        out.setPrefix(prefix)
    end

    private :indent, :process_channel
end

# Simplified Ruby implementation of (({org.clapper.util.io.WordWrapWriter})).
# I'd use (({WordWrapWriter})), but JRuby (version 0.7.0) seems to have trouble
# invoking overloading methods from Java classes. This Ruby class does a
# subset of what (({WordWrapWriter})) does, enough for this class.
class WordWrapper

    def initialize(out)
        @out = out
        @currentLength = 0
        @prefix = ""
        @lastWasNewline = true
    end

    def setPrefix(str)
        @prefix = str
    end

    def print(str)
        tokens = str.split(' ')

        for token in tokens
            # If the word (plus a delimiting space) exceeds the line length,
            # wrap now.

            if (@currentLength + token.length + 1) > 79
                newline()
            end

            if @lastWasNewline
                @out.print(@prefix)
                @currentLength = @prefix.length
                @lastWasNewline = false
            else
                @out.print(" ");
                @currentLength += 1
            end

            @out.print(token)
            @currentLength += token.length
        end
    end

    def println(str)
        print(str)
        newline()
    end

    def newline
        @out.print("\n")
        @currentLength = 0;
        @lastWasNewline = true
    end

    def flush
    end
end


# ---------------------------------------------------------------------------

handler = TextOutputHandler.new()
logger = handler.logger()
begin
    handler.processChannels()
rescue Exception => ex
    logger.debug(ex)
    logger.debug(ex.backtrace.join("\n"))
    raise
end
