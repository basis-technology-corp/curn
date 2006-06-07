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
import org.clapper.curn.PostFeedParsePlugIn;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The <tt>ShowDatesPlugIn</tt> handles enabling/disabling display of the
 * "date" fields on feeds and feed items. It intercepts the following
 * configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th align="left">Section</th>
 *     <th align="left">Parameter</th>
 *     <th align="left">Meaning</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[curn]</tt></td>
 *     <td><tt>ShowDates</tt></td>
 *     <td>Default (global) value for the show dates capability.
 *         Defaults to false.</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[Feed<i>xxx</i>]</tt></td>
 *     <td><tt>ShowDates</tt></td>
 *     <td>Whether or not to show dates for the feed. If not specified,
 *         the global default is used.</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class ShowDatesPlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PostFeedParsePlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_SHOW_DATES = "ShowDates";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed author flag, by feed
     */
    private Map<FeedInfo,Boolean> perFeedShowDatesFlag =
        new HashMap<FeedInfo,Boolean>();

    /**
     * Global default
     */
    private boolean showDatesDefault = false;

    /**
     * Saved reference to the configuration
     */
    private CurnConfig config = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (ShowDatesPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public ShowDatesPlugIn()
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
        return "Show Dates";
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
            if (paramName.equals (VAR_SHOW_DATES))
            {
                showDatesDefault =
                    config.getRequiredBooleanValue (sectionName, paramName);
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
     * @return <tt>true</tt> to continue processing the feed,
     *         <tt>false</tt> to skip it
     *
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public boolean runFeedConfigItemPlugIn (String     sectionName,
                                            String     paramName,
                                            CurnConfig config,
                                            FeedInfo   feedInfo)
	throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_SHOW_DATES))
            {
                boolean flag = config.getRequiredBooleanValue (sectionName,
                                                               paramName);
                perFeedShowDatesFlag.put (feedInfo, flag);
                log.debug ("[" + sectionName + "]: "
                         + paramName
                         + "="
                         + flag);
            }

            return true;
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
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
    public boolean runPostFeedParsePlugIn (FeedInfo   feedInfo,
                                           RSSChannel channel)
	throws CurnException
    {
        Boolean showBoxed = perFeedShowDatesFlag.get (feedInfo);
        boolean show = showDatesDefault;

        if (showBoxed != null)
            show = showBoxed;

        log.debug ("Post-parse, "
                 + feedInfo.getURL()
                 + ": showDates="
                 + show);

        if (! show)
        {
            log.debug ("Removing date fields from feed \""
                     + feedInfo.getURL().toString()
                     + "\"");

            channel.setPublicationDate (null);
            for (RSSItem item : channel.getItems())
                item.setPublicationDate (null);
        }

        return true;
    }
}
