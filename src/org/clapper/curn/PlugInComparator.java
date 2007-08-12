/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2007 Brian M. Clapper."

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

package org.clapper.curn;

import java.util.Comparator;

/**
 * Compares plug-in classes based on their declared sort keys.
 *
 * @version <tt>$Revision$</tt>
 */
public class PlugInComparator implements Comparator<PlugIn>
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of <tt>PlugInComparator</tt>
     */
    PlugInComparator()
    {
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Compare two plug-in classes, using their declared sort keys.
     *
     * @param plugIn1  first plug-in
     * @param plugIn2  second plug-in
     *
     * @return < 0 if <tt>plugIn1</tt> is less than <tt>plugIn2</tt>,
     *         = 0 if both plug-ins have the same sort key,
     *         > 0 if <tt>plugIn1</tt> is greater than <tt>plugIn2</tt>
     *
     * @see PlugIn#getSortKey
     */
    public int compare(final PlugIn plugIn1, final PlugIn plugIn2)
    {
        return plugIn1.getPlugInSortKey()
                      .compareToIgnoreCase(plugIn2.getPlugInSortKey());
    }

    /**
     * Determine whether this comparator is equal to another object
     *
     * @param o the object to compare
     *
     * @return <tt>true</tt> if <tt>o</tt> is an instance of this class,
     *         <tt>false</tt> otherwise
     */
    @Override
    public boolean equals(final Object o)
    {
        return (o instanceof PlugInComparator);
    }

    /**
     * Get the hash code for this object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /*----------------------------------------------------------------------*\
                               Protected Methods
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Private Methods
    \*----------------------------------------------------------------------*/
}
