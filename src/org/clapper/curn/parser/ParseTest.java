/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn.parser;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.text.TextUtil;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class ParseTest
{
    private static WordWrapWriter out = new WordWrapWriter (System.out);

    private ParseTest()
    {
        // Nothing to do
    }

    public static void main (String args[])
    {
        try
        {
            runTest (args);
        }

        catch (Exception ex)
        {
            ex.printStackTrace();   // NOPMD
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
            System.err.println ("Usage: java " + ParseTest.class.getName() +
                                " parserClass XMLfile [XMLfile] ...");
            System.exit (1);
        }

        Class<?> parserClass = Class.forName (args[0]);
        Constructor constructor = parserClass.getConstructor();
        RSSParser parser = (RSSParser) constructor.newInstance();

        for (int i = 1; i < args.length; i++)
        {
            out.println ();
            out.println (args[i] + ":");
            out.println ();

            File f = new File (args[i]);
            FileInputStream is = new FileInputStream (f);
            RSSChannel channel = parser.parseRSSFeed (f.toURI().toURL(),
                                                      is,
                                                      null);
            if (channel != null)
                show (channel);
        }
    }

    private static void show (RSSChannel channel)
    {
        out.println ("Channel title:  " + channel.getTitle());
        out.println ("Channel link:   " + channel.getURL());

        Collection<String> authors = channel.getAuthors();        
        String s = null;
        if (authors != null)
            s = TextUtil.join (authors, ", ");

        out.println ("Channel author: " + ((s == null) ? "<null>" : s));
        out.println ("RSS version:    " + channel.getRSSFormat());
        out.print   ("Channel date:   ");

        Date date = channel.getPublicationDate();
        if (date != null)
            out.println (date);
        else
            out.println ("<null>");

        for (Iterator it = channel.getItems().iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();

            out.println ();
            out.println ("Item title:    " + item.getTitle());

            out.print ("Item categories: ");
            Collection<String> categories = item.getCategories();
            if ((categories == null) || (categories.size() == 0))
                out.println ("<none>");
            else
                out.println (TextUtil.join (categories, ", "));

            out.print ("Item author(s):  ");
            authors = item.getAuthors();
            if ((authors == null) || (authors.size() == 0))
                out.println ("<none>");
            else
                out.println (TextUtil.join (authors, ", "));

            out.println ("Item link:     " + item.getURL());

            out.print ("Item date:     ");
            date = item.getPublicationDate();
            s = "<null>";
            if (date != null)
                s = date.toString();
            out.println (s);

            s = item.getSummary();
            if (s != null)
            {
                out.setPrefix ("Item desc:     ");
                out.println (s);
                out.setPrefix (null);
            }
        }
    }
}
