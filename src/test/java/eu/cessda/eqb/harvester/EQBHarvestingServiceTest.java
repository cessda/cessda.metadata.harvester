package eu.cessda.eqb.harvester;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;

import static java.io.InputStream.nullInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EQBHarvestingServiceTest
{
    private static final Repo testRepository = new Repo(
        Collections.singleton( new Repo.MetadataFormat( null, "oai_ddi", "DDI_2_5", URI.create( "https://cmv.cessda.eu/profiles/cdc/ddi-2.5/latest/profile.xml" ) ) ),
        "TEST",
        "Test Repository",
        URI.create( "http://localhost:8080/v0/oai?set=study_group:paihde" ),
        false,
        "en",
        "BASIC"
    );

	public Harvester getHarvester(Path harvesterDirectory) throws IOException
    {
		var harvesterConfiguration = new HarvesterConfiguration();
		harvesterConfiguration.setDir( harvesterDirectory );
		harvesterConfiguration.setFrom( new HarvesterConfiguration.From(null) );
		harvesterConfiguration.setTimeout( Duration.ofSeconds( 10 ) );
        harvesterConfiguration.getRepos().add( testRepository );

        var httpClient = mock( HttpClient.class );
        when( httpClient.getHttpResponse( any(URI.class) ) ).thenReturn( nullInputStream() );

        return new Harvester( httpClient, harvesterConfiguration, new IOUtilities(), new RepositoryClient( httpClient ) );
	}

	@Test
	void indexAllTest(@TempDir Path tempDir) throws IOException
    {
		getHarvester( tempDir ).run();

        // Read pipeline.json, assert fields are as expected.
        var pipeline = new ObjectMapper().readValue( new File(tempDir + "/wrapped/TEST/oai_ddi/pipeline.json"), SharedRepositoryModel.class );
        assertEquals( testRepository.code(), pipeline.code() );
        assertEquals( testRepository.name(), pipeline.name() );
        assertEquals( testRepository.url(), pipeline.url() );
        assertEquals( testRepository.validationGate(), pipeline.validationGate() );
        assertEquals( testRepository.defaultLanguage(), pipeline.defaultLanguage() );

        // Validate the metadata fields are as expected.
        var metadata = testRepository.metadataPrefixes().stream().findAny().orElseThrow();
        assertEquals( metadata.metadataPrefix(), pipeline.metadataPrefix() );
        assertEquals( metadata.setSpec(), pipeline.setSpec() );
        assertEquals( metadata.ddiVersion(), pipeline.ddiVersion() );
        assertEquals( metadata.validationProfile(), pipeline.profile() );
	}
}
