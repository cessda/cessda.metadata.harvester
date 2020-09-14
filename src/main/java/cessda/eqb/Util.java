package cessda.eqb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class Util
{
	public static final Logger log = LoggerFactory.getLogger( Util.class );

	private final TransformerFactory tf;

	public Util()
	{
		tf = TransformerFactory.newInstance();
		tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
		tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
		tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
	}

	public void printDocument( Document doc, OutputStream out ) throws TransformerException
	{
		try
		{
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );

			transformer.transform( new DOMSource( doc ), new StreamResult( new OutputStreamWriter( out, StandardCharsets.UTF_8 ) ) );
		}
		catch ( TransformerConfigurationException e )
		{
			throw new IllegalStateException( e );
		}
	}
}
