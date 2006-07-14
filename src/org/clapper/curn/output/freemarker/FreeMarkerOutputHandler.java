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

package org.clapper.curn.output.freemarker;

import org.clapper.curn.Constants;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.Version;
import org.clapper.curn.output.FileOutputHandler;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;
import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;
import org.clapper.util.text.TextUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import java.util.Collection;
import java.util.Date;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateException;

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
 * to the data structure. When the  {@link #flush} method is invoked,
 * this handler loads the FreeMarker template and feeds it the
 * FreeMarker data model, producing the output. The FreeMarker template
 * can produce any kind of document; this handler doesn't care.</p>
 *
 * <h3>The FreeMarker Data Model</h3>
 *
 * <p>This handler builds the following FreeMarker data model tree.</p>
 *
 * <pre>
 * <b>Tree</b>                                         <b>Description</b>
 *
 * (root)
 *  |
 *  +-- curn
 *  |    |
 *  |    +-- showToolInfo                       (boolean) whether or not
 *  |    |                                      to display curn information
 *  |    |                                      in the output
 *  |    |
 *  |    +-- version                            version of curn
 *  |    |
 *  |    +-- buildID                            curn's build ID
 *  |
 *  +-- totalItems                              total items for all channels
 *  |
 *  +-- dateGenerated                           date generated
 *  |
 *  +-- extraText                               extra text, from the config
 *  |
 *  +-- encoding                                encoding, from the config
 *  |
 *  +-- tableOfContents                         hash of TOC data
 *  |    |
 *  |    +-- needed                             whether a TOC is needed
 *  |    |
 *  |    +-- channels                           sequence of channel TOC
 *  |          |                                items
 *  |          |
 *  |          +-- (channel)                    TOC entry for one channel
 *  |                |
 *  |                +-- title                  channel title
 *  |                |
 *  |                +-- totalItems             total items in channel
 *  |                |
 *  |                +-- channelAnchor          HTML anchor for channel
 *  |
 *  +-- channels                                sequence of channel data
 *         |
 *         +-- (channel)                        hash for a single channel
 *                 |
 *                 +-- index                    channel's index in list
 *                 |
 *                 +-- totalItems               total items in channel
 *                 |
 *                 +-- title                    channel title
 *                 |
 *                 +-- anchorName               HTML anchor for channel
 *                 |
 *                 +-- url                      channel's URL
 *                 |
 *                 +-- date                     channel's last-modified date
 *                 |                            (might be missing)
 *                 |
 *                 +-- items                    sequence of channel items
 *                       |
 *                       +-- (item)             entry for one item
 *                             |
 *                             +-- index        item's index in channel
 *                             |
 *                             +-- title        item's title
 *                             |
 *                             +-- url          item's unique URL
 *                             |
 *                             +-- date         the date
 *                             |                (might be missing)
 *                             |
 *                             +-- author       the author (might be missing)
 *                             |
 *                             +-- description  description/summary
 * </pre>
 *
 * <p>In addition, the data model provides (at the top level) the following
 * methods:</p>
 *
 * <pre>
 * (root)
 *  |
 *  +-- wrapText (string[, indentation[, lineLength]])
 *  |
 *  +-- indentText (string, indentation)
 *  |
 *  +-- stripHTML (string)
 * </pre>
 *
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
     * Configuration variable: table-of-contents item threshold
     */
    public static final String CFG_TOC_ITEM_THRESHOLD = "TOCItemThreshold";

    /**
     * Configuration variable: template file
     */
    public static final String CFG_TEMPLATE_FILE = "TemplateFile";

    /**
     * Configuration keyword for built-in template
     */
    public static final String CFG_TEMPLATE_LOAD_BUILTIN = "builtin";

    /**
     * Configuration keyword: Built-in HTML template
     */
    public static final String CFG_BUILTIN_HTML_TEMPLATE = "html";

    /**
     * Configuration keyword: Built-in text template
     */
    public static final String CFG_BUILTIN_TEXT_TEMPLATE = "text";

    /**
     * Configuration keyword: Built-in summary template
     */
    public static final String CFG_BUILTIN_SUMMARY_TEMPLATE = "summary";

    /**
     * Configuration keyword for template loading from classpath
     */
    public static final String CFG_TEMPLATE_LOAD_FROM_CLASSPATH = "classpath";

    /**
     * Configuration keyword for template loading from URL
     */
    public static final String CFG_TEMPLATE_LOAD_FROM_URL = "url";

    /**
     * Configuration keyword for template loading from file
     */
    public static final String CFG_TEMPLATE_LOAD_FROM_FILE = "file";

    /**
     * Built-in HTML template.
     */
    public final static TemplateLocation BUILTIN_HTML_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/HTML.ftl");

    /**
     * Built-in text template
     */
    public final static TemplateLocation BUILTIN_TEXT_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/Text.ftl");

    /**
     * Built-in summary template
     */
    public final static TemplateLocation BUILTIN_SUMMARY_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/Summary.ftl");

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default number of feeds (channels) that must be present for a
     * table of contents to be rendered.
     */
    private static final int DEFAULT_TOC_THRESHOLD = Integer.MAX_VALUE;

    /**
     * Prefix to use with generated channel anchors.
     */
    private static final String CHANNEL_ANCHOR_PREFIX = "feed";

    /**
     * Default title
     */
    private static final String DEFAULT_TITLE = "RSS Feeds";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private PrintWriter       out                 = null;
    private TemplateLocation  templateLocation    = null;
    private String            mimeType            = "text/html";
    private boolean           allowEmbeddedHTML   = false;
    private int               tocThreshold        = DEFAULT_TOC_THRESHOLD;
    private int               totalChannels       = 0;
    private int               totalItems          = 0;

    private freemarker.template.Configuration freemarkerConfig;
    private SimpleHash                        freemarkerDataModel;
    private SimpleHash                        freemarkerTOCData;
    private SimpleSequence                    freemarkerTOCItems;
    private SimpleSequence                    freemarkerChannelsData;

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
    public void initOutputHandler (final CurnConfig              config,
                                   final ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        this.totalChannels = 0;

        Date now = new Date();

        // Parse handler-specific configuration variables

        String section = cfgHandler.getSectionName();
        String title = DEFAULT_TITLE;
        String extraText = "";

        try
        {
            if (section != null)
            {
                // Parse the TemplateFile parameter. Also gets the MIME type.

                parseTemplateLocation (config, section);

                // Determine whether we should strip HTML tags.

                this.allowEmbeddedHTML =
                    config.getOptionalBooleanValue
                        (section,
                         CurnConfig.CFG_ALLOW_EMBEDDED_HTML,
                         false);

                // Get the title.

                title = config.getOptionalStringValue (section,
                                                       CFG_TITLE,
                                                       title);

                // Get the extra text.

                extraText = config.getOptionalStringValue (section,
                                                           CFG_EXTRA_TEXT,
                                                           extraText);

                // Get the table of contents threshold

                tocThreshold = config.getOptionalIntegerValue
                                               (section,
                                                CFG_TOC_ITEM_THRESHOLD,
                                                DEFAULT_TOC_THRESHOLD);
            }
        }

        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        // Create the FreeMarker configuration.

        freemarkerConfig = new freemarker.template.Configuration();
        freemarkerConfig.setObjectWrapper (new DefaultObjectWrapper());
        freemarkerConfig.setTemplateLoader (new CurnTemplateLoader());
        freemarkerConfig.setLocalizedLookup (false);

        // Create the FreeMarker data model and populate it with the
        // values that aren't channel-dependent.

        freemarkerDataModel = new SimpleHash();
        freemarkerDataModel.put ("dateGenerated",
                                 new SimpleDate (now, SimpleDate.DATETIME));
        freemarkerDataModel.put ("title", title);
        freemarkerDataModel.put ("extraText", extraText);

        String encoding = super.getOutputEncoding();
        if (encoding == null)
            encoding = FileUtil.getDefaultEncoding();

        freemarkerDataModel.put ("encoding", encoding);

        SimpleHash map = new SimpleHash();

        freemarkerDataModel.put ("configFile", map);
        URL configFileURL = config.getConfigurationFileURL();
        if (configFileURL == null)
            map.put ("url", "?");
        else
            map.put ("url", configFileURL.toString());

        map = new SimpleHash();
        freemarkerDataModel.put ("curn", map);
        map.put ("version", Version.getVersionNumber());
        map.put ("buildID", Version.getBuildID());
        if (super.displayToolInfo())
            map.put ("showToolInfo", true);
        else
            map.put ("showToolInfo", false);

        this.freemarkerTOCData = new SimpleHash();
        freemarkerDataModel.put ("tableOfContents", this.freemarkerTOCData);
        freemarkerTOCItems = new SimpleSequence();
        this.freemarkerTOCData.put ("channels", freemarkerTOCItems);

        freemarkerChannelsData = new SimpleSequence();
        freemarkerDataModel.put ("channels", freemarkerChannelsData);


        // Methods accessible from the template

        freemarkerDataModel.put ("wrapText", new WrapTextMethod());
        freemarkerDataModel.put ("indentText", new IndentTextMethod());
        freemarkerDataModel.put ("stripHTML", new StripHTMLMethod());

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
    public void displayChannel (final RSSChannel channel,
                                final FeedInfo   feedInfo)
        throws CurnException
    {
        // Both the feed AND the handler must enable HTML for it not to
        // be stripped.

        if (! allowEmbeddedHTML)
            channel.stripHTML();

        // Add the channel information to the data model.

        Collection<RSSItem> items = channel.getItems();
        int totalItemsInChannel = items.size();

        if (totalItemsInChannel == 0)
            return;

        this.totalItems += totalItemsInChannel;
        totalChannels++;

        String channelAnchorName = CHANNEL_ANCHOR_PREFIX
                                 + String.valueOf (totalChannels);
        String channelTitle = channel.getTitle();
        RSSLink link;

        // Store the channel data.

        SimpleHash channelData = new SimpleHash();
        freemarkerChannelsData.add (channelData);
        channelData.put ("index", new SimpleNumber (totalChannels));
        channelData.put ("totalItems", new SimpleNumber (totalItemsInChannel));
        channelData.put ("anchorName", channelAnchorName);
        channelData.put ("title", channelTitle);

        URL channelURL;
        link = channel.getLinkWithFallback ("text/html");
        if (link == null)
            channelURL = feedInfo.getURL();
        else
            channelURL = link.getURL();
        channelData.put ("url", channelURL.toString());

        Date channelDate = null;
        channelData.put ("showDate", true);

        if (channelDate != null)
        {
            channelData.put ("date", new SimpleDate (channelDate,
                                                     SimpleDate.DATETIME));
        }

        // Store a table of contents entry for the channel.

        SimpleHash tocData = new SimpleHash();
        tocData.put ("title", channelTitle);
        tocData.put ("totalItems", new SimpleNumber (totalItemsInChannel));
        tocData.put ("channelAnchor", channelAnchorName);
        freemarkerTOCItems.add (tocData);

        // Create a collection for the channel items.

        SimpleSequence itemsData = new SimpleSequence();
        channelData.put ("items", itemsData);

        // Now, put in the data for each item in the channel.

        String[] desiredItemDescTypes;

        int i = 0;
        for (RSSItem item : items)
        {
            SimpleHash itemData = new SimpleHash();
            itemsData.add (itemData);

            i++;
            itemData.put ("index", new SimpleNumber (i));
            itemData.put ("showDate", true);
            Date itemDate = item.getPublicationDate();
            if (itemDate != null)
            {
                itemData.put ("date", new SimpleDate (itemDate,
                                                      SimpleDate.DATETIME));
            }

            link = item.getLinkWithFallback ("text/html");
            assert (link != null);
            URL itemURL = link.getURL();
            itemData.put ("url", itemURL.toString());

            itemData.put ("showAuthor", true);
            String authorString = null;
            Collection<String> authors = item.getAuthors();
            if ((authors != null) && (authors.size() > 0))
            {
                authorString = TextUtil.join (authors, ", ");
                itemData.put ("author", authorString);
            }

            String itemTitle = item.getTitle();
            if (itemTitle == null)
                itemTitle = "(No Title)";
            itemData.put ("title", itemTitle);

            String desc = item.getSummary();

            if (desc == null)
                desc = "";

            itemData.put ("description", desc);
        }
    }

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        log.debug ("Generating output.");

        String templateName = templateLocation.getName();
        Template template;

        freemarkerDataModel.put ("totalItems", new SimpleNumber (totalItems));

        if (totalItems >= tocThreshold)
            freemarkerTOCData.put ("needed", TemplateBooleanModel.TRUE);
        else
            freemarkerTOCData.put ("needed", TemplateBooleanModel.FALSE);

        try
        {
            // Create the FreeMarker template.

            template = freemarkerConfig.getTemplate (templateName);
        }

        catch (IOException ex)
        {
            log.error ("Error creating FreeMarker template", ex);
            throw new CurnException
                         (Constants.BUNDLE_NAME,
                          "FreeMarkerOutputHandler.cantGetFreeMarkerTemplate",
                          "Cannot create FreeMarker template",
                          ex);
        }

        try
        {
            template.process (freemarkerDataModel, out);
        }

        catch (TemplateException ex)
        {
            log.error ("Error processing FreeMarker template", ex);
            throw new CurnException
                          (Constants.BUNDLE_NAME,
                           "FreeMarkerOutputHandler.cantProcessTemplate",
                           "Error while processing FreeMarker template " +
                           "\"{0}\"",
                           new Object[] {templateLocation.getLocation()});
        }

        catch (IOException ex)
        {
            throw new CurnException
                          (Constants.BUNDLE_NAME,
                           "FreeMarkerOutputHandler.cantProcessTemplate",
                           "Error while processing FreeMarker template " +
                           "\"{0}\"",
                           new Object[] {templateLocation.getLocation()});
        }

        out.flush();
        out.close();
        out = null;

        // Kill the FreeMarker config and FreeMarker data model

        freemarkerDataModel = null;
        freemarkerConfig    = null;
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return this.mimeType;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse and validate the template file configuration parameter. Sets
     * the templateFile instance variable.
     *
     * @param config     the parsed <i>curn</i> configuration data
     * @param section    the name of the section
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           any other error
     */
    private void parseTemplateLocation (final CurnConfig config,
                                        final String     section)
        throws ConfigurationException,
               CurnException
    {
        // Get the template file configuration as explicit tokens from the
        // config parser. Saves parsing them here, plus the config file has
        // mechanisms for quoting white space within a token.

        String[] templateTokens =
            config.getConfigurationTokens (section, CFG_TEMPLATE_FILE);

        if (templateTokens == null)
        {
            templateTokens = new String[]
                             {
                                 CFG_TEMPLATE_LOAD_BUILTIN,
                                 CFG_BUILTIN_HTML_TEMPLATE
                             };
        }

        else
        {
            // The configuration parser only breaks the line into tokens if
            // there are quoted fields. So, it's possible for there to be
            // one token (no quoted fields), two tokens (a single quoted
            // field) or many tokens.

            if (templateTokens.length == 1)
            {
                // Split it on white space.

                templateTokens = templateTokens[0].split (" ");
            }

            if (templateTokens.length != 2)
            {
                throw new ConfigurationException
                    (section,
                     "\"TemplateFile\" value \"" +
                     config.getConfigurationValue (section,
                                                   CFG_TEMPLATE_FILE) +
                     "\" (\"" +
                     config.getRawValue (section, CFG_TEMPLATE_FILE) +
                     "\") must have two fields.");
            }
        }

        String templateType = templateTokens[0].trim();

        if (templateType.equalsIgnoreCase (CFG_TEMPLATE_LOAD_BUILTIN))
        {
            if (templateTokens[1].equals (CFG_BUILTIN_HTML_TEMPLATE))
            {
                this.templateLocation = BUILTIN_HTML_TEMPLATE;
                this.mimeType = "text/html";
            }

            else if (templateTokens[1].equals (CFG_BUILTIN_TEXT_TEMPLATE))
            {
                this.templateLocation = BUILTIN_TEXT_TEMPLATE;
                this.mimeType = "text/plain";
            }

            else if (templateTokens[1].equals (CFG_BUILTIN_SUMMARY_TEMPLATE))
            {
                this.templateLocation = BUILTIN_SUMMARY_TEMPLATE;
                this.mimeType = "text/plain";
            }

            else
            {
                throw new ConfigurationException (section,
                                                  "Unknown built-in " +
                                                  "template file \"" +
                                                  templateTokens[1] + "\"");
            }
        }

        else if (templateType.equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_URL))
        {
            this.templateLocation = new TemplateLocation (TemplateType.URL,
                                                          templateTokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (templateType.equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_FILE))
        {
            this.templateLocation = new TemplateLocation (TemplateType.FILE,
                                                          templateTokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (templateType.equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_CLASSPATH))
        {
            this.templateLocation = new TemplateLocation (TemplateType.CLASSPATH,
                                                          templateTokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else
        {
            throw new ConfigurationException
                (section,
                 "\"TemplateFile\" value \"" +
                 config.getRawValue (section, CFG_TEMPLATE_FILE) +
                 "\" has unknown type \"" + templateType + "\".");
        }
    }
}
