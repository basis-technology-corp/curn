/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.net.URL;

/**
 * <p>Contains configuration data for one RSS feed (or site).</p>
 *
 * @see RSSGetConfiguration
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSFeedInfo
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private boolean  pruneURLsFlag = false;
    private boolean  summaryOnly   = false;
    private boolean  enabled       = true;
    private int      daysToCache   = 0;
    private String   titleOverride = null;
    private String   itemURLEditCmd= null;
    private URL      siteURL       = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Only accessible within this package.
     *
     * @param siteURL  the main URL for the site's RSS feed. This constructor
     *                 normalizes the URL.
     *
     * @see Util#normalizeURL
     */
    RSSFeedInfo (URL siteURL)
    {
        this.siteURL = Util.normalizeURL (siteURL);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the main RSS URL for the site.
     *
     * @return the site's main RSS URL
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
}
