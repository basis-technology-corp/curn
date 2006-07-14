/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.util.misc.BundleUtil;

import java.util.Locale;

/**
 * <tt>FeedException</tt> is thrown when there's an error with a feed.
 * It contains the corresponding <tt>FeedInfo</tt> object, and its
 * <tt>getMessage()</tt> method includes the feed URL in the message.
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedException extends CurnException
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private final FeedInfo feedInfo;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     *
     * @param feedInfo  the <tt>FeedInfo</tt> object for the feed
     */
    public FeedException (FeedInfo feedInfo)
    {
        super();
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param feedInfo   the <tt>FeedInfo</tt> object for the feed
     * @param exception  the exception to contain
     */
    public FeedException (FeedInfo feedInfo, Throwable exception)
    {
        super (exception);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param feedInfo the <tt>FeedInfo</tt> object for the feed
     * @param message  the message to associate with this exception
     */
    public FeedException (FeedInfo feedInfo, String message)
    {
        super (message);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param feedInfo   the <tt>FeedInfo</tt> object for the feed
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public FeedException (FeedInfo  feedInfo,
                          String    message,
                          Throwable exception)
    {
        super (message, exception);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #FeedException(FeedInfo,String,String,String,Object[])}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to <tt>getMesage(Locale)</tt> will attempt to
     * retrieve the top-most message (i.e., the message from this
     * exception, not from nested exceptions) by querying the named
     * resource bundle. Calls to <tt>printStackTrace(PrintWriter,Locale)</tt>
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param feedInfo    the <tt>FeedInfo</tt> object for the feed
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     *
     * @see #FeedException(FeedInfo,String,String,String,Object[])
     */
    public FeedException (FeedInfo feedInfo,
                          String   bundleName,
                          String   messageKey,
                          String   defaultMsg)
    {
        super (bundleName, messageKey, defaultMsg);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #FeedException(FeedInfo,String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt> parameter.
     * Calls to <tt>getMessage(Locale)</tt> will attempt to
     * retrieve the top-most message (i.e., the message from this exception,
     * not from nested exceptions) by querying the named resource bundle.
     * Calls to <tt>printStackTrace(PrintWriter,Locale)</tt>
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param feedInfo    the <tt>FeedInfo</tt> object for the feed
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     *
     * @see #FeedException(FeedInfo,String,String,String,Object[],Throwable)
     */
    public FeedException (FeedInfo feedInfo,
                          String   bundleName,
                          String   messageKey,
                          String   defaultMsg,
                          Object[] msgParams)
    {
        super (bundleName, messageKey, defaultMsg, msgParams);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling the
     * {@link #FeedException(FeedInfo,String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to <tt>getMessage(Locale)</tt> will attempt to
     * retrieve the top-most message (i.e., the message from this
     * exception, not from nested exceptions) by querying the named
     * resource bundle. Calls to <tt>printStackTrace(PrintWriter,Locale)</tt>
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param feedInfo    the <tt>FeedInfo</tt> object for the feed
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param exception   the exception to nest
     *
     * @see #FeedException(FeedInfo,String,String,String,Object[],Throwable)
     */
    public FeedException (FeedInfo  feedInfo,
                          String    bundleName,
                          String    messageKey,
                          String    defaultMsg,
                          Throwable exception)
    {
        super (bundleName, messageKey, defaultMsg, null, exception);
        this.feedInfo = feedInfo;
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message format (in case the resource bundle can't be
     * found), arguments to be incorporated in the message via
     * <tt>java.text.MessageFormat</tt>, and another exception.
     * Calls to <tt>getMessage(Locale)</tt> will attempt to retrieve the
     * top-most message (i.e., the message from this exception, not from
     * nested exceptions) by querying the named resource bundle. Calls to
     * <tt>printStackTrace(PrintWriter,Locale)</tt> will do the same, where
     * applicable. The message is not retrieved until one of those methods
     * is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param feedInfo    the <tt>FeedInfo</tt> object for the feed
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     * @param exception   exception to be nested
     *
     * @see #FeedException(FeedInfo,String,String,String,Object[])
     */
    public FeedException (FeedInfo  feedInfo,
                          String    bundleName,
                          String    messageKey,
                          String    defaultMsg,
                          Object[]  msgParams,
                          Throwable exception)
    {
        super (bundleName, messageKey, defaultMsg, msgParams, exception);
        this.feedInfo = feedInfo;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Returns the error message string for this exception. If the
     * exception was instantiated with a message of its own, then that
     * message is returned. Otherwise, this method returns the class name,
     * along with the class name of the first nested exception, if any.
     * Unlike the parent <tt>Exception</tt> class, this method will never
     * return null.
     *
     * @return  the error message string for this exception
     */
    public String getMessage()
    {
        return getMessage (Locale.getDefault());
    }

    /**
     * Returns the error message string for this exception. If the
     * exception was instantiated with a message of its own, then that
     * message is returned. Otherwise, this method returns the class name,
     * along with the class name of the first nested exception, if any.
     * Unlike the parent <tt>Exception</tt> class, this method will never
     * return null. If a localized version of the message is available, it
     * will be returned.
     *
     * @param locale the locale to use, or null for the default
     *
     * @return  the error message string for this exception
     */
    public String getMessage (final Locale locale)
    {
        StringBuilder buf = new StringBuilder();

        buf.append (BundleUtil.getMessage (Constants.BUNDLE_NAME,
                                           locale,
                                           "FeedException.feedPrefix",
                                           "feed"));
        buf.append (" \"");
        buf.append (feedInfo.getURL().toString());
        buf.append ("\": ");

        buf.append (super.getMessage (locale));

        return buf.toString();
    }

}
