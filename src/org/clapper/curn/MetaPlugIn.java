/*---------------------------------------------------------------------------*\
  $Id: OutputHandler.java 5749 2006-03-24 16:21:51Z bmc $
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

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

import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedCache;
import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.logging.Logger;

import java.io.File;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A <tt>MetaPlugIn</tt> object is basically a plug-in that contains all the
 * loaded plug-ins. It's a singleton that makes it easier for <i>curn</i>
 * to invoke the various loaded plugins. It is not used outside of
 * <i>curn</i>. The <tt>MetaPlugIn</tt> singleton object is loaded by an
 * instance of the {@link PlugInManager} class.
 *
 * @see PlugIn
 * @see PlugInManager
 * @see AbstractPlugIn
 * @see Curn
 *
 * @version <tt>$Revision: 5749 $</tt>
 */
public class MetaPlugIn implements PlugIn
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The loaded plug-ins.
     */
    private Collection<PlugIn> plugIns = null;

    /**
     * Lock object, for synchronization
     */
    private static Object lock = new Object();

    /**
     * The singleton
     */
    private static MetaPlugIn metaPlugIn = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (MetaPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Cannot be instantiated normally.
     */
    private MetaPlugIn()
    {
        plugIns = new ArrayList<PlugIn>();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getName()
    {
        return getClass().getName();
    }

    /**
     * Get the <tt>MetaPlugIn</tt> singleton.
     *
     * @return the <tt>MetaPlugIn</tt> singleton
     */
    public static MetaPlugIn getMetaPlugIn()
    {
        synchronized (lock)
        {
            if (metaPlugIn == null)
                metaPlugIn = new MetaPlugIn();
        }

        return metaPlugIn;
    }

    /**
     * Add a plug-in to the list of plug-ins wrapped in this object.
     * This method is only intended for use by the {@link PlugInManager}.
     *
     * @param plugIn  the {@link PlugIn} to add
     */
    public void addPlugIn (PlugIn plugIn)
    {
        synchronized (lock)
        {
            plugIns.add (plugIn);
        }
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    /**
     * Called by the plug-in manager right after <i>curn</i> has started,
     * but before it has loaded its configuration file or its cache.
     *
     * @throws CurnException on error
     */
    public void runStartupHook()
        throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runStartupHook", plugIn);
                plugIn.runStartupHook();
            }
        }
    }

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item. All configuration items are passed,
     * one by one, to each loaded plug-in. If a plug-in class is not
     * interested in a particular configuration item, this method should
     * simply return without doing anything. Note that some configuration
     * items may simply be variable assignment; there's no real way to
     * distinguish a variable assignment from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param paramValue   the parameter value
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runConfigurationEntryHook (String sectionName,
                                           String paramName,
                                           String paramValue)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runConfigurationEntryHook",
                                   plugIn,
                                   sectionName,
                                   paramName,
                                   paramValue);
                plugIn.runConfigurationEntryHook (sectionName,
                                                  paramName,
                                                  paramValue);
            }
        }
    }

    /**
     * Called after the entire configuration has been read and parsed, but
     * before any feeds are processed. Intercepting this event is useful
     * for plug-ins that want to adjust the configuration. For instance,
     * the <i>curn</i> command-line wrapper intercepts this plug-in event
     * so it can adjust the configuration to account for command line
     * options.
     *
     * @param config  the parsed {@link CurnConfig} object
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runPostConfigurationHook (CurnConfig config)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostConfigurationHook", plugIn);
                plugIn.runPostConfigurationHook (config);
            }
        }
    }

    /**
     * Called after the <i>curn</i> cache has been read (and after any
     * expired entries have been purged), but before any feeds are processed.
     *
     * @param cache  the loaded {@link FeedCache} object
     * 
     * @throws CurnException on error
     *
     * @see FeedCache
     */
    public void runCacheLoadedHook (FeedCache cache)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runCacheLoadedHook", plugIn);
                plugIn.runCacheLoadedHook (cache);
            }
        }
    }

    /**
     * Called just before a feed is downloaded. This method can return
     * <tt>false</tt> to signal <i>curn</i> that the feed should be skipped.
     * For instance, a plug-in that filters on feed URL could use this
     * method to weed out non-matching feeds before they are downloaded.
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed to be
     *                  downloaded
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed. A return value of
     *         <tt>false</tt> aborts all further processing on the feed.
     *         In particular, <i>curn</i> will not pass the feed along to
     *         other plug-ins that have yet to be notified of this event.
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPreFeedDownloadHook (FeedInfo feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreFeedDownloadHook", plugIn);
                keepGoing = plugIn.runPreFeedDownloadHook (feedInfo);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    /**
     * Called immediately after a feed is downloaded. This method can
     * return <tt>false</tt> to signal <i>curn</i> that the feed should be
     * skipped. For instance, a plug-in that filters on the unparsed XML
     * feed content could use this method to weed out non-matching feeds
     * before they are downloaded.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded
     * @param feedDataFile  the file containing the downloaded, unparsed feed 
     *                      XML. <b><i>curn</i> may delete this file after all
     *                      plug-ins are notified!</b>
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed. A return value of
     *         <tt>false</tt> aborts all further processing on the feed.
     *         In particular, <i>curn</i> will not pass the feed along to
     *         other plug-ins that have yet to be notified of this event.
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPostFeedDownloadHook (FeedInfo feedInfo,
					    File     feedDataFile)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostFeedDownloadHook", plugIn);
                keepGoing = plugIn.runPostFeedDownloadHook (feedInfo,
                                                            feedDataFile);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    /**
     * Called immediately after a feed is parsed, but before it is
     * otherwise processed. This method can return <tt>false</tt> to signal
     * <i>curn</i> that the feed should be skipped. For instance, a plug-in
     * that filters on the parsed feed data could use this method to weed
     * out non-matching feeds before they are downloaded. Similarly, a
     * plug-in that edits the parsed data (removing or editing individual
     * items, for instance) could use method to do so.
     *
     * @param channel       the {@link RSSChannel} object containing the
     *                      parsed feed data
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed. A return value of
     *         <tt>false</tt> aborts all further processing on the feed.
     *         In particular, <i>curn</i> will not pass the feed along to
     *         other plug-ins that have yet to be notified of this event.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public boolean runPostFeedParseHook (RSSChannel channel,
					 FeedInfo   feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPostFeedParseHook", plugIn);
                keepGoing = plugIn.runPostFeedParseHook (channel, feedInfo);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    /**
     * Called immediately before a parsed feed is passed to the configured
     * output handlers. This method cannot affect the feed's processing.
     * (The time to stop the processing of a feed is in one of the
     * other, preceding phases.)
     *
     * @param channel       the {@link RSSChannel} object containing the
     *                      parsed feed data
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public boolean runPreFeedOutputHook (RSSChannel channel,
					 FeedInfo   feedInfo)
	throws CurnException
    {
        boolean keepGoing = true;

        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreFeedOutputHook", plugIn);
                keepGoing = plugIn.runPreFeedOutputHook (channel, feedInfo);
                if (! keepGoing)
                    break;
            }
        }

        return keepGoing;
    }

    /**
     * Called right before the <i>curn</i> cache is to be saved. A plug-in
     * might choose to edit the cache at this point.
     *
     * @param cache  the {@link FeedCache} object
     * 
     * @throws CurnException on error
     *
     * @see FeedCache
     */
    public void runPreCacheSaveHook (FeedCache cache)
	throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runPreCacheSaveHook", plugIn);
                plugIn.runPreCacheSaveHook (cache);
            }
        }
    }

    /**
     * Called by the plug-in manager right before <i>curn</i> gets ready
     * to exit. This hook allows plug-ins to perform any clean-up they
     * require.
     *
     * @throws CurnException on error
     */
    public void runShutdownHook()
        throws CurnException
    {
        synchronized (lock)
        {
            for (PlugIn plugIn : plugIns)
            {
                logHookInvocation ("runShutdownHook", plugIn);
                plugIn.runShutdownHook();
            }
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Log a hook invocation.
     *
     * @param methodName  calling method name
     * @param plugIn      plug-in class
     * @param args        method args, if any
     */
    private void logHookInvocation (String    methodName,
                                    PlugIn    plugIn,
                                    Object... args)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();

            buf.append ("invoking ");
            buf.append (methodName);
            String sep = "(";

            for (Object arg : args)
            {
                buf.append (sep);
                sep = ", ";

                if (arg instanceof Number)
                    buf.append (arg.toString());

                else
                {
                    buf.append ('"');
                    buf.append (arg.toString());
                    buf.append ('"');
                }
            }

            buf.append (") for plug-in ");
            buf.append (plugIn.getClass().getName());
            log.debug (buf.toString());
        }
    }
}
