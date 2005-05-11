package org.osaf.cosmo.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 */
public class Role extends BaseModelObject {

    private Long id;
    private String name;
    private Date dateCreated;
    private Date dateModified;
    private Set users;

    /**
     */
    public Role() {
        users = new HashSet();
    }

    /**
     */
    public Long getId() {
        return id;
    }

    /**
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     */
    public String getName() {
        return name;
    }

    /**
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     */
    public Set getUsers() {
        return users;
    }

    /**
     */
    public void setUsers(Set users) {
        this.users = users;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof Role)) {
            return false;
        }
        Role it = (Role) o;
        return new EqualsBuilder().
            append(name, it.name).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(name).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("name", name).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
}
