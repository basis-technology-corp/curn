/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.parser.minirss;

import org.clapper.util.text.Unicode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @version <tt>$Revision$</tt>
 */
class ElementStackEntry
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private String        elementName     = null;
    private StringBuffer  charBuffer      = null;
    private Object        container       = null;
    private Throwable     allocationPoint = null;

    private static Pattern demoronizePattern = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    ElementStackEntry (String elementName)
    {
        this (elementName, null);
    }

    ElementStackEntry (String elementName, Object container)
    {
        this.elementName = elementName;

        if (container != null)
            setContainer (container);

        try
        {
            throw new Throwable ("allocationPoint");
        }

        catch (Throwable ex)
        {
            this.allocationPoint = ex;
        }
    }

    /**
     * Return a string representation of this element.
     *
     * @return the string (the element name, really)n
     *
     * @see #getElementName
     */
    public String toString()
    {
        return getElementName();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the element name
     *
     * @return the element name
     */
    String getElementName()
    {
        return elementName;
    }

    /**
     * Get the buffer that holds character data for the element. The buffer
     * isn't created until the first call to thise method. Note that this
     * method does not translate the character buffer, as
     * {@link #getCharacters()} does. Instead, it returns the raw buffer.
     *
     * @return the character data buffer
     *
     * @see #getCharacters
     */
    StringBuffer getCharBuffer()
    {
        if (charBuffer == null)
            charBuffer = new StringBuffer();

        return charBuffer;
    }

    /**
     * Retrieve the characters that have been parsed so far. This method
     * also performs some kludgy conversions. For instance, it scans the
     * buffered characters and converts embedded entity codes for the
     * Windows 1252 character codes. Some sites include entity codes that
     * are Windows-specific, despite the fact that the values being encoded
     * don't correspond to valid Unicode or even ISO Latin 1 characters.
     * (So-called smart quotes are one example, as are the em-dash and
     * en-dash.)
     *
     * @return the possibly translated character stream
     *
     * @see #getCharBuffer
     */
    String getCharacters()
    {
        return demoronize (getCharBuffer ().toString());
    }

    /**
     * Set the object that contains the parsed data.
     *
     * @param container  the container object
     *
     * @see #getContainer
     */
    void setContainer (Object container)
    {
        this.container = container;
    }

    /**
     * Set the object that will contain the parsed data.
     *
     * @return the container object
     *
     * @see #getContainer
     */
    Object getContainer()
    {
        return container;
    }

    /**
     * Get the exception that captures the stack trace for this object's
     * allocation point.
     *
     * @return the allocation point
     */
    Throwable getAllocationPoint()
    {
        return allocationPoint;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Convert (a) bogus entity escapes for Windows 1252-specific characters,
     * (b) other entity escapes that are legal but not recognized by the XML
     * parser.
     *
     * @param s   string to convert
     *
     * @return the converted string
     */
    private String demoronize (String s)
    {
        StringBuffer buf = new StringBuffer();

        synchronized (ElementStackEntry.class)
        {
            try
            {
                if (demoronizePattern == null)
                    demoronizePattern = Pattern.compile ("&(#?[^; \t]+);");
            }

            catch (PatternSyntaxException ex)
            {
                // If this happens, there's a code bug.

                assert (false);
            }
        }

        Matcher matcher = null;

        synchronized (ElementStackEntry.class)
        {
            matcher = demoronizePattern.matcher (s);
        }

        for (;;)
        {
            String match = null;
            String preMatch = null;
            String postMatch = null;

            if (! matcher.find())
                break;

            match = matcher.group (1);
            preMatch = s.substring (0, matcher.start (1) - 1);
            postMatch = s.substring (matcher.end (1) + 1);

            if (preMatch != null)
                buf.append (preMatch);

            if (match.charAt (0) != '#')
            {
                buf.append ('&');
                buf.append (match);
                buf.append (';');
            }
                
            else
            {
                int code = -1;

                try
                {
                    code = Integer.parseInt (match.substring (1));
                }

                catch (NumberFormatException ex)
                {
                    code = -1;
                }

                switch (code)
                {
                    // Windows 1252 character set escapes

                    case 0x85:      // ellipsis
                        buf.append ("...");
                        break;

                    case 0x91:      // smart open single quote
                        buf.append (Unicode.LEFT_SINGLE_QUOTE);
                        break;

                    case 0x92:      // smart close single quote
                        buf.append (Unicode.RIGHT_SINGLE_QUOTE);
                        break;

                    case 0x93:      // smart open double quote
                        buf.append (Unicode.LEFT_DOUBLE_QUOTE);
                        break;

                    case 0x94:      // smart close double quote
                        buf.append (Unicode.RIGHT_DOUBLE_QUOTE);
                        break;

                    case 0x96:      // em dash
                        //buf.append (Unicode.EM_DASH);
                        buf.append ("--");
                        break;

                    case 0x97:      // en dash
                        buf.append (Unicode.EN_DASH);
                        break;

                    case 0x98:      // tilde (~)
                        buf.append ('~');
                        break;

                    case 0x99:      // trademark
                        buf.append (Unicode.TRADEMARK);
                        break;

                    default:
                        // Is this a valid Unicode sequence?

                        if (Character.isDefined ((char) code))
                        {
                            buf.append ((char) code);
                        }

                        else
                        {
                            // Put it back.

                            buf.append ('&');
                            buf.append (match);
                            buf.append (';');
                        }
                    break;
                }
            }

            if (postMatch == null)
                break;

            s = postMatch;
            matcher.reset (s);
        }

        if (s.length() > 0)
            buf.append (s);

        return buf.toString();
    }
}
