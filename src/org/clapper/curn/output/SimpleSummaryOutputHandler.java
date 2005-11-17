/*---------------------------------------------------------------------------*\
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
\*---------------------------------------------------------------------------*/

package org.clapper.curn.output;

import org.clapper.curn.ConfigFile;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.Curn;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.curn.output.freemarker.FreeMarkerOutputHandler;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;
import org.clapper.util.text.VariableSubstitutionException;

/**
 * <p>Provides an output handler that produces a summary of the number of new
 * items in a configuration file, without actually displaying the items
 * or their URLs. In addition, the summary contains a user-defined string
 * from the configuration file. The user-defined string is configured via
 * the <tt>Message</tt> configuration item. For example:</p>
 *
 * <blockquote><pre>
 * [OutputHandlerSummary]
 * Class: org.clapper.curn.output.SimpleSummaryOutputHandler
 * Message: Full details at http://localhost/rss/feeds.html
 * </pre></blockquote>
 *
 * <p>This handler is particularly useful in conjunction with a true output
 * handler. For instance, my mail reader does not support HTML (and I like
 * it that way); however, I prefer the output produced by the
 * {@link org.clapper.curn.output.html.HTMLOutputHandler HTMLOutputHandler}
 * class to the output produced by the
 * {@link TextOutputHandler TextOutputHandler}
 * class. So, my configuration file contains the following output handlers:</p>
 *
 * <blockquote><pre>
 * [OutputHandlerSummary]
 * Class: org.clapper.curn.output.SimpleSummaryOutputHandler
 * Message: Full details at http://localhost/rss/feeds.html
 *
 * [OutputHandlerHTML]
 * Class: org.clapper.curn.output.html.HTMLOutputHandler
 * SaveAs: /usr/local/www/htdocs/rss/feeds.html
 * SaveOnly: true
 * </pre></blockquote>
 *
 * <p>When <i>curn</i> runs, it parses the RSS feeds and passes the resulting
 * {@link org.clapper.curn.parser.RSSChannel RSSChannel} objects to each
 * output handler. Because its <tt>SaveOnly</tt> configuration parameter is
 * set, the <tt>HTMLOutputHandler</tt> object does not return any output to
 * <i>curn</i>; instead, it simply saves the HTML output to the specified file.
 * The <tt>SimpleSummaryOutputHandler</tt> object returns a brief summary
 * of the feeds, along with the message:</p>
 *
 * <blockquote>
 * <pre>Full details at http://localhost/rss/feeds.html</pre>
 * </blockquote>
 *
 * <p>When I look at <i>curn</i>'s output in my mail reader, I can click on
 * the link (my mail reader <i>will</i> highlight HTML links, even though it
 * won't render full HTML), and view the resulting HTML document in my
 * browser.</p>
 *
 * <p><b>Note:</b> This handler is now implemented in terms of the
 * {@link FreeMarkerOutputHandler} class and may be removed in the future.</p>
 *
 * @see org.clapper.curn.OutputHandler
 * @see FileOutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class SimpleSummaryOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * We forward to a FreeMarkerOutputHandler instance, instead of extending
     * the class, because of implementation constraints. For instance,
     * the initOutputHandler() method must do some work before calling the
     * FreeMarkerOutputHandler class's initOutputHandler() method. But if
     * we subclass FreeMarkerOutputHandler, then the super() call must come
     * first.
     */
    private FreeMarkerOutputHandler outputHandler;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>SimpleSummaryOutputHandler</tt>
     */
    public SimpleSummaryOutputHandler()
    {
        super();

        outputHandler = new FreeMarkerOutputHandler();
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
        // Parse handler-specific configuration variables not also processed
        // by the FreeMarkerOutputHandler class.

        String section  = cfgHandler.getSectionName();

        try
        {
            if (section != null)
            {
                // Map the encoding

                // Add the AllowEmbeddedHTML directive. Set it to false,
                // unconditionally; that way, embedded HTML will be
                // controlled solely by the feed-specific setting (which is
                // the way this handler used to work);

                config.setVariable
                              (section,
                               FreeMarkerOutputHandler.CFG_ALLOW_EMBEDDED_HTML,
                               "false",
                               false);

                // Add the template-file directive, specifying the built-in
                // summary template.

                StringBuilder val = new StringBuilder();
                val.append (FreeMarkerOutputHandler.CFG_TEMPLATE_LOAD_BUILTIN);
                val.append (" ");
                val.append (FreeMarkerOutputHandler.CFG_BUILTIN_SUMMARY_TEMPLATE);
                config.setVariable (section,
                                    FreeMarkerOutputHandler.CFG_TEMPLATE_FILE,
                                    val.toString(),
                                    false);
            }
        }
        
        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        catch (VariableSubstitutionException ex)
        {
            throw new ConfigurationException (ex);
        }

        outputHandler.init (config, cfgHandler);
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output is written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()}
     * method.
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
        outputHandler.displayChannel (channel, feedInfo);
    }

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        outputHandler.flush();
    }
    
    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return outputHandler.getContentType();
    }
}
