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

package org.clapper.curn;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import java.text.MessageFormat;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.clapper.curn.parser.RSSParserFactory;
import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSParserException;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.FileUtil;
import org.clapper.util.misc.Logger;
import org.clapper.util.config.ConfigurationException;

/**
 * <p><i>curn</i>: Curiously Uncomplicated RSS Notifier.</p>
 *
 * <p><i>curn</i> is an RSS reader. It scans a configured set of URLs, each
 * one representing an RSS feed, and summarizes the results in an
 * easy-to-read text format. <i>curn</i> keeps track of URLs it's seen
 * before, using an on-disk cache; when using the cache, it will suppress
 * displaying URLs it has already reported (though that behavior can be
 * disabled). <i>curn</i> can be extended to use any RSS parser; its
 * built-in RSS parser, the
 * {@link org.clapper.curn.parser.minirss.MiniRSSParser MiniRSSParser}
 * class, can handle files in
 * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
 * format (0.3) and RSS formats
 * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
 * 0.92,
 * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
 * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.</p>
 *
 * <p>The <tt>Curn</tt> class represents the API entry point into the
 * <i>curn</i> processing. Any program can call a <tt>Curn</tt> object's
 * {@link #processRSSFeeds processRSSFeeds()} method to invoke a <i>curn</i>
 * run. In practice, most people use the existing <tt>Tool</tt> command-line
 * program.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class Curn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String EMAIL_HANDLER_CLASS =
                            "org.clapper.curn.email.EmailOutputHandlerImpl";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private ConfigFile  config           = null;
    private boolean     useCache         = true;
    private FeedCache   cache            = null;
    private Date        currentTime      = new Date();
    private Collection  outputHandlers   = new ArrayList();
    private Collection  emailAddresses   = new ArrayList();

    /**
     * For log messages
     */
    private static Logger log = new Logger (Curn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    public Curn (ConfigFile config)
    {
        this.config = config;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Set the cache's notion of the current time. This method will change
     * the time used when reading and pruning the cache from the current time
     * to the specified time. This method must be called before
     * <tt>processRSSFeeds()</tt>.
     *
     * @param newTime  the time to pretend is the current time
     */
    public void setCurrentTime (Date newTime)
    {
        this.currentTime = newTime;
    }

    /**
     * Read the RSS feeds specified in a parsed configuration, writing them
     * to the output handler(s) specified in the configuration.
     *
     * @param configuration   the parsed configuration
     * @param emailAddresses  a collection of (string) email addresses to
     *                        receive the output, or null (or empty collection)
     *                        for none.
     * @param useCache        whether or not to use the cache
     *
     * @throws IOException              unable to open or read a required file
     * @throws ConfigurationException   error in configuration file
     * @throws RSSParserException       error parsing XML feed(s)
     * @throws CurnException          any other error
     */
    public void processRSSFeeds (ConfigFile configuration,
                                 Collection emailAddresses,
                                 boolean    useCache)
        throws IOException,
               ConfigurationException,
               RSSParserException,
               CurnException
    {
        Iterator    it;
        String      parserClassName;
        Collection  channels;
        boolean     parsingEnabled = true;

        loadOutputHandlers (configuration, emailAddresses);

        if (useCache && (configuration.getCacheFile() != null))
        {
            cache = new FeedCache (configuration);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        if (outputHandlers.size() == 0)
        {
            // No output handlers. No need to instantiate a parser.

            log.debug ("No output handlers. Skipping XML parse phase.");
            parsingEnabled = false;
        }

        Collection feeds = configuration.getFeeds();
        if (feeds.size() == 0)
            throw new ConfigurationException ("No configured RSS feed URLs.");

        if ((configuration.getMaxThreads() == 1) || (feeds.size() == 1))
            channels = doSingleThreadedFeedDownload (parsingEnabled,
                                                     cache,
                                                     configuration);
        else
            channels = doMultithreadedFeedDownload (parsingEnabled,
                                                    cache,
                                                    configuration);

        if (channels.size() > 0)
            displayChannels (channels);

        if ((cache != null) && configuration.mustUpdateCache())
            cache.saveCache();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void loadOutputHandlers (ConfigFile configuration,
                                     Collection emailAddresses)
        throws ConfigurationException,
               CurnException
    {
        String         className;
        OutputHandler  handler;
        Iterator       it;

        if (configuration.totalOutputHandlers() > 0)
        {
            for (it = configuration.getOutputHandlerClassNames().iterator();
                 it.hasNext(); )
            {
                className = (String) it.next();
                handler   = OutputHandlerFactory.getOutputHandler (className);
                outputHandlers.add (handler);
            }

            // If there are email addresses, then attempt to load the email
            // handler, and wrap the other output handlers inside it.

            if ((emailAddresses != null) && (emailAddresses.size() > 0))
            {
                EmailOutputHandler emailHandler;

                emailHandler = (EmailOutputHandler)
                                      OutputHandlerFactory.getOutputHandler
                                                   (EMAIL_HANDLER_CLASS);

                // Place all the other handlers inside the EmailOutputHandler

                for (it = outputHandlers.iterator(); it.hasNext(); )
                    emailHandler.addOutputHandler ((OutputHandler) it.next());

                // Add the email addresses to the handler

                for (it = emailAddresses.iterator(); it.hasNext(); )
                    emailHandler.addRecipient ((String) it.next());

                // Clear the existing set of output handlers and replace it
                // with the email handler.

                outputHandlers.clear();
                outputHandlers.add (emailHandler);
            }
        }
    }

    /**
     * Download the configured feeds sequentially. This method is called
     * when the configured number of concurrent download threads is 1.
     *
     * @param parsingEnabled <tt>true</tt> if parsing is to be done,
     *                       <tt>false</tt> otherwise
     * @param feedCache      the loaded cache of feed data; may be modified
     * @param configuration  the parsed configuration
     *
     * @return a <tt>Collection</tt> of <tt>RSSChannel</tt> objects
     *
     * @throws RSSParserException error parsing feeds
     * @throws CurnException      some other error
     */
    private Collection doSingleThreadedFeedDownload (boolean    parsingEnabled,
                                                     FeedCache  feedCache,
                                                     ConfigFile configuration)
        throws RSSParserException,
               CurnException
    {
        // Instantiate a single FeedDownloadThread object, but call it
        // within this thread, instead of spawning another thread.

        Collection channels = new ArrayList();
        FeedDownloadThread downloadThread;

        log.info ("Doing single-threaded download of feeds.");

        downloadThread = new FeedDownloadThread ("main",
                                                 getRSSParser (configuration),
                                                 feedCache,
                                                 configuration,
                                                 null);

        for (Iterator it = configuration.getFeeds().iterator(); it.hasNext(); )
        {
            FeedInfo feedInfo = (FeedInfo) it.next();

            if (! feedShouldBeProcessed (feedInfo, parsingEnabled))
            {
                // Log messages already emitted.

                continue;
            }

            downloadThread.processFeed (feedInfo);
            if (downloadThread.errorOccurred())
            {
                RSSParserException ex = downloadThread.getException();
                throw new RSSParserException (ex);
            }

            RSSChannel channel = feedInfo.getParsedChannelData();

            if (channel != null)
                channels.add (feedInfo);
        }

        return channels;
    }

    /**
     * Download the configured feeds using multiple simultaneous threads.
     * This method is called when the configured number of concurrent
     * download threads is greater than 1.
     *
     * @param parsingEnabled <tt>true</tt> if parsing is to be done,
     *                       <tt>false</tt> otherwise
     * @param feedCache      the loaded cache of feed data; may be modified
     * @param configuration  the parsed configuration
     *
     * @return a <tt>Collection</tt> of <tt>RSSChannel</tt> objects
     *
     * @throws RSSParserException error parsing feeds
     * @throws CurnException      some other error
     */
    private Collection doMultithreadedFeedDownload (boolean    parsingEnabled,
                                                    FeedCache  feedCache,
                                                    ConfigFile configuration)
        throws RSSParserException,
               CurnException
    {
        int                 maxThreads = configuration.getMaxThreads();
        Collection          feeds      = configuration.getFeeds();
        int                 totalFeeds = feeds.size();
        Collection          channels   = new ArrayList();
        Collection          threads    = new ArrayList();
        List                feedQueue  = new LinkedList();
        Iterator            it;
        FeedDownloadThread  thread;

        if (maxThreads > totalFeeds)
            maxThreads = totalFeeds;

        log.info ("Doing multithreaded download of feeds, using "
                + maxThreads
                + " threads.");

        // Fill the feed queue and make it a synchronized list.

        for (it = feeds.iterator(); it.hasNext(); )
        {
            FeedInfo feedInfo = (FeedInfo) it.next();

            if (! feedShouldBeProcessed (feedInfo, parsingEnabled))
            {
                // Log messages already emitted.

                continue;
            }

            feedQueue.add (feedInfo);
        }

        if (feedQueue.size() == 0)
            throw new CurnException ("All configured feeds are disabled.");

        feedQueue = Collections.synchronizedList (feedQueue);

        // Create the thread objects. They'll pull feeds off the queue
        // themselves.

        for (int i = 0; i < maxThreads; i++)
        {
            thread = new FeedDownloadThread (String.valueOf (i),
                                             getRSSParser (configuration),
                                             feedCache,
                                             configuration,
                                             feedQueue);
            thread.start();
            threads.add (thread);
        }

        log.debug ("Main thread priority is "
                 + Thread.currentThread().getPriority());

        log.debug ("All feeds have been parceled out to threads. Waiting for "
                 + "threads to complete.");

        for (it = threads.iterator(); it.hasNext(); )
        {
            thread = (FeedDownloadThread) it.next();
            String threadName = thread.getName();

            log.info ("Waiting for thread " + threadName);

            try
            {
                thread.join();
            }

            catch (InterruptedException ex)
            {
                log.debug ("Interrupted during thread join: " + ex.toString());
            }

            log.info ("Joined thread " + threadName);
        }

        // Now, scan the list of feeds and find those with channel data.

        for (it = feeds.iterator(); it.hasNext(); )
        {
            FeedInfo   feedInfo = (FeedInfo) it.next();
            RSSChannel channel  = feedInfo.getParsedChannelData();

            if (channel != null)
                channels.add (feedInfo);
        }

        return channels;
    }

    /**
     * Get a new instance of an RSS parser.
     *
     * @param configuration the parsed configuration
     *
     * @return the RSSParser
     *
     * @throws RSSParserException error instantiating parser
     */
    private RSSParser getRSSParser (ConfigFile configuration)
        throws RSSParserException
    {
        String parserClassName = configuration.getRSSParserClassName();
        log.info ("Getting parser \"" + parserClassName + "\"");
        return RSSParserFactory.getRSSParser (parserClassName);
    }

    /**
     * Determine whether a feed should be processed. If this method
     * determines that a feed should not be processed, it emits appropriate
     * log messages.
     *
     * @param feedInfo        the feed information
     * @param parsingEnabled  whether or not RSS parsing is enabled
     *
     * @return <tt>true</tt> if the feed should be processed,
     *         <tt>false</tt> if it should be skipped
     */
    private boolean feedShouldBeProcessed (FeedInfo feedInfo,
                                           boolean  parsingEnabled)
    {
        boolean process = true;

        if (! feedInfo.feedIsEnabled())
        {
            log.info ("Skipping disabled feed: "
                    + feedInfo.getURL().toString());
            process = false;
        }

        else if ((! parsingEnabled) && (feedInfo.getSaveAsFile() == null))
        {
            log.debug ("Feed "
                     + feedInfo.getURL().toString()
                     + ": RSS parsing is disabled and there's no save "
                     + "file. There's no sense processing this feed.");
            process = false;
        }

        return process;
    }

    private void displayChannels (Collection channels)
        throws CurnException,
               ConfigurationException
    {
        OutputHandler firstOutput = null;

        // Dump the output to each output handler

        for (Iterator itHandler = outputHandlers.iterator();
             itHandler.hasNext(); )
        {
            OutputHandler handler;

            handler = (OutputHandler) itHandler.next();

            log.info ("Initializing output handler "
                    + handler.getClass().getName());
            handler.init (config);

            for (Iterator itChannel = channels.iterator();
                 itChannel.hasNext(); )
            {
                FeedInfo fi = (FeedInfo) itChannel.next();
                handler.displayChannel (fi.getParsedChannelData(), fi);
            }

            handler.flush();

            if ((firstOutput == null) && (handler.hasGeneratedOutput()))
                firstOutput = handler;
        }

        // If we're not emailing the output, then dump the output from the
        // first handler to the screen.

        if (emailAddresses.size() == 0)
        {
            if (firstOutput == null)
                log.info ("None of the output handlers produced output.");

            else
            {
                InputStream output = firstOutput.getGeneratedOutput();

                try
                {
                    FileUtil.copyStream (output, System.out);
                    output.close();
                }

                catch (IOException ex)
                {
                    throw new CurnException ("Failed to copy output from "
                                           + "handler "
                                           + firstOutput.getClass().getName()
                                           + " to standard output.",
                                             ex);
                }
            }
        }
    }
}
