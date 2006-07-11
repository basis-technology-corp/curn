/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

/**
 *
 * @version <tt>$Revision$</tt>
 */
public class BootstrapException extends Exception
{

    /**
     * Creates a new instance of <tt>BootstrapException</tt> without a
     * detail message.
     */
    public BootstrapException()
    {
    }

    /**
     * Constructs an instance of <tt>BootstrapException</tt> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public BootstrapException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs an instance of <tt>BootstrapException</tt> with the specified
     * detail message and nested exception.
     *
     * @param msg the detail message.
     * @param ex  the real cause of the exception
     */
    public BootstrapException(String msg, Throwable ex)
    {
        super(msg);
        initCause (ex);
    }
}
