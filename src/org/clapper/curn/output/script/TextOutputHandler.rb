# $Id$
# ---------------------------------------------------------------------------
#
# This software is released under a Berkeley-style license:
#
# Copyright(c) 2004 Brian M. Clapper. All rights reserved.
#
# Redistribution and use in source and binary forms are permitted provided
# that:(1) source distributions retain this entire copyright notice and
# comment; and(2) modifications made to the software are prominently
# mentioned, and a copy of the original software(or a pointer to its
# location) are included. The name of the author may not be used to endorse
# or promote products derived from this software without specific prior
# written permission.
#
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
#
# Effectively, this means you can do what you want with the software except
# remove this notice or take advantage of the author's name. If you modify
# the software and redistribute your modified version, you must indicate that
# your version is a modification of the original, and you must provide either
# a pointer to or a copy of the original.

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
        @channels    = $bsf.lookupBean("channels")
        @outputPath  = $bsf.lookupBean("outputPath")
        @mimeTypeOut = $bsf.lookupBean("mimeType")
        @config      = $bsf.lookupBean("config")
        @sectionName = $bsf.lookupBean("configSection")
        @logger      = $bsf.lookupBean("logger")
        @version     = $bsf.lookupBean("version")
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

            @mimeTypeOut.print("text/plain")
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
        urlString = channel.getLink().toString()
        @logger.debug("Processing channel \"" + urlString + "\"")

        # Print a channel header

        config = @config

        indent(out, indentation)
        out.println (HORIZONTAL_RULE)
        out.println(channel.getTitle())
        out.println(channel.getLink().toString())
        out.println(channel.getItems().size().to_s() + " item(s)")
        if(config.showDates())
            date = channel.getPublicationDate()
            if(date != nil)
                out.println(date.toString())
            end
        end

        indentation = indentation + 1
        indent(out, indentation)
        iterator = channel.getItems().iterator()
        while iterator.hasNext()
            # These are RSSItem objects
            item = iterator.next()

            out.newline()
            out.println(item.getTitle())
            out.println(item.getLink().toString())

            if config.showDates()
                date = item.getPublicationDate();
                if date != nil
                    out.println(date.toString())
                end
            end

            out.newline()

            if not feed_info.summarizeOnly()
                summary = item.getSummary()
                if summary != nil
                    indent(out, indentation + 1)
                    out.println(summary)
                    indent(out, indentation)
                end
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
