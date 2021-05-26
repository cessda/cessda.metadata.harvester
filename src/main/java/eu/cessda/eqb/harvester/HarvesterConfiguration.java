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
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
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
@ConfigurationProperties( prefix = "harvester" )
class HarvesterConfiguration
{
    private Path dir = Path.of(System.getProperty( "java.io.tmpdir" ));
    private String recipient = null;
    private boolean keepOAIEnvelope = true;
    private boolean removeOAIEnvelope = false;
    private List<Repo> repos = new ArrayList<>();
    private From from;
    private int timeout;

    public boolean removeOAIEnvelope()
    {
        return removeOAIEnvelope;
    }

    public void setRemoveOAIEnvelope( boolean removeOAIEnvelope )
    {
        this.removeOAIEnvelope = removeOAIEnvelope;
    }

    public boolean keepOAIEnvelope()
    {
        return keepOAIEnvelope;
    }

    public void setKeepOAIEnvelope( boolean keepOAIEnvelope )
    {
        this.keepOAIEnvelope = keepOAIEnvelope;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void setRecipient( String recipient )
    {
        this.recipient = recipient;
    }

    public Path getDir()
    {
        return dir;
    }

    public void setDir( Path dir )
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

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        HarvesterConfiguration that = (HarvesterConfiguration) o;
        return keepOAIEnvelope == that.keepOAIEnvelope && removeOAIEnvelope == that.removeOAIEnvelope &&
                timeout == that.timeout && Objects.equals( dir, that.dir ) &&
                Objects.equals( recipient, that.recipient ) &&
                Objects.equals( repos, that.repos ) && Objects.equals( from, that.from );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( dir, recipient, keepOAIEnvelope, removeOAIEnvelope, repos, from, timeout );
    }

    @Override
    public String toString()
    {
        return "HarvesterConfiguration{" +
                "dir=" + dir +
                ", recipient='" + recipient + '\'' +
                ", keepOAIEnvelope=" + keepOAIEnvelope +
                ", removeOAIEnvelope=" + removeOAIEnvelope +
                ", repos=" + repos +
                ", from=" + from +
                ", timeout=" + timeout +
                '}';
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

        public String getSingle()
        {
            return single;
        }

        public void setSingle( String single )
        {
            this.single = single;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;
            From from = (From) o;
            return Objects.equals( incremental, from.incremental ) &&
                    Objects.equals( initial, from.initial ) && Objects.equals( full, from.full ) &&
                    Objects.equals( single, from.single );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( incremental, initial, full, single );
        }

        @Override
        public String toString()
        {
            return "From{" +
                    "incremental='" + incremental + '\'' +
                    ", initial='" + initial + '\'' +
                    ", full='" + full + '\'' +
                    ", single='" + single + '\'' +
                    '}';
        }
    }
}
