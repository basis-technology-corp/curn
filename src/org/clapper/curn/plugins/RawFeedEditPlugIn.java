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

import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PostFeedDownloadPlugIn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <tt>RawFeedEditPlugIn</tt> edits the raw downloaded XML before it's
 * parsed. It can be used to fix known errors in the XML. It intercepts the
 * following per-feed configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>PreparseEdit<i>suffix</i></tt></td>
 *     <td>Specifies a regular expression substitution to be applied to the
 *     XML. Multiple expressions may be specified per feed. See the User's
 *     Guide for details.
 *     </td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class RawFeedEditPlugIn
    extends AbstractXMLEditPlugIn
    implements FeedConfigItemPlugIn,
               PostFeedDownloadPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_PREPARSE_EDIT     = "PreparseEdit";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed edit info
     */
    class FeedEditInfo
    {
        List<String> editCommands = new ArrayList<String>();

        FeedEditInfo()
        {
            // Nothing to do
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,FeedEditInfo> perFeedEditInfoMap =
        new HashMap<FeedInfo,FeedEditInfo>();

    /**
     * For log messages
     */
    private static final Logger log = new Logger (RawFeedEditPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public RawFeedEditPlugIn()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getName()
    {
        return "Raw Feed Edit";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
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
            if (paramName.startsWith (VAR_PREPARSE_EDIT))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo (feedInfo);
                String value = config.getConfigurationValue (sectionName,
                                                             paramName);
                editInfo.editCommands.add (value);
                log.debug ("[" + sectionName + "]: added regexp " + value);
            }

            return true;
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
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
    public boolean runPostFeedDownloadPlugIn (FeedInfo feedInfo,
                                              File     feedDataFile,
                                              String   encoding)
	throws CurnException
    {
        FeedEditInfo editInfo  = perFeedEditInfoMap.get (feedInfo);

        if ((editInfo != null) && (editInfo.editCommands.size() > 0))
            editXML (feedInfo, feedDataFile, encoding, editInfo.editCommands);

        return true;
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    protected Logger getLogger()
    {
        return log;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private FeedEditInfo getOrMakeFeedEditInfo (FeedInfo feedInfo)
    {
        FeedEditInfo editInfo = perFeedEditInfoMap.get (feedInfo);
        if (editInfo == null)
        {
            editInfo = new FeedEditInfo();
            perFeedEditInfoMap.put (feedInfo, editInfo);
        }

        return editInfo;
    }
}
