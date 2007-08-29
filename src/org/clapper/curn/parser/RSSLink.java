/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

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
}
