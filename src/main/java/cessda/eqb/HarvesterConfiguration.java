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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

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
 *
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

	public String getDialectDefinitionName()
{
		return dialectDefinitionName;
	}

	public void setDialectDefinitionName( String dialectDefinitionName )
{
		this.dialectDefinitionName = dialectDefinitionName;
	}

	private String metadataFormat = null;
	private boolean removeOAIEnvelope = false;

	public boolean isRemoveOAIEnvelope()
{
		return removeOAIEnvelope;
	}

	public void setRemoveOAIEnvelope( boolean removeOAIEnvelope )
{
		this.removeOAIEnvelope = removeOAIEnvelope;
	}

	private List<Repo> repos = new ArrayList<>();

	private Cron cron;

	private From from;

	private Integer timeout;

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

	public List<String> getRepoBaseUrls()
{

		List<String> res = new ArrayList<>();
		for ( Repo repo : repos )
	{

			res.add( repo.url );
		}
		return res;
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

		private String url;

		private String setName;

		private String dataProvider;

		private String metaDataProvider;

		public String getUrl()
	{

			return url;
		}

		public void setUrl( String url )
	{

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

		public String getDataProvider()
	{

			return dataProvider;
		}

		public void setDataProvider( String dataProvider )
	{

			this.dataProvider = dataProvider;
		}

		public String getMetaDataProvider()
	{

			return metaDataProvider;
		}

		public void setMetaDataProvider( String metaDataProvider )
	{

			this.metaDataProvider = metaDataProvider;
		}

		@Override
		public String toString()
	{

			return "\n- url: " + url + "\n  setName: '" + setName + "' \n  dataProvider: '" + dataProvider
					+ "' \n  metaDataProvider: '" + metaDataProvider + "'\n";
		}
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

	public Integer getTimeout()
{

		return timeout;
	}

	public void setTimeout( Integer timeout )
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

}
