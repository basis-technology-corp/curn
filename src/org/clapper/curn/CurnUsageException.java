/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
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


package org.clapper.curn;

/**
 * Thrown to indicate a <i>curn</i> usage exception (usually, something
 * under the user's control), rather than a system problem.
 *
 * @version <tt>$Revision$</tt>
 */
public class CurnUsageException extends CurnException
{
    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public CurnUsageException()
    {
        super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public CurnUsageException(Throwable exception)
    {
        super(exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public CurnUsageException(String message)
    {
        super(message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public CurnUsageException(String message, Throwable exception)
    {
        super(message, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CurnUsageException(String,String,String,Object[])} constructor,
     * with a null pointer for the <tt>Object[]</tt> parameter.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     *
     * @see #CurnUsageException(String,String,String,Object[])
     */
    public CurnUsageException(String bundleName,
                              String messageKey,
                              String defaultMsg)
    {
        super(bundleName, messageKey, defaultMsg);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CurnUsageException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt> parameter.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     *
     * @see #CurnUsageException(String,String,String,Object[],Throwable)
     */
    public CurnUsageException(String   bundleName,
                              String   messageKey,
                              String   defaultMsg,
                              Object[] msgParams)
    {
        super(bundleName, messageKey, defaultMsg, msgParams);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling the
     * {@link #CurnUsageException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param exception   the exception to nest
     *
     * @see #CurnUsageException(String,String,String,Object[],Throwable)
     */
    public CurnUsageException(String    bundleName,
                              String    messageKey,
                              String    defaultMsg,
                              Throwable exception)
    {
        this(bundleName, messageKey, defaultMsg, null, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message format (in case the resource bundle can't be
     * found), arguments to be incorporated in the message via
     * <tt>java.text.MessageFormat</tt>, and another exception.
     * Calls to {@link #getMessage(Locale)} will attempt to retrieve the
     * top-most message (i.e., the message from this exception, not from
     * nested exceptions) by querying the named resource bundle. Calls to
     * {@link #printStackTrace(PrintWriter,Locale)} will do the same, where
     * applicable. The message is not retrieved until one of those methods
     * is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     * @param exception   exception to be nested
     *
     * @see #CurnUsageException(String,String,String,Object[])
     */
    public CurnUsageException(String    bundleName,
                              String    messageKey,
                              String    defaultMsg,
                              Object[]  msgParams,
                              Throwable exception)
    {
        super(bundleName, messageKey, defaultMsg, msgParams, exception);
    }
}
