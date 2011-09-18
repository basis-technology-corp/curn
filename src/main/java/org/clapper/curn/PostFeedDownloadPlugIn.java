/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
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


package org.clapper.curn;

import java.io.File;

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified just after <i>curn</i> downloads a feed.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PreFeedDownloadPlugIn
 * @see PostFeedParsePlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PostFeedDownloadPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called immediately after a feed is downloaded. This method can
     * return <tt>false</tt> to signal <i>curn</i> that the feed should be
     * skipped. For instance, a plug-in that filters on the unparsed XML
     * feed content could use this method to weed out non-matching feeds
     * before they are downloaded.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded
     * @param feedDataFile  the file containing the downloaded, unparsed feed 
     *                      XML. <b><i>curn</i> may delete this file after all
     *                      plug-ins are notified!</b>
     * @param encoding      the encoding used to store the data in the file,
     *                      or null for the default
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed. A return value of
     *         <tt>false</tt> aborts all further processing on the feed.
     *         In particular, <i>curn</i> will not pass the feed along to
     *         other plug-ins that have yet to be notified of this event.
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     */
    public boolean runPostFeedDownloadPlugIn (FeedInfo feedInfo,
                                              File     feedDataFile,
                                              String   encoding)
        throws CurnException;

}
