/*---------------------------------------------------------------------------*\
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
\*---------------------------------------------------------------------------*/

package org.clapper.curn.output.script;

import org.clapper.curn.ConfigFile;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.Version;

import org.clapper.curn.output.FileOutputHandler;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;

import org.clapper.util.io.FileUtil;

import org.clapper.util.misc.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.net.URL;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFManager;

/**
 * Provides an output handler calls a script via the Apache Jakarta
 * {@link <a href="http://jakarta.apache.org/bsf/">Bean Scripting Framework</a>}
 * (BSF). This handler supports any scripting language supported by BSF. In
 * addition to the  configuration parameters supported by the
 * {@link FileOutputHandler} base class, this handler supports the
 * following additional configuration variables, which must be specified in
 * the handler's configuration section.
 *
 * <table border="1" align="center">
 *   <tr>
 *     <th>Parameter</th>
 *     <th>Explanation</th>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>Script</tt></td>
 *     <td>Path to the script to be invoked. The script will be called
 *         as if from the command line, except that additional objects will
 *         be available via BSF.
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>Language</tt></td>
 *     <td>The scripting language, as recognized by BSF. Examples:
 *         "jython", "jruby", "javascript"
 *     </td>
 *   </tr>
 * </table>
 *
 * <p>This handler's {@link #displayChannel displayChannel()} method does
 * not invoke the script; instead, it buffers up all the channels so that
 * the {@link #flush} method can invoke the script. That way, the overhead
 * of invoking the script only occurs once. Via the BSF engine, this
 * handler makes available an iterator of special objects that wrap both
 * the {@link RSSChannel} and {@link FeedInfo} objects for a given channel.
 * See below for a more complete description.</p>
 *
 * <p>The complete list of objects bound into the BSF beanspace follows.
 *
 * <table border="0">
 *   <tr valign="top">
 *     <th>Bound name</th>
 *     <th>Java type</th>
 *     <th>Explanation</th>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>channels</td>
 *     <td><tt>java.util.Collection</tt></td>
 *     <td>An <tt>Collection</tt> of special internal objects that wrap
 *         both {@link RSSChannel} and {@link FeedInfo} objects. The
 *         wrapper objects provide two methods:</td>
 *
 *         <ul>
 *           <li><tt>getChannel()</tt> gets the <tt>RSSChannel</tt> object
 *           <li><tt>getFeedInfo()</tt> gets the <tt>FeedInfo</tt> object
 *         </ul>
 *    </tr>
 *
 *   <tr valign="top">
 *     <td>outputPath</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>The path to an output file. The script should write its output
 *         to that file. Overwriting the file is fine. If the script generates
 *         no output, then it can ignore the file.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>config</td>
 *     <td><tt>{@link ConfigFile}</tt></td>
 *     <td>The <tt>org.clapper.curn.ConfigFile</tt> object that represents
 *         the parsed configuration data. Useful in conjunction with the
 *         "configSection" object, to parse additional parameters from
 *         the configuration.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>configSection</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>The name of the configuration file section in which the output
 *         handler was defined. Useful if the script wants to access
 *         additional script-specific configuration data.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>mimeType</td>
 *     <td><tt>java.lang.StringBuffer</tt></td>
 *     <td>A <tt>StringBuffer</tt> object to which the script should
 *         append the MIME type that corresponds to the generated output.
 *         If the script generates no output, then it can ignore this
 *         object.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>logger</td>
 *     <td>{@link Logger org.clapper.util.misc.Logger}</td>
 *     <td>A <tt>Logger</tt> object, useful for logging messages to
 *         the <i>curn</i> log file.</td>
 *   </tr>

 *   <tr valign="top">
 *     <td>version</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>Full <i>curn</i> version string, in case the script wants to
 *         include it in the generated output
 *   </tr>
 * </table>
 *
 * <p>For example, the following Jython script can be used as a template
 * for a Jython output handler.</p>
 *
 * <blockquote>
 * <pre>
 * import sys
 *
 * def main():
 *     channels = bsf.lookupBean ("channels")
 *     channel_iterator = channels.iterator()
 *     outputPath = bsf.lookupBean ("outputPath")
 *     mimeTypeBuf = bsf.lookupBean ("mimeType")
 *     mimeTypeBuf.append ("text/html") # or whatever
 *
 *     out = open (outputPath, "w")
 *
 *     while channel_iterator.hasNext():
 *         channel_wrapper = channel_iterator.next()
 *         channel = channel_wrapper.getChannel()
 *         feed_info = channel_wrapper.getFeedInfo()
 *         process_channel (channel_iterator.next())
 *
 * def process_channel (channel, feed_info):
 *     item_iterator = channel.getItems().iterator()
 *     while item_iterator.hasNext():
 *         # Do output for item
 *         ...
 *
 * main()
 * </pre>
 * </blockquote>
 *
 * @see OutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class ScriptOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * External scripting languages that don't come bundled with BSF.
     * The language is the index, and the value is the class name.
     */
    private static Map OTHER_SCRIPT_ENGINES = new HashMap();

    static
    {
        OTHER_SCRIPT_ENGINES.put ("jruby",
                                  "org.jruby.javasupport.bsf.JRubyEngine");
    }

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Wraps an RSSChannel object and its FeedInfo object.
     */
    public class ChannelWrapper
    {
        private RSSChannel channel;
        private FeedInfo   feedInfo;

        ChannelWrapper (RSSChannel channel, FeedInfo feedInfo)
        {
            this.channel  = channel;
            this.feedInfo = feedInfo;
        }

        public RSSChannel getChannel()
        {
            return this.channel;
        }

        public FeedInfo getFeedInfo()
        {
            return this.feedInfo;
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private BSFManager    bsfManager     = null;
    private ConfigFile    config         = null;
    private Collection    channels       = new ArrayList();
    private String        scriptPath     = null;
    private String        scriptString   = null;
    private StringBuffer  mimeTypeBuffer = new StringBuffer();
    private String        language       = null;
    private Logger        scriptLogger   = null;

    /**
     * For logging
     */
    private static Logger log = new Logger (ScriptOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>ScriptOutputHandler</tt>.
     */
    public ScriptOutputHandler()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config     the parsed <i>curn</i> configuration data
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public void initOutputHandler (ConfigFile              config,
                                   ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        this.config = config;

        // Parse handler-specific configuration variables

        String section = cfgHandler.getSectionName();

        try
        {
            if (section != null)
            {
                scriptPath = config.getConfigurationValue (section, "Script");
                language  = config.getConfigurationValue (section, "Language");

            }
        }
        
        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        // Verify that the script exists.

        File scriptFile = new File (scriptPath);
        if (! scriptFile.exists())
        {
            scriptPath = null;
            throw new ConfigurationException (section,
                                              "Script file \""
                                            + scriptFile.getPath()
                                            + "\" does not exist.");
        }

        if (! scriptFile.isFile())
        {
            scriptPath = null;
            throw new ConfigurationException (section,
                                              "Script file \""
                                            + scriptFile.getPath()
                                            + "\" is not a regular file.");
        }

        // Register popular BSF languages that don't come bundled with the BSF
        // jar. It doesn't matter whether these languages are actually present
        // in the environment, since BSFManager.registerScriptingEngine() does
        // not actually attempt to load the referenced class. The referenced
        // class is loaded only when BSFManager.loadScriptingEngine() is called
        // (which happens in the ScriptOutputHandler.flush() method).

        for (Iterator it = OTHER_SCRIPT_ENGINES.keySet().iterator();
             it.hasNext(); )
        {
            String otherLang   = (String) it.next();
            String otherEngine = (String) OTHER_SCRIPT_ENGINES.get (otherLang);

            BSFManager.registerScriptingEngine (otherLang, otherEngine,
                                                new String[] {"rb"});
        }

        // Allocate a new BSFManager. This must happen after all the extra
        // scripting engines are registered.

	bsfManager = new BSFManager();

        // Set up a logger for the script. The logger name can't have dots
        // in it, because the underlying logging API (Jakarta Commons
        // Logging) strips them out, thinking they're class/package
        // delimiters. That means we have to strip the extension.
        // Unfortunately, the extension conveys information (i.e., the
        // language). Add the script language to the stripped name.

        StringBuffer scriptLoggerName = new StringBuffer();
        String scriptName = scriptFile.getName();
        scriptLoggerName.append (FileUtil.getFileNameNoExtension (scriptName));
        scriptLoggerName.append ("[" + language + "]");
        scriptLogger = new Logger (scriptLoggerName.toString());

        // Register the beans we know about now. The other come after we
        // process the channels.

        bsfManager.registerBean ("mimeType", mimeTypeBuffer);
        bsfManager.registerBean ("config", config);
        bsfManager.registerBean ("configSection", section);
        bsfManager.registerBean ("logger", scriptLogger);
        bsfManager.registerBean ("version", Version.getFullVersion());

        // Load the contents of the script into an in-memory buffer.

        scriptString = loadScript (scriptFile);

        channels.clear();
        mimeTypeBuffer.setLength (0);
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. This handler simply buffers up
     * the channel, so that {@link #flush} can pass all the channels to the
     * script.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws CurnException
    {
        // Do some textual conversion on the channel data.

        channel.setTitle (convert (channel.getTitle()));

        Collection items = channel.getItems();
        if ((items != null) && (items.size() > 0))
        {
            for (Iterator it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();
                item.setTitle (convert (item.getTitle()));

                String s = item.getAuthor();
                if (s != null)
                    item.setAuthor (convert (s));

                s = item.getSummary();
                if (s != null)
                    item.setSummary (convert (s));
            }
        }

        // Save the channel.

        channels.add (new ChannelWrapper (channel, feedInfo));
    }
    
    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        try
        {
            // Load the scripting engine

            BSFEngine scriptEngine = bsfManager.loadScriptingEngine (language);

            // Register the various script beans.

            bsfManager.registerBean ("channels", channels);
            bsfManager.registerBean ("outputPath", getOutputFile().getPath());

            // Run the script

            scriptEngine.exec (scriptPath, 0, 0, scriptString);
        }

        catch (BSFException ex)
        {
            throw new CurnException ("Error interacting with Bean Scripting "
                                   + "Framework.",
                                     ex);
        }
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return mimeTypeBuffer.toString();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the contents of the external script (any file, really) into an
     * in-memory buffer.
     *
     * @param scriptFile    the script file
     *
     * @return the string representing the loaded script
     *
     * @throws CurnException on error
     */
    private String loadScript (File scriptFile)
        throws CurnException
    {
        try
        {
            Reader       r = new BufferedReader (new FileReader (scriptFile));
            StringWriter w = new StringWriter();
            int          c;

            while ((c = r.read()) != -1)
                w.write (c);

            r.close();

            return w.toString();
        }

        catch (IOException ex)
        {
            throw new CurnException ("Error loading script \""
                                   + scriptFile.getPath()
                                   + "\" into memory.",
                                     ex);
        }
    }
}
