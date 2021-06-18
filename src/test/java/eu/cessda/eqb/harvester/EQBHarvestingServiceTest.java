package eu.cessda.eqb.harvester;

import org.junit.jupiter.api.Test;

import javax.xml.transform.TransformerConfigurationException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

		harvester = new Harvester( harvesterConfiguration, new IOUtilities() );
	}

	@Test
	void indexAllTest()
	{
		harvester.run(  );
		assertTrue( Files.exists( Paths.get( "data2" ) ) );
	}
}
