/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.email;

import org.clapper.curn.OutputHandler;
import org.clapper.curn.EmailOutputHandler;
import org.clapper.curn.Util;
import org.clapper.curn.Version;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.ConfigFile;
import org.clapper.curn.FeedException;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.mail.EmailMessage;
import org.clapper.util.mail.EmailTransport;
import org.clapper.util.mail.SMTPEmailTransport;
import org.clapper.util.mail.EmailAddress;
import org.clapper.util.mail.EmailException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Provides an output handler that wraps the real output handlers and
 * sends an email message, using the contents of the real handlers.
 *
 * @see OutputHandler
 * @see org.clapper.curn.curn
 * @see org.clapper.curn.parser.RSSChannel
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
	OutputHandler     handler;
	File              tempFile;
        FileOutputStream  fileOut;

	HandlerTableEntry (OutputHandler handler)
	    throws FeedException
	{
            try
            {
                this.handler = handler;

                tempFile = File.createTempFile ("curn", null);
                tempFile.deleteOnExit();

                fileOut = new FileOutputStream (tempFile);
            }

            catch (IOException ex)
            {
                throw new FeedException ("Can't initialize handler", ex);
            }   
	}

        void init (ConfigFile config)
            throws FeedException
        {
            handler.init (new OutputStreamWriter (fileOut), config);
        }
    }

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private Collection           handlers   = new ArrayList();
    private Collection           recipients = new ArrayList();
    private ConfigFile  config     = null;

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
     * @param writer  the <tt>OutputStreamWriter</tt> where the handler
     *                should send output. Not used here.
     * @param config  the parsed <i>curn</i> configuration data
     *
     * @throws FeedException initialization error
     */
    public void init (OutputStreamWriter writer, ConfigFile config)
        throws FeedException
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
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws FeedException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws FeedException
    {
	for (Iterator it = handlers.iterator(); it.hasNext(); )
        {
            HandlerTableEntry entry = (HandlerTableEntry) it.next();

            entry.handler.displayChannel (channel, feedInfo);
        }
    }
    
    /**
     * Flush any buffered-up output. <i>curn</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws FeedException  unable to write output
     */
    public void flush() throws FeedException
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
            throw new FeedException (ex);
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
     * @throws FeedException error opening a temporary file for the output
     */
    public void addOutputHandler (OutputHandler handler)
	throws FeedException
    {
	handlers.add (new HandlerTableEntry (handler));
    }

    /**
     * Add one or more email addresses to the output handler. The
     * <tt>flush()</tt> method actually sends the message.
     *
     * @param emailAddress  email address to add
     *
     * @throws FeedException  bad email address
     */
    public void addRecipient (String emailAddress)
        throws FeedException
    {
        try
        {
            recipients.add (new EmailAddress (emailAddress));
        }

        catch (EmailException ex)
        {
            throw new FeedException (ex);
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
     * @throws FeedException  on error
     */
    private void emailOutput()
        throws FeedException
    {
        try
        {
            EmailMessage       message   = new EmailMessage();
            String             smtpHost  = config.getSMTPHost();
            EmailTransport     transport = new SMTPEmailTransport (smtpHost);
            Iterator           it;
            HandlerTableEntry  entry;

            for (it = recipients.iterator(); it.hasNext(); )
                message.addTo ((EmailAddress) it.next());

            message.addHeader ("X-Mailer",
                               "curn, version " + Version.VERSION);
            message.setSubject (config.getEmailSubject());

            // Add the output. If there's only one handler, and its output
            // is text, then there's no need for attachments. Just set it
            // as the text part, and set the appropriate Content-type:
            // header. Otherwise, make a multipart-alternative message with
            // separate attachments for each output.

            if (handlers.size() == 1)
            {
                entry = (HandlerTableEntry) handlers.iterator().next();
                String contentType = entry.handler.getContentType();

                message.setMultipartSubtype
                                     (EmailMessage.MULTIPART_MIXED);
                if (contentType.startsWith ("text/"))
                    message.setText (entry.tempFile, contentType);
                else
                    message.addAttachment (entry.tempFile, contentType);
            }

            else
            {
                message.setMultipartSubtype
                                     (EmailMessage.MULTIPART_ALTERNATIVE);

                for (it = handlers.iterator(); it.hasNext(); )
                {
                    entry = (HandlerTableEntry) it.next();
                    message.addAttachment (entry.tempFile,
                                           entry.handler.getContentType());
                }
            }

            transport.send (message);
        }

        catch (EmailException ex)
        {
            throw new FeedException (ex);
        }
    }
}
