/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser.minirss;

import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSParser;
import org.clapper.rssget.parser.RSSParserException;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;

import java.net.URL;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p><tt>MiniRSSParser</tt> is a stripped down RSS parser. It handles
 * files in RSS formats 0.91, 0.92, 1.0 and 2.0. However, it doesn't store
 * all the possible RSS items. It stores those items that the <i>rssget</i>
 * utility requires (plus a few more), but lacks support for others. For
 * instance, it ignores <tt>image</tt>, <tt>cloud</tt>, <tt>textinput</tt>
 * and other elements that <i>rssget</i> has no interest in displaying. As
 * such, <tt>MiniRSSParser</tt> is not suitable as a general-purpose RSS
 * parser. However, it is very suitable for use with <i>rssget</i>.</p>
 *
 * <p><b>Notes:</b>
 *
 * <ol>
 *    <li> This API relies on the SAX 2 (org.xml.sax.*) package of XML parser
 *         classes; you must have those classes in your CLASSPATH to use this
 *         API.
 *
 *    <li> If a specific XML parser class is not specified to the constructor,
 *         this class defaults to using the Apache Xerces XML parser class.
 *
 *    <li> The SAX <code>DocumentHandler</code>-required functions are
 *         <code>final</code> in this class; they cannot be overridden by
 *         subclasses.
 * </ol>
 *
 * @version <tt>$Revision$</tt>
 */
public class MiniRSSParser
    extends DefaultHandler implements RSSParser
{
    /*----------------------------------------------------------------------*\
			     Private Constants
    \*----------------------------------------------------------------------*/

    private static final String DEFAULT_XML_PARSER_CLASS_NAME =
                                      "org.apache.xerces.parsers.SAXParser";

    /*----------------------------------------------------------------------*\
			   Private Instance Data
    \*----------------------------------------------------------------------*/

    private Channel   channel         = null;
    private String    parserClassName = DEFAULT_XML_PARSER_CLASS_NAME;
    private XMLReader xmlReader       = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    public MiniRSSParser()
    {
        this (null);
    }

    public MiniRSSParser (String parserClassName)
    {
	if (parserClassName == null)
	    parserClassName = DEFAULT_XML_PARSER_CLASS_NAME;

	this.parserClassName = parserClassName;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    public RSSChannel parseRSSFeed (URL url)
        throws IOException,
               RSSParserException
    {
        return parse (url);
    }

    public final Channel parse (File path)
	throws FileNotFoundException,
	       IOException,
	       RSSParserException
    {
	return parse (path, null);
    }

    public final Channel parse (File path, String encoding)
	throws FileNotFoundException,
	       IOException,
	       RSSParserException
    {
        FileInputStream   fs =  new FileInputStream (path);
        InputStreamReader is;

	if (encoding == null)
	    is = new InputStreamReader (fs);
	else
	    is = new InputStreamReader (fs, encoding);

	return parse (is);
    }

    public final Channel parse (URL url)
	throws IOException,
	       RSSParserException
    {
	return parse (new InputStreamReader (url.openStream()));
    }

    public final Channel parse (Reader r)
	throws IOException,
	       RSSParserException
    {
	try
        {
            xmlReader = XMLReaderFactory.createXMLReader (parserClassName);
            xmlReader.setContentHandler (this);

            xmlReader.parse (new InputSource (r));
        }

        catch (SAXException ex)
        {
            throw new RSSParserException (ex);
        }

        return channel;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
                        Overriding XMLReaderAdapter
    \*----------------------------------------------------------------------*/

    /**
     * Handle the start of an XML element. This method assumes that it's
     * getting the first element in the RSS file. It examines that element,
     * and hands off control to either a <tt>V1Parser</tt> or
     * <tt>V2Parser</tt> object for version-specific parsing.
     *
     * @param namespaceURI       the Namespace URI, or the empty string if the
     *                           element has no Namespace URI or if Namespace
     *                           processing is not being performed
     * @param namespaceLocalName the local name (without prefix), or the empty
     *                           string if Namespace processing is not being
     *                           performed.
     * @param elementName        the qualified element name (with prefix), or
     *                           the empty string if qualified names are not
     *                           available
     * @param attributes         the attributes attached to the element.
     *
     * @throws SAXException parsing error
     */
    public void startElement (String     namespaceURI,
                              String     namespaceLocalName,
                              String     elementName,
                              Attributes attributes)
        throws SAXException
    {
        // We're at the top of the document.

        channel = new Channel();

        if (elementName.equals ("rdf:RDF"))
        {
            channel.setRSSFormat ("RSS 1.0");
            xmlReader.setContentHandler (new V1Parser (channel, elementName));
        }

        else if (elementName.equals ("rss"))
        {
            String version = attributes.getValue ("version");
            channel.setRSSFormat ("RSS " + version);

            // For rssget's purposes, there's considerable similarity between
            // RSS version 0.91 and RSS version 2--so much so that the same
            // parser logic will work for both.

            if (version.startsWith ("0.9") || version.startsWith ("2."))
            {
                xmlReader.setContentHandler (new V2Parser (channel,
                                                           elementName));
            }

            else
            {
                throw new SAXException ("Unknown RSS version: " + version);
            }
        }

        else
        {
            throw new SAXException ("First element is not <rss> or <rdf>");
        }
    }
}
