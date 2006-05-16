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

import java.io.File;

/**
 * This abstract class defines a base class for a plug-in. It defines no-op
 * methods for all the plug-in hooks defined in the {@link PlugIn}
 * interface, so subclasses of this class can override only the methods for
 * the events they are interested in intercepting.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see Curn
 *
 * @version <tt>$Revision: 5749 $</tt>
 */
public abstract class AbstractPlugIn implements PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in. This default version just
     * returns the class name.
     *
     * @return the name
     */
    public String getName()
    {
        return getClass().getName();
    }

    /**
     * Called by the plug-in manager right after <i>curn</i> has started,
     * but before it has loaded its configuration file or its cache.
     *
     * @throws CurnException on error
     */
    public void runStartupHook()
        throws CurnException
    {
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
        return true;
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
        return true;
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
        return true;
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
        return true;
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
    }
}
