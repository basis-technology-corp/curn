/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

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
     * isn't created until the first call to thise method.
     *
     * @return the character data buffer
     */
    StringBuffer getCharBuffer()
    {
        if (charBuffer == null)
            charBuffer = new StringBuffer();

        return charBuffer;
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
}
