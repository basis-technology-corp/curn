/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.net.URL;
import java.io.File;

import org.clapper.curn.parser.RSSChannel;

/**
 * <p>Contains data for one feed (or site). Most, but not all, of the data
 * comes from the configuration file.</p>
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

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private boolean     pruneURLsFlag         = false;
    private boolean     summaryOnly           = false;
    private boolean     enabled               = true;
    private int         daysToCache           = 0;
    private String      titleOverride         = null;
    private String      itemURLEditCmd        = null;
    private URL         siteURL               = null;
    private File        saveAsFile            = null;
    private int         sortBy                = SORT_BY_NONE;
    private boolean     ignoreDuplicateTitles = false;
    private RSSChannel  parsedChannelData     = null;

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
     * Get the hash code for this feed
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return getCacheKey().hashCode();
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
     * Get the unique key to use to cache information about the feed.
     *
     * @return the unique ID
     */
    public String getCacheKey()
    {
        return getURL().toExternalForm();
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
     * It's intended to be used with the Jakarta ORO <tt>Perl5Util</tt>
     * class to edit the item URLs before displaying them.
     *
     * @return the item URL edit command, or null if none
     */
    public String getItemURLEditCommand()
    {
        return itemURLEditCmd;
    }

    /**
     * Get the item URL edit command for this feed. This command, if supplied,
     * is a Perl-style substitution command, e.g.:
     *
     * <pre>s/foo/bar/g</pre>
     *
     * It's intended to be used with the Jakarta ORO <tt>Perl5Util</tt>
     * class to edit the item URLs before displaying them.
     *
     * @param cmd   the item URL edit command, or null if none
     */
    public void setItemURLEditCommand (String cmd)
    {
        itemURLEditCmd = cmd;
    }

    /**
     * Determine whether this feed is enabled or not. A disabled feed will
     * not be fetched or displayed. (A feed is enabled by default.)

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
    public RSSChannel getParsedChannelData()
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
    public void setParsedChannelData (RSSChannel channel)
    {
        this.parsedChannelData = channel;
    }
}
