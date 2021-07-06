package eu.cessda.eqb.harvester;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Repo
{
    private String code;
    private URI url;
    private final HashSet<String> sets = new HashSet<>();
    private final HashSet<String> metadataPrefixes = new HashSet<>();
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

    public Set<String> getSets()
    {
        return sets;
    }

    public void setSets( Collection<String> sets )
    {
        Objects.requireNonNull( sets, "sets must not be null" );
        this.sets.clear();
        this.sets.addAll( sets );
    }

    public Set<String> getMetadataPrefixes()
    {
        return metadataPrefixes;
    }

    public void setMetadataPrefixes( Collection<String> metadataPrefixes )
    {
        Objects.requireNonNull( metadataPrefixes, "metadataFormats must not be null" );
        this.metadataPrefixes.clear();
        this.metadataPrefixes.addAll( metadataPrefixes );
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
                sets.equals( repo.sets ) &&
                metadataPrefixes.equals( repo.metadataPrefixes );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( code, url, sets, metadataPrefixes, discoverSets );
    }

    @Override
    public String toString()
    {
        return "Repo{" +
                "code='" + code + '\'' +
                ", url=" + url +
                ", sets='" + sets + '\'' +
                ", metadataPrefixes='" + metadataPrefixes + '\'' +
                ", discoverSets=" + discoverSets +
                '}';
    }
}
