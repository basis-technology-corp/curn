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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.clapper.util.logging.Logger;

/**
 * This class provides a factory for retrieving a specific
 * <tt>OutputHandler</tt> implementation.
 *
 * @see OutputHandler
 *
 * @version <tt>$Revision$</tt>
 */
public class OutputHandlerFactory
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static final Logger log = new Logger (OutputHandlerFactory.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private OutputHandlerFactory()
    {
        // Can't be instantiated directly.
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get an instance of the named <tt>OutputHandler</tt> class. This
     * method loads the specified class, verifies that it conforms to the
     * {@link OutputHandler} interface, instantiates it (via its default
     * constructor), and returns the resulting <tt>OutputHandler</tt> object.
     *
     * @param cls  the class for the output handler
     *
     * @return an <tt>OutputHandler</tt> object
     *
     * @throws CurnException Error instantiating class. The exception will
     *                       contain the real underlying exception.
     */
    public static OutputHandler getOutputHandler (final Class<?> cls)
        throws CurnException
    {
        try
        {
            log.debug ("Instantiating output handler: " + cls.getName());

            Constructor constructor = cls.getConstructor();
            return (OutputHandler) constructor.newInstance();
        }

        catch (NoSuchMethodException ex)
        {
            throw new CurnException (ex);
        }

        catch (InvocationTargetException ex)
        {
            throw new CurnException (ex);
        }

        catch (InstantiationException ex)
        {
            throw new CurnException (ex);
        }

        catch (IllegalAccessException ex)
        {
            throw new CurnException (ex);
        }
    }

    /**
     * Get an instance of the named <tt>OutputHandler</tt> class. This
     * method loads the specified class, verifies that it conforms to the
     * {@link OutputHandler} interface, instantiates it (via its default
     * constructor), and returns the resulting <tt>OutputHandler</tt> object.
     *
     * @param className  the class name
     *
     * @return an <tt>OutputHandler</tt> object
     *
     * @throws CurnException Error instantiating class. The exception will
     *                       contain the real underlying exception.
     */
    public static OutputHandler getOutputHandler (final String className)
        throws CurnException
    {
        try
        {
            return getOutputHandler (Class.forName (className));
        }

        catch (ClassNotFoundException ex)
        {
            throw new CurnException (ex);
        }
    }
}
