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

import java.util.List;

import org.clapper.util.logging.Logger;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker method (put in the FreeMarker data model) that permits a
 * template to indent plain text, without wrapping it.
 *
 * @see WrapTextMethod
 *
 * @version <tt>$Revision$</tt>
 */
class IndentTextMethod implements TemplateMethodModel
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static Logger log = new Logger (IndentTextMethod.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>IndentTextMethod</tt> object.
     */
    public IndentTextMethod()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Execute the method.
     *
     * @param args the arguments:
     *             <ul>
     *               <li> text to be wrapped (required)
     *               <li> indentation (required)
     *             </ul>
     */
    public TemplateModel exec (List args) throws TemplateModelException
    {
        if (args.size() != 2)
            throw new TemplateModelException ("Wrong number of arguments");

        StringBuilder buf = new StringBuilder();

        String sIndent = (String) args.get (1);
        int indentation = 0;
        try
        {
            indentation = Integer.parseInt (sIndent);
        }

        catch (NumberFormatException ex)
        {
            throw new TemplateModelException ("Bad indentation value \"" +
                                              sIndent + "\"");
        }

        while (indentation-- > 0)
            buf.append (' ');

        buf.append (args.get (0));

        return new SimpleScalar (buf.toString());
    }
}
