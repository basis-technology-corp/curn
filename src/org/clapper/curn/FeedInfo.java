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

package org.clapper.curn;

import java.net.URL;
import java.io.File;

import org.clapper.curn.util.Util;
import org.clapper.curn.parser.RSSChannel;

/**
 * <p>Contains data for one feed (or site). The data about the feed
 * comes from the configuration file. The feed itself comes from parsing
 * the RSS data.</p>
 *
 * @see ConfigFile
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedInfo
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Possible value for the "SortBy" parameter; indicates that items
     * within a feed should not be sorted (i.e., should be presented in
     * feed order).
     *
     * @see #getSortBy
     */
    public static final int SORT_BY_NONE = 0;

    /**
     * Possible value for the "SortBy" parameter; indicates that items
     * within a feed should be sorted by timestamp.
     *
     * @see #getSortBy
     */
    public static final int SORT_BY_TIME = 1;

    /**
     * Possible value for the "SortBy" parameter; indicates that items
     * within a feed should be sorted by title.
     *
     * @see #getSortBy
     */
    public static final int SORT_BY_TITLE = 2;

    /**
     * Default encoding for "save as" file.
     *
     * @see #getSaveAsFile
     * @see #setSaveAsEncoding
     */
    public static final String DEFAULT_SAVE_AS_ENCODING = "utf-8";

    /**
     * Default value for the maximum summary size. Indicates "no maximum."
     *
     * @see #getMaxSummarySize
     * @see #setMaxSummarySize
     */
    public static final int NO_MAX_SUMMARY_SIZE = 0;

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private boolean     pruneURLsFlag         = false;
    private boolean     summaryOnly           = false;
    private boolean     enabled               = true;
    private boolean     saveOnly              = false;
    private int         daysToCache           = 0;
    private String      titleOverride         = null;
    private String      itemURLEditCmd        = null;
    private URL         siteURL               = null;
    private File        saveAsFile            = null;
    private String      saveAsEncoding        = DEFAULT_SAVE_AS_ENCODING;
    private int         sortBy                = SORT_BY_NONE;
    private boolean     ignoreDuplicateTitles = false;
    private RSSChannel  parsedChannelData     = null;
    private String      forcedEncoding        = null;
    private String[]    preparseEditCommands  = null;
    private String      userAgent             = null;
    private int         maxSummarySize        = NO_MAX_SUMMARY_SIZE;
    private boolean     showAuthors           = false;
    private boolean     allowEmbeddedHTML     = false;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor.
     *
     * @param siteURL  the main URL for the site's RSS feed. This constructor
     *                 normalizes the URL.
     *
     * @see Util#normalizeURL
     */
    public FeedInfo (URL siteURL)
    {
        this.siteURL = Util.normalizeURL (siteURL);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Whether or not embedded HTML markup should be honored or suppressed
     * for this feed. (Even if set, this flag may not be meaningful to all
     * output handlers.)
     *
     * @return <tt>true</tt> if embedded HTML markup should be honored
     *         (if possible) for this feed, <tt>false</tt> if it should
     *         be stripped.
     *
     * @see #setAllowEmbeddedHTMLFlag
     */
    public boolean allowEmbeddedHTML()
    {
        return allowEmbeddedHTML;
    }

    /**
     * Set the flag that controls whether or not embedded HTML markup
     * should be honored or suppressed for this feed. (Even if set, this
     * flag may not be meaningful to all output handlers.)
     *
     * @param allow <tt>true</tt> if embedded HTML markup should be honored
     *              (if possible) for this feed, <tt>false</tt> if it should
     *              be stripped.
     *
     * @see #allowEmbeddedHTML()
     */
    public void setAllowEmbeddedHTMLFlag (boolean allow)
    {
        this.allowEmbeddedHTML = allow;
    }

    /**
     * Get the hash code for this feed
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return getURL().hashCode();
    }

    /**
     * Determine whether this <tt>FeedInfo</tt> object is equivalent to
     * another one, based on the URL.
     *
     * @param obj  the other object
     *
     * @return <tt>true</tt> if <tt>obj</tt> is a <tt>FeedInfo</tt> object
     *         that specifies the same URL, <tt>false</tt> otherwise
     */
    public boolean equals (Object obj)
    {
        boolean eq = false;

        if (obj instanceof FeedInfo)
            eq = this.siteURL.equals (((FeedInfo) obj).siteURL);

        return eq;
    }

    /**
     * Get the main RSS URL for the site.
     *
     * @return the site's main RSS URL, guaranteed to be normalized
     *
     * @see Util#normalizeURL
     */
    public URL getURL()
    {
        return siteURL;
    }

    /**
     * Get the number of days that URLs from this site are to be cached.
     *
     * @return the number of days to cache URLs from this site.
     *
     * @see #setDaysToCache
     */
    public int getDaysToCache()
    {
        return daysToCache;
    }

    /**
     * Get the number of milliseconds that URLs from this site are to be
     * cached. This is a convenience front-end to <tt>getDaysToCache()</tt>.
     *
     * @return the number of milliseconds to cache URLs from this site
     *
     * @see #getDaysToCache
     * @see #setDaysToCache
     */
    public long getMillisecondsToCache()
    {
        long days = (long) getDaysToCache();
        return days * 25 * 60 * 60 * 1000;
    }
   
    /**
     * Set the "days to cache" value.
     *
     * @param cacheDays  new value
     *
     * @see #getDaysToCache
     * @see #getMillisecondsToCache
     */
    public void setDaysToCache (int cacheDays)
    {
        this.daysToCache = cacheDays;
    }

    /**
     * Get the "prune URLs" flag. If this flag is set, then URLs from this
     * feed should be displayed without any HTTP parameter information.
     * Otherwise, they should be displayed as is.
     *
     * @return <tt>true</tt> if the flag is set, <tt>false</tt> otherwise
     *
     * @see #setPruneURLsFlag
     */
    public boolean pruneURLs()
    {
        return pruneURLsFlag;
    }

    /**
     * Set the "prune URLs" flag. If this flag is set, then URLs from this
     * feed should be displayed without any HTTP parameter information.
     * Otherwise, they should be displayed as is.
     *
     * @param val <tt>true</tt> to set the flag, <tt>false</tt> to clear it
     *
     * @see #pruneURLs
     */
    public void setPruneURLsFlag (boolean val)
    {
        pruneURLsFlag = val;
    }

    /**
     * Return the value of "summary only" flag. If this flag is set, then
     * any description for this feed should be suppressed. If this flag is
     * not set, then this feed's description (if any) should be displayed.
     *
     * @return <tt>true</tt> if "summary only" flag is set, <tt>false</tt>
     *         otherwise
     */
    public boolean summarizeOnly()
    {
        return summaryOnly;
    }

    /**
     * Set the value of the "summary only" flag.
     *
     * @param val <tt>true</tt> to set the "summary only" flag,
     *            <tt>false</tt> to clear it
     */
    public void setSummarizeOnlyFlag (boolean val)
    {
        this.summaryOnly = val;
    }

    /**
     * Return the value of the global "show authors" flag. This flag
     * controls whether to display the authors associated with each item,
     * if available. It can be overridden on a per-feed basis.
     *
     * @return <tt>true</tt> if "show authors" flag is set, <tt>false</tt>
     *         otherwise
     *
     * @see #setShowAuthorsFlag
     */
    public boolean showAuthors()
    {
        return showAuthors;
    }

    /**
     * Set the value of the global "show authors" flag. This flag controls
     * whether to display the authors associated with each item, if
     * available. It can be overridden on a per-feed basis.
     *
     * @param val <tt>true</tt> to set the "show authors" flag, <tt>false</tt>
     *            to clear it
     *
     * @see #showAuthors
     */
    public void setShowAuthorsFlag (boolean val)
    {
        this.showAuthors = val;
    }

    /**
     * Get the title override for this feed. The title override value
     * overrides any title supplied in the RSS feed itself. This is an
     * optional value.
     *
     * @return the title override, or null if none
     */
    public String getTitleOverride()
    {
        return titleOverride;
    }

    /**
     * Set the title override for this feed. The title override value
     * overrides any title supplied in the RSS feed itself. This is an
     * optional value.
     *
     * @param s  the title override, or null if none
     */
    public void setTitleOverride (String s)
    {
        titleOverride = s;
    }

    /**
     * Get the item URL edit command for this feed. This command, if supplied,
     * is a Perl-style substitution command, e.g.:
     *
     * <pre>s/foo/bar/g</pre>
     *
     * It's intended to be used with the
     * <tt>org.clapper.util.regex.RegexUtil</tt> class's
     * <tt>substitute()</tt> method to edit the item URLs before displaying
     * them.
     *
     * @return the item URL edit command, or null if none
     */
    public String getItemURLEditCommand()
    {
        return itemURLEditCmd;
    }

    /**
     * Set the item URL edit command for this feed. This command, if supplied,
     * is a Perl-style substitution command, e.g.:
     *
     * <pre>s/foo/bar/g</pre>
     *
     * It's intended to be used with the
     * <tt>org.clapper.util.regex.RegexUtil</tt> class's class's
     * <tt>substitute()</tt> method to edit the item URLs before displaying
     * them.
     *
     * @param cmd   the item URL edit command, or null if none
     */
    public void setItemURLEditCommand (String cmd)
    {
        itemURLEditCmd = cmd;
    }

    /**
     * Get the list of edit commands to be applied to the downloaded XML
     * before it is parsed. Each element in the returned array is a
     * Perl-style substitution command, e.g.:
     *
     * <pre>s/foo/bar/g</pre>
     *
     * They're intended to be used with the
     * <tt>org.clapper.util.regex.RegexUtil</tt>
     * class's <tt>substitute()</tt> method to edit the XML before parsing it.
     *
     * @return an array of edit commands, or null if not specified in the
     *         configuration for this feed
     */
    public String[] getPreparseEditCommands()
    {
        return preparseEditCommands;
    }

    /**
     * Set the list of edit commands to be applied to the downloaded XML
     * before it is parsed. Each element in the returned array is a
     * Perl-style substitution command, e.g.:
     *
     * <pre>s/foo/bar/g</pre>
     *
     * They're intended to be used with the
     * <tt>org.clapper.util.regex.RegexUtil</tt>
     * class's <tt>substitute()</tt> method to edit the XML before parsing it.
     *
     * @param cmds an array of edit commands, or null if not specified in the
     *             configuration for this feed
     */
    public void setPreparseEditCommands (String[] cmds)
    {
        preparseEditCommands = cmds;
    }

    /**
     * Determine whether this feed is enabled or not. A disabled feed will
     * not be fetched or displayed. (A feed is enabled by default.)
     *
     * @return <tt>true</tt> if the feed is enabled in the configuration,
     *         <tt>false</tt> otherwise.
     *
     * @see #setEnabledFlag
     */
    public boolean feedIsEnabled()
    {
        return enabled;
    }

    /**
     * Set or clear the "enabled" flag. A disabled feed will not be fetched
     * or displayed. (A feed is enabled by default.)
     *
     * @param enabled <tt>true</tt> if the feed is to be enabled and
     *                <tt>false</tt> if it is to be disabled.
     *
     * @see #feedIsEnabled
     */
    public void setEnabledFlag (boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Get the maximum summary size.
     *
     * @return the maximum summary size, in characters, or 0 for no limit.
     *
     * @see #setMaxSummarySize
     */
    public int getMaxSummarySize()
    {
        return maxSummarySize;
    }

    /**
     * Set the maximum summary size.
     *
     * @param newSize the new maximum summary size, in characters, or 0 for
     *                no limit. Value must be non-negative.
     *
     * @see #getMaxSummarySize
     */
    public void setMaxSummarySize (int newSize)
    {
        assert (newSize >= 0);
        this.maxSummarySize = newSize;
    }

    /**
     * Get the file to which the downloaded XML feed should be saved, if any.
     *
     * @return the file, or null if not specified. Note that this method
     *         does not verify that the file can be opened, written to, or
     *         otherwise accessed.
     *
     * @see #setSaveAsFile
     */
    public File getSaveAsFile()
    {
        return saveAsFile;
    }

    /**
     * Set the file to which the downloaded XML feed should be saved, if any.
     *
     * @param f the file, or null to clear the field. Note that this method
     *          does not verify that the file can be opened, written to, or
     *          otherwise accessed.
     *
     * @see #getSaveAsFile
     */
    public void setSaveAsFile (File f)
    {
        this.saveAsFile = f;
    }

    /**
     * Get the encoding to use when saving the downloaded file. Only applicable
     * if the "save as" file is also defined.
     *
     * @return the encoding
     *
     * @see #setSaveAsEncoding
     * @see #getSaveAsFile
     */
    public String getSaveAsEncoding()
    {
        return saveAsEncoding;
    }

    /**
     * Set the encoding to use when saving the downloaded file. Only applicable
     * if the "save as" file is also defined.
     *
     * @param encoding the encoding to use, or null to reset to the default
     *                 <i>curn</i> encoding of "utf-8"
     *
     * @see #getSaveAsEncoding
     * @see #setSaveAsFile
     */
    public void setSaveAsEncoding (String encoding)
    {
        if (encoding == null)
            encoding = "utf-8";

        this.saveAsEncoding = encoding;
    }

    /**
     * Get the value of the "save only" flag. If this flag is set, then the
     * feed is retrieved and saved (provided a "save as" value is specified),
     * but not parsed.
     *
     * @return <tt>true</tt> if "save only" is set, <tt>false</tt> otherwise
     *
     * @see #setSaveOnlyFlag
     */
    public boolean saveOnly()
    {
        return saveOnly;
    }

    /**
     * Set the value of the "save only" flag. If this flag is set, then the
     * feed is retrieved and saved (provided a "save as" value is specified),
     * but not parsed.
     *
     * @param newValue <tt>true</tt> to set the "save only" flag,
     *                 <tt>false</tt> to clear it
     *
     * @see #saveOnly
     */
    public void setSaveOnlyFlag (boolean newValue)
    {
        this.saveOnly = newValue;
    }

    /**
     * Get the "sort by" value, indicating how items from this feed are to
     * be sorted for presentation.
     *
     * @return one of:
     *         <ul>
     *           <li> {@link #SORT_BY_NONE}: don't sort the items
     *           <li> {@link #SORT_BY_TITLE}: sort by title
     *           <li> {@link #SORT_BY_TIME}: sort by timestamp
     *         </ul>
     *
     * @see #setSortBy
     */
    public int getSortBy()
    {
        return sortBy;
    }

    /**
     * Set the "sort by" value, indicating how items from this feed are to
     * be sorted for presentation.
     *
     * @param val  The new value, which must be one of:
     *             <ul>
     *               <li> {@link #SORT_BY_NONE}: don't sort the items
     *               <li> {@link #SORT_BY_TITLE}: sort by title
     *               <li> {@link #SORT_BY_TIME}: sort by timestamp
     *             </ul>
     *
     * @see #getSortBy
     */
    public void setSortBy (int val)
    {
        switch (val)
        {
            case SORT_BY_NONE:
            case SORT_BY_TITLE:
            case SORT_BY_TIME:
                sortBy = val;
                break;

            default:
                throw new IllegalArgumentException ("(BUG) Bad value "
                                                  + String.valueOf (val)
                                                  + " for setSortBy() "
                                                  + "parameter.");
        }
    }

    /**
     * Get the forced character set encoding for this feed. If this
     * parameter is set, <i>curn</i> will ignore the character set encoding
     * advertised by the remote server (if any), and use the character set
     * specified by this configuration item instead. This is useful in the
     * following cases:
     *
     * <ul>
     *   <li>the remote HTTP server doesn't supply an HTTP Content-Encoding
     *       header, and the local (Java) default encoding doesn't match
     *       the document's encoding
     *   <li>the remote HTTP server supplies the wrong encoding
     * </ul>
     *
     * @return the forced character set encoding, or null if not configured
     */
    public String getForcedCharacterEncoding()
    {
        return forcedEncoding;
    }
     
    /**
     * Determine whether items with duplicate titles should be ignored.
     * This feature (hack, really) is useful for sites (like Yahoo! News)
     * whose feeds often contain duplicate items that have different IDs
     * and different URLs (and thus appear to be unique).
     *
     * @return <tt>true</tt> if items whose titles match other items
     *         should be cached, but otherwise ignored, <tt>false</tt>
     *         otherwise.
     *
     * @see #setIgnoreItemsWithDuplicateTitlesFlag
     */
    public boolean ignoreItemsWithDuplicateTitles()
    {
        return ignoreDuplicateTitles;
    }

    /**
     * Set or clear the "ignore items with duplicate titles" flag.
     *
     * @param val <tt>true</tt> if items whose titles match other items
     *            should be cached, but otherwise ignored, <tt>false</tt>
     *            otherwise.
     *
     * @see #ignoreItemsWithDuplicateTitles
     */
    public void setIgnoreItemsWithDuplicateTitlesFlag (boolean val)
    {
        this.ignoreDuplicateTitles = val;
    }

    /**
     * Get the configured user agent for this feed. Returns a default if
     * none has been configured.
     *
     * @return the user agent header to use
     *
     * @see #setUserAgent
     */
    public String getUserAgent()
    {
        assert (userAgent != null);
        return userAgent;
    }

    /**
     * Set the configured user agent for this feed.
     *
     * @param userAgent the user agent header to use
     *
     * @see #getUserAgent
     */
    public void setUserAgent (String userAgent)
    {
        this.userAgent = userAgent;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the parsed channel data for this feed. This field is set by the
     * main processing logic and does not come from the configuration.
     *
     * @return the <tt>RSSChannel</tt> object representing the current
     *         parsed data from this feed, or null if not set
     *
     * @see #setParsedChannelData
     * @see Curn
     * @see RSSChannel
     */
    RSSChannel getParsedChannelData()
    {
        return parsedChannelData;
    } 

    /**
     * Set the parsed channel data for this feed. This field is set by the
     * main processing logic and does not come from the configuration.
     *
     * @param channel the <tt>RSSChannel</tt> object representing the current
     *                parsed data from this feed, or null if not set
     *
     * @see #getParsedChannelData
     * @see Curn
     * @see RSSChannel
     */
    void setParsedChannelData (RSSChannel channel)
    {
        this.parsedChannelData = channel;
    }
    /*
     * Set the forced character set encoding for this feed.
     *
     * @param encoding the encoding
     *
     * @see #getForcedCharacterEncoding
     */
    public void setForcedCharacterEncoding (String encoding)
    {
        this.forcedEncoding = encoding;
    }
}
