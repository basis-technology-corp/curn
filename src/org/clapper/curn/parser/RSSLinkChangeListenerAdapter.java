/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

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

/**
 * An stub, abstract {@link RSSLinkChangeListener} that can be extended
 * to create simple listeners.
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSLinkChangeListenerAdapter implements RSSLinkChangeListener
{
    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of <tt>RSSLinkChangeListenerAdapter</tt>
     */
    public RSSLinkChangeListenerAdapter()
    {
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Fired when the URL is changed.
     *
     * @param rssLink  the changed object
     * @param oldURL   the old URL
     * @param newURL   the new URL
     */
    public void onURLChange(RSSLink rssLink, URL oldURL, URL newURL)
    {
    }

    /**
     * Fired when the link type is changed.
     *
     * @param rssLink  the changed object
     * @param oldType  the old link type
     * @param newType  the new link type
     */
    public void onLinkTypeChange(RSSLink      rssLink,
                                 RSSLink.Type oldType,
                                 RSSLink.Type newType)
    {
    }

    /**
     * Fired when the MIME type changes.
     *
     * @param rssLink  the changed object
     * @param oldType  the old MIME type
     * @param newType  the new MIME type
     */
    public void onMIMETypeChange(RSSLink rssLink,
                                 String  oldType,
                                 String  newType)
    {
    }
}
