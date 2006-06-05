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

package org.clapper.curn.plugins;

import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PostConfigPlugIn;
import org.clapper.curn.PreFeedDownloadPlugIn;
import org.clapper.curn.Version;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import java.net.URLConnection;

import java.util.Map;
import java.util.HashMap;

/**
 * The <tt>UserAgentPlugIn</tt> handles setting the global and per-feed
 * HTTP user agent settings, overriding the default <i>curn</i> user agent
 * setting. It intercepts the following configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th>Section</th>
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[curn]</tt></td>
 *     <td><tt>UserAgent</tt></td>
 *     <td>The default user agent, if none is supplied in individual feed
 *         sections.</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[Feed<i>xxx</i>]</tt></td>
 *     <td><tt>UserAgent</tt></td>
 *     <td>User agent to use for a given feed.</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class UserAgentPlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PostConfigPlugIn,
               PreFeedDownloadPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

     private static final String VAR_USER_AGENT = "UserAgent";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,String> perFeedUserAgentMap =
        new HashMap<FeedInfo,String>();

    /**
     * Default user agent
     */
    private String defaultUserAgent = null;

    /**
     * Saved reference to the configuration
     */
    private CurnConfig config = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (UserAgentPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public UserAgentPlugIn()
    {
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
        return "User Agent";
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in the main [curn] configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
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
    public void runMainConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
	throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_USER_AGENT))
            {
                defaultUserAgent = config.getConfigurationValue (sectionName,
                                                                 paramName);
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
    }

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in a "feed" configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the active configuration
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
    public void runFeedConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config,
                                         FeedInfo   feedInfo)
	throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_USER_AGENT))
            {
                String value = config.getConfigurationValue (sectionName,
                                                             paramName);
                perFeedUserAgentMap.put (feedInfo, value);
                log.debug ("[" + sectionName + "]: UserAgent=" + value);
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
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
    public void runPostConfigurationPlugIn (CurnConfig config)
	throws CurnException
    {
        this.config = config;

        if (defaultUserAgent == null)
        {
            StringBuilder buf = new StringBuilder();

            // Standard format seems to be:
            //
            // tool/version (+url)
            //
            // e.g.: Googlebot/2.1 (+http://www.google.com/bot.htm

            buf.append (Version.getUtilityName());
            buf.append ('/');
            buf.append (Version.getVersionNumber());
            buf.append (" (+");
            buf.append (Version.getWebSite());
            buf.append (')');
            defaultUserAgent = buf.toString();
        }
    }

    /**
     * <p>Called just before a feed is downloaded. This method can return
     * <tt>false</tt> to signal <i>curn</i> that the feed should be
     * skipped. The plug-in method can also set values on the
     * <tt>URLConnection</tt> used to download the plug-in, via
     * <tt>URL.setRequestProperty()</tt>. (Note that <i>all</i> URLs, even
     * <tt>file:</tt> URLs, are passed into this method. Setting a request
     * property on the <tt>URLConnection</tt> object for a <tt>file:</tt>
     * URL will have no effect--though it isn't specifically harmful.)</p>
     *
     * <p>Possible uses for a pre-feed download plug-in include:</p>
     *
     * <ul>
     *   <li>filtering on feed URL to prevent downloading non-matching feeds
     *   <li>changing the default User-Agent value
     *   <li>setting a non-standard HTTP header field
     * </ul>
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed to be
     *                  downloaded
     * @param urlConn   the <tt>java.net.URLConnection</tt> object that will
     *                  be used to download the feed's XML.
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPreFeedDownloadPlugIn (FeedInfo      feedInfo,
                                             URLConnection urlConn)
	throws CurnException
    {
        String userAgent = perFeedUserAgentMap.get (feedInfo);
        if (userAgent == null)
            userAgent = defaultUserAgent;

        // Set the user-agent header.

        log.debug ("Using user agent \""
                 + userAgent
                 + "\" for feed \""
                 + feedInfo.getURL()
                 + "\"");
        urlConn.setRequestProperty ("User-Agent", userAgent);

        return true;
    }
}