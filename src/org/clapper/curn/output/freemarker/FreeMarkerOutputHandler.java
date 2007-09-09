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

package org.clapper.curn.output.freemarker;

import org.clapper.curn.CurnConfig;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.output.FileOutputHandler;
import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;
import org.clapper.util.logging.Logger;
import java.io.PrintWriter;

/**
 * Provides an output handler that uses the
 * {@link <a href="http://freemarker.sourceforge.net/">FreeMarker</a>}
 * template engine to generate output. In addition to the configuration
 * parameters supported by the {@link FileOutputHandler} base class, this
 * handler supports the following additional configuration variables, which
 * must be specified in the handler's configuration section.
 *
 * <table border="1" align="center">
 *   <tr>
 *     <th>Parameter</th>
 *     <th>Explanation</th>
 *     <th>Default</th>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>AllowEmbeddedHTML</tt></td>
 *     <td>Whether or not to pass embedded HTML to the FreeMarker template</td>
 *     <td><tt>false</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>MimeType</tt></td>
 *     <td>The MIME type of the document produced by the template.
 *         (See <tt>TemplateFile</tt>, below.) Required for all template
 *         types except "builtin".</td>
 *     <td>None</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>TOCItemThreshold</tt></td>
 *     <td>The total number of items (not feeds, but individual items) that
 *         must be displayed before curn will generate a table of contents
 *         header. A value of 0 means "generate a table of
 *         contents regardless of how many items are displayed." The
 *         FreeMarker template is not obligated to honor this parameter.
 *         (Note, though, that the default FreeMarker HTML template does
 *         honor it.)</td>
 *     <td>infinite (i.e., no table of contents is generated)</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>Title</tt></td>
 *     <td>Document title to pass to the template.</td>
 *     <td>"RSS Feeds"</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>TemplateFile</tt></td>
 *     <td>Specifies the location of the FreeMarker template file.
 *         The location is specified with two white space-delimited
 *         fields:
 *         <ul>
 *          <li>A <i>type</i>, which may be "file", "classpath",
 *              "url" or "builtin"
 *          <li>An identifier string
 *        </ul>
 *
 *       The form of the identifier string depends on the <i>type</i>
 *       value.
 *       <ul>
 *          <li>For the "file" type, the identifier must be the path
 *              to the template file, on the machine where <i>curn</i>
 *              is running.
 *          <li>For the "classpath" type, the identifier must be a relative
 *              path to a template file that can be found by searching the
 *              jar files and directories in the class path.
 *          <li>For the "url" type, the identifier must be a valid URL.
 *          <li>For the "builtin" type, the identifier can be one of three
 *              values: "html", "summary" or "text"
 *       </ul>
 *
 *       Examples:
 *
 *       <blockquote>
 *       <pre>
 * file c:\curn\html.ftl
 * url http://localhost/html.ftl
 * classpath org/clapper/curn/output/freemarker/HTML.ftl
 * builtin html</pre>
 *       </blockquote>
 *
 *       Note:
 *
 *       <ul>
 *         <li><tt>builtin html</tt> is short-hand for
 *             <tt>classpath org/clapper/curn/output/freemarker/HTML.ftl</tt>
 *         <li><tt>builtin text</tt> is short-hand for
 *             <tt>classpath org/clapper/curn/output/freemarker/Text.ftl</tt>
 *         <li><tt>builtin summary</tt> is short-hand for
 *             <tt>classpath org/clapper/curn/output/freemarker/Summary.ftl</tt>
 *       </ul>
 *
 *       But, use the "builtin" form, rather than the "classpath" form,
 *       to refer to the built-in templates; if the locations of the
 *       built-in templates change in the future, your <i>curn</i>
 *       configuration file won't break if you're using the "builtin" forms.
 *     </td>
 *     <td>"RSS Feeds"</td>
 *   </tr>
 * </table>
 *
 * <p>This handler builds a FreeMarker data model; each call to
 * {@link #displayChannel displayChannel()} adds the data for a channel
 * to the data structure. When the {@link #flush} method is invoked,
 * this handler loads the FreeMarker template and feeds it the
 * FreeMarker data model, producing the output. The FreeMarker template
 * can produce any kind of document; this handler doesn't care.</p>
 *
 * <p>The actual processing is done by the {@link FreeMarkerFeedTransformer}
 * class. Please consult the documentation for that class, or the
 * <i>curn User's Guide</i>, for a complete description of the curn FreeMarker
 * data model.</p>
 *
 * @see FreeMarkerFeedTransformer
 * @see org.clapper.curn.OutputHandler
 * @see FileOutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class FreeMarkerOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Configuration variable: extra text
     */
    public static final String CFG_EXTRA_TEXT = "ExtraText";

    /**
     * Configuration variable: title
     */
    public static final String CFG_TITLE = "Title";

    /**
     * Configuration variable: MIME type
     *
     * @deprecated MIME type is now specified as part of TemplateFile
     */
    public static final String CFG_MIME_TYPE = "MimeType";

    /**
     * Configuration variable: table-of-contents item threshold
     */
    public static final String CFG_TOC_ITEM_THRESHOLD = "TOCItemThreshold";

    /**
     * Configuration variable: template file
     */
    public static final String CFG_TEMPLATE_FILE = "TemplateFile";

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default number of feeds (channels) that must be present for a
     * table of contents to be rendered.
     */
    private static final int DEFAULT_TOC_THRESHOLD = Integer.MAX_VALUE;

    /**
     * Default title
     */
    private static final String DEFAULT_TITLE = "RSS Feeds";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private PrintWriter       out                 = null;
    private boolean           allowEmbeddedHTML   = false;
    private int               tocThreshold        = DEFAULT_TOC_THRESHOLD;

    private FreeMarkerFeedTransformer feedTransformer = null;

    /**
     * For logging
     */
    private static final Logger log =
        new Logger (FreeMarkerOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FreeMarkerOutputHandler</tt>.
     */
    public FreeMarkerOutputHandler()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config     the parsed <i>curn</i> configuration data
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public void initOutputHandler(final CurnConfig              config,
                                  final ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        // Parse handler-specific configuration variables

        String section = cfgHandler.getSectionName();
        String title = DEFAULT_TITLE;
        String extraText = "";

        try
        {
            if (section != null)
            {
                // Determine whether we should strip HTML tags.

                this.allowEmbeddedHTML =
                    config.getOptionalBooleanValue
                        (section,
                         CurnConfig.CFG_ALLOW_EMBEDDED_HTML,
                         false);

                // Get the title.

                title = config.getOptionalStringValue(section,
                                                      CFG_TITLE,
                                                      title);

                // Get the extra text.

                extraText = config.getOptionalStringValue(section,
                                                          CFG_EXTRA_TEXT,
                                                          extraText);

                // Get the table of contents threshold

                tocThreshold = config.getOptionalIntegerValue
                                               (section,
                                                CFG_TOC_ITEM_THRESHOLD,
                                                DEFAULT_TOC_THRESHOLD);

                // Warn about the deprecated MIME type parameter

                if (config.getOptionalStringValue(section,
                                                  CFG_MIME_TYPE,
                                                  null) != null)
                {
                    log.error("Configuration section \"" + section +
                              "\": Parameter " + CFG_MIME_TYPE + " is no " +
                              "longer supported.");
                }
            }
        }

        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }


        feedTransformer = new FreeMarkerFeedTransformer(config,
                                                        super.displayToolInfo(),
                                                        tocThreshold);
        feedTransformer.setTemplateFromConfig(section, CFG_TEMPLATE_FILE);
        feedTransformer.setTitle(title);
        feedTransformer.setEncoding(super.getOutputEncoding());
        feedTransformer.setExtraText(extraText);

        // Open the output file.

        this.out = super.openOutputFile();
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. This handler simply buffers up
     * the channel, so that {@link #flush} can pass all the channels to the
     * script.
     *
     * @param channel  The channel containing the items to emit. <i>curn</i>
     *                 will pass a copy of the actual {@link RSSChannel}
     *                 object, so the output handler can edit its contents,
     *                 if necessary, without affecting other output
     *                 handlers.

     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel(final RSSChannel channel,
                               final FeedInfo   feedInfo)
        throws CurnException
    {
        log.debug("displayChannel: channel has " + channel.getItems().size() +
                  " items");
        feedTransformer.addChannel(channel, feedInfo, allowEmbeddedHTML);
    }

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        log.debug ("Generating output.");

        feedTransformer.transform(out);

        out.flush();
        out.close();
        out = null;
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return feedTransformer.getMIMEType();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

}
