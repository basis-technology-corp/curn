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

import org.clapper.util.io.WordWrapWriter;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class tester
{
    private static WordWrapWriter out = new WordWrapWriter (System.out);

    private tester()
    {
    }

    public static void main (String args[])
    {
        try
        {
            runTest (args);
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit (1);
        }

        System.exit (0);
    }

    private static void runTest (String args[])
        throws ClassNotFoundException,
               NoSuchMethodException,
               InvocationTargetException,
               IllegalAccessException,
               InstantiationException,
               FileNotFoundException,
               IOException,
               RSSParserException
    {
        if (args.length < 2)
        {
            System.err.println ("Usage: java " + tester.class.getName() +
                                " parserClass XMLfile [XMLfile] ...");
            System.exit (1);
        }

        Class parserClass = Class.forName (args[0]);
        Constructor constructor = parserClass.getConstructor();
        RSSParser parser = (RSSParser) constructor.newInstance();

        for (int i = 1; i < args.length; i++)
        {
            out.println ();
            out.println (args[i] + ":");
            out.println ();

            FileInputStream is = new FileInputStream (args[i]);
            RSSChannel channel = parser.parseRSSFeed (is, null);
            if (channel != null)
                show (channel);
        }
    }

    private static void show (RSSChannel channel)
    {
        out.println ("Channel title: " + channel.getTitle());
        out.println ("Channel link:  " + channel.getLink());
        out.println ("RSS version:   " + channel.getRSSFormat());
        out.print   ("Channel date:  ");

        Date date = channel.getPublicationDate();
        if (date != null)
            out.println (date);
        else
            out.println ("<null>");

        for (Iterator it = channel.getItems().iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();

            out.println ();
            out.println ("Item title:  " + item.getTitle());

            out.print ("Item author: ");
            String s = item.getAuthor();
            out.println ((s == null) ? "<null>" : s);

            out.println ("Item link:   " + item.getLink());

            out.print ("Item date:   ");
            date = item.getPublicationDate();
            s = "<null>";
            if (date != null)
                s = date.toString();
            out.println (s);

            s = item.getSummary();
            if (s != null)
            {
                out.setPrefix ("Item desc:   ");
                out.println (s);
                out.setPrefix (null);
            }
        }
    }
}
