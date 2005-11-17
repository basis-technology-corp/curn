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
 *   </tr>
 *
 *   <tr>
 *     <td><tt>Script</tt></td>
 *     <td>Path to the script to be invoked. The script will be called
 *         as if from the command line, except that additional objects will
 *         be available via BSF.
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>Language</tt></td>
 *     <td><p>The scripting language, as recognized by BSF. This handler
 *         supports all the scripting language engines that are registered
 *         with the BSF software. Some of the scripting language engines
 *         are actually bundled with BSF. Some are not. Regardless, of
 *         course, the actual the jar files for the scripting
 *         languages themselves must be in the CLASSPATH at runtime, for those
 *         languages to be available.</p>
 * 
 *         <p>If you want to use a BSF scripting language engine that isn't
 *         one of the above, simply extend this class and override the
 *         {@link #registerAdditionalScriptingEngines} method. In that method,
 *         call <tt>BSFManager.registerScriptingEngine()</tt> for each
 *         additional language you want to support. For example, to provide
 *         a handler that supports
 *         {@link <a href="http://www.judoscript.com/">JudoScript</a>},
 *         you might write an output handler that looks like this:</p>
 * <blockquote><pre>
 * import org.clapper.curn.CurnException;
 * import org.clapper.curn.output.script.FreeMarkerOutputHandler;
 * import org.apache.bsf.BSFManager;
 *
 * public class MyOutputHandler extends FreeMarkerOutputHandler
 * {
 *     public JudoFreeMarkerOutputHandler()
 *     {
 *         super();
 *     }
 *
 *     public void registerAdditionalScriptingEngines()
 *         throws CurnException
 *     {
 *         BSFManager.registerScriptingEngine ("judoscript",
 *                                             "com.judoscript.BSFJudoEngine",
 *                                             new String[] {"judo", "jud"});
 *     }
 * }
 * </pre></blockquote>
 *
 *         Then, simply use your class instead of <tt>FreeMarkerOutputHandler</tt>
 *         in your configuration file.
 *     </td>
 *   </tr>
 * </table>
 *
 * <p>This handler's {@link #displayChannel displayChannel()} method does
 * not invoke the script; instead, it buffers up all the channels so that
 * the {@link #flush} method can invoke the script. That way, the overhead
 * of invoking the script only occurs once. Via the BSF engine, this
 * handler makes available an iterator of special objects that wrap both
 * the {@link RSSChannel} and {@link FeedInfo} objects for a given channel.
 * See below for a more complete description.</p>
 *
 * <p>The complete list of objects bound into the BSF beanspace follows.</p>
 *
 * <table border="0">
 *   <tr valign="top">
 *     <th>Bound name</th>
 *     <th>Java type</th>
 *     <th>Explanation</th>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>channels</td>
 *     <td><tt>java.util.Collection</tt></td>
 *     <td>An <tt>Collection</tt> of special internal objects that wrap
 *         both {@link RSSChannel} and {@link FeedInfo} objects. The
 *         wrapper objects provide two methods:</td>
 *
 *         <ul>
 *           <li><tt>getChannel()</tt> gets the <tt>RSSChannel</tt> object
 *           <li><tt>getFeedInfo()</tt> gets the <tt>FeedInfo</tt> object
 *         </ul>
 *    </tr>
 *
 *   <tr valign="top">
 *     <td>outputPath</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>The path to an output file. The script should write its output
 *         to that file. Overwriting the file is fine. If the script generates
 *         no output, then it can ignore the file.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>config</td>
 *     <td><tt>{@link ConfigFile}</tt></td>
 *     <td>The <tt>org.clapper.curn.ConfigFile</tt> object that represents
 *         the parsed configuration data. Useful in conjunction with the
 *         "configSection" object, to parse additional parameters from
 *         the configuration.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>configSection</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>The name of the configuration file section in which the output
 *         handler was defined. Useful if the script wants to access
 *         additional script-specific configuration data.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>mimeType</td>
 *     <td><tt>java.io.PrintWriter</tt></td>
 *     <td>A <tt>PrintWriter</tt> object to which the script should print
 *         the MIME type that corresponds to the generated output.
 *         If the script generates no output, then it can ignore this
 *         object.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>logger</td>
 *     <td>{@link Logger org.clapper.util.logging.Logger}</td>
 *     <td>A <tt>Logger</tt> object, useful for logging messages to
 *         the <i>curn</i> log file.</td>
 *   </tr>
 *
 *   <tr valign="top">
 *     <td>version</td>
 *     <td><tt>java.lang.String</tt></td>
 *     <td>Full <i>curn</i> version string, in case the script wants to
 *         include it in the generated output
 *   </tr>
 * </table>
 *
 * <p>For example, the following Jython script can be used as a template
 * for a Jython output handler.</p>
 *
 * <blockquote>
 * <pre>
 * import sys
 *
 * def __init__ (self):
 *     """
 *     Initialize a new TextOutputHandler object.
 *     """
 *     self.__channels    = bsf.lookupBean ("channels")
 *     self.__outputPath  = bsf.lookupBean ("outputPath")
 *     self.__mimeTypeOut = bsf.lookupBean ("mimeType")
 *     self.__config      = bsf.lookupBean ("config")
 *     self.__sectionName = bsf.lookupBean ("configSection")
 *     self.__logger      = bsf.lookupBean ("logger");
 *     self.__version     = bsf.lookupBean ("version")
 *     self.__message     = None
 *
 * def processChannels (self):
 *     """
 *     Process the channels passed in through the Bean Scripting Framework.
 *     """
 *
 *     out = open (self.__outputPath, "w")
 *     msg = self.__config.getOptionalStringValue (self.__sectionName,
 *                                                 "Message",
 *                                                 None)
 *
 *     totalNew = 0
 *
 *     # First, count the total number of new items
 *
 *     iterator = self.__channels.iterator()
 *     while iterator.hasNext():
 *         channel_wrapper = iterator.next()
 *         channel = channel_wrapper.getChannel()
 *         totalNew = totalNew + channel.getItems().size()
 *
 *     if totalNew > 0:
 *         # If the config file specifies a message for this handler,
 *         # display it.
 *
 *         if msg != None:
 *             out.println (msg)
 *             out.println ()
 *
 *         # Now, process the items
 *
 *         iterator = self.__channels.iterator()
 *         while iterator.hasNext():
 *             channel_wrapper = iterator.next()
 *             channel = channel_wrapper.getChannel()
 *             feed_info = channel_wrapper.getFeedInfo()
 *             self.__process_channel (out, channel, feed_info, indentation)
 *
 *         self.__mimeTypeBuf.print ("text/plain")
 *
 *         # Output a footer
 *
 *         self.__indent (out, indentation)
 *         out.write ("\n")
 *         out.write (self.__version + "\n")
 *         out.close ()
 *
 * def process_channel (channel, feed_info):
 *     item_iterator = channel.getItems().iterator()
 *     while item_iterator.hasNext():
 *         # Do output for item
 *         ...
 *
 * main()
 * </pre>
 * </blockquote>
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
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private final static TemplateLocation BUILTIN_HTML_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/HTML.ftl");

    private final static TemplateLocation BUILTIN_TEXT_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/Text.ftl");

    private final static TemplateLocation BUILTIN_SUMMARY_TEMPLATE =
        new TemplateLocation (TemplateType.CLASSPATH,
                              "org/clapper/curn/output/freemarker/Summary.ftl");
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

    /**
     * The FreeMarker datamodel looks like this:
     *
     * (root)
     *  |
     *  +-- curn
     *  |    |
     *  |    +-- version                            version of curn
     *  |
     *  +-- totalItems                              total items for all
     *  |                                           channels
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
     *                 +-- date                     channel's last-modified
     *                 |                            date
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
     *                             |
     *                             +-- showAuthor   whether to show author
     *                             |
     *                             +-- author       the author
     *                             |
     *                             +-- description  description/summary
     *
     * In addition, the data model provides (at the top level) the following
     * methods:
     *
     * (root)
     *  |
     *  +-- wrapText (string[, indentation[, lineLength]])
     *  |
     *  +-- indentText (string, indentation)
     * 
     */
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
    public final void initOutputHandler (ConfigFile              config,
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
        String title = "";
        String extraText = "";

        try
        {
            if (section != null)
            {
                // Parse the TemplateFile parameter. Also gets the MIME type.

                parseTemplateLocation (config, section);

                // Determine whether we should strip HTML tags.

                this.allowEmbeddedHTML = config.getOptionalBooleanValue
                    (section, "AllowEmbeddedHTML", false);

                
                // Get the title.

                title = config.getOptionalStringValue (section,
                                                       "Title",
                                                       title);

                // Get the extra text.

                extraText = config.getOptionalStringValue (section,
                                                           "ExtraText",
                                                           extraText);

                // Get the table of contents threshold

                tocThreshold = config.getOptionalIntegerValue
                                               (section,
                                                "TOCItemThreshold",
                                                DEFAULT_TOC_THRESHOLD);

                encoding = config.getOptionalStringValue
                                               (section,
                                                "Encoding",
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

        this.freemarkerTOCData = new HashMap<String,Object>();
        freemarkerDataModel.put ("tableOfContents", this.freemarkerTOCData);
        freemarkerTOCItems = new ArrayList<Object>();
        this.freemarkerTOCData.put ("channels", freemarkerTOCItems);

        freemarkerChannelsData = new ArrayList<Object>();
        freemarkerDataModel.put ("channels", freemarkerChannelsData);

        // Methods accessible from the template

        freemarkerDataModel.put ("wrapText", new WrapTextMethod());
        freemarkerDataModel.put ("indentText", new IndentTextMethod());

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
    public final void displayChannel (RSSChannel  channel,
                                      FeedInfo    feedInfo)
        throws CurnException
    {
        // Both the feed AND the handler must enable HTML for it not to
        // be stripped.

        boolean permitEmbeddedHTML = feedInfo.allowEmbeddedHTML() &&
                                     this.allowEmbeddedHTML;
        if (! permitEmbeddedHTML)
            convertChannelText (channel);

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

        Date date = null;
        TemplateBooleanModel showDate;
        if (config.showDates())
        {
            showDate = TemplateBooleanModel.TRUE;
            channelData.put ("showDate", showDate);
            date = channel.getPublicationDate();
        }

        else
        {
            showDate = TemplateBooleanModel.FALSE;
            channelData.put ("showDate", showDate);
        }            

        if (date == null)
            date = now;

        channelData.put ("date", new SimpleDate (date, SimpleDate. DATETIME));

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
            date = null;
            if (config.showDates())
                date = item.getPublicationDate();

            if (date == null)
                date = now;

            itemData.put ("date", new SimpleDate (date, SimpleDate.DATETIME));

            link = item.getLinkWithFallback ("text/html");
            assert (link != null);
            URL itemURL = link.getURL();
            itemData.put ("url", itemURL.toString());

            itemData.put ("showAuthor", showAuthor);
            if (feedInfo.showAuthors())
            {
                Collection<String> authors = item.getAuthors();
                if ((authors != null) && (authors.size() > 0))
                    itemData.put ("author", TextUtil.join (authors, ", "));
            }

            String itemTitle = item.getTitle();
            if (itemTitle == null)
                itemTitle = "(No Title)";
            itemData.put ("title", itemTitle);

            String desc =  item.getSummaryToDisplay (feedInfo,
                                                     desiredItemDescTypes,
                                                     ! permitEmbeddedHTML);

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
    public final void flush() throws CurnException
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
    public final String getContentType()
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
            config.getConfigurationValue (section, "TemplateFile");
        String[] tokens;

        if ( (templateFileString == null) ||
             (templateFileString.trim().length() == 0) )
        {
            tokens = new String[] {"builtin", "html"};
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

        if (tokens[0].equalsIgnoreCase ("builtin"))
        {
            if (tokens[1].equals ("html"))
            {
                this.templateLocation = BUILTIN_HTML_TEMPLATE;
                this.mimeType = "text/html";
            }

            else if (tokens[1].equals ("text"))
            {
                this.templateLocation = BUILTIN_TEXT_TEMPLATE;
                this.mimeType = "text/plain";
            }

            else if (tokens[1].equals ("summary"))
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

        else if (tokens[0].equalsIgnoreCase ("url"))
        {
            this.templateLocation = new TemplateLocation (TemplateType.URL,
                                                          tokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (tokens[0].equalsIgnoreCase ("file"))
        {
            this.templateLocation = new TemplateLocation (TemplateType.FILE,
                                                          tokens[1]);
            this.mimeType = config.getConfigurationValue (section, "MimeType");
        }

        else if (tokens[0].equalsIgnoreCase ("classpath"))
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

    /**
     * Copy the resource a URL specifies to a temporary file.
     *
     * @param url  the URL
     *
     * @return the temporary file
     *
     * @throws CurnException  on error (including I/O error)
     */
    private File copyURLToTempFile (URL url)
        throws CurnException
    {
        try
        {
            String suffix   = FileUtil.getFileNameExtension (url.getPath());
            File tempFile   = File.createTempFile ("curn", suffix);
            InputStream is  = url.openStream();
            OutputStream os = new FileOutputStream (tempFile);

            FileUtil.copyStream (is, os);
            is.close();
            os.close();

            return tempFile;
        }

        catch (IOException ex)
        {
            throw new CurnException (Curn.BUNDLE_NAME,
                                     "FreeMarkerOutputHandler.cantCopyURL",
                                     "Unable to copy URL \"{0}\" to temporary "
                                   + "file",
                                     new Object[] {url.toString()},
                                     ex);
        }
    }

    /**
     * Copy classpath resource to temporary file.
     *
     * @param resource  the resource identifier
     *
     * @return the temporary file
     *
     * @throws CurnException on error
     */
    private File copyClasspathResourceToTempFile (String resource)
        throws CurnException
    {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource (resource);
        if (url == null)
        {
            throw new CurnException (Curn.BUNDLE_NAME,
                                     "FreeMarkerOutputHandler.missingResource",
                                     "Cannot find resource \"{0}\" in any of "
                                   + "the elements of the classpath.",
                                     new Object[] {resource});
        }

        return copyURLToTempFile (url);
    }
}
