/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.curn.output.freemarker;

import org.clapper.curn.CurnException;
import org.clapper.curn.util.Util;

/**
 * @version <tt>$Revision$</tt>
 */
public enum TemplateType
{
    /*----------------------------------------------------------------------*\
                                Enum Values
    \*----------------------------------------------------------------------*/

    URL,
    CLASSPATH,
    FILE;

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the appropriate enumerated value from a string. The string must
     * have been returned by a previous call to the <tt>toString()</tt>
     * method.
     *
     * @return the enumerated value
     *
     * @throws CurnException  bad string value
     */
    static TemplateType fromString (String s)
        throws CurnException
    {
        for (TemplateType t : TemplateType.values())
        {
            if (t.toString().equals (s))
                return t;
        }

        throw new CurnException (Util.BUNDLE_NAME,
                                 "TemplateType.badString",
                                 "Cannot decode TemplateType from string "
                               + "\"{0}\"",
                                 new Object[] {s});
    }
};
