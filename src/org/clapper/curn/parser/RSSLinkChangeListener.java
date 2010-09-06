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


package org.clapper.curn.parser;

import java.net.URL;

/**
 * Implements a change listener for <tt>RSSLink</tt> objects. An
 * <tt>RSSLink</tt> object can be associated with change listeners that
 * fire if the <tt>RSSLink</tt> object is changed. This capability exists
 * primarily to permit underlying parser adapters to detect when a link
 * has been changed (e.g., by a plug-in).
 *
 * @version <tt>$Revision$</tt>
 */
public interface RSSLinkChangeListener
{
    /**
     * Fired when the URL is changed.
     *
     * @param rssLink  the changed object
     * @param oldURL   the old URL
     * @param newURL   the new URL
     */
    public void onURLChange(RSSLink rssLink, URL oldURL, URL newURL);

    /**
     * Fired when the link type is changed.
     *
     * @param rssLink  the changed object
     * @param oldType  the old link type
     * @param newType  the new link type
     */
    public void onLinkTypeChange(RSSLink      rssLink,
                                 RSSLink.Type oldType,
                                 RSSLink.Type newType);

    /**
     * Fired when the MIME type changes.
     *
     * @param rssLink  the changed object
     * @param oldType  the old MIME type
     * @param newType  the new MIME type
     */
    public void onMIMETypeChange(RSSLink rssLink,
                                 String  oldType,
                                 String  newType);
}
