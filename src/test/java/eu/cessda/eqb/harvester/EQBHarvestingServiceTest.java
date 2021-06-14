package eu.cessda.eqb.harvester;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

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
class EQBHarvestingServiceTest
{
	private final Harvester harvester;

	public EQBHarvestingServiceTest() throws TransformerConfigurationException
	{
		var harvesterConfiguration = new HarvesterConfiguration();
		harvesterConfiguration.setDir( Path.of("data2") );
		harvesterConfiguration.setFrom( new HarvesterConfiguration.From() );
		harvesterConfiguration.getFrom().setSingle( LocalDate.parse("2020-12-08") );
		harvesterConfiguration.setTimeout( Duration.ofSeconds( 10 ) );
		var repo = new Repo();
		repo.setCode( "TEST" );
		repo.setUrl( URI.create( "http://localhost:8080/v0/oai?set=study_group:paihde" ) );
		repo.setMetadataFormat( "oai_ddi" );
		harvesterConfiguration.getRepos().add( repo );

		var httpClient = mock( HttpClient.class );
		when( httpClient.getHttpResponse( any(URL.class), any(Duration.class) ) )
			.thenReturn( InputStream.nullInputStream() );

		harvester = new Harvester( harvesterConfiguration, httpClient );
	}

	@Test
	void indexAllTest()
	{
		harvester.singleHarvesting( 0 );
		Assertions.assertTrue( Files.exists( Paths.get( "data2" ) ) );
	}
}
