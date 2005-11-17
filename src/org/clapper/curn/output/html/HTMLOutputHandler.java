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

package org.clapper.curn.output.html;

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
import org.clapper.util.logging.Logger;
import org.clapper.util.text.HTMLUtil;
import org.clapper.util.text.TextUtil;
import org.clapper.util.text.Unicode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLMetaElement;
import org.w3c.dom.html.HTMLTableRowElement;
import org.w3c.dom.html.HTMLTableCellElement;
import org.w3c.dom.html.HTMLAnchorElement;

import org.enhydra.xml.xmlc.XMLCUtil;
import org.enhydra.xml.xmlc.html.HTMLObject;

/**
 * Provides an output handler that produces HTML output.
 *
 * @see org.clapper.curn.OutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class HTMLOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final DateFormat OUTPUT_DATE_FORMAT =
                             new SimpleDateFormat ("dd MMM, yyyy, HH:mm:ss");

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

    private PrintWriter           out                 = null;
    private RSSOutputHTML         dom                 = null;
    private String                oddItemRowClass     = null;
    private String                oddChannelClass     = null;
    private int                   rowCount            = 0;
    private int                   channelCount        = 0;
    private int                   totalItems          = 0;
    private ConfigFile            config              = null;
    private HTMLTableRowElement   channelRow          = null;
    private HTMLTableRowElement   channelSeparatorRow = null;
    private Node                  channelRowParent    = null;
    private HTMLAnchorElement     itemAnchor          = null;
    private HTMLTableCellElement  itemTitleTD         = null;
    private HTMLTableCellElement  itemDescTD          = null;
    private HTMLTableCellElement  channelTD           = null;
    private String                title               = null;
    private int                   tocThreshold        = DEFAULT_TOC_THRESHOLD;
    private Set<Node>             addedNodes          = new HashSet<Node>();
    private Map<Node,List<Node>>  removedNodes        = new HashMap<Node,List<Node>>();

    /**
     * For logging
     */
    private static Logger log = new Logger (HTMLOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>HTMLOutputHandler</tt>.
     */
    public HTMLOutputHandler()
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
        this.dom                 = new RSSOutputHTML();
        this.config              = config;
        this.oddChannelClass     = dom.getElementChannelTD().getClassName();
        this.oddItemRowClass     = dom.getElementItemTitleTD().getClassName();
        this.channelRow          = dom.getElementChannelRow();
        this.channelRow.removeAttribute ("id");

        this.channelSeparatorRow = dom.getElementChannelSeparatorRow();
        this.channelSeparatorRow.removeAttribute ("id");

        this.channelRowParent    = channelRow.getParentNode();

        this.itemAnchor          = dom.getElementItemAnchor();
        this.itemAnchor.removeAttribute ("id");

        this.itemTitleTD         = dom.getElementItemTitleTD();
        this.itemTitleTD.removeAttribute ("id");

        this.itemDescTD          = dom.getElementItemDescTD();
        this.itemDescTD.removeAttribute ("id");

        this.channelTD           = dom.getElementChannelTD();
        this.channelTD.removeAttribute ("id");

        this.title               = null;

        // Parse handler-specific configuration variables

        String section  = cfgHandler.getSectionName();
        String encoding = null;

        try
        {
            if (section != null)
            {
                title = config.getOptionalStringValue (section, "Title", null);
                encoding = config.getOptionalStringValue
                                               (section,
                                                "HTMLEncoding",
                                                DEFAULT_CHARSET_ENCODING);
                tocThreshold = config.getOptionalIntegerValue
                                               (section,
                                                "TOCItemThreshold",
                                                DEFAULT_TOC_THRESHOLD);
            }
        }
        
        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        // Set the content type in the document

        HTMLElement      headElement;
        HTMLMetaElement  metaElement;

        headElement = getHeadElement (dom);
        metaElement = (HTMLMetaElement) dom.createElement ("META");
        metaElement.setHttpEquiv ("Content-Type");
        metaElement.setContent (this.getContentType()
                              + "; charset="
                              + encoding);
        headElement.insertBefore (metaElement, headElement.getFirstChild());

        // Open the output file.

        File outputFile = super.getOutputFile();
        dom.setTextTimestamp (OUTPUT_DATE_FORMAT.format (new Date()));

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
     * is defined for the underlying class.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws CurnException
    {
        Collection<RSSItem> items = channel.getItems();
        int totalItemsInChannel = items.size();

        if (totalItemsInChannel == 0)
            return;

        this.totalItems += totalItemsInChannel;

        int       i = 0;
        Date      date;
        boolean   allowEmbeddedHTML = feedInfo.allowEmbeddedHTML();
        String    channelTitle = channel.getTitle();

        // NOTE: We generate the table of contents regardless, since we
        // don't know how many items we'll have until we finish processing
        // all channels. At the end of processing, in the flush() method,
        // we'll decide whether or not to remove the DOM subtree containing
        // the table of contents.

        channelCount++;
        removedNodes.clear();
        addedNodes.clear();

        // Generate an anchor name for the channel.

        String channelAnchorName = CHANNEL_ANCHOR_PREFIX
                                 + String.valueOf (channelCount);

        dom.getElementChannelAnchor().setName (channelAnchorName);

        // Generate a table of contents link for this channel.

        HTMLElement tocEntry = dom.getElementTOCEntry();
        tocEntry.removeAttribute ("id");
        Node tocEntryParent = tocEntry.getParentNode();
        HTMLAnchorElement tocFeedLink = dom.getElementTOCFeedLink();
        tocFeedLink.removeAttribute ("id");
        tocFeedLink.setHref ("#" + channelAnchorName);

        dom.getElementTOCEntryText().removeAttribute ("id");
        dom.getElementTOCFeedTotalItems().removeAttribute ("id");

        String totalSuffix = " item";
        if (totalItemsInChannel > 1)
            totalSuffix = totalSuffix + "s";

        dom.setTextTOCFeedTotalItems (String.valueOf (totalItemsInChannel) +
                                      totalSuffix);

        dom.setTextTOCEntryText (channelTitle);

        tocEntryParent.insertBefore (tocEntry.cloneNode (true), tocEntry);

        // Insert separator row first.

        channelRowParent.insertBefore (channelSeparatorRow.cloneNode (true),
                                       channelSeparatorRow);
        // Do the rows of output.

        dom.getElementChannelDate().removeAttribute ("id");
        dom.getElementItemDate().removeAttribute ("id");
        dom.getElementChannelLink().removeAttribute ("id");

        // Note: Assumes the author block is the last child. Not a great
        // assumption, if we want to be completely decoupled from the
        // presentation.

        HTMLElement itemAuthorBlock = dom.getElementItemAuthorBlock();
        Node itemAuthorBlockParent = itemAuthorBlock.getParentNode();
        itemAuthorBlock.removeAttribute ("id");

        i = 0;
        for (RSSItem item : items)
        {
            HTMLAnchorElement channelAnchor = dom.getElementChannelLink();
            channelAnchor.removeAttribute ("id");

            RSSLink link;
            URL itemURL;

            link = item.getLinkWithFallback ("text/html");
            assert (link != null);
            itemURL = link.getURL();

            if (i == 0)
            {
                // First row in channel has channel title and link.

                addTextNode (channelAnchor,
                             channelTitle,
                             allowEmbeddedHTML,
                             addedNodes);

                link = channel.getLinkWithFallback ("text/html");
                URL channelURL;
                if (link == null)
                    channelURL = feedInfo.getURL();
                else
                    channelURL = link.getURL();

                channelAnchor.setHref (channelURL.toExternalForm());

                date = null;
                if (config.showDates())
                    date = channel.getPublicationDate();
                if (date != null)
                    dom.setTextChannelDate (OUTPUT_DATE_FORMAT.format (date));
                else
                    dom.setTextChannelDate ("");

                itemAnchor.setHref (itemURL.toExternalForm());
            }

            else
            {
                addTextNode (channelAnchor, "", false, addedNodes);
                dom.setTextChannelDate ("");
                channelAnchor.setHref ("");
                itemAnchor.setHref ("");
            }

            // Item title

            String itemTitle = item.getTitle();
            if (itemTitle == null)
                itemTitle = "(No Title)";

            addTextNode (itemAnchor, itemTitle, allowEmbeddedHTML, addedNodes);

            // Item summary (i.e., description)

            String desc = item.getSummaryToDisplay (feedInfo,
                                                    new String[]
                                                    {
                                                        "text/plain",
                                                        "text/html"
                                                    },
                                                    false);
            if (desc == null)
                desc = String.valueOf (Unicode.NBSP);

            addTextNode (itemDescTD, desc, allowEmbeddedHTML, addedNodes);

            itemAnchor.setHref (itemURL.toExternalForm());

            // Item date

            date = null;
            if (config.showDates())
                date = item.getPublicationDate();
            if (date != null)
                dom.setTextItemDate (OUTPUT_DATE_FORMAT.format (date));
            else
                dom.setTextItemDate ("");

            itemTitleTD.removeAttribute ("class");
            itemDescTD.removeAttribute ("class");

            // Item author

            String author = null;

            if (feedInfo.showAuthors())
            {
                Collection<String> authors = item.getAuthors();
                if ((authors != null) && (authors.size() > 0))
                    author = TextUtil.join (authors, ", ");
            }

            if (author != null)
            {
                HTMLElement itemAuthorTD = dom.getElementItemAuthorTD();
                itemAuthorTD.removeAttribute ("id");
                addTextNode (itemAuthorTD,
                             author,
                             allowEmbeddedHTML,
                             addedNodes);
            }
            else
            {
                // Remove the author separator (to prevent a blank line in
                // the output), and set the author string to nothing.

                itemAuthorBlockParent.removeChild (itemAuthorBlock);
                addRemovedNode (itemAuthorBlockParent,
                                itemAuthorBlock,
                                removedNodes);
            }

            // Set the odd/even class attribute on the row
            if ((rowCount % 2) == 1)
            {
                // Want to use the "odd row" class to distinguish the
                // rows. For the description, though, only do that if
                // if's not empty.

                itemTitleTD.setAttribute ("class", oddItemRowClass);

                if (desc != null)
                    itemDescTD.setAttribute ("class", oddItemRowClass);
            }

            if ((channelCount % 2) == 1)
                channelTD.setClassName (oddChannelClass);
            else
                channelTD.setClassName ("");

            channelRowParent.insertBefore (channelRow.cloneNode (true),
                                           channelSeparatorRow);

            // Remove added elements, and add back removed elements.

            for (Node addedNode : addedNodes)
                removeElement (addedNode);

            for (Node parentNode : removedNodes.keySet())
            {
                List<Node> nodes = removedNodes.get (parentNode);

                for (Node removedNode : nodes)
                    parentNode.appendChild (removedNode);
            }

            i++;
            rowCount++;
        }
    }
    
    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        // Remove the cloneable channel row.

        removeElement (dom.getElementChannelRow());

        // Should we keep the table of contents, or nuke it?

        if (totalItems >= tocThreshold)
        {
            // Keep it. Remove the cloneable table-of-contents element.

            removeElement (dom.getElementTOCEntry());
        }

        else
        {
            // Nuke it.

            removeElement (dom.getElementTableOfContents());
        }

        // Set the title and the top header.

        if (title != null)
        {
            XMLCUtil.getFirstText (dom.getElementTitle()).setData (title);
            dom.setTextTitleHeading (title);
        }

        // Add time stamp and machine.

        Date now = new Date();
        dom.setTextGenerationDate (OUTPUT_DATE_FORMAT.format (now));

        String thisHost = "unknown host";

        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            thisHost = addr.getHostName();
        }

        catch (Exception ex)
        {
            log.error ("Can't get name of local host: " + ex.toString());
        }

        dom.setTextGeneratedOnHost (thisHost);

        // Add configuration info, if available and requested.

        if (! displayToolInfo())
        {
            removeElement (dom.getElementToolInfo());
        }

        else
        {
            dom.setTextVersion (Version.getVersionNumber());

            URL configFileURL = config.getConfigurationFileURL();
            if (configFileURL == null)
                removeElement (dom.getElementConfigFileRow());
            else
                dom.setTextConfigURL (configFileURL.toString());
        }

        // Write the document.

        log.debug ("Generating HTML");

        out.print (dom.toDocument());
        out.flush();
        out.close();
        out = null;

        // Kill the document.

        dom = null;
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return "text/html";
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Remove an element from the document.
     *
     * @param element the <tt>Node</tt> representing the element in the DOM
     */
    private Node removeElement (Node element)
    {
        Node parentNode = element.getParentNode();

        if (parentNode != null)
            parentNode.removeChild (element);

        return element;
    }

    /**
     * Get the document's <HEAD> element in a DOM.
     *
     * @param doc  the loaded DOM for the document
     *
     * @return the <HEAD> element
     *
     * @throws IllegalStateException Bug: Missing <HEAD> element
     */
    public HTMLElement getHeadElement (HTMLObject doc)
        throws IllegalStateException
    {
        Element htmlElement = dom.getDocumentElement();
        Node    headNode;

        // First, find the <HEAD> element. We start by finding the <HTML>
        // element, relying on the following behavior (as documented in the
        // javadoc for org.w3c.dom.Document):
        //
        // "[Document.getDocumentElement()] is a convenience attribute that
        // allows direct access to the child node that is the root element
        // of the document. For HTML documents, this is the element with
        // the tagName "HTML".

        if (htmlElement == null)
        {
            throw new IllegalStateException ("(BUG) No <HTML> element in "
                                           + "document.");
        }

        headNode = htmlElement.getFirstChild();
        while (headNode != null)
        {
            String name = headNode.getNodeName().toLowerCase();
            if (name.equals ("head"))
                break;

            headNode = headNode.getNextSibling();
        }

        if (headNode == null)
        {
            throw new IllegalStateException ("(BUG) No <HEAD> element in "
                                           + "document.");
        }

        return (HTMLElement) headNode;
    }

    /**
     * Determine whether a string contains embedded HTML markup.
     *
     * @param s   the string
     *
     * @return true if the string appears to have HTML markup,
     *         false if not
     */
    private boolean containsHTMLMarkup (String s)
    {
        // For now, assume the description contains HTML markup if it
        // has an equal number of "<" and ">" characters.

        int lt = 0;
        int gt = 0;

        char[] ch = s.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            switch (ch[i])
            {
                case '<':
                    lt++;
                    break;

                case '>':
                    gt++;
                    break;
            }
        }

        return (lt == gt) && (lt > 0);
    }

    /**
     * Adds a removed node to the specified map, indexed by parent node.
     *
     * @param parent  the parent node
     * @param removed the removed node
     * @param map     the map
     */
    private void addRemovedNode (Node                 parent,
                                 Node                 removed,
                                 Map<Node,List<Node>> map)
    {
        List<Node> list = map.get (parent);

        if (list == null)
        {
            list = new ArrayList<Node>();
            map.put (parent, list);
        }

        list.add (removed);
    }

    /**
     * Set a text element in the DOM. Adds the element as a child of the
     * specified parent. If the text contains HTML markup, then either (a)
     * the HTML markup is removed and a pure text node is inserted (if
     * allowEmbeddedHTML is false), or (b) the HTML markup is preserved in
     * a CDATA section (if allowEmbeddedHTML is true), which tells XMLC to
     * leave it alone. If the text contains no markup, then a pure text
     * node is inserted.
     *
     * @param parentNode        the parent, or containing, node
     * @param text              the text to insert
     * @param allowEmbeddedHTML whether or not the feed permits embedded HTML
     * @param addedNodes        collection to which to add the added node
     *
     * @return the node that was inserted
     */
    private Node addTextNode (Node             parentNode,
                              String           text,
                              boolean          allowEmbeddedHTML,
                              Collection<Node> addedNodes)
    {
        Document document = parentNode.getOwnerDocument();
        Node     node  = null;

        if (containsHTMLMarkup (text))
        {
            if (allowEmbeddedHTML)
            {
                // Use an XMLC trick: An inserted CDATA section will be
                // treated as raw markup by XMLC. So, take this raw HTML,
                // and encode it in a CDATA section. Then, insert it in
                // place of the normal text node.

                node = document.createCDATASection (text);
            }

            else
            {
                node = document.createTextNode (HTMLUtil.textFromHTML (text));
            }
        }

        else
        {
            node = document.createTextNode (HTMLUtil.textFromHTML (text));
        }

        parentNode.appendChild (node);
        addedNodes.add (node);
        return node;        
    }
}

