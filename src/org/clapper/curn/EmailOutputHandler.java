/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.Collection;

/**
 * This interface defines the additional methods that must be supported by
 * a class that sends output via email. The main reason this interface
 * exists is to permit conditional building of the actual implementing
 * class, which depends on third-party APIs that might not be present.
 *
 * @see org.clapper.curn.Curn
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
     * @throws CurnException error adding handler
     *
     * @see OutputHandler#init
     */
    public void addOutputHandler (OutputHandler outputHandler)
        throws CurnException;

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws CurnException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws CurnException;
}
