package eu.cessda.eqb.harvester;

import java.net.URI;
import java.util.Objects;

public class Repo
{
    private URI url;
    private String set;
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

    public String getSet()
    {
        return set;
    }

    public void setSet( String set )
    {
        this.set = set;
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
        return discoverSets == repo.discoverSets &&
                Objects.equals( url, repo.url ) &&
                Objects.equals( set, repo.set ) &&
                Objects.equals( metadataFormat, repo.metadataFormat );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( url, set, metadataFormat, discoverSets );
    }

    @Override
    public String toString()
    {
        return "Repo{" +
                "url=" + url +
                ", setName='" + set + '\'' +
                ", metadataFormat='" + metadataFormat + '\'' +
                ", discoverSets=" + discoverSets +
                '}';
    }
}
