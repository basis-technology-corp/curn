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

package org.clapper.curn.parser;

import java.io.*;
import java.util.*;
import java.lang.reflect.Constructor;

public class tester
{
    public static void main (String args[]) throws Throwable
    {
        if (args.length < 2)
        {
            System.err.println ("Usage: java " + tester.class.getName() +
                                " parserClass XMLfile [XMLfile] ...");
            System.exit (1);
        }

        Class parserClass = Class.forName (args[0]);
        Constructor constructor = parserClass.getConstructor (null);
        RSSParser parser = (RSSParser) constructor.newInstance (null);

        for (int i = 1; i < args.length; i++)
        {
            System.out.println ();
            System.out.println (args[i] + ":");
            System.out.println ();

            FileInputStream is = new FileInputStream (args[i]);
            RSSChannel channel = parser.parseRSSFeed (is, null);
            if (channel != null)
                show (channel);
        }

        System.exit (0);
    }

    private static void show (RSSChannel channel)
        throws Throwable
    {
        System.out.println ("Channel title: " + channel.getTitle());
        System.out.println ("Channel link:  " + channel.getLink());
        System.out.println ("RSS version:   " + channel.getRSSFormat());
        System.out.print   ("Channel date:  ");

        Date date = channel.getPublicationDate();
        if (date != null)
            System.out.println (date);
        else
            System.out.println ("<null>");

        for (Iterator it = channel.getItems().iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();

            System.out.println ();
            System.out.println ("Item title: " + item.getTitle());
            System.out.println ("Item link:  " + item.getLink());

            String s = item.getSummary();
            if (s != null)
                System.out.println ("Item desc:  " + s);

            System.out.print ("Item date:  ");
            date = item.getPublicationDate();
            if (date != null)
                System.out.println (date);
            else
                System.out.println ("<null>");
        }
    }
}
