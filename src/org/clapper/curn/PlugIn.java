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
}
