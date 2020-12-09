package cessda.eqb.harvester.service;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import javax.xml.transform.TransformerException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.xml.sax.SAXException;

import cessda.eqb.Server;

@TestPropertySource(
		properties = { "spring.mail.host=localhost",
				"harvester.removeOAIEnvelope=true",
				"harvester.recipient=tech@dessda.org",
				"harvester.timeout=6",
				"harvester.dir=data2",
				"harvester.from.single='2020-12-08'",
				"harvester.from.initial='2020-12-08'",
				"harvester.from.full='2020-12-08'",
				"harvester.from.incremental='2020-12-08'",
				"harvester.cron.initialDelay=5000000",
				"harvester.repos[0].url=http://services.fsd.uta.fi/v0/oai?set=study_group:paihde",
				"spring.boot.admin.client.enabled=false" } )
@SpringBootTest( classes = Server.class )
public class EQBHarvestingServiceTest2
{
	private static final Logger log = LoggerFactory.getLogger( EQBHarvestingServiceTest.class );

	@Autowired
	Server server;

	@Value( "${harvester.dir}" )
	String dir;
	@Value( "${harvester.repos[2].url}" )
	String url2;
	@Value( "$harvester.from.single" )
	String single;
	@Value( "$harvester.from.initial" )
	String initial;
	@Value( "$harvester.from.full" )
	String full;
	@Value( "$harvester.from.incremental" )
	String incremental;

	@Test
	public void indexAllTest() throws InterruptedException, IOException, SAXException, TransformerException
	{

		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
		Date now = new Date( System.currentTimeMillis() );
		String snow = sdf.format( now );
		log.info( snow );
		initial = snow;
		full = snow;
		single = snow;
		incremental = snow;
		MatcherAssert.assertThat( dir, Matchers.equalTo( "data2" ) );
		server.singleHarvesting( 0 );
		Assertions.assertTrue( new File( dir ).exists() );

	}
}
