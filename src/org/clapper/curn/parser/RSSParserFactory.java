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


package org.clapper.curn.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class provides a factory for retrieving a specific RSS parser
 * implementation.
 *
 * @see RSSParser
 * @see RSSChannel
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserFactory
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private RSSParserFactory()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the named RSS parser. This method loads the specified class,
     * verifies that it conforms to the {@link RSSParser} interface,
     * instantiates it (via its default constructor), and returns the
     * resulting <tt>RSSParser</tt> object.
     *
     * @param className  the class name
     *
     * @return an <tt>RSSParser</tt> object
     *
     * @throws RSSParserException        Error instantiating class. The
     *                                   exception will contain (i.e., nest)
     *                                   the real underlying exception.
     */
    public static RSSParser getRSSParser (String className)
        throws RSSParserException
    {
        try
        {
            Class<?> parserClass = Class.forName (className);
            Constructor constructor = parserClass.getConstructor();
            return (RSSParser) constructor.newInstance();
        }

        catch (ClassNotFoundException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (NoSuchMethodException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (InvocationTargetException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (InstantiationException ex)
        {
            throw new RSSParserException (ex);
        }

        catch (IllegalAccessException ex)
        {
            throw new RSSParserException (ex);
        }

    }
}
