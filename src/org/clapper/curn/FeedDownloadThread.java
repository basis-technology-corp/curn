/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.clapper.curn.util.Util;

import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSParserException;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.FileUtil;
import org.clapper.util.misc.Logger;

import org.apache.oro.text.perl.Perl5Util;

class FeedDownloadThread extends Thread
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/ 

    private Logger              log             = null;
    private String              id              = null;
    private ConfigFile          configuration   = null;
    private RSSParser           rssParser       = null;
    private FeedCache           cache           = null;
    private RSSParserException  exception       = null;  
    private Perl5Util           perl5Util       = new Perl5Util();
    private List                feedQueue       = null;

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/ 

    private class ItemComparator implements Comparator
    {
        private Date  now = new Date();
        private int   sortBy;

        ItemComparator (int sortBy)
        {
            this.sortBy = sortBy;
        }

        public int compare (Object o1, Object o2)
        {
            int      cmp = 0;
            RSSItem  i1  = (RSSItem) o1;
            RSSItem  i2  = (RSSItem) o2;

            switch (sortBy)
            {
                case FeedInfo.SORT_BY_TITLE:
                    String title1 = i1.getTitle();
                    if (title1 == null)
                        title1 = "";

                    String title2 = i2.getTitle();
                    if (title2 == null)
                        title2 = "";

                    cmp = title1.compareToIgnoreCase (title2);
                    break;

                case FeedInfo.SORT_BY_TIME:
                    Date time1 = i1.getPublicationDate();
                    if (time1 == null)
                        time1 = now;

                    Date time2 = i2.getPublicationDate();
                    if (time2 == null)
                        time2 = now;

                    cmp = time1.compareTo (time2);
                    break;

                default:
                    cmp = -1;
                    break;
            }

            return cmp;
        }

        public boolean equals (Object o)
        {
            return (o instanceof ItemComparator);
        }
    }

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/ 

    FeedDownloadThread (String     threadId,
                        RSSParser  parser,
                        FeedCache  feedCache,
                        ConfigFile configFile,
                        List       feedQueue)
    {

        this.id = threadId;

        String name = "FeedDownloadThread-" + this.id;

        super.setName (name);
        this.log = new Logger (name);
        this.configuration = configFile;
        this.rssParser = parser;
        this.cache = feedCache;
        this.feedQueue = feedQueue;

        //setPriority (getPriority() + 1);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/ 

    public void run()
    {
        log.info ("Thread is alive at priority " + getPriority());

        for (;;)
        {
            FeedInfo feed = null;

            log.debug ("Checking feed queue.");

            synchronized (feedQueue)
            {
                if (feedQueue.size() > 0)
                    feed = (FeedInfo) feedQueue.remove (0);
            }

            if (feed == null)
            {
                log.info ("Queue of feeds is empty. Nothing left to do.");
                break;
            }

            processFeed (feed);
        }

        log.debug ("Thread is finishing.");
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/ 

    void processFeed (FeedInfo feed)
    {
        this.exception = null;

        try
        {
            log.info ("Processing feed: " + feed.getURL().toString());
            feed.setParsedChannelData (handleFeed (feed,
                                                   rssParser,
                                                   configuration));
        }

        catch (RSSParserException ex)
        {
            this.exception = ex;
        }
    }

    RSSParserException getException()
    {
        return this.exception;
    }

    boolean errorOccurred()
    {
        return (this.exception != null);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/ 

    /**
     * Actually processes a feed. This method is called by checkFeed()
     * after checkFeed() determines that there's a reason to try to download
     * the feed (i.e., the feed has a "save as" setting, and/or parsing is
     * desired.
     * @param feedInfo      the info about the feed
     * @param parser        the RSS parser to use, or null if parsing is to
     *                      be skipped
     * @param configuration the parsed configuration data
     *
     * @return the <tt>RSSChannel</tt> representing the parsed feed, if
     *         parsing was enabled; otherwise, null.
     *
     * @throws RSSParserException parser error
     */
    private RSSChannel handleFeed (FeedInfo   feedInfo,
                                   RSSParser  parser,
                                   ConfigFile configuration)
        throws RSSParserException
    {
        URL         feedURL = feedInfo.getURL();
        String      feedURLString = feedURL.toString();
        RSSChannel  channel = null;

        try
        {
            log.info ("Checking for new data from RSS feed " + feedURLString);

            // Open the connection.

            URLConnection conn = feedURL.openConnection();

            // Don't download the channel if it hasn't been modified since
            // we last checked it. We set the If-Modified-Since header, to
            // tell the web server not to return the content if it's not
            // newer than what we saw before. However, as a double-check
            // (for web servers that ignore the header), we also check the
            // Last-Modified header, if any, that's returned; if it's not
            // newer, we don't bother to parse and process the returned
            // XML.

            setIfModifiedSinceHeader (conn, feedInfo);

            // If the config allows us to transfer gzipped content, then
            // set that header, too.

            setGzipHeader (conn, configuration);

            // If the feed has actually changed, process it.

            if (! feedHasChanged (conn, feedInfo))
            {
                log.info ("Feed has not changed. Skipping it.");
            }

            else
            {
                log.debug ("Feed may have changed. "
                         + "Downloading and processing it.");

                InputStream is = getURLInputStream (conn);

                // Download the feed to a file. We'll parse the file.

                File temp = File.createTempFile ("curn", ".xml", null);
                temp.deleteOnExit();

                int totalBytes = downloadFeed (is, feedURLString, temp);
                is.close();

                if (totalBytes == 0)
                {
                    log.debug ("Feed \""
                             + feedURLString
                             + "\" returned no data.");
                }

                else
                {
                    File saveAsFile = feedInfo.getSaveAsFile();

                    if (saveAsFile != null)
                    {
                        log.debug ("Copying temporary file \""
                                 + temp.getPath()
                                 + "\" to \""
                                 + saveAsFile.getPath()
                                 + "\"");
                        FileUtil.copyFile (temp, saveAsFile);
                    }

                    if (parser == null)
                        log.debug ("No RSS parser. Skipping XML parse phase.");
                    else
                    {
                        log.debug ("Using RSS parser "
                                 + parser.getClass().getName()
                                 + " to parse the feed.");

                        is = new FileInputStream (temp);
                        channel = parser.parseRSSFeed (is);
                        is.close();

                        processChannelItems (channel, feedInfo);
                        if (channel.getItems().size() == 0)
                            channel = null;
                    }
                }

                temp.delete();
            }
        }

        catch (MalformedURLException ex)
        {
            log.error ("", ex);
        }

        catch (RSSParserException ex)
        {
            log.error ("RSS parse exception: ", ex); 
        }

        catch (IOException ex)
        {
            log.error ("", ex);
        }

        return channel;
    }

    private int downloadFeed (InputStream urlStream, String feedURL, File file)
        throws IOException
    {
        int totalBytes = 0;

        log.debug ("Downloading \""
                 + feedURL
                 + "\" to file \""
                 + file.getPath());
        OutputStream os = new FileOutputStream (file);
        totalBytes = FileUtil.copyStream (urlStream, os);
        os.close();

        // It's possible for totalBytes to be zero if, for instance, the
        // use of the If-Modified-Since header caused an HTTP server to
        // return no content.

        return totalBytes;
    }

    private InputStream getURLInputStream (URLConnection conn)
        throws IOException
    {
        InputStream is = conn.getInputStream();
        String ce = conn.getHeaderField ("content-encoding");

        if (ce != null)
        {
            String urlString = conn.getURL().toString();

            log.debug ("URL \""
                     + urlString
                     + "\" -> Content-Encoding: "
                     + ce);
            if (ce.indexOf ("gzip") != -1)
            {
                log.debug ("URL \""
                         + urlString
                         + "\" is compressed. Using GZIPInputStream.");
                is = new GZIPInputStream (is);
            }
        }

        return is;
    }

    private void setGzipHeader (URLConnection conn, ConfigFile configuration)
    {
        if (configuration.retrieveFeedsWithGzip())
        {
            log.debug ("Setting header \"Accept-Encoding\" to \"gzip\"");
            conn.setRequestProperty ("Accept-Encoding", "gzip");
        }
    }

    private void setIfModifiedSinceHeader (URLConnection conn,
                                           FeedInfo      feedInfo)
    {
        long     lastSeen = 0;
        boolean  hasChanged = false;
        URL      feedURL = feedInfo.getURL();

        if (cache != null)
        {
            FeedCacheEntry entry = cache.getItemByURL (feedURL);

            if (entry != null)
            {
                lastSeen = entry.getTimestamp();

                if (lastSeen > 0)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug ("Setting If-Modified-Since header for "
                                 + "feed \""
                                 + feedURL.toString()
                                 + "\" to: "
                                 + String.valueOf (lastSeen)
                                 + " ("
                                 + new Date (lastSeen).toString()
                                 + ")");
                    }

                    conn.setIfModifiedSince (lastSeen);
                }
            }
        }
    }

    private boolean feedHasChanged (URLConnection conn, FeedInfo feedInfo)
        throws IOException
    {
        long     lastSeen = 0;
        long     lastModified = 0;
        boolean  hasChanged = false;
        URL      feedURL = feedInfo.getURL();

        if (cache != null)
        {
            FeedCacheEntry entry = cache.getItemByURL (feedURL);

            if (entry != null)
                lastSeen = entry.getTimestamp();
        }

        if (lastSeen == 0)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has no recorded last-seen time.");
            hasChanged = true;
        }

        else if ((lastModified = conn.getLastModified()) == 0)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" provides no last-modified time.");
            hasChanged = true;
        }

        else if (lastSeen >= lastModified)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has Last-Modified time of "
                     + new Date (lastModified).toString()
                     + ", which is not newer than last-seen time of "
                     + new Date (lastSeen).toString()
                     + ". Feed has no new data.");
        }

        else
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has Last-Modified time of "
                     + new Date (lastModified).toString()
                     + ", which is newer than last-seen time of "
                     + new Date (lastSeen).toString()
                     + ". Feed might have new data.");
            hasChanged = true;
        }

        return hasChanged;
    }

    private void processChannelItems (RSSChannel  channel,
                                      FeedInfo    feedInfo)
        throws RSSParserException,
               MalformedURLException
    {
        Collection  items;
        Iterator    it;
        String      titleOverride = feedInfo.getTitleOverride();
        boolean     pruneURLs = feedInfo.pruneURLs();
        String      editCmd = feedInfo.getItemURLEditCommand();
        String      channelName = channel.getLink().toString();

        if (titleOverride != null)
            channel.setTitle (titleOverride);

        if (editCmd != null)
        {
            log.debug ("Channel \""
                     + channelName
                     + "\": Edit command is: "
                     + editCmd);
        }

        items = sortChannelItems (channel.getItems(), feedInfo);

        // First, weed out the ones we don't care about.

        log.info ("Channel \""
                + channelName
                + "\": "
                + String.valueOf (items.size())
                + " total items");
        for (it = items.iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();
            URL itemURL = item.getLink();

            if (itemURL == null)
            {
                log.debug ("Skipping item with null URL.");
                it.remove();
                continue;
            }

            if (pruneURLs || (editCmd != null))
            {
                // Prune the URL of its parameters, if configured for this
                // site. This must be done before checking the cache, because
                // the pruned URLs are what end up in the cache.

                String sURL = itemURL.toExternalForm();

                if (pruneURLs)
                {
                    int i = sURL.indexOf ("?");

                    if (i != -1)
                        sURL = sURL.substring (0, i);
                }

                if (editCmd != null)
                    sURL = perl5Util.substitute (editCmd, sURL);

                itemURL = new URL (sURL);
            }

            // Normalize the URL and save it.

            itemURL = Util.normalizeURL (itemURL);
            item.setLink (itemURL);

            // Skip it if it's cached.

            String itemID = item.getID();
            log.debug ("Item link: " + itemURL);
            log.debug ("Item ID: " + ((itemID == null) ? "<null>" : itemID));
            if (cache != null)
            {
                if ((itemID != null) && (cache.containsID (itemID)))
                {
                    log.debug ("Skipping cached ID \""
                             + itemID
                             + "\" (item URL \""
                             + itemURL.toString()
                             + "\")");
                    it.remove();
                }

                else if (cache.containsURL (itemURL))
                {
                    log .debug ("Skipping cached URL \""
                             + itemURL.toString()
                             + "\")");
                    it.remove();
                }
            }
        }

        // Add all the items to the cache.

        if ((items.size() > 0) && (cache != null))
        {
            for (it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();

                log.debug ("Cacheing URL: " + item.getLink().toString());
                cache.addToCache (item.getID(),
                                  item.getLink(),
                                  feedInfo);
            }
        }

        // If we're to ignore items with duplicate titles, now is the time
        // to do it. It must be done AFTER cacheing, to be sure we don't show
        // the weeded-out duplicates during the next run.

        if (feedInfo.ignoreItemsWithDuplicateTitles())
            items = pruneDuplicateTitles (items);

        // Change the channel's items to the ones that are left.

        channel.setItems (items);
    }

    private Collection sortChannelItems (Collection items, FeedInfo feedInfo)
    {
        Collection result = items;
        int        total  = items.size();

        if (total > 0)
        {
            int sortBy = feedInfo.getSortBy();

            switch (sortBy)
            {
                case FeedInfo.SORT_BY_NONE:
                    break;

                case FeedInfo.SORT_BY_TITLE:
                case FeedInfo.SORT_BY_TIME:

                    // Can't just use a TreeSet, with a Comparator, because
                    // then items with the same title will be weeded out.

                    Object[] array = items.toArray();
                    Arrays.sort (array, new ItemComparator (sortBy));
                    result = Arrays.asList (array);
                break;

            default:
                throw new IllegalStateException ("Bad FeedInfo.getSortBy() "
                                               + "value of "
                                               + String.valueOf (sortBy));
            }
        }

        return result;
    }

    private Collection pruneDuplicateTitles (Collection items)
    {
        Set         titlesSeen = new HashSet();
        Collection  result     = new ArrayList();

        for (Iterator it = items.iterator(); it.hasNext(); )
        {
            RSSItem item  = (RSSItem) it.next();
            String  title = item.getTitle().toLowerCase();

            if (title == null)
                title = "";

            if (! titlesSeen.contains (title))
            {
                result.add (item);
                titlesSeen.add (title);
            }
        }

        return result;
    }
}
