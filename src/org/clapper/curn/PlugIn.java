/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;

/**
 * <p>This interface defines the methods that must be supported by a class
 * that is to be plugged into <i>curn</i> as a generalized plug-in.
 * <i>curn</i> plug-ins are invoked at various phases of <i>curn</i>
 * execution.</p>
 *
 * <p>Each plug-in phase is represented by its own Java interface, and
 * each interface has exactly one method. A plug-in that intercepts
 * multiple <i>curn</i> processing phases must implement the interfaces for
 * each of the phases. Here are the plug-in phases, in execution order.</p>
 *
 * <table border="1" align="center">
 *   <tr valign="top">
 *     <th align="left">Plug-in interface</th>
 *     <th align="left">Plug-in method</th>
 *     <th align="left">Description</th>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link StartupPlugIn}</td>
 *     <td align="left">{@link StartupPlugIn#runStartupPlugIn runStartupPlugIn()}</td>
 *     <td>Called immediately after <i>curn</i> has started, but before it has
 *         loaded its configuration file or its cache. Intercepting this phase
 *         is useful if a plug-in needs to perform initialization.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link MainConfigItemPlugIn}</td>
 *     <td align="left">{@link MainConfigItemPlugIn#runMainConfigItemPlugIn runMainConfigItemPlugIn()}</td>
 *     <td>Called immediately after <i>curn</i> has read and processed a
 *         configuration item in the main <tt>[curn]</tt> configuration
 *         section. All configuration items are passed, one by one, to each
 *         loaded plug-in. If a plug-in class is not interested in a
 *         particular configuration item, its
 *         <tt>runMainConfigItemPlugIn()</tt> method should simply return
 *         without doing anything. Note that some configuration items may
 *         simply be variable assignment; there's no real way to distinguish a
 *         variable assignment from a blessed configuration item.</td>
 *
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link FeedConfigItemPlugIn}</td>
 *     <td align="left">{@link FeedConfigItemPlugIn#runFeedConfigItemPlugIn runFeedConfigItemPlugIn()}</td>
 *     <td>Called immediately after <i>curn</i> has read and processed a
 *         configuration item in a "Feed" configuration section. All
 *         configuration items are passed, one by one, to each loaded plug-in.
 *         If a plug-in class is not interested in a particular configuration
 *         item, its <tt>runFeedConfigItemPlugIn()</tt> method should simply
 *         return without doing anything. Note that some configuration items
 *         may simply be variable assignment; there's no real way to
 *         distinguish a variable assignment from a blessed configuration
 *         item.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link OutputHandlerConfigItemPlugIn}</td>
 *     <td align="left">{@link OutputHandlerConfigItemPlugIn#runOutputHandlerConfigItemPlugIn runOutputHandlerConfigItemPlugIn()}</td>
 *     <td>Called immediately after <i>curn</i> has read and processed a
 *         configuration item in an "OutputHandler" configuration section. All
 *         configuration items are passed, one by one, to each loaded plug-in.
 *         If a plug-in class is not interested in a particular configuration
 *         item, its <tt>runOutputHandlerConfigItemPlugIn()</tt> method should
 *         simply return without doing anything. Note that some configuration
 *         items may simply be variable assignment; there's no real way to
 *         distinguish a variable assignment from a blessed configuration
 *         item.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link UnknownSectionConfigItemPlugIn}</td>
 *     <td align="left">{@link UnknownSectionConfigItemPlugIn#runUnknownSectionConfigItemPlugIn runUnknownSectionConfigItemPlugIn()}</td>
 *     <td>Called immediately after <i>curn</i> has read and processed a
 *         configuration item in an unknown configuration section. All
 *         configuration items are passed, one by one, to each loaded plug-in.
 *         If a plug-in class is not interested in a particular configuration
 *         item, its <tt>runUnknownSectionConfigItemPlugIn()</tt> method should
 *         simply return without doing anything. Note that some configuration
 *         items may simply be variable assignment; there's no real way to
 *         distinguish a variable assignment from a blessed configuration
 *         item.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostConfigPlugIn}</td>
 *     <td align="left">{@link PostConfigPlugIn#runPostConfigPlugIn runPostConfigPlugIn()}</td>
 *     <td>Called after the entire configuration has been read and parsed, but
 *         before any feeds are processed. Intercepting this event is useful
 *         for plug-ins that want to adjust the configuration. For instance,
 *         the <i>curn</i> command-line wrapper intercepts this plug-in event
 *         so it can adjust the configuration to account for command line
 *         options.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link CacheLoadedPlugIn}</td>
 *     <td align="left">{@link CacheLoadedPlugIn#runCacheLoadedPlugIn runCacheLoadedPlugIn()}</td>
 *     <td>Called after the <i>curn</i> cache has been read (and after any
 *         expired entries have been purged), but before any feeds are processed.
 *       </td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PreFeedDownloadPlugIn}</td>
 *     <td align="left">{@link PreFeedDownloadPlugIn#runPreFeedDownloadPlugIn runPreFeedDownloadPlugIn()}</td>
 *     <td>Called just before a feed is downloaded. This method can return
 *         <tt>false</tt> to signal <i>curn</i> that the feed should be
 *         skipped. The plug-in method can also set values on the
 *         <tt>URLConnection</tt> used to download the plug-in, via
 *         <tt>URL.setRequestProperty()</tt>. (Note that <i>all</i> URLs, even
 *         <tt>file:</tt> URLs, are passed into this method. Setting a request
 *         property on the <tt>URLConnection</tt> object for a <tt>file:</tt>
 *         URL will have no effect--though it isn't specifically harmful.)</p>
 *
 *         <p>Possible uses for a pre-feed download plug-in include:</p>
 *
 *         <ul>
 *           <li>filtering on feed URL to prevent downloading non-matching feeds
 *           <li>changing the default User-Agent value
 *           <li>setting a non-standard HTTP header field
 *         </ul>
 *       </td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostFeedDownloadPlugIn}</td>
 *     <td align="left">{@link PostFeedDownloadPlugIn#runPostFeedDownloadPlugIn runPostFeedDownloadPlugIn()}</td>
 *     <td> Called immediately after a feed is downloaded. This method can
 *         return <tt>false</tt> to signal <i>curn</i> that the feed should be
 *         skipped. For instance, a plug-in that filters on the unparsed XML
 *         feed content could use this method to weed out non-matching feeds
 *         before they are downloaded.
 *       </td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostFeedParsePlugIn}</td>
 *     <td align="left">{@link PostFeedParsePlugIn#runPostFeedParsePlugIn runPostFeedParsePlugIn()}</td>
 *     <td>Called immediately after a feed is parsed, but before it is
 *         otherwise processed. A post-feed parse plug-in has access to the
 *         <i>parsed</i> RSS feed data, via an {@link RSSChannel} object. This
 *         method can return <tt>false</tt> to signal <i>curn</i> that the
 *         feed should be skipped. For instance, a plug-in that filters on the
 *         parsed feed data could use this method to weed out non-matching
 *         feeds before they are downloaded. Similarly, a plug-in that edits
 *         the parsed data (removing or editing individual items, for
 *         instance) could use method to do so.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PreFeedOutputPlugIn}</td>
 *     <td align="left">{@link PreFeedOutputPlugIn#runPreFeedOutputPlugIn runPreFeedOutputPlugIn()}</td>
 *     <td>Called immediately before a parsed feed is passed to an output
 *         handler. A pre-feed output plug-in cannot affect the feed's
 *         processing. (The time to stop the processing of a feed is in one of
 *         the other, preceding phases.) This method will be called multiple
 *         times for each feed if there are multiple output handlers.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostFeedOutputPlugIn}</td>
 *     <td align="left">{@link PostFeedOutputPlugIn#runPostFeedOutputPlugIn runPostFeedOutputPlugIn()}</td>
 *     <td>Called immediately after a parsed feed is passed to an output
 *         handler. A post-feed output plug-in cannot affect the feed's
 *         processing. (The time to stop the processing of a feed is in one of
 *         the other, preceding phases.) This method will be called multiple
 *         times for each feed if there are multiple output handlers.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostOutputHandlerFlushPlugIn}</td>
 *     <td align="left">{@link PostOutputHandlerFlushPlugIn#runPostOutputHandlerFlushPlugIn runPostOutputHandlerFlushPlugIn()}</td>
 *     <td>Called immediately after an output handler is flushed (i.e., after
 *         it has been called to process all feeds and its output has been
 *         written to a temporary file), but before that output is
 *         displayed, emailed, etc.
 *       </td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PostOutputPlugIn}</td>
 *     <td align="left">{@link PostOutputPlugIn#runPostOutputPlugIn runPostOutputPlugIn()}</td>
 *     <td>Called after <i>curn</i> has flush <i>all</i> output handlers. A
 *         post-output plug-in is a useful place to consolidate the output from
 *         all output handlers. For instance, such a plug-in might pack all the
 *         output into a zip file, or email it.</td>
 *       </td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link PreCacheSavePlugIn}</td>
 *     <td align="left">{@link PreCacheSavePlugIn#runPreCacheSavePlugIn runPreCacheSavePlugIn()}</td>
 *     <td>Called right before the <i>curn</i> cache is to be saved. A plug-in
 *         might choose to edit the cache at this point.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td align="left">{@link ShutdownPlugIn}</td>
 *     <td align="left">{@link ShutdownPlugIn#runShutdownPlugIn runShutdownPlugIn()}</td>
 *     <td> Called just before <i>curn</i> gets ready to exit. This method
 *          allows plug-ins to perform any clean-up they require.</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public interface PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getPlugInName();

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getPlugInSortKey();

    /**
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called.
     *
     * @throws CurnException on error
     */
    public void initPlugIn()
        throws CurnException;
}
