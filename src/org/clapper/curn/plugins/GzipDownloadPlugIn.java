/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2009 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2009 Brian M. Clapper."

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

package org.clapper.curn.plugins;

import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PreFeedDownloadPlugIn;
import org.clapper.curn.CurnUtil;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import java.net.URLConnection;

import java.util.Map;
import java.util.HashMap;

/**
 * The <tt>GzipDownloadPlugIn</tt> handles setting the global and
 * per-feed HTTP header that requests gzipped (compressed) feed data
 * (assuming the remote server honors that header). It intercepts the
 * following configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th>Section</th>
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[curn]</tt></td>
 *     <td><tt>GzipDownload</tt></td>
 *     <td>The global default setting, if none is supplied in individual feed
 *         sections. Defaults to true.</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[Feed<i>xxx</i>]</tt></td>
 *     <td><tt>GzipDownload</tt></td>
 *     <td>Whether or not to ask for gzipped data for a particular feed.
 *         Defaults to the global setting if not specified.</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class GzipDownloadPlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PreFeedDownloadPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_OLD_GET_GZIPPED_FEEDS = "GetGzippedFeeds";
    private static final String VAR_GZIP_DOWNLOAD         = "GzipDownload";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,Boolean> perFeedGzipFlag =
        new HashMap<FeedInfo,Boolean>();

    /**
     * Default setting
     */
    private boolean requestGzipDefault = true;

    /**
     * For log messages
     */
    private static final Logger log =
        new Logger (GzipDownloadPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public GzipDownloadPlugIn()
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
    public String getPlugInName()
    {
        return "Gzip Download";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getPlugInSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
    }

    /**
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called.
     *
     * @throws CurnException on error
     */
    public void initPlugIn()
        throws CurnException
    {
    }

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
            if (paramName.equals (VAR_GZIP_DOWNLOAD))
            {
                requestGzipDefault =
                    config.getRequiredBooleanValue (sectionName, paramName);
            }

            else if (paramName.equals (VAR_OLD_GET_GZIPPED_FEEDS))
            {
                String msg =
                    config.getDeprecatedParamMessage (paramName,
                                                      VAR_GZIP_DOWNLOAD);
                CurnUtil.getErrorOut().println (msg);
                log.warn (msg);

                requestGzipDefault =
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
            if (paramName.equals (VAR_GZIP_DOWNLOAD))
            {
                boolean flag = config.getRequiredBooleanValue (sectionName,
                                                               paramName);
                perFeedGzipFlag.put (feedInfo, flag);
                log.debug ("[" + sectionName + "]: " + paramName + "=" +
                           flag);
            }

            else if (paramName.equals (VAR_OLD_GET_GZIPPED_FEEDS))
            {
                String msg =
                    config.getDeprecatedParamMessage (paramName,
                                                      VAR_GZIP_DOWNLOAD);
                CurnUtil.getErrorOut().println (msg);
                log.warn (msg);

                boolean flag = config.getRequiredBooleanValue (sectionName,
                                                               paramName);
                perFeedGzipFlag.put (feedInfo, flag);
                log.debug ("[" + sectionName + "]: " + paramName + "=" +
                           flag);
            }

            return true;
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
        Boolean gzipBoxed = perFeedGzipFlag.get (feedInfo);
        boolean gzip = requestGzipDefault;

        if (gzipBoxed != null)
            gzip = gzipBoxed;

        if (gzip)
        {
            log.debug ("Setting header \"Accept-Encoding\" to \"gzip\" " +
                       "for feed \"" + feedInfo.getURL() + "\"");

            urlConn.setRequestProperty ("Accept-Encoding", "gzip");
        }

        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}
