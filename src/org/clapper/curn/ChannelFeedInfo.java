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

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.ResourceBundle;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.config.ConfigurationException;

/**
 * Used internally to associate a parsed <tt>RSSChannel</tt> with its
 * <tt>FeedInfo</tt> object.
 *
 * @version <tt>$Revision$</tt>
 */
class ChannelFeedInfo
{
    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    private FeedInfo    feedInfo;
    private RSSChannel  channel;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>ChannelFeedInfo</tt> object that associates
     * parsed channel data with a <tt>FeedInfo</tt> object.
     *
     * @param feedInfo  the <tt>FeedInfo</tt> object that describes the feed
     * @param channel   the parsed channel data
     */
    ChannelFeedInfo (FeedInfo feedInfo, RSSChannel channel)
    {
        this.feedInfo = feedInfo;
        this.channel  = channel;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the <tt>RSSChannel</tt> object contained in this object.
     *
     * @return the <tt>RSSChannel</tt> object
     */
    RSSChannel getChannel()
    {
        return channel;
    }

    /**
     * Get the <tt>FeedInfo</tt> object contained in this object.
     *
     * @return the <tt>FeedInfo</tt> object
     */
    FeedInfo getFeedInfo()
    {
        return feedInfo;
    }
}
