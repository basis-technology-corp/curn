package org.clapper.rssget.parser.minirss;

import java.io.*;
import java.util.*;

public class tester
{
    public static void main (String args[]) throws Throwable
    {
        MiniRSSParser parser = new MiniRSSParser();

        Channel channel = parser.parse (new File (args[0]));

        if (channel != null)
        {
            System.out.println ("Channel title: " + channel.getTitle());
            System.out.println ("Channel link:  " + channel.getLink());
            System.out.println ("RSS version:   " + channel.getRSSFormat());

            Date date = channel.getPublicationDate();
            if (date != null)
                System.out.println ("Channel date:  " + date);

            for (Iterator it = channel.getItems().iterator(); it.hasNext(); )
            {
                Item item = (Item) it.next();

                System.out.println ();
                System.out.println ("Item title: " + item.getTitle());
                System.out.println ("Item link:  " + item.getLink());

                String s = item.getDescription();
                if (s != null)
                    System.out.println ("Item desc:  " + s);

                date = channel.getPublicationDate();
                if (date != null)
                    System.out.println ("Item date:  " + date);
            }
        }

        System.exit (0);
    }
}
