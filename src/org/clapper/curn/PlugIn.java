/*---------------------------------------------------------------------------*\
  $Id$
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
 * This interface defines the methods that must be supported by a class
 * that is to be plugged into <i>curn</i> as a generalized plug-in.
 * <i>curn</i> plug-ins are invoked at various phases of <i>curn</i>
 * execution:
 *
 * <table>
 *   <tr>
 *     <th>Plug-in Phase</th>
 *     <th>Explanation</th>
 *     <th>Method ("hook")</th>
 *   </tr>
 *   <tr>
 *     <td><i>Startup</i></td>
 *     <td>Called after <i>curn</i> has started, but before it has loaded
 *         its configuration file or its cache.</td>
 *     <td>{@link #runStartupHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Main Configuration Entry Read</i></td>
 *     <td>Called after a configuration item is encountered and read in the
 *         main [curn*] configuration file section. All configuration data
 *         items are passed to all plug-ins. If a plug-in is not interested
 *         in a configuration item, should simply ignore it.
 *     </td>
 *     <td>{@link #runMainConfigItemHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Feed Configuration Entry Read</i></td>
 *     <td>Called after a configuration item is encountered and read in a
 *         [Feed*] configuration file section. All configuration data items
 *         are passed to all plug-ins. If a plug-in is not interested in a
 *         configuration item, should simply ignore it.
 *     </td>
 *     <td>{@link #runFeedConfigItemHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Output Handler Configuration Entry Read</i></td>
 *     <td>Called after a configuration item is encountered and read in an
 *         [OutputHandler*] configuration file section. All configuration
 *         data items are passed to all plug-ins. If a plug-in is not
 *         interested in a configuration item, should simply ignore it.
 *     </td>
 *     <td>{@link #runOutputHandlerConfigItemHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Unknown Section Configuration Entry Read</i></td>
 *     <td>Called after a configuration item is encountered and read in an
 *         unknown configuration file section. All configuration
 *         data items are passed to all plug-ins. If a plug-in is not
 *         interested in a configuration item, should simply ignore it.
 *     </td>
 *     <td>{@link #runUnknownSectionConfigItemHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Post-Configuration</i></td>
 *     <td>Called after the entire configuration has been read and parsed,
 *         but before any feeds are processed. Intercepting this event is
 *         useful for plug-ins that want to adjust the configuration. For
 *         instance, the <i>curn</i> command-line wrapper intercepts this
 *         plug-in event so it can adjust the configuration to account for
 *         command line options.
 *     </td>
 *     <td>{@link #runPostConfigHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Cache Loaded</i></td>
 *     <td>Called after the <i>curn</i> cache has been read (and after
 *         any expired entries have been purged), but before any feeds
 *         are processed.
 *     </td>
 *     <td>{@link #runCacheLoadedHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Pre-feed download</i></td>
 *     <td>Called right before a feed is downloaded.</td>
 *     <td>{@link #runPreFeedDownloadHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Post-feed download</i></td>
 *     <td>Called right after a feed's XML has been downloaded,
 *         but before it has been parsed. Receives a <tt>File</tt> that
 *         points to the downloaded XML.</td>
 *     <td>{@link #runPostFeedDownloadHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Post-feed parse</td>
 *     <td>Gets an individual parsed {@link RSSChannel}, right after
 *         it has been parsed and before it is processed. It is at this
 *         point that plug-ins could weed out or reorder channel items.</td>
 *     <td>{@link #runPostFeedParseHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Pre-channel output</td>
 *     <td>Called with an {@link RSSChannel} object just before it is
 *         passed to the output handlers. This method will get the opportunity
 *         to suppress the output, by returning <tt>false</tt>.</td>
 *     <td>{@link #runPreFeedOutputHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Pre-cache save</i></td>
 *     <td>Called just before <i>curn</i> saves its cache. This phase gives
 *         a plug-in the opportunity to alter the cache.</td>
 *     <td>{@link #runPreCacheSaveHook}</td>
 *   </tr>
 *   <tr>
 *     <td><i>Shutdown</i></td>
 *     <td>Called after <i>curn</i> saves its cache, just before it exits.</td>
 *     <td>{@link #runShutdownHook}</td>
 *   </tr>
 * </table>
 *
 * <p>A plug-in class must define all the methods defined by this interface;
 * however, for plug-ins that don't want to intercept all the phases, there's
 * an {@link AbstractPlugIn} class that defines no-op methods for each of the
 * methods in this interface. A plug-in class can extend the
 * {@link AbstractPlugIn} class and provide only those methods it wants.</p>
 *
 * @see AbstractPlugIn
 * @see MetaPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PlugIn
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getName();

    /**
     * Called by the plug-in manager right after <i>curn</i> has started,
     * but before it has loaded its configuration file or its cache. This
     * method can be used for plug-in initialization.
     *
     * @throws CurnException on error
     */
    public void runStartupHook()
        throws CurnException;

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item in the main [curn] configuration
     * section. All configuration items are passed, one by one, to each
     * loaded plug-in. If a plug-in class is not interested in a particular
     * configuration item, this method should simply return without doing
     * anything. Note that some configuration items may simply be variable
     * assignment; there's no real way to distinguish a variable assignment
     * from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runMainConfigItemHook (String     sectionName,
                                       String     paramName,
                                       CurnConfig config)
	throws CurnException;

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item in a "feed" configuration section.
     * All configuration items are passed, one by one, to each loaded
     * plug-in. If a plug-in class is not interested in a particular
     * configuration item, this method should simply return without doing
     * anything. Note that some configuration items may simply be variable
     * assignment; there's no real way to distinguish a variable assignment
     * from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * @param feedInfo     partially complete <tt>FeedInfo</tt> object
     *                     for the feed. The URL is guaranteed to be
     *                     present, but no other fields are.
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public void runFeedConfigItemHook (String     sectionName,
                                       String     paramName,
                                       CurnConfig config,
                                       FeedInfo   feedInfo)
	throws CurnException;

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item in an output handler configuration
     * section. All configuration items are passed, one by one, to each
     * loaded plug-in. If a plug-in class is not interested in a particular
     * configuration item, this method should simply return without doing
     * anything. Note that some configuration items may simply be variable
     * assignment; there's no real way to distinguish a variable assignment
     * from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * @param handler      partially complete {@link ConfiguredOutputHanlder}
     *                     object. The class name is guaranteed to be set,
     *                     but the other fields may not be.
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public void
    runOutputHandlerConfigItemHook (String                  sectionName,
                                    String                  paramName,
                                    CurnConfig              config,
                                    ConfiguredOutputHandler handler)
	throws CurnException;

    /**
     * Called by the plug-in manager right after <i>curn</i> has read and
     * processed a configuration item in an unknown configuration section.
     * All configuration items are passed, one by one, to each loaded
     * plug-in. If a plug-in class is not interested in a particular
     * configuration item, this method should simply return without doing
     * anything. Note that some configuration items may simply be variable
     * assignment; there's no real way to distinguish a variable assignment
     * from a blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     * 
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public void
    runUnknownSectionConfigItemHook (String     sectionName,
                                     String     paramName,
                                     CurnConfig config)
	throws CurnException;

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
	throws CurnException;

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
	throws CurnException;

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
     *         feed, <tt>false</tt> to skip the feed
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPreFeedDownloadHook (FeedInfo feedInfo)
	throws CurnException;

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
     * @param encoding      the encoding used to store the data in the file,
     *                      or null for the default
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
					    File     feedDataFile,
                                            String   encoding)
	throws CurnException;

    /**
     * Called immediately after a feed is parsed, but before it is
     * otherwise processed. This method can return <tt>false</tt> to signal
     * <i>curn</i> that the feed should be skipped. For instance, a plug-in
     * that filters on the parsed feed data could use this method to weed
     * out non-matching feeds before they are downloaded. Similarly, a
     * plug-in that edits the parsed data (removing or editing individual
     * items, for instance) could use method to do so.
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed that
     *                  has been downloaded and parsed.
     * @param channel   the parsed channel data
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
    public boolean runPostFeedParseHook (FeedInfo feedInfo, RSSChannel channel)
	throws CurnException;

    /**
     * Called immediately before a parsed feed is passed to an output
     * handler. This method cannot affect the feed's processing. (The time
     * to stop the processing of a feed is in one of the other, preceding
     * phases.) This method will be called multiple times for each feed if
     * there are multiple output handlers.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed.
     * @param channel       the parsed channel data. The plug-in is free
     *                      to edit this data; it's receiving a copy
     *                      that's specific to the output handler.
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public void runPreFeedOutputHook (FeedInfo      feedInfo,
                                      RSSChannel    channel,
                                      OutputHandler outputHandler)
	throws CurnException;

    /**
     * Called immediately after a parsed feed is passed to an output
     * handler. This method cannot affect the feed's processing. (The time
     * to stop the processing of a feed is in one of the other, preceding
     * phases.) This method will be called multiple times for each feed if
     * there are multiple output handlers.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed.
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public void runPostFeedOutputHook (FeedInfo      feedInfo,
                                       OutputHandler outputHandler)
	throws CurnException;

    /**
     * Called immediately after an output handler is flushed (i.e., after
     * its output has been written to a temporary file), but before that
     * output is displayed, emailed, etc.
     *
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @return <tt>true</tt> if <i>curn</i> should process the output,
     *         <tt>false</tt> to skip the output from the handler.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public boolean runPostOutputHandlerFlushHook (OutputHandler outputHandler)
	throws CurnException;

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
	throws CurnException;

    /**
     * Called by the plug-in manager right before <i>curn</i> gets ready
     * to exit. This hook allows plug-ins to perform any clean-up they
     * require.
     *
     * @throws CurnException on error
     */
    public void runShutdownHook()
        throws CurnException;
}
