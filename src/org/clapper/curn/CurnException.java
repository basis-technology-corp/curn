/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import org.clapper.util.misc.NestedException;

/**
 * <p><tt>RSSGetException</tt> defines a special <tt>Exception</tt> class
 * that permits exceptions to wrap other exceptions. While
 * <tt>RSSGetException</tt> can be used directly, it is most useful as a
 * base class for other exception classes.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSGetException extends NestedException
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public RSSGetException()
    {
	super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public RSSGetException (Throwable exception)
    {
	super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public RSSGetException (String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public RSSGetException (String message, Throwable exception)
    {
	super (message, exception);
    }
}
