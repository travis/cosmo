package org.osaf.cosmo.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.validator.ValidatorForm;
import org.apache.struts.action.ActionMapping;

/**
 * Action for managing users.
 */
public class UserForm extends ValidatorForm {

    private String id;
    private String username;
    private String email;
    private String password;
    private String confirm;
    private String[] role;

    /**
     */
    public UserForm() {
        initialize();
    }

    /**
     */
    public String getId() {
        return id;
    }

    /**
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     */
    public void setId(Long id) {
        this.id = id.toString();
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
    public String getConfirm() {
        return confirm;
    }

    /**
     */
    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    /**
     */
    public String[] getRole() {
        return role;
    }

    /**
     */
    public void setRole(String[] role) {
        this.role = role;
    }

    /**
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        super.reset(mapping, request);
        initialize();
    }

    /**
     */
    private void initialize() {
        username = null;
        email = null;
        password = null;
        confirm = null;
        role = new String[0];
    }
}
