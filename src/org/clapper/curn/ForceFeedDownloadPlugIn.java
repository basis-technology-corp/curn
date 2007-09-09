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

package org.clapper.curn;

/**
 * Defines the method to be implemented by plug-ins that want to force
 * one or more feeds to be downloaded, even if they haven't changed.
 *
 * @version <tt>$Revision$</tt>
 */
public interface ForceFeedDownloadPlugIn extends PlugIn
{
    /**
     * This method determines (based on some internal criteria) whether
     * a given feed should be downloaded even if it hasn't changed. If multiple
     * plug-ins implement this interface, then only one needs to return
     * <tt>true</tt> for the feed download to be forced.
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed that
     *                  has been downloaded and parsed.
     * @param feedCache the feed cache, or null if there isn't one
     *
     * @return <tt>true</tt> if the feed should be downloaded and parsed
     *         even if it's not out of date; <tt>false</tt> if <i>curn</i>'s
     *         normal downloading rules should apply.
     *
     * @throws CurnException on error
     */
    public boolean forceFeedDownload(FeedInfo feedInfo, FeedCache feedCache)
        throws CurnException;
}
