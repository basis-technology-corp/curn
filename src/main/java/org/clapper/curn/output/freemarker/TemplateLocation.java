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


package org.clapper.curn.output.freemarker;

import org.clapper.curn.Constants;
import org.clapper.curn.CurnException;

import org.clapper.util.text.TextUtil;

/**
 * @version <tt>$Revision$</tt>
 */
public class TemplateLocation implements Comparable<TemplateLocation>
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String NAME_TOKEN_DELIM = "$";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private TemplateType type;
    private String       location;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>TemplateLocation</tt> object.
     *
     * @param type      the type
     * @param location  the location (URL, path, etc.) for the file
     */
    public TemplateLocation (TemplateType type, String location)
    {
        this.type     = type;
        this.location = location;
    }

    /**
     * Construct a new <tt>TemplateLocation</tt> object from a previously
     * constructed <tt>TemplateLocation</tt> name (i.e., as returned by
     * {@link #getName}).
     *
     * @param name  the name
     *
     * @throws CurnException bad name
     */
    public TemplateLocation (String name)
        throws CurnException
    {
        // Can't use String.split(), because the delimiter is a regular
        // expression metacharacter.

        String[] tokens = TextUtil.split (name, NAME_TOKEN_DELIM);

        if (tokens.length != 2)
        {
            throw new CurnException (Constants.BUNDLE_NAME,
                                     "TemplateLocation.tooManyTokensInName",
                                     "Cannot decode TemplateLocation from " +
                                     "string \"{0}\":  String should have 2 " +
                                     "fields, but it has {1}.",
                                     new Object[]
                                     {
                                         name,
                                         tokens.length
                                     });
        }

        this.type     = TemplateType.fromString (tokens[0]);
        this.location = tokens[1];
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Compare this object and another object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     *
     * @param o  the other object to be compared with this one
     *
     * @return a negative integer, zero, or a positive integer as this
     *         object is less than, equal to, or greater than <tt>o</tt>.
     */
    public int compareTo (TemplateLocation o)
    {
        int cmp = (this.type.ordinal() - o.type.ordinal());

        if (cmp == 0)
            cmp = this.location.compareTo (o.location);

        return cmp;
    }

    /**
     * Compare this object and another object for equality.
     *
     * @param o  the other object to be compared with this one
     *
     * @return <tt>true</tt> if the other object is equivalent to this one,
     *         <tt>false</tt> otherwise.
     */
    public boolean equals (Object o)
    {
        return (o instanceof TemplateLocation)
            ? (compareTo ((TemplateLocation) o) == 0)
            : false;
    }

    /**
     * Get a string representation of this object.
     *
     * @return the string representation
     */
    public String toString()
    {
        return getName();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a FreeMarker-suitable name for this template.
     *
     * @return  the name
     */
    public String getName()
    {
        StringBuilder buf = new StringBuilder();

        buf.append (type.toString());
        buf.append (NAME_TOKEN_DELIM);
        buf.append (location);

        return buf.toString();
    }

    /**
     * Get the type of this template location.
     *
     * @return the type
     *
     * @see TemplateType
     */
    public TemplateType getType()
    {
        return this.type;
    }

    /**
     * Get the location string for this template location. How the returned
     * string is interpreted depends on the type.
     *
     * @return the location string
     *
     * @see #getType
     */
    public String getLocation()
    {
        return this.location;
    }
}
