/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.*;

/**
 * This interface defines the methods required to be a verbose messages
 * handler.
 *
 * @see rssget
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public interface VerboseMessagesHandler
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Display a verbose message, if verbosity level is suitably high.
     * is defined for the underlying class.
     *
     * @param level   the level at or above which the message should appear
     * @param msg     the message
     */
    public void verbose (int level, String msg);
}
