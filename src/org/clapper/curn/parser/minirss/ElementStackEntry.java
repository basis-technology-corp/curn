/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

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
            throw new Throwable ("allocationPoint");                 // NOPMD
        }

        catch (Throwable ex)                                         // NOPMD
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
