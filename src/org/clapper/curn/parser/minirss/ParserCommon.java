/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Stack;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;

/**
 * <p>Common logic and data shared between <tt>V1Parser</tt> and
 * <tt>V2Parser</tt>.
 *
 * @version <tt>$Revision$</tt>
 */
class ParserCommon extends DefaultHandler
{
    /*----------------------------------------------------------------------*\
			     Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * A date format for parsing RFC 822-style dates.
     */
    private static DateFormat[] RFC822_DATE_FORMATS;

    /**
     * Patterns for the more common W3C date/time formats.
     */
    private static DateFormat[] W3C_DATE_FORMATS;

    static
    {
        W3C_DATE_FORMATS = new SimpleDateFormat[]
        {
            new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm"),
            new SimpleDateFormat ("yyyy-MM-dd"),
            new SimpleDateFormat ("yyyy-MM"),
            new SimpleDateFormat ("yyyy")
        };

        RFC822_DATE_FORMATS = new SimpleDateFormat[]
        {
            new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss z"),
            new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss")
        };
    };

    /*----------------------------------------------------------------------*\
                          Protected Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The channel being filled.
     */
    protected Channel channel = null;

    /**
     * Element stack. Contains ElementStackEntry objects.
     */
    protected Stack elementStack = new Stack();

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Saves the <tt>Channel</tt> object and creates a new
     * element stack.
     */
    protected ParserCommon (Channel channel)
    {
        this.channel = channel;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
                        Overriding XMLReaderAdapter
    \*----------------------------------------------------------------------*/

    /**
     * Handle character data parsed from the XML file.
     *
     * @param ch      characters from the XML document
     * @param start   the start position in the array
     * @param length  the number of characters to read from the array
     *
     * @throws SAXException parsing error
     */
    public void characters (char[] ch, int start, int length)
        throws SAXException
    {
        if (! elementStack.empty())
        {
            // Get the top-most entry on the stack, and put the characters
            // in that entry's character buffer. This strategy allows this
            // method to work even if it's called multiple times for the
            // same XML element--which is permitted by the SAX parser
            // interface.

            ElementStackEntry entry = (ElementStackEntry) elementStack.peek();
            StringBuffer      buf   = entry.getCharBuffer();
            boolean           lastWasNewline = false;

            int end = start + length;
            while (start < end)
            {
                char c = ch[start++];

                // Substitute a space for each newline sequence. Be sure to
                // handle all combinations of newline sequences (e.g.,
                // \n, \r, \r\n, \n\r). We want just one space to replace
                // a given newline sequence. The easiest solution is to
                // replace all consecutive newline (\n or \r) characters with
                // one space. This has the effect of nuking blank lines, but
                // blank lines are meaningless within character data anyway,
                // at least in this context.
                if ((c == '\n') || (c == '\r'))
                {
                    if (! lastWasNewline)
                        buf.append (' ');
                    lastWasNewline = true;
                }

                else
                {
                    lastWasNewline = false;
                    if (! Character.isISOControl (c))
                        buf.append (c);
                }
            }
        }
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse an RFC 822-style date string.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable
     */
    protected Date parseRFC822Date (String sDate)
    {
        return parseDate (sDate, RFC822_DATE_FORMATS, TimeZone.getDefault());
    }

    /**
     * Parse a W3C date string. Not comprehensive.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable
     */
    protected Date parseW3CDate (String sDate)
    {
        TimeZone       timeZone = TimeZone.getDefault();
        int            tzIndex;

        // First, extract the time zone, if present.

        if (((tzIndex = sDate.lastIndexOf ('Z')) != -1) ||
            ((tzIndex = sDate.lastIndexOf ('-')) != -1) ||
            ((tzIndex = sDate.lastIndexOf ('+')) != -1))
        {
            timeZone = parseW3CTimeZone (sDate.substring (tzIndex));
        }

        // Now, parse the date.

        return parseDate (sDate, W3C_DATE_FORMATS, timeZone);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse a date/time using an array of DateFormat objects
     *
     * @param sDate    the date string
     * @param formats  the array of DateFormat objects to try, one by one
     * @param timeZone time zone to use
     *
     * @return the parsed date, or null if not parseable
     */
    private Date parseDate (String        sDate,
                            DateFormat[]  formats,
                            TimeZone      timeZone)
    {
        Date result = null;

        for (int i = 0; i < formats.length; i++)
        {
            formats[i].setTimeZone (timeZone);
            try
            {
                result = formats[i].parse (sDate, new ParsePosition (0));
            }

            catch (NumberFormatException ex)
            {
                result = null;
            }

            if (result != null)
                break;
        }

        return result;
    }

    /**
     * Parse a string into a W3C time zone. Returns the default time zone
     * if the string doesn't parse.
     *
     * @param tz  the alleged time zone string
     *
     * @return a TimeZone
     */
    private TimeZone parseW3CTimeZone (String tz)
    {
        TimeZone timeZone = null;

        // Do we have a time zone?

        switch (tz.charAt (0))
        {
            case 'Z':
                timeZone = TimeZone.getTimeZone ("GMT");
                break;

            case '-':
            case '+':
                // +hh:mm -hh:mm

                Perl5Compiler reCompiler = new Perl5Compiler();
                Perl5Matcher reMatcher = new Perl5Matcher();
                Pattern re = null;

                try
                {
                    re = reCompiler.compile ("^[+-][0-9][0-9]:[0-9][0-9]$");
                }

                catch (MalformedPatternException ex)
                {
                    // Shouldn't happen.

                    throw new IllegalStateException (ex.toString());
                }

                if (reMatcher.contains (tz, re))
                    timeZone = TimeZone.getTimeZone ("GMT" + tz);

                if (timeZone == null)
                    timeZone = TimeZone.getDefault();

                break;

            default:
                // Whatever it is, it's not a time zone. Assume
                // local time zone.

                timeZone = TimeZone.getDefault();
                break;
                        
        }

        return timeZone;
    }
}
