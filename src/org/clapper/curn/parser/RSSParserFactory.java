/*---------------------------------------------------------------------------*\
  $Id$
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
                                 Constants
    \*----------------------------------------------------------------------*/

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
            Class parserClass = Class.forName (className);
            Constructor constructor = parserClass.getConstructor (null);
            return (RSSParser) constructor.newInstance (null);
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
