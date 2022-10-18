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
package eu.cessda.eqb.harvester;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Loads the repositories from application.yml into list of Repo Objects
 *
 * <pre>
 * harvester:
 *   repos:
 *   - url: http://www.da-ra.de/oaip/oai?verb=ListRecords&amp;metadataPrefix=oai_dc&amp;set=39
 *     setName: 'da|ra: ICPSR – Interuniversity Consortium for Political and Social Research'
 *     dataProvider: da|ra (Registration agency for social science and economic data)
 * </pre>
 *
 * @author kraemets
 */
@Configuration
@ConfigurationProperties( prefix = "harvester" )
class HarvesterConfiguration
{
    /**
     * List of repositories to harvest.
     */
    private final ArrayList<Repo> repos = new ArrayList<>();
    /**
     * The output directory of the harvester.
     */
    private Path dir = Path.of(System.getProperty( "java.io.tmpdir" ));
    /**
     * Keep the OAI envelope.
     */
    private boolean keepOAIEnvelope = true;
    /**
     * Remove the OAI envelope.
     */
    private boolean removeOAIEnvelope = false;
    /**
     * Incrementally harvest
     */
    private boolean incremental = false;
    /**
     * Date to harvest from.
     */
    private From from;
    /**
     * Timeout for HTTP requests, defaults to 30 seconds if unspecified.
     */
    private Duration timeout = Duration.ofSeconds( 30 );

    /**
     * Keep the OAI envelope.
     */
    public boolean keepOAIEnvelope()
    {
        return keepOAIEnvelope;
    }

    public void setKeepOAIEnvelope( boolean keepOAIEnvelope )
    {
        this.keepOAIEnvelope = keepOAIEnvelope;
    }

    /**
     * Remove the OAI envelope.
     */
    public boolean removeOAIEnvelope()
    {
        return removeOAIEnvelope;
    }

    public void setRemoveOAIEnvelope( boolean removeOAIEnvelope )
    {
        this.removeOAIEnvelope = removeOAIEnvelope;
    }

    /**
     * Harvest incrementally.
     */
    public boolean incremental()
    {
        return incremental;
    }

    public void setIncremental( boolean incremental )
    {
        this.incremental = incremental;
    }

    /**
     * Gets the output directory of the harvester.
     */
    public Path getDir()
    {
        return dir;
    }

    /**
     * Sets the output directory of the harvester.
     * @param dir the directory, must not be {@code null}.
     */
    public void setDir( Path dir )
    {
        Objects.requireNonNull( dir, "dir must not be null" );
        this.dir = dir;
    }

    /**
     * Get the list of repositories to harvest.
     */
    public List<Repo> getRepos()
    {
        return repos;
    }

    /**
     * Set the list of repositories to harvest.
     * @param repos the collection of repos to add, must not be {@code null}.
     */
    public void setRepos( Collection<Repo> repos )
    {
        Objects.requireNonNull( repos, "repos must not be null" );
        this.repos.clear();
        this.repos.addAll( repos );
    }

    public From getFrom()
    {
        return from;
    }

    public void setFrom( From from )
    {
        this.from = from;
    }

    /**
     * Gets the timeout for HTTP requests.
     */
    public Duration getTimeout()
    {
        return timeout;
    }

    /**
     * Sets the timeout for HTTP requests.
     * @param timeout the timeout, must not be {@code null}.
     */
    public void setTimeout( Duration timeout )
    {
        Objects.requireNonNull( timeout, "timeout must not be null" );
        this.timeout = timeout;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        HarvesterConfiguration that = (HarvesterConfiguration) o;
        return keepOAIEnvelope == that.keepOAIEnvelope && removeOAIEnvelope == that.removeOAIEnvelope &&
                incremental == that.incremental && timeout == that.timeout &&
                Objects.equals( dir, that.dir ) && Objects.equals( repos, that.repos ) &&
                Objects.equals( from, that.from );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( dir, keepOAIEnvelope, removeOAIEnvelope, incremental, repos, from, timeout );
    }

    @Override
    public String toString()
    {
        return "HarvesterConfiguration{" +
                "repos=" + repos +
                ", dir=" + dir +
                ", keepOAIEnvelope=" + keepOAIEnvelope +
                ", removeOAIEnvelope=" + removeOAIEnvelope +
                ", incremental=" + incremental +
                ", from=" + from +
                ", timeout=" + timeout +
                '}';
    }

    record From(LocalDate incremental) {}

    @Component
    @ConfigurationPropertiesBinding
    public static final class LocalDateConverter implements Converter<String, LocalDate>
    {
        @Override
        public LocalDate convert( String s )
        {
            return LocalDate.parse( s );
        }
    }
}
