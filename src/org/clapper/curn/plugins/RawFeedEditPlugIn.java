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
import org.clapper.curn.PostFeedDownloadPlugIn;
import org.clapper.curn.Util;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;

import org.clapper.util.regex.RegexUtil;
import org.clapper.util.regex.RegexException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <tt>RawFeedEditPlugIn</tt> edits the raw downloaded XML before it's
 * parsed. It can be used to fix known errors in the XML. It intercepts the
 * following per-feed configuration parameters:
 *
 * <table>
 *   <tr valign="top">
 *     <td><tt>PreparseEdit<i>suffix</i></tt></td>
 *     <td>Specifies a regular expression to be applied to the XML. Multiple
 *         expressions may be specified per feed. See the User's Guide for
 *         details.
 *     </td>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class RawFeedEditPlugIn
    implements FeedConfigItemPlugIn,
               PostFeedDownloadPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    public static final String VAR_PREPARSE_EDIT     = "PreparseEdit";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed save info
     */
    class FeedEditInfo
    {
        List<String> regexps = new ArrayList<String>();

        FeedEditInfo()
        {
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,FeedEditInfo> perFeedSaveAsMap =
        new HashMap<FeedInfo,FeedEditInfo>();

    /**
     * Saved reference to the configuration
     */
    private CurnConfig config = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (RawFeedEditPlugIn.class);

    /**
     * Regular expression helper
     */
    private RegexUtil regexUtil = new RegexUtil();

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public RawFeedEditPlugIn()
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
        return "Raw Feed Edit";
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
            if (paramName.startsWith (VAR_PREPARSE_EDIT))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo (feedInfo);
                String value = config.getConfigurationValue (sectionName,
                                                             paramName);
                editInfo.regexps.add (value);
                log.debug ("[" + sectionName + "]: added regexp " + value);
            }
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
        FeedEditInfo editInfo  = perFeedSaveAsMap.get (feedInfo);

        if ((editInfo != null) && (editInfo.regexps.size() > 0))
            doEdit (feedInfo, feedDataFile, encoding);

        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private FeedEditInfo getOrMakeFeedEditInfo (FeedInfo feedInfo)
    {
        FeedEditInfo editInfo = perFeedSaveAsMap.get (feedInfo);
        if (editInfo == null)
        {
            editInfo = new FeedEditInfo();
            perFeedSaveAsMap.put (feedInfo, editInfo);
        }

        return editInfo;
    }

    private void doEdit (FeedInfo feedInfo, File feedDataFile, String encoding)
        throws CurnException
    {
        String         feedURL = feedInfo.getURL().toString();
        BufferedReader in = null;
        PrintWriter    out = null;

        try
        {
            File tempOutputFile = Util.createTempXMLFile();

            if (encoding != null)
            {
                in = new BufferedReader
                         (new InputStreamReader
                             (new FileInputStream (feedDataFile), encoding));
                out = new PrintWriter
                          (new OutputStreamWriter
                              (new FileOutputStream (tempOutputFile),
                               encoding));
            }

            else
            {
                in  = new BufferedReader (new FileReader (feedDataFile));
                out = new PrintWriter (new FileWriter (tempOutputFile));
            }

            String       line;
            int          lineNumber = 0;
            FeedEditInfo editInfo = perFeedSaveAsMap.get (feedInfo);

            while ((line = in.readLine()) != null)
            {
                lineNumber++;
                for (String editCommand : editInfo.regexps)
                {
                    if (log.isDebugEnabled() && (lineNumber == 1))
                    {
                        log.debug ("Applying edit command \""
                                 + editCommand
                                 + "\" to downloaded XML for feed \""
                                 + feedURL
                                 + ", line "
                                 + lineNumber);
                    }

                    line = regexUtil.substitute (editCommand, line);
                }

                out.println (line);
            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            log.debug ("Copying temporary (edited) file \""
                     + tempOutputFile.getPath()
                     + "\" back over top of file \""
                     + feedDataFile.getPath()
                     + "\".");
            FileUtil.copyTextFile (tempOutputFile,
                                   encoding,
                                   feedDataFile,
                                   encoding);

            tempOutputFile.delete();
        }

        catch (IOException ex)
        {
            throw new CurnException (ex);
        }

        catch (RegexException ex)
        {
            throw new CurnException (ex);
        }

        finally
        {
            try
            {
                if (in != null)
                    in.close();

                if(out != null)
                    out.close();
            }

            catch (IOException ex)
            {
                log.error ("I/O error", ex);
            }
        }
    }
}
