package org.clapper.rssget.parser.minirss;

import java.io.*;
import java.util.*;

public class tester
{
    public static void main (String args[]) throws Throwable
    {
        MiniRSSParser parser = new MiniRSSParser();

        for (int i = 0; i < args.length; i++)
        {
            System.out.println ();
            System.out.println (args[i] + ":");
            System.out.println ();

            Channel channel = parser.parse (new File (args[i]));
            if (channel != null)
                show (channel);
        }

        System.exit (0);
    }

    private static void show (Channel channel)
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
            Item item = (Item) it.next();

            System.out.println ();
            System.out.println ("Item title: " + item.getTitle());
            System.out.println ("Item link:  " + item.getLink());

            String s = item.getDescription();
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
