package eu.cessda.eqb.harvester;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;

import static java.io.InputStream.nullInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EQBHarvestingServiceTest
{
	private final Harvester harvester;

	public EQBHarvestingServiceTest() throws IOException
    {
		var harvesterConfiguration = new HarvesterConfiguration();
		harvesterConfiguration.setDir( Path.of("data2") );
		harvesterConfiguration.setFrom( new HarvesterConfiguration.From() );
		harvesterConfiguration.setTimeout( Duration.ofSeconds( 10 ) );
		var repo = new Repo(
            Collections.singleton(new Repo.MetadataFormat(null,"oai_ddi", "DDI_2_5", URI.create( "https://cmv.cessda.eu/profiles/cdc/ddi-2.5/latest/profile.xml" ) ) ),
            "TEST",
            null,
            URI.create( "http://localhost:8080/v0/oai?set=study_group:paihde" ),
            false,
            "en",
            "BASIC"
        );
		harvesterConfiguration.getRepos().add( repo );

        var httpClient = mock( HttpClient.class );
        when( httpClient.getHttpResponse( any(URI.class) ) ).thenReturn( nullInputStream() );

        harvester = new Harvester( httpClient, harvesterConfiguration, new IOUtilities(), new RepositoryClient( httpClient ) );
	}

	@Test
	void indexAllTest()
	{
		harvester.run(  );
		assertTrue( Files.exists( Paths.get( "data2" ) ) );
	}
}
