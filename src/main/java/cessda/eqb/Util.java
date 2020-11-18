/*-
 * #%L
 * CESSDA Euro Question Bank: Metadata Harvester
 * %%
 * Copyright (C) 2020 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package cessda.eqb;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Util
{
	public static final Logger log = LoggerFactory.getLogger( Util.class );

	private final TransformerFactory transformerFactory;

	public Util()
	{
		transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
		transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
		transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
	}

	public void printDocument( Document doc, OutputStream out ) throws TransformerException
	{
		try
		{
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );

			transformer.transform( new DOMSource( doc ),
					new StreamResult( new OutputStreamWriter( out, StandardCharsets.UTF_8 ) ) );
		}
		catch (TransformerConfigurationException e)
		{
			throw new IllegalStateException( e );
		}
	}
}
