/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.email;

import org.clapper.rssget.OutputHandler;
import org.clapper.rssget.EmailOutputHandler;
import org.clapper.rssget.Util;
import org.clapper.rssget.RSSGetConfiguration;
import org.clapper.rssget.RSSGetException;
import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Provides an output handler that wraps the real output handlers and
 * sends an email message, using the contents of the real handlers.
 *
 * @see OutputHandler
 * @see org.clapper.rssget.rssget
 * @see org.clapper.rssget.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class EmailOutputHandlerImpl implements EmailOutputHandler
{
    /*----------------------------------------------------------------------*\
			       Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Defines an entry in the table of handlers
     */
    private class HandlerTableEntry
    {
	OutputHandler handler;
	File          tempFile;
        FileWriter    fileOut;

	HandlerTableEntry (OutputHandler handler)
	    throws RSSGetException
	{
            try
            {
                this.handler = handler;

                tempFile = File.createTempFile ("rssget", null);
                tempFile.deleteOnExit();

                fileOut = new FileWriter (tempFile);
            }

            catch (IOException ex)
            {
                throw new RSSGetException ("Can't initialize handler", ex);
            }   
	}

        void init (RSSGetConfiguration config)
            throws RSSGetException
        {
            handler.init (new PrintWriter (fileOut), config);
        }
    }

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private Collection           handlers   = new ArrayList();
    private Collection           recipients = new ArrayList();
    private RSSGetConfiguration  config     = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>EmailOutputHandlerImpl</tt>.
     */
    public EmailOutputHandlerImpl()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param writer  the <tt>PrintWriter</tt> where the handler should send
     *                output. Not used here.
     * @param config  the parsed <i>rssget</i> configuration data
     *
     * @throws RSSGetException initialization error
     */
    public void init (PrintWriter         writer,
                      RSSGetConfiguration config)
        throws RSSGetException
    {
        this.config = config;

	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            HandlerTableEntry entry = (HandlerTableEntry) it.next();

            entry.init (config);
        }
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class.
     *
     * @param channel The channel containing the items to emit. The method
     *                should emit all the items in the channel; the caller
     *                is responsible for clearing out any items that should
     *                not be seen.
     *
     * @throws RSSGetException  unable to write output
     */
    public void displayChannel (RSSChannel channel)
        throws RSSGetException
    {
	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            HandlerTableEntry entry = (HandlerTableEntry) it.next();

            entry.handler.displayChannel (channel);
        }
    }
    
    /**
     * Flush any buffered-up output. <i>rssget</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws RSSGetException  unable to write output
     */
    public void flush() throws RSSGetException
    {
        try
        {
            for (Iterator it = handlers.iterator(); it.hasNext(); )
            {
                HandlerTableEntry entry = (HandlerTableEntry) it.next();

                entry.handler.flush();
                entry.fileOut.close();
            }

            emailOutput();
        }

        catch (IOException ex)
        {
            throw new RSSGetException (ex);
        }
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
	// Not really applicable.

        return null;
    }

    /**
     * Add an <tt>OutputHandler</tt> to this handler. The output handler
     * will be used to generate output that is attached to the email message.
     * This method must be called <i>after</i> <tt>init()</tt> is called.
     *
     * @param handler  the handler to add
     *
     * @throws RSSGetException error opening a temporary file for the output
     */
    public void addOutputHandler (OutputHandler handler)
	throws RSSGetException
    {
	handlers.add (new HandlerTableEntry (handler));
    }

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws RSSGetException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws RSSGetException
    {
        try
        {
            recipients.add (new EmailAddress (emailAddress));
        }

        catch (EmailException ex)
        {
            throw new RSSGetException (ex);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Send buffered-up output to the specified list of recipients.
     *
     * @param recipients  collection of email addresses, as strings.
     *
     * @throws RSSGetException  initialization error
     */
    private void emailOutput()
        throws RSSGetException
    {
        try
        {
            EmailMessage    message   = new EmailMessage();
            String          smtpHost  = config.getSMTPHost();
            EmailTransport  transport = new SMTPEmailTransport (smtpHost);
            Iterator        it;

            message.setMultipartSubtype (EmailMessage.MULTIPART_ALTERNATIVE);

            for (it = recipients.iterator(); it.hasNext(); )
                message.addTo ((EmailAddress) it.next());

            for (it = handlers.iterator(); it.hasNext(); )
            {
                HandlerTableEntry entry = (HandlerTableEntry) it.next();
                message.addAttachment (entry.tempFile,
                                       entry.handler.getContentType());
            }

            message.addHeader ("X-Mailer",
                               "rssget, version " + Util.getVersion());

            message.setSubject (config.getEmailSubject());
            transport.send (message);
        }

        catch (EmailException ex)
        {
            throw new RSSGetException (ex);
        }
    }
}
