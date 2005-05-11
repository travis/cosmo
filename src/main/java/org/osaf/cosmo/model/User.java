package org.osaf.cosmo.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 */
public class User extends BaseModelObject {

    private Long id;
    private String username;
    private String password;
    private String email;
    private Date dateCreated;
    private Date dateModified;
    private Set roles;

    /**
     */
    public User() {
        roles = new HashSet();
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
    public String getUsername() {
        return username;
    }

    /**
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     */
    public String getPassword() {
        return password;
    }

    /**
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     */
    public String getEmail() {
        return email;
    }

    /**
     */
    public void setEmail(String email) {
        this.email = email;
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
    public Set getRoles() {
        return roles;
    }

    /**
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     */
    public void removeRole(Role role) {
        roles.remove(role);
    }

    /**
     */
    public void setRoles(Set roles) {
        this.roles = roles;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof User)) {
            return false;
        }
        User it = (User) o;
        return new EqualsBuilder().
            append(username, it.username).
            append(password, it.password).
            append(email, it.email).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(username).
            append(password).
            append(email).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("username", username).
            append("password", password).
            append("email", email).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
}
