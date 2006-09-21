/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

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
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.clapper.curn.FeedMetaDataRegistry;
import org.clapper.util.io.FileUtil;

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

    private static final String VAR_PREPARSE_EDIT      = "PreparseEdit";
    private static final String VAR_SAVE_EDITED_XML_AS = "SaveEditedXMLAs";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed edit info
     */
    class FeedEditInfo
    {
        List<String> editCommands = new ArrayList<String>();
        File saveAs = null;

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
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called; it gives the plug-in the chance to register
     * itself as a <tt>FeedMetaDataClient}</tt>, which allows the plug-in to
     * save and restore its own feed-related metadata from the persistent feed
     * metadata store. A plug-in that isn't interested in saving and restoring
     * data can simply ignore the registry.
     *
     * @param metaDataRegistry  the {@link FeedMetaDataRegistry}
     *
     * @throws CurnException on error
     */
    public void init(FeedMetaDataRegistry metaDataRegistry)
        throws CurnException
    {
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

            else if (paramName.equals(VAR_SAVE_EDITED_XML_AS))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo (feedInfo);
                String value = config.getConfigurationValue (sectionName,
                                                             paramName);
                editInfo.saveAs = new File(value);
                log.debug ("[" + sectionName + "]: will save edited XML " +
                           "as \"" + editInfo.saveAs.getPath() + "\"");
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
        {
            editXML (feedInfo, feedDataFile, encoding, editInfo.editCommands);
            if (editInfo.saveAs != null)
            {
                try
                {
                    FileUtil.copyTextFile(feedDataFile,
                                          encoding,
                                          editInfo.saveAs,
                                          encoding);
                }

                catch (IOException ex)
                {
                    throw new CurnException(ex);
                }
            }
        }

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
