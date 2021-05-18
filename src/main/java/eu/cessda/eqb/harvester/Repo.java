package eu.cessda.eqb.harvester;

import java.net.URI;
import java.util.Objects;

public class Repo
{
    private URI url;
    private String setName;
    private String dataProvider;
    private String metaDataProvider;
    private String metadataFormat;
    private boolean discoverSets;

    public URI getUrl()
    {
        return url;
    }

    public void setUrl( URI url )
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

    public String getMetadataFormat()
    {
        return metadataFormat;
    }

    public void setMetadataFormat( String metadataFormat )
    {
        this.metadataFormat = metadataFormat;
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
        return discoverSets == repo.discoverSets && Objects.equals( url, repo.url ) && Objects.equals( setName, repo.setName ) && Objects.equals( dataProvider, repo.dataProvider ) && Objects.equals( metaDataProvider, repo.metaDataProvider ) && Objects.equals( metadataFormat, repo.metadataFormat );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( url, setName, dataProvider, metaDataProvider, metadataFormat, discoverSets );
    }

    @Override
    public String toString()
    {
        return "Repo{" +
                "url=" + url +
                ", setName='" + setName + '\'' +
                ", dataProvider='" + dataProvider + '\'' +
                ", metaDataProvider='" + metaDataProvider + '\'' +
                ", metadataFormat='" + metadataFormat + '\'' +
                ", discoverSets=" + discoverSets +
                '}';
    }
}
