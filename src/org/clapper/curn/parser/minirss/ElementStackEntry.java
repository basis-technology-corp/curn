/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

import org.clapper.util.text.Unicode;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcherInput;

/**
 *
 * @version <tt>$Revision$</tt>
 */
class ElementStackEntry
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private String        elementName = null;
    private StringBuffer  charBuffer  = null;
    private Object        container   = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    ElementStackEntry (String elementName)
    {
        this.elementName = elementName;
    }

    ElementStackEntry (String elementName, Object container)
    {
        this.elementName = elementName;
        setContainer (container);
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
        return demoronize (getCharBuffer().toString());
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
        Perl5Compiler compiler = new Perl5Compiler();
        Perl5Matcher  matcher  = new Perl5Matcher();
        Pattern       pattern  = null;
        StringBuffer  buf      = new StringBuffer();

        try
        {
            pattern = compiler.compile ("^(.*)&#([0-9]+);(.*)$");
        }

        catch (MalformedPatternException ex)
        {
            // If this happens, there's a code bug.

            throw new IllegalStateException (ex.toString());
        }

        buf.setLength (0);
        buf.append (s);

        PatternMatcherInput input = new PatternMatcherInput (s);

        while (matcher.contains (input, pattern))
        {
            MatchResult result = matcher.getMatch();

            buf.setLength (0);
            buf.append (result.group (1));
            int code = Integer.parseInt (result.group (2));
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

                case 0x96:      // en dash
                    buf.append (Unicode.EM_DASH);
                    break;

                case 0x97:      // em dash
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
                        // Put it back, but with a different escape
                        // sequence, so it doesn't match the regular
                        // expression.

                        buf.append ('[');
                        buf.append (String.valueOf (code));
                        buf.append (']');
                    }
                    break;
            }

            buf.append (result.group (3));
            input = new PatternMatcherInput (buf.toString());
        }

        if (buf.length() > 0)
            s = buf.toString();

        return s;
    }
}
