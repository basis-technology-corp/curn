/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2006 Brian M. Clapper. All rights reserved.

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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import java.io.File;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Main program that bootstraps <i>curn</i> by adding new elements to the
 * classpath on the fly. This utility takes a list of jar files, zip files
 * and/or directories. It loads them all into a class loader and, at the
 * same time, searches any directories (not recursively) for jars and zip
 * files. It then invokes the tool specified on the command line. Usage:
 *
 * <pre>
 * java org.clapper.curn.Bootstrap [jar|zip|dir] ... -- programClassName [args]
 * </pre>
 *
 * Supports the following substitutions in the <tt>[jar|zip|dir]</tt>
 * parameters:
 *
 * <table border="0">
 *   <tr valign="top">
 *     <td><tt>@user.home</tt></td>
 *     <td>Replaced with the value of the "user.home" Java system property.
 *     </td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 */
public class Bootstrap
{
    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    /**
     * Main program
     */
    public static void main (String[] args) throws Throwable
    {
        try
        {
            // Use as few classes as possible.

            int i = 0;
            int endOfArgsIndex = 0;

            while ( (i < args.length) && (! args[i].equals ("--")))
                i++;

            if (i == args.length)
            {
                System.err.println (args[i]);
                usage();
                System.exit (1);
            }

            endOfArgsIndex = i;
            File[] searchItems = new File[endOfArgsIndex];
            String userHome = System.getProperty ("user.home");
            for (i = 0; i < endOfArgsIndex; i++)
            {
                String path = args[i].replaceAll ("@user.home", userHome);
                searchItems[i] = new File (path);
            }

            // Save the command class name and arguments.

            i++;
            String commandClassName = args[i++];
            String commandArgs[] = null;

            if (i < args.length)
            {
                commandArgs = new String[args.length - i];
                System.arraycopy (args, i, commandArgs, 0, commandArgs.length);
            }

            // Expand the search items.

            ArrayList<File> expandedSearchItems = new ArrayList<File>();
            expandSearchItems (searchItems, expandedSearchItems);

            // Add entire set of expanded items to the class path.

            String classPath = System.getProperty ("java.class.path");
            String pathSep =
                (classPath.endsWith (File.pathSeparator) ? ""
                                                         : File.pathSeparator);
            StringBuilder newClassPath = new StringBuilder();
            newClassPath.append (classPath);
            for (File f : expandedSearchItems)
            {
                newClassPath.append (pathSep);
                newClassPath.append (f.getPath());
                pathSep = File.pathSeparator;
            }

            System.setProperty ("java.class.path", newClassPath.toString());

            // Create the class loader.

            ClassLoader classLoader = createClassLoader (expandedSearchItems);

            // Load and run the utility.

            loadAndRun (commandClassName, commandArgs, classLoader);
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            usage();
            System.exit (1);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private static void usage()
    {
        System.out.println ("Usage: java "
                          + Bootstrap.class
                          + " [jar|zip|directory] ... -- "
                          + "programClassName [args]");
    }

    private static ClassLoader createClassLoader (ArrayList<File> searchItems)
        throws MalformedURLException
    {
        ArrayList<URL> urlList = new ArrayList<URL>();

        for (File f : searchItems)
        {
            // URLClassLoader wants directory URLs to end in "/".

            if (f.isDirectory())
            {
                String fileName = f.getName();
                if (! fileName.endsWith ("/"))
                    f = new File (fileName + "/");
            }

            urlList.add (f.toURL());
        }

/*
        // Add the class path.

        String classpath = System.getProperty ("java.class.path");
        if (classpath != null)
        {
            StringTokenizer tok = new StringTokenizer (classpath,
                                                       File.pathSeparator);
            while (tok.hasMoreTokens())
                urlList.add (new File (tok.nextToken()).toURL());
        }
*/

        System.out.println ("Loader will search:" + urlList);
        return new URLClassLoader (urlList.toArray (new URL[urlList.size()]),
                                   ClassLoader.getSystemClassLoader());
    }

    private static void expandSearchItems (File[]          searchItems,
                                           ArrayList<File> expandedItems)
        throws MalformedURLException
    {
        for (int i = 0; i < searchItems.length; i++)
        {
            File f = searchItems[i];
            String fileName = f.getName();

            if (! f.exists())
                continue;

            if (f.isDirectory())
            {
                File[] items = f.listFiles();
                if (items != null)
                    expandSearchItems (items, expandedItems);

                expandedItems.add (f);
            }

            else if (fileName.endsWith (".jar") ||
                     fileName.endsWith (".zip"))
            {
                expandedItems.add (f);
            }
        }
    }

    private static void loadAndRun (String commandClassName,
                                    String[] args,
                                    ClassLoader classLoader)
        throws Throwable
    {
        Class cls = classLoader.loadClass (commandClassName);
        Method mainMethod = cls.getMethod ("main",
                                           new Class[] {String[].class});
        if ((mainMethod.getModifiers() & Modifier.STATIC) == 0)
            throw new Exception (commandClassName + ".main() is not static");

        try
        {
/*
            System.out.print ("+ (java) " + cls);
            if ((args != null) && (args.length > 0))
            {
                for (String arg : args)
                    System.out.print (" " + arg);
            }
            System.out.println();
            System.out.println ("Class loader: " + cls.getClassLoader());
*/
            Thread.currentThread().setContextClassLoader (classLoader);
            mainMethod.invoke (null, new Object[] {args});
        }

        catch (InvocationTargetException ex)
        {
            throw ex.getTargetException();
        }
    }


static class BootstrapMainThread extends Thread
{
    private Method mainMethod;
    private String[] args;

    BootstrapMainThread (Method mainMethod, String[] args)
    {
        this.mainMethod = mainMethod;
        this.args = args;
    }

    public void run()
    {
        try
        {
            mainMethod.invoke (null, new Object[] {args});
        }

        catch (Throwable ex)
        {
            throw new UndeclaredThrowableException (ex);
        }
    }
}
}
