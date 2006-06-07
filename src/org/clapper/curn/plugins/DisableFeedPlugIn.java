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

import org.clapper.curn.Constants;
import org.clapper.curn.Curn;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PreFeedDownloadPlugIn;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;

import java.io.File;
import java.io.IOException;

import java.net.URLConnection;

import java.util.Map;
import java.util.HashMap;

/**
 * The <tt>DisableFeedPlugIn</tt> handles disabling a feed. It intercepts
 * the following per-feed configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>Disabled</tt></td>
 *     <td>Flag indicating whether or not to disable the feed.</td>
 *   </tr>
 *   <tr valign="top">
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class DisableFeedPlugIn
    implements FeedConfigItemPlugIn,
               PreFeedDownloadPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_DISABLED = "Disabled";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed "ignore" flags, by feed
     */
    private Map<FeedInfo,Boolean> perFeedDisabledFlagMap =
        new HashMap<FeedInfo,Boolean>();

    /**
     * Saved reference to the configuration
     */
    private CurnConfig config = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (DisableFeedPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public DisableFeedPlugIn()
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
        return "Save As";
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

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
            if (paramName.equals (VAR_DISABLED))
            {
                boolean flag = config.getRequiredBooleanValue (sectionName,
                                                               paramName);
                perFeedDisabledFlagMap.put (feedInfo, flag);
                log.debug ("[" + sectionName + "]: "
                         + paramName
                         + "="
                         + flag);
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
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
        Boolean disabled = perFeedDisabledFlagMap.get (feedInfo);
        boolean processFeed = true;

        if ((disabled != null) && (disabled))
        {
            log.debug ("Feed "
                     + feedInfo.getURL().toString()
                     + " is marked disabled. Skipping it.");
            processFeed = false;
        }

        return processFeed;
    }
}
