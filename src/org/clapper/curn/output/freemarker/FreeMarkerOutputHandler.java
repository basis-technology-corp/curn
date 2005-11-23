/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.output.freemarker;

import org.clapper.curn.ConfigFile;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.Curn;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
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
 *     <td><tt>Encoding</tt></td>
 *     <td>Encoding to use for the generated document</td>
 *     <td>UTF-8</td>
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
 *                 +-- showDate                 whether or not to show date
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
 *                             +-- showDate     whether to show date
 *                             |
 *                             +-- date         the date
 *                             |                (might be missing)
 *                             |
 *                             +-- showAuthor   whether to show author
 *                             |
 *                             +-- author       the author, or ""
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
     * Configuration variable: allow embedded HTML
     */
    public static final String CFG_ALLOW_EMBEDDED_HTML = "AllowEmbeddedHTML";

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
     * Configuration variable: encoding
     */
    public static final String CFG_ENCODING = "Encoding";

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
     * Default encoding value
     */
    private static final String DEFAULT_CHARSET_ENCODING = "utf-8";

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
    private ConfigFile        config              = null;
    private TemplateLocation  templateLocation    = null;
    private String            mimeType            = "text/html";
    private boolean           allowEmbeddedHTML   = false;
    private int               tocThreshold        = DEFAULT_TOC_THRESHOLD;
    private int               totalChannels       = 0;
    private int               totalItems          = 0;
    private Date              now                 = null;
    private String            handlerName         = null;

    private freemarker.template.Configuration freemarkerConfig;
    private Map<String,Object>                freemarkerDataModel;
    private Map<String,Object>                freemarkerTOCData;
    private Collection<Object>                freemarkerTOCItems;
    private Collection<Object>                freemarkerChannelsData;

    /**
     * For logging
     */
    private static Logger log = new Logger (FreeMarkerOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FreeMarkerOutputHandler</tt>.
     */
    public FreeMarkerOutputHandler()
    {
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
    public void initOutputHandler (ConfigFile              config,
                                   ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        this.config = config;
        this.totalChannels = 0;
        this.now = new Date();

        // Parse handler-specific configuration variables

        String section = cfgHandler.getSectionName();
        String encoding = null;
        String title = DEFAULT_TITLE;
        String extraText = "";

        this.handlerName = section;

        try
        {
            if (section != null)
            {
                // Parse the TemplateFile parameter. Also gets the MIME type.

                parseTemplateLocation (config, section);

                // Determine whether we should strip HTML tags.

                this.allowEmbeddedHTML = config.getOptionalBooleanValue
                                                     (section,
                                                      CFG_ALLOW_EMBEDDED_HTML,
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

                encoding = config.getOptionalStringValue
                                               (section,
                                                CFG_ENCODING,
                                                DEFAULT_CHARSET_ENCODING);
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

        freemarkerDataModel = new HashMap<String,Object>();
        freemarkerDataModel.put ("dateGenerated",
                                 new SimpleDate (now, SimpleDate.DATETIME));
        freemarkerDataModel.put ("title", title);
        freemarkerDataModel.put ("extraText", extraText);
        freemarkerDataModel.put ("encoding", encoding);

        Map<String,Object> map = new HashMap<String,Object>();

        freemarkerDataModel.put ("configFile", map);
        URL configFileURL = config.getConfigurationFileURL();
        if (configFileURL == null)
            map.put ("url", "?");
        else
            map.put ("url", configFileURL.toString());

        map = new HashMap<String,Object>();
        freemarkerDataModel.put ("curn", map);
        map.put ("version", Version.getVersionNumber());
        if (super.displayToolInfo())
            map.put ("showToolInfo", TemplateBooleanModel.TRUE);
        else
            map.put ("showToolInfo", TemplateBooleanModel.FALSE);

        this.freemarkerTOCData = new HashMap<String,Object>();
        freemarkerDataModel.put ("tableOfContents", this.freemarkerTOCData);
        freemarkerTOCItems = new ArrayList<Object>();
        this.freemarkerTOCData.put ("channels", freemarkerTOCItems);

        freemarkerChannelsData = new ArrayList<Object>();
        freemarkerDataModel.put ("channels", freemarkerChannelsData);


        // Methods accessible from the template

        freemarkerDataModel.put ("wrapText", new WrapTextMethod());
        freemarkerDataModel.put ("indentText", new IndentTextMethod());
        freemarkerDataModel.put ("stripHTML", new StripHTMLMethod());

        // Open the output file.

        File outputFile = super.getOutputFile();

        try
        {
            log.debug ("Opening output file \"" + outputFile + "\"");
            this.out = new PrintWriter
                          (new OutputStreamWriter
                               (new FileOutputStream (outputFile), encoding));
        }

        catch (IOException ex)
        {
            throw new CurnException (Curn.BUNDLE_NAME,
                                     "OutputHandler.cantOpenFile",
                                     "Cannot open file \"{0}\" for output",
                                     new Object[] {outputFile.getPath()},
                                     ex);
        }
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. This handler simply buffers up
     * the channel, so that {@link #flush} can pass all the channels to the
     * script.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel channel, FeedInfo feedInfo)
        throws CurnException
    {
        // Both the feed AND the handler must enable HTML for it not to
        // be stripped.

        boolean permitEmbeddedHTML = feedInfo.allowEmbeddedHTML() &&
                                     this.allowEmbeddedHTML;
        if (! permitEmbeddedHTML)
            channel = convertChannelText (channel);

        // Add the channel information to the data model.

        Collection<RSSItem> items = channel.getSortedItems();
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

        Map<String,Object> channelData = new HashMap<String,Object>();
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
        TemplateBooleanModel showDate;
        if (config.showDates())
        {
            showDate = TemplateBooleanModel.TRUE;
            channelData.put ("showDate", showDate);
            channelDate = channel.getPublicationDate();
        }

        else
        {
            showDate = TemplateBooleanModel.FALSE;
            channelData.put ("showDate", showDate);
        }            

        if (channelDate != null)
        {
            channelData.put ("date", new SimpleDate (channelDate,
                                                     SimpleDate.DATETIME));
        }

        // Store a table of contents entry for the channel.

        Map<String,Object> tocData = new HashMap<String,Object>();
        tocData.put ("title", channelTitle);
        tocData.put ("totalItems", new SimpleNumber (totalItemsInChannel));
        tocData.put ("channelAnchor", channelAnchorName);
        freemarkerTOCItems.add (tocData);

        // Create a collection for the channel items.

        Collection<Object> itemsData = new ArrayList<Object>();
        channelData.put ("items", itemsData);

        // Now, put in the data for each item in the channel.

        TemplateBooleanModel showAuthor;
        if (feedInfo.showAuthors())
            showAuthor = TemplateBooleanModel.TRUE;
        else
            showAuthor = TemplateBooleanModel.FALSE;

        String[] desiredItemDescTypes;

        if (permitEmbeddedHTML)
            desiredItemDescTypes = new String[] {"text/html", "text/plain"};
        else
            desiredItemDescTypes = new String[] {"text/plain", "text/html"};

        int i = 0;
        for (RSSItem item : items)
        {
            Map<String,Object> itemData = new HashMap<String,Object>();
            itemsData.add (itemData);

            i++;
            itemData.put ("index", new SimpleNumber (i));
            itemData.put ("showDate", showDate);
            Date itemDate = null;
            if (config.showDates())
                itemDate = item.getPublicationDate();

            if (itemDate != null)
            {
                itemData.put ("date", new SimpleDate (itemDate,
                                                      SimpleDate.DATETIME));
            }

            link = item.getLinkWithFallback ("text/html");
            assert (link != null);
            URL itemURL = link.getURL();
            itemData.put ("url", itemURL.toString());

            itemData.put ("showAuthor", showAuthor);
            String authorString = null;
            if (feedInfo.showAuthors())
            {
                Collection<String> authors = item.getAuthors();
                if ((authors != null) && (authors.size() > 0))
                    authorString = TextUtil.join (authors, ", ");
            }

            if (authorString == null)
                itemData.put ("author", "");
            else
                itemData.put ("author", authorString);

            String itemTitle = item.getTitle();
            if (itemTitle == null)
                itemTitle = "(No Title)";
            itemData.put ("title", itemTitle);

            String desc =  item.getSummaryToDisplay (feedInfo,
                                                     desiredItemDescTypes,
                                                     (! permitEmbeddedHTML));

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
                         (Curn.BUNDLE_NAME,
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
                          (Curn.BUNDLE_NAME,
                           "FreeMarkerOutputHandler.cantProcessTemplate",
                           "Error while processing FreeMarker template "
                         + "\"{0}\"",
                           new Object[] {templateLocation.getLocation()});
        }

        catch (IOException ex)
        {
            throw new CurnException
                          (Curn.BUNDLE_NAME,
                           "FreeMarkerOutputHandler.cantProcessTemplate",
                           "Error while processing FreeMarker template "
                         + "\"{0}\"",
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
    private void parseTemplateLocation (ConfigFile config, String section)
        throws ConfigurationException,
               CurnException
    {
        String templateFileString =
            config.getConfigurationValue (section, CFG_TEMPLATE_FILE);
        String[] tokens;

        if ( (templateFileString == null) ||
             (templateFileString.trim().length() == 0) )
        {
            tokens = new String[]
                         {
                             CFG_TEMPLATE_LOAD_BUILTIN,
                             CFG_BUILTIN_HTML_TEMPLATE
                         };
        }

        else
        {
            tokens = TextUtil.split (templateFileString);
        }

        if (tokens.length != 2)
        {
            throw new ConfigurationException (section,
                                              "\"TemplateFile\" value \""
                                            + templateFileString
                                            + "\" must have two fields.");
        }

        if (tokens[0].equalsIgnoreCase (CFG_TEMPLATE_LOAD_BUILTIN))
        {
            if (tokens[1].equals (CFG_BUILTIN_HTML_TEMPLATE))
            {
                this.templateLocation = BUILTIN_HTML_TEMPLATE;
                this.mimeType = "text/html";
            }

            else if (tokens[1].equals (CFG_BUILTIN_TEXT_TEMPLATE))
            {
                this.templateLocation = BUILTIN_TEXT_TEMPLATE;
                this.mimeType = "text/plain";
            }

            else if (tokens[1].equals (CFG_BUILTIN_SUMMARY_TEMPLATE))
            {
                this.templateLocation = BUILTIN_SUMMARY_TEMPLATE;
                this.mimeType = "text/plain";
            }

            else
            {
                throw new ConfigurationException (section,
                                                  "Unknown built-in template "
                                                + "file \""
                                                + tokens[1]
                                                + "\"");
            }
        }

        else if (tokens[0].equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_URL))
        {
            this.templateLocation = new TemplateLocation (TemplateType.URL,
                                                          tokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (tokens[0].equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_FILE))
        {
            this.templateLocation = new TemplateLocation (TemplateType.FILE,
                                                          tokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (tokens[0].equalsIgnoreCase (CFG_TEMPLATE_LOAD_FROM_CLASSPATH))
        {
            this.templateLocation = new TemplateLocation (TemplateType.CLASSPATH,
                                                          tokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else
        {
            throw new ConfigurationException (section,
                                              "\"TemplateFile\" value \""
                                            + templateFileString
                                            + "\" has unknown type \""
                                            + tokens[0]
                                            + "\".");
        }
    }
}
