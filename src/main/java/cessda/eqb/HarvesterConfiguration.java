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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Loads the repositories from application.yml into list of Repo Objects
 *
 * <pre>
 * harvester:
 *  repos:
 *   - url: http://www.da-ra.de/oaip/oai?verb=ListRecords&amp;metadataPrefix=oai_dc&amp;set=39
 *    setName: 'da|ra: ICPSR – Interuniversity Consortium for Political and Social Research'
 *    dataProvider: da|ra (Registration agency for social science and economic data)
 * </pre>
 *
 * @author kraemets
 */
@Configuration
@EnableMBeanExport
@EnableConfigurationProperties
@ConfigurationProperties( prefix = "harvester" )
public class HarvesterConfiguration
{
    private String dir = "/tmp";
    private String recipient = null;
    private String dialectDefinitionName = null;
    private String metadataFormat = null;
    private boolean removeOAIEnvelope = false;
    private List<Repo> repos = new ArrayList<>();
    private Cron cron;
    private From from;
    private int timeout;

    public String getDialectDefinitionName()
    {
        return dialectDefinitionName;
    }

    public void setDialectDefinitionName( String dialectDefinitionName )
    {
        this.dialectDefinitionName = dialectDefinitionName;
    }

    public boolean isRemoveOAIEnvelope()
    {
        return removeOAIEnvelope;
    }

    public void setRemoveOAIEnvelope( boolean removeOAIEnvelope )
    {
        this.removeOAIEnvelope = removeOAIEnvelope;
    }

    public String getRecipient()
    {

        return recipient;
    }

    public void setRecipient( String recipient )
    {

        this.recipient = recipient;
    }

    public String getDir()
    {

        return dir;
    }

    public void setDir( String dir )
    {

        this.dir = dir;
    }

    public List<Repo> getRepos()
    {

        return repos;
    }

    public void setRepos( List<Repo> repos )
    {

        this.repos = repos;
    }

    @Override
    public String toString()
    {

        return "HarvesterConfiguration [dir=" + dir + ", recipient=" + recipient + ", repos=" + repos + ", cron=" + cron
                + ", from=" + from + "]";
    }

    public Cron getCron()
    {

        return cron;
    }

    public void setCron( Cron cron )
    {

        this.cron = cron;
    }

    public From getFrom()
    {

        return from;
    }

    public void setFrom( From from )
    {

        this.from = from;
    }

    public int getTimeout()
    {

        return timeout;
    }

    public void setTimeout( int timeout )
    {

        this.timeout = timeout;
    }

    public String getMetadataFormat()
    {

        return metadataFormat;
    }

    public void setMetadataFormat( String metadataFormat )
    {

        this.metadataFormat = metadataFormat;
    }

    public static class From
    {

        private String incremental;

        private String initial;

        private String full;

        private String single;

        public String getIncremental()
        {

            return incremental;
        }

        public void setIncremental( String incremental )
        {

            this.incremental = incremental;
        }

        public String getInitial()
        {

            return initial;
        }

        public void setInitial( String initial )
        {

            this.initial = initial;
        }

        public String getFull()
        {

            return full;
        }

        public void setFull( String full )
        {

            this.full = full;
        }

        @Override
        public String toString()
        {

            return "From [incremental=" + incremental + ", initial=" + initial + ", full=" + full + ", single=" + single
                    + "]";
        }

        public String getSingle()
        {

            return single;
        }

        public void setSingle( String single )
        {

            this.single = single;
        }
    }

    public static class Cron
    {

        private String incremental;

        private String full;

        public String getIncremental()
        {

            return incremental;
        }

        public void setIncremental( String incremental )
        {

            this.incremental = incremental;
        }

        public String getFull()
        {

            return full;
        }

        public void setFull( String full )
        {

            this.full = full;
        }

        @Override
        public String toString()
        {

            return "Cron [incremental=" + incremental + ", full=" + full + "]";
        }
    }

    public static class Repo
    {

        private URI url;

        private String setName;

        private String metaDataProvider;

        private boolean discoverSets;

        public URI getUrl()
        {

            return url;
        }

        public void setUrl( URI url )
        {
            Objects.requireNonNull( url );
            this.url = url;
        }

        public String getSetName()
        {

            return setName;
        }

        public void setSetName( String setName )
        {

            this.setName = setName;
        }

        public String getMetaDataProvider()
        {

            return metaDataProvider;
        }

        public void setMetaDataProvider( String metaDataProvider )
        {

            this.metaDataProvider = metaDataProvider;
        }

        public boolean discoverSets()
        {
            return discoverSets;
        }

        public void setDiscoverSets( boolean discoverSets )
        {
            this.discoverSets = discoverSets;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;
            Repo repo = (Repo) o;
            return discoverSets == repo.discoverSets && url.equals( repo.url )
                    && Objects.equals( setName, repo.setName )
                    && Objects.equals( metaDataProvider, repo.metaDataProvider );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( url, setName, metaDataProvider, discoverSets );
        }

        @Override
        public String toString()
        {
            return "Repo{" +
                    "url=" + url +
                    ", setName='" + setName + '\'' +
                    ", metaDataProvider='" + metaDataProvider + '\'' +
                    ", discoverSets=" + discoverSets +
                    '}';
        }
    }

}
