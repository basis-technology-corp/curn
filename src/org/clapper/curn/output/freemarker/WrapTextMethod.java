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

import java.io.StringWriter;

import java.util.List;

import org.clapper.util.io.WordWrapWriter;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker method (put in the FreeMarker data model) that permits a
 * template to wrap plain text via the <tt>WordWrapWriter</tt> class.
 *
 * @version <tt>$Revision$</tt>
 */
class WrapTextMethod implements TemplateMethodModel
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private StringWriter   stringWriter = new StringWriter();
    private WordWrapWriter wrapWriter   = new WordWrapWriter (stringWriter);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>WrapTextMethod</tt> object.
     */
    public WrapTextMethod()
    {
        // Nothing to do.
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
     *               <li> indentation (optional; defaults to 0)
     *               <li> wrap column (optional; defaults to 79)
     *             </ul>
     */
    public TemplateModel exec (List args) throws TemplateModelException
    {
        int totalArgs = args.size();
        StringBuffer buf = stringWriter.getBuffer();

        buf.setLength (0);

        switch (totalArgs)
        {
            case 3:
                String sLineLen = (String) args.get (2);
                try
                {
                    wrapWriter.setLineLength (Integer.parseInt (sLineLen));
                }

                catch (NumberFormatException ex)
                {
                    throw new TemplateModelException ("Bad line length " +
                                                      "value \"" + sLineLen +
                                                      "\"");
                }
                // Fall through intentional

            case 2:
                String sIndent = (String) args.get (1);
                try
                {
                    wrapWriter.setIndentation (Integer.parseInt (sIndent));
                }

                catch (NumberFormatException ex)
                {
                    throw new TemplateModelException ("Bad indentation " +
                                                      "value \"" + sIndent +
                                                      "\"");
                }
                // Fall through intentional

            case 1:
                wrapWriter.println ((String) args.get (0));
                break;

            default:
                throw new TemplateModelException ("Wrong number of arguments");
        }

        // Strip the last trailing newline from the wrapped string and return
        // it.

        String s = buf.deleteCharAt (buf.length() - 1).toString();
        return new SimpleScalar (s);
    }
}
