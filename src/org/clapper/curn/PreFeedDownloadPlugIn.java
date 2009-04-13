/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2009 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2009 Brian M. Clapper."

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

package org.clapper.curn;

import java.net.URLConnection;

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified just before <i>curn</i> downloads a feed.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PostFeedDownloadPlugIn
 * @see PostFeedParsePlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PreFeedDownloadPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Called just before a feed is downloaded. This method can return
     * <tt>false</tt> to signal <i>curn</i> that the feed should be
     * skipped. The plug-in method can also set values on the
     * <tt>URLConnection</tt> used to download the plug-in, via
     * <tt>URL.setRequestProperty()</tt>. (Note that <i>all</i> URLs, even
     * <tt>file:</tt> URLs, are passed into this method. Setting a request
     * property on the <tt>URLConnection</tt> object for a <tt>file:</tt>
     * URL will have no effect--though it isn't specifically harmful.)</p>
     *
     * <p>Possible uses for a pre-feed download plug-in include:</p>
     *
     * <ul>
     *   <li>filtering on feed URL to prevent downloading non-matching feeds
     *   <li>changing the default User-Agent value
     *   <li>setting a non-standard HTTP header field
     * </ul>
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed to be
     *                  downloaded
     * @param urlConn   the <tt>java.net.URLConnection</tt> object that will
     *                  be used to download the feed's XML.
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPreFeedDownloadPlugIn (FeedInfo      feedInfo,
                                             URLConnection urlConn)
        throws CurnException;
}
