/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import org.clapper.util.misc.*;

/**
 * A <tt>RSSParserException</tt> is thrown by the
 * {@link Configuration} class to signify errors in a configuration file.
 *
 * @see NestedException
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserException extends NestedException
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public RSSParserException()
    {
	super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public RSSParserException (Throwable exception)
    {
	super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public RSSParserException (String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public RSSParserException (String message, Throwable exception)
    {
	super (message, exception);
    }
}
