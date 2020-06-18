package cessda.eqb;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Util
{
	public static Logger log = LoggerFactory.getLogger( Util.class );

	public static void printDocument( Document doc, OutputStream out )
	{
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();

			tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" ); 
			tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );

			transformer.transform( new DOMSource( doc ),
					new StreamResult( new OutputStreamWriter( out, "UTF-8" ) ) );
		}
		catch (IllegalArgumentException | UnsupportedEncodingException | TransformerFactoryConfigurationError
				| TransformerException e)
		{
			log.error( e.getLocalizedMessage() );

		}
	}
}
