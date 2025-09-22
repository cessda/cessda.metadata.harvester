package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2025 CESSDA ERIC
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

class EQBHarvestingServiceTests
{
    private static final Repo testRepository = new Repo(
        new Repo.OAIConfiguration( URI.create( "http://localhost:8080/v0/oai?set=study_group:paihde" ),"oai_ddi", null, false ),
        "TEST",
        "Test Repository",
        "en",
        URI.create( "https://cmv.cessda.eu/profiles/cdc/ddi-2.5/latest/profile.xml" ),
        "BASIC",
        Collections.emptySet()
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

        return new Harvester( httpClient, harvesterConfiguration, new RepositoryClient( httpClient ) );
	}

	@Test
	void indexAllTest(@TempDir Path tempDir) throws IOException
    {
		getHarvester( tempDir ).run();

        // Read pipeline.json, assert fields are as expected.
        var pipeline = new ObjectMapper().readValue( new File( tempDir + "/TEST/oai_ddi/pipeline.json" ), PipelineMetadata.class );
        assertEquals( testRepository.code(), pipeline.code() );
        assertEquals( testRepository.name(), pipeline.name() );
        assertEquals( testRepository.validationGate(), pipeline.validationGate() );
        assertEquals( testRepository.validationProfile(), pipeline.profile() );
        assertEquals( testRepository.defaultLanguage(), pipeline.defaultLanguage() );

        // Validate the metadata fields are as expected.
        var metadata = testRepository.oaiConfiguration();
        assertEquals( metadata.metadataPrefix(), pipeline.metadataPrefix() );
        assertEquals( metadata.setSpec(), pipeline.setSpec() );
        assertEquals( metadata.url(), pipeline.url() );
	}
}
