/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.util.Collection;

/**
 * This interface defines the additional methods that must be supported by
 * a class that sends output via email. The main reason this interface
 * exists is to permit conditional building of the actual implementing
 * class, which depends on third-party APIs that might not be present.
 *
 * @see rssget
 *
 * @version <tt>$Revision$</tt>
 */
public interface EmailOutputHandler extends OutputHandler
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a handler to the list of handlers that produce output to be attached
     * to the email message. THis method must be called after <tt>init()</tt>.
     *
     * @param outputHandler  the <tt>OutputHandler</tt> to wrap inside this
     *                       handler
     *
     * @throws RSSGetException error adding handler
     *
     * @see OutputHandler#init
     */
    public void addOutputHandler (OutputHandler outputHandler)
        throws RSSGetException;

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws RSSGetException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws RSSGetException;
}
