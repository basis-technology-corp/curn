/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2012 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn.parser;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * An <tt>RSSLink</tt> object describes a link to a URL, including any
 * metadata about the URL's content (if available).
 *
 * @see RSSItem
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSLink
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Link types.
     */
    public enum Type
    {
        /**
         * The link refers back to the RSS feed.
         */
        SELF,

        /**
         * The link refers to an alternate page containing the same
         * information, but perhaps in a different format (e.g., HTML).
         */
        ALTERNATE
    };

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private URL    url      = null;
    private String mimeType = null;
    private Type   linkType = Type.SELF;

    private Collection<RSSLinkChangeListener> changeListeners =
        new LinkedHashSet<RSSLinkChangeListener>();

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Create an empty <tt>RSSLink</tt> object.
     *
     * @see #RSSLink(URL,String,Type)
     */
    public RSSLink()
    {
        // nothing to do
    }

    /**
     * Create and populate a new <tt>RSSLink</tt> object.
     *
     * @param url      the link's URL
     * @param mimeType the MIME type. Must not be null.
     * @param linkType the link type
     */
    public RSSLink (URL url, String mimeType, Type linkType)
    {
        this(url, mimeType, linkType, null);
    }

    /**
     * Create and populate a new <tt>RSSLink</tt> object.
     *
     * @param url            the link's URL
     * @param mimeType       the MIME type. Must not be null.
     * @param linkType       the link type
     * @param changeListener initial {@link RSSLinkChangeListener} to add to
     *                       this object. Can be null.
     */
    public RSSLink (URL                   url,
                    String                mimeType,
                    Type                  linkType,
                    RSSLinkChangeListener changeListener)
    {
        this.url      = url;
        this.mimeType = mimeType;
        this.linkType = linkType;

        if (changeListener != null)
            addChangeListener(changeListener);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a change listener to this <tt>RSSLink</tt>.
     *
     * @param listener the listener to add
     *
     * @see #removeChangeListener
     */
    public void addChangeListener(RSSLinkChangeListener listener)
    {
        changeListeners.add(listener);
    }

    /**
     * Remove a change listener from this <tt>RSSLink</tt>.
     *
     * @param listener the listener to remove
     *
     * @see #addChangeListener
     */
    public void removeChangeListener(RSSLinkChangeListener listener)
    {
        changeListeners.remove(listener);
    }

    /**
     * Get the URL for this link.
     *
     * @return the URL, or null if not set yet.
     *
     * @see #setURL
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Set the URL for this link.
     *
     * @param url  the URL, or null if not set yet.
     *
     * @see #getURL
     */
    public void setURL (URL url)
    {
        URL oldURL = this.url;
        this.url = url;

        for (RSSLinkChangeListener listener : changeListeners)
            listener.onURLChange(this, oldURL, this.url);
    }

    /**
     * Get the MIME type for this link.
     *
     * @return the MIME type, or null if not set yet.
     *
     * @see #setMIMEType
     */
    public String getMIMEType()
    {
        return mimeType;
    }

    /**
     * Set the MIME type for this link.
     *
     * @param mimeType  the MIME Type, or null if not set yet.
     *
     * @see #getMIMEType
     */
    public void setMIMEType (String mimeType)
    {
        String oldMIMEType = this.mimeType;
        this.mimeType = mimeType;

        for (RSSLinkChangeListener listener : changeListeners)
            listener.onMIMETypeChange(this, oldMIMEType, this.mimeType);
    }

    /**
     * Get the link type for this link.
     *
     * @return the link type, or null if not set yet.
     *
     * @see #setLinkType
     */
    public Type getLinkType()
    {
        return linkType;
    }

    /**
     * Set the link type for this link.
     *
     * @param linkType  the link Type, or null if not set yet.
     *
     * @see #getLinkType
     */
    public void setLinkType (Type linkType)
    {
        Type oldType = this.linkType;
        this.linkType = linkType;

        for (RSSLinkChangeListener listener : changeListeners)
            listener.onLinkTypeChange(this, oldType, this.linkType);
    }

    /**
     * Get the string representation of this link (the URL).
     *
     * @return the string version
     */
    @Override
    public String toString()
    {
        return (url == null) ? "null" : url.toExternalForm();
    }

    /**
     * Compare this object with another.
     *
     * @param o  the other object
     *
     * @return <tt>true</tt> if they're equal, <tt>false</tt> if not.
     */
    public boolean equals(Object o)
    {
        boolean eq = false;

        if ((o != null) && (o instanceof RSSLink))
            eq = this.url.equals(((RSSLink) o).url);

        return eq;
    }

    /**
     * Get this object's hash code.
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return url.hashCode();
    }
}
