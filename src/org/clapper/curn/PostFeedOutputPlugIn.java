/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

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

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified after <i>curn</i> sends a parsed feed to an
 * {@link OutputHandler}.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PreFeedOutputPlugIn
 * @see PostOutputHandlerFlushPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PostFeedOutputPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called immediately after a parsed feed is passed to an output
     * handler. This method cannot affect the feed's processing. (The time
     * to stop the processing of a feed is in one of the other, preceding
     * phases.) This method will be called multiple times for each feed if
     * there are multiple output handlers.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed.
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @throws CurnException on error
     *
     * @see FeedInfo
     * @see OutputHandler
     */
    public void runPostFeedOutputPlugIn (FeedInfo      feedInfo,
                                         OutputHandler outputHandler)
	throws CurnException;
}
