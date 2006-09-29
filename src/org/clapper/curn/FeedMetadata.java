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

package org.clapper.curn;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.clapper.util.io.IOExceptionExt;
import org.clapper.util.io.XMLWriter;
import org.clapper.util.logging.Logger;
import org.clapper.util.text.TextUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Defines the in-memory format of the <i>curn</i> cache, and provides
 * methods for saving and restoring the cache.
 *
 * @see Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedMetaData implements FeedMetaDataRegistry
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String XML_ROOT_ELEMENT_TAG       = "curn_cache";
    private static final String XML_ROOT_ATTR_TIMESTAMP    = "time_written";
    private static final String XML_ENTRY_ELEMENT_TAG      = "cache_entry";
    private static final String XML_ENTRY_ATTR_TIMESTAMP   = "timestamp";
    private static final String XML_ENTRY_ATTR_CHANNEL_URL = "channel_URL";
    private static final String XML_ENTRY_ATTR_ENTRY_URL   = "entry_URL";
    private static final String XML_ENTRY_ATTR_ENTRY_ID    = "entry_ID";
    private static final String XML_ENTRY_ATTR_PUB_DATE    = "pub_date";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    private class FeedCacheMap extends HashMap<String, FeedCacheEntry>
    {
        FeedCacheMap()
        {
            super();
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The configuration
     */
    private final CurnConfig config;

    /**
     * The actual cache, indexed by unique ID.
     */
    private FeedCacheMap cacheByID;

    /**
     * Alternate cache (not saved, but regenerated on the fly), indexed
     * by URL.
     */
    private FeedCacheMap cacheByURL;

    /**
     * Whether or not the cache has been modified since saved or loaded
     */
    private boolean modified = false;

    /**
     * Current time
     */
    private long currentTime = System.currentTimeMillis();

    /**
     * List of interested FeedMetaDataClient objects
     */
    private Collection<FeedMetaDataClient> metaDataClients = 
        new HashSet<FeedMetaDataClient>();

    /**
     * For log messages
     */
    private static final Logger log = new Logger (FeedMetaData.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new, empty cache object.
     *
     * @param config  the <i>curn</i> configuration
     */
    FeedMetaData (CurnConfig config)
    {
        this.config = config;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Register a {@link FeedMetaDataClient} object with this registry.
     * When data for that client is read, this object will call the
     * client's {@link FeedMetaDataClient#processDataItem processDataItem}
     * method. When it's time to save the metadata, this object will call
     * the client's {@link FeedMetaDataClient#getDataItems getDataItems}
     * method. Multiple {@link FeedMetaDataClient} objects may be registered
     * with this object.
     *
     * @param client the {@link FeedMetaDataClient} object
     *
     * @throws CurnException on error
     */
    public void addFeedMetaDataClient(FeedMetaDataClient client)
        throws CurnException
    {
        metaDataClients.add(client);
    }

    /**
     * Load the cache from the file specified in the configuration. If the
     * file doesn't exist, this method quietly returns.
     *
     * @throws FeedMetaDataException  unable to read cache
     */
    public void loadCache()
        throws FeedMetaDataException
    {
        File cacheFile = config.getFeedMetaDataFile();
        this.cacheByURL = new FeedCacheMap();

        log.debug("Reading cache from \"" + cacheFile.getPath() + "\"");

        if (! cacheFile.exists())
        {
            log.debug("Cache \"" + cacheFile.getPath() + "\" doesn't exist.");
            this.cacheByID  = new FeedCacheMap();
        }

        else
        {
            this.cacheByID = readSerializedXMLCache(cacheFile);
            if (this.cacheByID == null)
            {
                throw new FeedMetaDataException
                    (Constants.BUNDLE_NAME, "FeedCache.badCacheFile",
                     "Unable to load cache file \"{0}\"",
                     new Object[] {cacheFile.getPath()});
            }

            if (log.isDebugEnabled())
                dumpCache("before pruning");
            pruneCache();
            if (log.isDebugEnabled())
                dumpCache("after pruning");
            modified = false;
        }
    }

    /**
     * Attempt to save the cache back to disk. Does nothing if the cache
     * hasn't been modified since it was saved.
     *
     * @param totalCacheBackups total number of backup files to keep
     * @throws FeedMetaDataException  unable to write cache
     */
    public void saveCache (final int totalCacheBackups)
        throws FeedMetaDataException
    {
        log.debug ("modified=" + this.modified);
        if (this.modified)
        {
            File cacheFile = config.getFeedMetaDataFile();

            try
            {
                log.debug("Saving cache to \"" + cacheFile.getPath() +
                          "\". Total backups=" + totalCacheBackups);

                // Create the DOM.

                Element root = new Element(XML_ROOT_ELEMENT_TAG);
                root.setAttribute (XML_ROOT_ATTR_TIMESTAMP,
                                   String.valueOf (System.currentTimeMillis()));

                for (String id : cacheByID.keySet())
                {
                    FeedCacheEntry entry = cacheByID.get (id);

                    Element e = new Element(XML_ENTRY_ELEMENT_TAG);

                    e.setAttribute (XML_ENTRY_ATTR_TIMESTAMP,
                                    String.valueOf (entry.getTimestamp()));
                    e.setAttribute (XML_ENTRY_ATTR_ENTRY_ID,
                                    entry.getUniqueID());
                    e.setAttribute (XML_ENTRY_ATTR_ENTRY_URL,
                                    entry.getEntryURL().toString());
                    e.setAttribute (XML_ENTRY_ATTR_CHANNEL_URL,
                                    entry.getChannelURL().toString());

                    // Only write the publication date if it's present.

                    Date pubDate = entry.getPublicationDate();
                    if (pubDate != null)
                    {
                        e.setAttribute (XML_ENTRY_ATTR_PUB_DATE,
                                        String.valueOf (pubDate.getTime()));
                    }

                    root.addContent(e);
                }

                Document document = new Document(root);
                XMLOutputter outputter = new XMLOutputter();

                // Open the cache file. For the cache file, the index
                // marker goes at the end of the file (since the extension
                // doesn't matter as much). This allows the file names to
                // sort better in a directory listing.

                Writer cacheOut = CurnUtil.openOutputFile
                                             (cacheFile,
                                              null,
                                              CurnUtil.IndexMarker.AFTER_EXTENSION,
                                              totalCacheBackups);

                outputter.output(document, new XMLWriter(cacheOut));
            }

            catch (IOException ex)
            {
                throw new FeedMetaDataException (ex);
            }

            catch (IOExceptionExt ex)
            {
                throw new FeedMetaDataException (ex);
            }
        }
    }

    /**
     * Determine whether the cache contains an entry with the specified
     * unique ID.
     *
     * @param id  the ID to check.
     *
     * @return <tt>true</tt> if the ID is present in the cache,
     *         <tt>false</tt> if not
     */
    public boolean containsID (final String id)
    {
        boolean hasKey = cacheByID.containsKey (id);
        log.debug ("Cache contains \"" + id + "\"? " + hasKey);
        return hasKey;
    }

    /**
     * Determine whether the cache contains the specified URL.
     *
     * @param url  the URL to check. This method normalizes it.
     *
     * @return <tt>true</tt> if the ID is present in the cache,
     *         <tt>false</tt> if not
     */
    public boolean containsURL (final URL url)
    {
        String  urlKey = CurnUtil.urlToLookupKey (url);
        boolean hasURL = cacheByURL.containsKey (urlKey);
        log.debug ("Cache contains \"" + urlKey + "\"? " + hasURL);
        return hasURL;
    }

    /**
     * Get an item from the cache by its unique ID.
     *
     * @param id  the unique ID to check
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getItem (final String id)
    {
        return (FeedCacheEntry) cacheByID.get (id);
    }

    /**
     * Get an item from the cache by its URL.
     *
     * @param url the URL
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getItemByURL (final URL url)
    {
        return (FeedCacheEntry) cacheByURL.get (CurnUtil.urlToLookupKey (url));
    }

    /**
     * Add (or replace) a cached URL.
     *
     * @param uniqueID   the unique ID string for the cache entry, or null.
     *                   If null, the URL is used as the unique ID.
     * @param url        the URL to cache. May be an individual item URL, or
     *                   the URL for an entire feed.
     * @param pubDate    the publication date, if known; or null
     * @param parentFeed the associated feed
     * @see CurnUtil#normalizeURL
     */
    public synchronized void addToCache (String         uniqueID,
                                         final URL      url,
                                         final Date     pubDate,
                                         final FeedInfo parentFeed)
    {
        String urlKey = CurnUtil.urlToLookupKey (url);

        if (uniqueID == null)
            uniqueID = urlKey;

        URL parentURL = parentFeed.getURL();
        FeedCacheEntry entry = new FeedCacheEntry (uniqueID,
                                                   parentURL,
                                                   url,
                                                   pubDate,
                                                   System.currentTimeMillis());

        log.debug ("Adding cache entry for URL \"" +
                   entry.getEntryURL().toExternalForm() +
                   "\". ID=\"" + uniqueID + "\", channel URL: \"" +
                   entry.getChannelURL().toExternalForm() +
                   "\"");
        cacheByID.put (uniqueID, entry);
        cacheByURL.put (urlKey, entry);
        modified = true;
    }

    /**
     * Set the cache's notion of the current time, which affects how elements
     * are pruned when loaded from the cache. Only meaningful if set before
     * the <tt>loadCache()</tt> method is called. If this method is never
     * called, then the cache uses the current time.
     *
     * @param datetime  the time to use
     */
    public void setCurrentTime (final Date datetime)
    {
        this.currentTime = datetime.getTime();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Attempt to load the specified cache file as an old-style file
     * of serialized Java objects. Logs, but does not throw any exceptions.
     *
     * @param cacheFile  the file to read
     * @return a deserialized FeedCacheMap on success, or null on failure
     * @throws FeedMetaDataException file is XML, but there's something wrong
     *                            with it
     */
    private FeedCacheMap readSerializedXMLCache(final File cacheFile)
        throws FeedMetaDataException
    {
        FeedCacheMap result        = null;
        String       cacheFilePath = cacheFile.getPath();

        // First, parse the XML file into a DOM.

        SAXBuilder builder = new SAXBuilder();
        Document document;

        log.info("Attempting to parse \"" + cacheFilePath + "\" as XML.");
        try
        {
            document = builder.build(cacheFile);
        }

        catch (JDOMException ex)
        {
            throw new FeedMetaDataException(ex);
        }

        catch (IOException ex)
        {
            throw new FeedMetaDataException(ex);
        }

        log.debug("XML parse succeeded.");

        // Get the top-level element and verify that it's the one
        // we want.

        Element root = document.getRootElement();
        String rootTagName = root.getName();

        if (! rootTagName.equals(XML_ROOT_ELEMENT_TAG))
        {
            throw new FeedMetaDataException
                (Constants.BUNDLE_NAME,
                 "FeedCache.nonCacheXML",
                 "File \"{0}\" is not a curn XML cache file. The root " +
                 "XML element is <{1}>, not the expected <{2}>.",
                 new Object[]
                 {
                     cacheFilePath,
                     rootTagName,
                     XML_ROOT_ELEMENT_TAG
                 });
        }

        // Okay, it's a curn cache. Start traversing the child nodes,
        // parsing each cache entry.

        result = new FeedCacheMap();

        List<?> childNodes = root.getChildren();
        for (Iterator<?> it = childNodes.iterator(); it.hasNext(); )
        {
            Element childNode = (Element) it.next();

            // Skip non-element nodes (like text).

            String nodeName = childNode.getName();
            if (! nodeName.equals(XML_ENTRY_ELEMENT_TAG))
            {
                log.warn("Skipping unexpected XML element <" +
                         nodeName + "> in curn XML cache file \"" +
                         cacheFilePath + "\".");
                continue;
            }

            try
            {
                FeedCacheEntry entry = parseXMLCacheEntry(childNode);
                result.put(entry.getUniqueID(), entry);
            }

            catch (FeedCacheEntryException ex)
            {
                // Bad entry. Log the error, but move on.

                log.error("Error parsing feed cache entry", ex);
            }
        }

        return result;
    }

    /**
     * Parse an XML feed cache entry.
     *
     * @param element  the XML element for the feed cache entry
     *
     * @return the FeedCacheEntry
     *
     * @throws FeedCacheEntryException bad entry
     */
    private FeedCacheEntry parseXMLCacheEntry (final Element element)
        throws FeedCacheEntryException
    {
        FeedCacheEntry result = null;

        // Parse out the attributes.

        try
        {
            String entryID = getRequiredXMLAttribute(element,
                                                     XML_ENTRY_ATTR_ENTRY_ID,
                                                     null);
            String sChannelURL =
                getRequiredXMLAttribute(element,
                                        XML_ENTRY_ATTR_CHANNEL_URL,
                                        entryID);
            String sEntryURL = getRequiredXMLAttribute(element,
                                                       XML_ENTRY_ATTR_ENTRY_URL,
                                                       entryID);
            String sTimestamp = getRequiredXMLAttribute(element,
                                                        XML_ENTRY_ATTR_TIMESTAMP,
                                                        entryID);
            String sPubDate =  getOptionalXMLAttribute(element,
                                                       XML_ENTRY_ATTR_PUB_DATE,
                                                       null);


            if ((entryID != null) &&
                (sChannelURL != null) &&
                (sEntryURL != null) &&
                (sTimestamp != null))
            {
                // Parse the timestamp.

                long timestamp = 0;
                try
                {
                    timestamp = Long.parseLong(sTimestamp);
                }

                catch (NumberFormatException ex)
                {
                    throw new FeedCacheEntryException
                        ("Bad timestamp value of \"" + sTimestamp +
                         "\" for <" + XML_ENTRY_ELEMENT_TAG +
                         "> with unique ID \"" + entryID +
                         "\". Skipping entry.");
                }

                // Parse the publication date, if any

                Date publicationDate = null;
                if (sPubDate != null)
                {
                    try
                    {
                        publicationDate = new Date(Long.parseLong(sPubDate));
                    }

                    catch (NumberFormatException ex)
                    {
                        log.error("Bad publication date value of \"" + sPubDate +
                                  "\" for <" + XML_ENTRY_ELEMENT_TAG +
                                  "> with unique ID \"" + entryID +
                                  "\". Ignoring publication date.");
                    }
                }

                // Parse the URLs.

                URL channelURL = null;
                try
                {
                    channelURL = new URL(sChannelURL);
                }

                catch (MalformedURLException ex)
                {
                    throw new FeedCacheEntryException
                        ("Bad channel URL \"" + sChannelURL + "\" for <" +
                         XML_ENTRY_ELEMENT_TAG + "> with unique ID \"" +
                         entryID + "\". Skipping entry.");
                }

                URL entryURL = null;
                try
                {
                    entryURL = new URL(sEntryURL);
                }

                catch (MalformedURLException ex)
                {
                    throw new FeedCacheEntryException
                        ("Bad item URL \"" + sChannelURL + "\" for <" +
                         XML_ENTRY_ELEMENT_TAG + "> with unique ID \"" +
                         entryID + "\". Skipping entry.");
                }

                result = new FeedCacheEntry(entryID,
                                            channelURL,
                                            entryURL,
                                            publicationDate,
                                            timestamp);
            }
        }

        catch (JDOMException ex)
        {
            throw new FeedCacheEntryException(ex);
        }

        return result;
    }

    /**
     * Prune the loaded cache of out-of-date data.
     */
    private void pruneCache()
    {
        log.debug ("PRUNING CACHE");
        log.debug ("Cache's notion of current time: " +
                   new Date (currentTime));

        for (Iterator itKeys = cacheByID.keySet().iterator();
             itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            FeedCacheEntry entry = (FeedCacheEntry) cacheByID.get (itemKey);
            URL channelURL = entry.getChannelURL();
            boolean removed = false;

            if (log.isDebugEnabled())
                dumpCacheEntry (itemKey, entry, "");

            FeedInfo feedInfo = config.getFeedInfoFor (channelURL);

            if (feedInfo == null)
            {
                // Cached URL no longer corresponds to a configured site
                // URL. Kill it.

                log.debug ("Cached item \"" + itemKey +
                           "\", with base URL \"" + channelURL.toString() +
                           "\" no longer corresponds to a configured feed. " +
                           "Tossing it.");
                itKeys.remove();
                removed = true;
            }

            else
            {
                long timestamp  = entry.getTimestamp();
                long maxCacheMS = feedInfo.getMillisecondsToCache();
                long expires    = timestamp + maxCacheMS;

                if (log.isDebugEnabled())
                {
                    log.debug ("    Cache time: " + feedInfo.getDaysToCache() +
                               " days (" + maxCacheMS + " ms)");
                    log.debug ("    Expires: " +
                               new Date (expires).toString());
                }

                if (timestamp > currentTime)
                {
                    log.debug ("Cache time for item \"" + itemKey +
                               "\" is in the future, relative to cache's " +
                               "notion of current time. Setting its " +
                               "timestamp to the current time.");
                    entry.setTimestamp (currentTime);
                    this.modified = true;
                }

                else if (expires < currentTime)
                {
                    log.debug ("Cache time for item \"" + itemKey +
                               "\" has expired. Deleting cache entry.");
                    itKeys.remove();
                    this.modified = true;
                    removed = true;
                }
            }

            if (! removed)
            {
                // Add to URL cache.

                String urlKey = CurnUtil.urlToLookupKey (entry.getEntryURL());
                log.debug ("Loading URL \"" + urlKey +
                           "\" into in-memory lookup cache.");
                cacheByURL.put (urlKey, entry);
            }
        }

        log.debug ("DONE PRUNING CACHE");
    }

    /**
     * Dump the contents of the cache, via the "debug" log facility.
     *
     * @param label  a label, or initial message, to identify the dump
     */
    private void dumpCache (final String label)
    {
        log.debug ("CACHE DUMP: " + label);
        Set<String> sortedKeys = new TreeSet<String> (cacheByID.keySet());
        for (String itemKey : sortedKeys)
            dumpCacheEntry (itemKey, (FeedCacheEntry) cacheByID.get (itemKey), "");
    }

    /**
     * Dump a single cache entry via the "debug" log facility.
     *
     * @param itemKey the hash table key for the item
     * @param entry   the cache entry
     * @param indent  string to use to indent output, if desired
     */
    private void dumpCacheEntry (final Object         itemKey,
                                 final FeedCacheEntry entry,
                                       String         indent)
    {
        long timestamp  = entry.getTimestamp();

        if (indent == null)
            indent = "";

        log.debug (indent + "Cached item \"" + itemKey.toString() + "\"");
        log.debug (indent + "    Item URL: " + entry.getEntryURL().toString());
        log.debug (indent + "    Channel URL: " +
                   entry.getChannelURL().toString());
        log.debug (indent + "    Cached on: " +
                   new Date (timestamp).toString());
    }

    /**
     * Retrieve an optional XML attribute value from a list of attributes.
     * If the attribute is missing or empty, the default is returned.
     *
     * @param element      the XML element
     * @param defaultValue the default value
     * @param name         the attribute name
     *
     * @return the attribute's value, or null if the attribute wasn't found
     *
     * @throws JDOMException  DOM parsing error
     */
    private String getOptionalXMLAttribute(final Element element,
                                           final String  name,
                                           final String  defaultValue)
        throws JDOMException
    {
        String value = element.getAttributeValue(name);
        if ((value != null) && TextUtil.stringIsEmpty(value))
            value = null;

        return (value == null) ? defaultValue : value;
    }

    /**
     * Retrieve an XML attribute value from a list of attributes. If the
     * attribute is missing, the error is logged (but an exception is not
     * thrown).
     *
     * @param element the element
     * @param name    the attribute name
     * @param entryID entry ID string, if available, for errors
     *
     * @return the attribute's value, or null if the attribute wasn't found
     *
     * @throws JDOMException  DOM parsing error
     */
    private String getRequiredXMLAttribute (final Element element,
                                            final String  name,
                                                  String  entryID)
        throws JDOMException
    {
        String value = getOptionalXMLAttribute (element, name, null);

        if (value == null)
        {
            if (entryID == null)
                entryID = "?";

            log.error("<" + XML_ENTRY_ELEMENT_TAG + "> is missing required " +
                      "\"" + name + "\" XML attribute.");
        }

        return value;
    }
}
