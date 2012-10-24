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


package org.clapper.curn.parser;

import org.clapper.util.misc.MIMETypeUtil;
import org.clapper.util.io.FileUtil;
import org.clapper.util.logging.Logger;

import java.net.URL;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Common utility routines that can be used by all parser implementations.
 *
 * @version <tt>$Revision$</tt>
 */
public final class ParserUtil
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default MIME type for RSS feeds, if we can't figure one out.
     */
    public static final String RSS_MIME_TYPE = "text/xml";

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
            // RFC822 dates are US-centric
            new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss z", Locale.US),
            new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss", Locale.US),
            new SimpleDateFormat ("EEE d MMM yyyy HH:mm:ss", Locale.US)
        };
    };

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private static Pattern w3cTZPattern = null;
    private static final Logger log = new Logger (ParserUtil.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor.
     */
    private ParserUtil()
    {
        // Cannot be instantiated.
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Try to parse a date using as many methods as possible, until one
     * works.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable or if the
     *         date string is empty or null
     */
    public static Date parseDate (String sDate)
    {
        Date result = null;

        if ((result = parseRFC822Date (sDate)) == null)
            result = parseW3CDate (sDate);

        return result;
    }

    /**
     * Parse an RFC 822-style date string.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable or if the
     *         date string is empty or null
     */
    public static Date parseRFC822Date (String sDate)
    {
        Date result = null;

        if ((sDate != null) && (sDate.length() > 0))
        {
            // DateFormat objects are not synchronized.

            synchronized (RFC822_DATE_FORMATS)
            {
                result = parseDate(sDate,
                                   RFC822_DATE_FORMATS,
                                   TimeZone.getDefault());
            }
        }

        return result;
    }

    /**
     * Parse a W3C date string. Not comprehensive.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable or if the
     *         date string is empty or null
     */
    public static Date parseW3CDate (String sDate)
    {
        Date result = null;

        if ((sDate != null) && (sDate.length() > 0))
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

            // Now, parse the date. NOTE: DateFormat objects are not
            // synchronized.

            synchronized (W3C_DATE_FORMATS)
            {
                result = parseDate(sDate, W3C_DATE_FORMATS, timeZone);
            }
        }

        return result;
    }

    /**
     * Used to process the characters found between two XML elements, this
     * method removes any leading and trailing white space, including
     * newlines. Embedded newlines are mapped to spaces.
     *
     * @param ch      the array of characters to process
     * @param start   the start position in the array
     * @param length  the number of characters to read from the array
     *
     * @return the resulting string
     *
     * @see #normalizeCharacterData(char[],int,int,StringBuffer)
     * @see #normalizeCharacterData(String)
     */
    public static String normalizeCharacterData (char[] ch,
                                                 int    start,
                                                 int    length)
    {
        StringBuffer buf = new StringBuffer();

        normalizeCharacterData (ch, start, length, buf);

        return buf.toString();
    }

    /**
     * Used to process the characters found between two XML elements, this
     * method removes any leading and trailing white space, including
     * newlines. Embedded newlines are mapped to spaces.
     *
     * @param s   the string to process
     *
     * @return the resulting string
     *
     * @see #normalizeCharacterData(char[],int,int,StringBuffer)
     * @see #normalizeCharacterData(String)
     */
    public static String normalizeCharacterData (String s)
    {
        if (s != null)
            s = normalizeCharacterData(s.toCharArray(), 0, s.length());

        return s;
    }

    /**
     * Used to process the characters found between two XML elements, this
     * method removes any leading and trailing white space, including
     * newlines. Embedded newlines are mapped to spaces.
     *
     * @param ch      the array of characters to process
     * @param start   the start position in the array
     * @param length  the number of characters to read from the array
     * @param buf     where to append the resulting characters
     *
     * @see #normalizeCharacterData(char[],int,int)
     * @see #normalizeCharacterData(String)
     */
    public static void normalizeCharacterData (char[]       ch,
                                               int          start,
                                               int          length,
                                               StringBuffer buf)
    {
        boolean lastWasNewline = false;

        int end = start + length;
        while (start < end)
        {
            char c = ch[start++];

            // Substitute a space for each newline sequence. Be sure to
            // handle all combinations of newline sequences (e.g., \n, \r,
            // \r\n, \n\r). We want just one space to replace a given
            // newline sequence. The easiest solution is to replace all
            // consecutive newline (\n or \r) characters with one space.
            // This has the effect of nuking blank lines, but blank lines
            // are meaningless within character data anyway, at least in
            // this context.

            if ((c == '\n') || (c == '\r') || (start == 0))
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


    /**
     * Find the {@link RSSLink} object with a specific MIME type and one of
     * a set of link types.
     *
     * @param links     collection of {@link RSSLink} objects to search
     * @param mimeType  the desired MIME type
     * @param linkTypes one or more link types that are acceptable
     *
     * @return the link, or null no matches were found
     */
    public static RSSLink findMatchingLink (Collection<RSSLink> links,
                                            String              mimeType,
                                            RSSLink.Type ...    linkTypes)
    {
        RSSLink result = null;

        outerLoop:
        for (RSSLink link : links)
        {
            if (link.getMIMEType().equals (mimeType))
            {
                // MIME type matches. See if there's a match on the link type.

                RSSLink.Type linkType = link.getLinkType();
                for (RSSLink.Type desiredLinkType : linkTypes)
                {
                    if (desiredLinkType == linkType)
                    {
                        result = link;
                        break outerLoop;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Find the first {@link RSSLink} object with a specific link type.
     *
     * @param links     collection of {@link RSSLink} objects to search
     * @param linkType  the desired link type
     *
     * @return the link, or null no matches were found
     */
    public static RSSLink findMatchingLink (Collection<RSSLink> links,
                                            RSSLink.Type        linkType)
    {
        RSSLink result = null;

        for (RSSLink link : links)
        {
            if (link.getLinkType() == linkType)
            {
                result = link;
                break;
            }
        }

        return result;
    }

    /**
     * Find the first {@link RSSLink} object with a specific MIME type.
     *
     * @param links     collection of {@link RSSLink} objects to search
     * @param mimeType  the desired MIME type
     *
     * @return the link, or null no matches were found
     */
    public static RSSLink findMatchingLink (Collection<RSSLink> links,
                                            String              mimeType)
    {
        RSSLink result = null;

        for (RSSLink link : links)
        {
            if (link.getMIMEType().equals (mimeType))
            {
                result = link;
                break;
            }
        }

        return result;
    }

    /**
     * Get the MIME type for a link from an RSS feed.
     *
     * @param url  The URL
     *
     * @return the MIME type, or a default one
     */
    public static String getLinkMIMEType (URL url)
    {
        String mimeType  = RSS_MIME_TYPE;
        String extension = FileUtil.getFileNameExtension (url.getPath());

        if (extension != null)
        {
            mimeType = MIMETypeUtil.MIMETypeForFileExtension (extension,
                                                              RSS_MIME_TYPE);
        }

        return mimeType;
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
    private static Date parseDate (String        sDate,
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

            catch (Exception ex)
            {
                log.error ("Unexpected exception while parsing date \"" +
                           sDate + "\" using format \"" +
                           formats[i].toString() + "\"",
                           ex);
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
    private static TimeZone parseW3CTimeZone (String tz)
    {
        TimeZone timeZone = null;

        synchronized (ParserUtil.class)
        {
            try
            {
                w3cTZPattern = Pattern.compile ("^[+-][0-9][0-9]:[0-9][0-9]$");
            }

            catch (PatternSyntaxException ex)
            {
                // Should not happen.

                assert (false);
            }
        }

        // Do we have a time zone?

        switch (tz.charAt (0))
        {
            case 'Z':
                timeZone = TimeZone.getTimeZone ("GMT");
                break;

            case '-':
            case '+':
                // +hh:mm -hh:mm

                Matcher matcher = w3cTZPattern.matcher (tz);
                if (matcher.matches())
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
