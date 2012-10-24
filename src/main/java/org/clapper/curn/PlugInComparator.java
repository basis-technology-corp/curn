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
     * @see PlugIn#getPlugInSortKey
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
