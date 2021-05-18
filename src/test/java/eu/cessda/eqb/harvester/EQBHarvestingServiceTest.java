package eu.cessda.eqb.harvester;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import javax.xml.transform.TransformerConfigurationException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;

@TestPropertySource(
		properties = {"spring.mail.host=localhost",
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
				"spring.boot.admin.client.enabled=false"} )
public class EQBHarvestingServiceTest
{
	private static final Logger log = LoggerFactory.getLogger( EQBHarvestingServiceTest.class );

	private final Server server;

	public EQBHarvestingServiceTest() throws TransformerConfigurationException
	{
		var harvesterConfiguration = new HarvesterConfiguration();
		harvesterConfiguration.setDir( "data2" );
		harvesterConfiguration.setFrom( new HarvesterConfiguration.From() );
		harvesterConfiguration.getFrom().setSingle( "2020-12-08" );
		var repo = new Repo();
		repo.setUrl( URI.create( "http://services.fsd.uta.fi/v0/oai?set=study_group:paihde" ) );
		repo.setMetadataFormat( "oai_ddi" );
		harvesterConfiguration.getRepos().add( repo );
		server = new Server( harvesterConfiguration );
	}

	@Test
	public void indexAllTest()
	{

		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
		Date now = new Date( System.currentTimeMillis() );
		String snow = sdf.format( now );
		log.info( snow );
		server.singleHarvesting( 0 );
		Assertions.assertTrue( Files.exists( Paths.get( "data2" ) ) );

	}
}
