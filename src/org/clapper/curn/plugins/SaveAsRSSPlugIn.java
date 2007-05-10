/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

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

import org.clapper.curn.Constants;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PostConfigPlugIn;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Map;
import java.util.HashMap;
import org.clapper.curn.CurnUtil;
import org.clapper.curn.PostFeedParsePlugIn;
import org.clapper.curn.output.freemarker.FreeMarkerFeedTransformer;
import org.clapper.curn.output.freemarker.TemplateLocation;
import org.clapper.curn.output.freemarker.TemplateType;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.util.text.TextUtil;

/**
 * <p>The <tt>SaveAsRSSPlugIn</tt> acts sort of like a single-feed output
 * handler: It takes a feed that's been parsed, converts the parsed data to RSS
 * or Atom format, and writes it to a file. It differs from an output handler in
 * that an output handler must handle multiple feeds, whereas this plug-in
 * handles a single feed at a time.</p>
 *
 * <p>This plug-in intercepts the following per-feed configuration
 * parameters:</p>
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>SaveAsRSS <i>type path [encoding]</i></tt></td>
 *     <td>
 *       <ul>
 *         <li><i>type</i> is the type of RSS output to generate. Currently,
 *             the legal values are: "rss1", "rss2", "atom"
 *         <li><i>path</i> is the path to the RSS file to be written
 *         <li><i>encoding</i> is encoding to use for the file. It defaults
 *             to "utf-8"
 *       </ul>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td><tt>SaveRSSOnly</tt></td>
 *     <td>If set to "true", this parameter indicates that the RSS file should
 *         generated, but that all further processing on the feed should be
 *         skip. In particular, the feed won't be passed to any other plug-ins,
 *         and it won't be passed to any output handlers. This parameter cannot
 *         be specified unless <tt>SaveAsRSS</tt> is also specified.</td>
 *   </tr>
 * </table>
 *
 * <p>Note: If this plug-in is used in conjunction with the
 * {@link RawFeedSaveAsPlugIn} class, and the {@link RawFeedSaveAsPlugIn}
 * class's <tt>SaveOnly</tt> parameter is specified, this plug-in will
 * <i>not</i> be invoked.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class SaveAsRSSPlugIn
    implements FeedConfigItemPlugIn,
               PostConfigPlugIn,
               PostFeedParsePlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_SAVE_AS_RSS   = "SaveAsRSS";
    private static final String VAR_SAVE_RSS_ONLY = "SaveRSSOnly";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed save info
     */
    class FeedSaveInfo
    {
        String  sectionName;
        File    saveAsFile;
        boolean saveOnly;
        String  saveAsEncoding = "utf-8";
        TemplateLocation templateLocation = null;

        FeedSaveInfo()
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
    private Map<FeedInfo,FeedSaveInfo> perFeedSaveAsMap =
        new HashMap<FeedInfo,FeedSaveInfo>();

    /**
     * Saved reference to the configuration
     */
    private CurnConfig config = null;

    /**
     * For log messages
     */
    private static final Logger log = new Logger(SaveAsRSSPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public SaveAsRSSPlugIn()
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
        return "Save As RSS";
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
    public boolean runFeedConfigItemPlugIn(String     sectionName,
                                           String     paramName,
                                           CurnConfig config,
                                           FeedInfo   feedInfo)
        throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_SAVE_AS_RSS))
            {
                FeedSaveInfo saveInfo = getOrMakeFeedSaveInfo(feedInfo);
                String value = config.getConfigurationValue(sectionName,
                                                            paramName);

                // Split the value into the 2 or 3 supported tokens.

                String[] tokens = TextUtil.split(value);
                if ((tokens.length != 2) && (tokens.length != 3))
                {
                    throw new CurnException("Section \"" + sectionName +
                                            "\": Parameter \"" + paramName +
                                            "\" must have two or three values.");
                }

                // First token is the RSS type.

                String templatePath = null;
                if (tokens[0].equalsIgnoreCase("rss1"))
                    templatePath = "org/clapper/curn/output/freemarker/RSS1.ftl";
                else if (tokens[0].equalsIgnoreCase("rss2"))
                    templatePath = "org/clapper/curn/output/freemarker/RSS2.ftl";
                else if (tokens[0].equalsIgnoreCase("atom"))
                    templatePath = "org/clapper/curn/output/freemarker/Atom.ftl";
                else
                {
                    throw new CurnException("Section \"" + sectionName +
                                            "\": Parameter \"" + paramName +
                                            "\" has unknown RSS type \"" +
                                            tokens[0] + "\"");
                }

                saveInfo.templateLocation =
                    new TemplateLocation(TemplateType.CLASSPATH, templatePath);
                saveInfo.saveAsFile = CurnUtil.mapConfiguredPathName(tokens[1]);

                // Third token is the encoding, and is optional.

                if (tokens.length == 3)
                    saveInfo.saveAsEncoding = tokens[2];

                saveInfo.sectionName = sectionName;
                log.debug ("[" + sectionName + "]: SaveAsRSS=" + value);
            }

            else if (paramName.equals (VAR_SAVE_RSS_ONLY))
            {
                FeedSaveInfo saveInfo = getOrMakeFeedSaveInfo (feedInfo);
                saveInfo.saveOnly =
                    config.getOptionalBooleanValue (sectionName,
                                                    paramName,
                                                    false);
                saveInfo.sectionName = sectionName;
                log.debug ("[" + sectionName + "]: SaveRSSOnly=" +
                           saveInfo.saveOnly);
            }

            return true;
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
    public void runPostConfigPlugIn(CurnConfig config)
        throws CurnException
    {
        this.config = config;

        for (FeedInfo feedInfo : perFeedSaveAsMap.keySet())
        {
            FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);

            if (saveInfo.saveOnly && (saveInfo.saveAsFile == null))
            {
                throw new CurnException
                    (Constants.BUNDLE_NAME,
                     "CurnConfig.saveOnlyButNoSaveAs",
                     "Configuration section \"{0}\": " +
                     "\"[1}\" may only be specified if \"{2}\" is set.",
                     new Object[]
                     {
                         saveInfo.sectionName,
                         VAR_SAVE_RSS_ONLY,
                         VAR_SAVE_AS_RSS
                     });
            }
        }
    }

    /**
     * <p>Called just after the feed has been parsed, but before it is
     * otherwise processed.
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed
     * @param channel   the parsed feed data
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     * @see RSSChannel
     */
    public boolean runPostFeedParsePlugIn(FeedInfo feedInfo, RSSChannel channel)
        throws CurnException
    {
        boolean keepGoing = true;
        FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);

        if ((saveInfo != null) && (saveInfo.saveAsFile != null))
        {
            // Create a feed transformer and set the invariant stuff.

            FreeMarkerFeedTransformer feedTransformer =
                new FreeMarkerFeedTransformer(config, true);
            feedTransformer.setEncoding(saveInfo.saveAsEncoding);
            feedTransformer.setTemplate(saveInfo.templateLocation, "text/xml");

            // Now, add the channel.

            feedTransformer.addChannel(channel, feedInfo, true);

            // Now, transform the feed.

            try
            {
                log.debug("Generating RSS output file \"" +
                          saveInfo.saveAsFile + "\" (encoding " +
                          saveInfo.saveAsEncoding + ")");

                Writer out =
                    new OutputStreamWriter
                        (new FileOutputStream(saveInfo.saveAsFile),
                         saveInfo.saveAsEncoding);
                feedTransformer.transform(out);
                out.close();
            }

            catch (IOException ex)
            {
                throw new CurnException ("Can't write RSS output to \"" +
                                         saveInfo.saveAsFile + "\": ",
                                         ex);
            }

            keepGoing = ! saveInfo.saveOnly;
        }

        return keepGoing;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private FeedSaveInfo getOrMakeFeedSaveInfo (FeedInfo feedInfo)
    {
        FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);
        if (saveInfo == null)
        {
            saveInfo = new FeedSaveInfo();
            perFeedSaveAsMap.put (feedInfo, saveInfo);
        }

        return saveInfo;
    }
}
