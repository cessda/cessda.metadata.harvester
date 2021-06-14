package eu.cessda.eqb.harvester;

import java.net.URI;
import java.util.Objects;

public class Repo
{
    private String code;
    private URI url;
    private String set;
    private String metadataFormat;
    private boolean discoverSets;

    public String getCode()
    {
        return code;
    }

    public void setCode( String code )
    {
        this.code = code;
    }

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
                Objects.equals( code, repo.code ) &&
                Objects.equals( url, repo.url ) &&
                Objects.equals( set, repo.set ) &&
                Objects.equals( metadataFormat, repo.metadataFormat );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( code, url, set, metadataFormat, discoverSets );
    }

    @Override
    public String toString()
    {
        return "Repo{" +
                "code='" + code + '\'' +
                ", url=" + url +
                ", set='" + set + '\'' +
                ", metadataFormat='" + metadataFormat + '\'' +
                ", discoverSets=" + discoverSets +
                '}';
    }
}
