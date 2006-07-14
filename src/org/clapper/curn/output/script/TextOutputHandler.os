// $Id$
// ---------------------------------------------------------------------------
//
// ObjectScript (http://objectscript.sourceforge.net) script output handler
// for curn.
//
// This software is released under a BSD-style license:
//
// Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// 1.  Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
// 2.  The end-user documentation included with the redistribution, if any,
//     must include the following acknowlegement:
//
//       "This product includes software developed by Brian M. Clapper
//       (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
//       copyright (c) 2004-2006 Brian M. Clapper."
//
//     Alternately, this acknowlegement may appear in the software itself,
//     if wherever such third-party acknowlegements normally appear.
//
// 3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
//     nor any of the names of the project contributors may be used to
//     endorse or promote products derived from this software without prior
//     written permission. For written permission, please contact
//     bmc@clapper.org.
//
// 4.  Products derived from this software may not be called "clapper.org
//     Java Utility Library", nor may "clapper.org" appear in their names
//     without prior written permission of Brian M.a Clapper.
//
// THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
// NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
// NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
// THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// ---------------------------------------------------------------------------

public function TextOutputHandler()
{
    private var HORIZONTAL_RULE = "------------------------------------------------------------------------------";
    private var channels    = curn.channels;
    private var outputPath  = curn.outputPath;
    private var config      = curn.config;
    private var sectionName = curn.configSection;
    private var logger      = curn.logger;
    private var version     = curn.getVersion();
    private var out         = null;

    public function getLogger()
    {
        return logger;
    }

    public function processChannels()
    {
        try
        {
            logger.debug ("Total channels=" + channels.size());

            var org = new JavaPackage ("org");
            pkg.system.declareJavaPackage ("org.clapper.util.io");
            logger.debug ("Opening " + outputPath);
            out = new org.clapper.util.io.WordWrapWriter (new java.io.PrintWriter (outputPath));
            handleChannels();
        }

        finally
        {
            logger.debug ("Flushing output.");
            out.flush();
            out.close();
        }
    }

    private function handleChannels()
    {

        //
        // Process the channels passed in through the Bean Scripting Framework.
        //

        // If we didn't care about wrapping the output, we'd just use:
        //
        //     @out = open(@outputPath, "w")
        //
        // But it'd be nice to wrap long summaries on word boundaries at
        // the end of an 80-character line. For that reason, we use the
        // Java org.clapper.util.io.WordWrapWriter class.

        out.setPrefix ("");
        var msg = config.getOptionalStringValue (sectionName, "Message", null);
        var totalNew = 0;

        // First, count the total number of new items

	for (var channel_wrapper : channels)
	{
            var channel = channel_wrapper.getChannel();
            totalNew = totalNew + channel.getItems().size();
        }

        logger.debug ("totalNew=" + totalNew);
        if (totalNew > 0)
        {
            // If the config file specifies a message for this handler,
            // display it.

            if (msg != null)
            {
                out.println(msg);
                out.println();
            }

            // Now, process the items

            var indentation = 0;
	    for (var channel_wrapper : channels)
            {
                var channel = channel_wrapper.getChannel();
                var feed_info = channel_wrapper.getFeedInfo();
                process_channel (out, channel, feed_info, indentation);
            }

            curn.setMIMEType("text/plain");
            // Output a footer

            indent(out, indentation);
            out.println();
            out.println(HORIZONTAL_RULE);
            out.println(version);
            out.flush();
        }
    }

    function process_channel (out, channel, feed_info, indentation)
    {
        //
        // Process all items within a channel.
        //
        var urlString = channel.getLinks()[0];
        logger.debug("Processing channel \"" + urlString + "\"");

        // Print a channel header

        indent(out, indentation);
        out.println(HORIZONTAL_RULE);
        out.println(channel.getTitle());
        out.println(urlString);
        out.println(channel.getItems().size().toString() + " item(s)");

        var date = channel.getPublicationDate();
        if (date != null)
            out.println(date.toString());

        indentation = indentation + 1;
        indent(out, indentation);
        var iterator = channel.getItems().iterator();
        while (iterator.hasNext())
        {
            // These are RSSItem objects
            var item = iterator.next();

            out.println();
            out.println(item.getTitle());
            var author = item.getAuthor();
            if (author != null)
                out.println(author);

            out.println(item.getLinks().iterator().next().toString());

            var date = item.getPublicationDate();
            if (date != null)
                out.println(date.toString());

            out.println();

            var summary = item.getSummary();
            if (summary != null)
            {
                indent(out, indentation + 1);
                out.println(summary);
                indent(out, indentation);
            }
        }
    }

    function indent(out, indentation)
    {
        //
        // Apply a level of indentation to a WordWrapWriter, by changing
        // the WordWrapWriter's prefix string.
        //
        // out         - the org.clapper.util.io.WordWrapWriter
        // indentation - the numeric indentation level
        //

        var prefix = "";
        for (var i = 0; i < indentation; i++)
            prefix = prefix + "    ";

        out.setPrefix(prefix);
    }
}

// ---------------------------------------------------------------------------

var handler = new TextOutputHandler();
var logger = handler.getLogger();
try
{
    handler.processChannels();
}
catch (java.lang.Exception ex)
{
    logger.debug(ex);
}


