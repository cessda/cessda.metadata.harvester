package eu.cessda.eqb.harvester;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A configuration of a remote repository.
 */
public class Repo implements Serializable
{
    private static final long serialVersionUID = 3176537038193526776L;

    private String code;
    private URI url;
    private final HashSet<String> sets = new HashSet<>();
    private final HashSet<String> metadataPrefixes = new HashSet<>();
    private boolean discoverSets;

    /**
     * Gets the friendly name of the repository.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Sets the friendly name of the repository.
     */
    public void setCode( String code )
    {
        this.code = code;
    }

    /**
     * Gets the URL of the repository endpoint.
     */
    public URI getUrl()
    {
        return url;
    }

    /**
     * Sets the URL of the repository endpoint.
     */
    public void setUrl( URI url )
    {
        this.url = url;
    }

    /**
     * Gets the sets to harvest from the repository.
     */
    public Set<String> getSets()
    {
        return sets;
    }

    /**
     * Sets the sets to harvest from the repository.
     */
    public void setSets( Collection<String> sets )
    {
        Objects.requireNonNull( sets, "sets must not be null" );
        this.sets.clear();
        this.sets.addAll( sets );
    }

    /**
     * Gets the metadata prefixes to harvest from the repository
     */
    public Set<String> getMetadataPrefixes()
    {
        return metadataPrefixes;
    }

    /**
     * Sets the metadata prefixes to harvest from the repository
     */
    public void setMetadataPrefixes( Collection<String> metadataPrefixes )
    {
        Objects.requireNonNull( metadataPrefixes, "metadataFormats must not be null" );
        this.metadataPrefixes.clear();
        this.metadataPrefixes.addAll( metadataPrefixes );
    }

    /**
     * If true, the repository should be queried for the sets contained.
     */
    public boolean discoverSets()
    {
        return discoverSets;
    }

    /**
     * Sets if the repository should be queried for its sets.
     */
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
