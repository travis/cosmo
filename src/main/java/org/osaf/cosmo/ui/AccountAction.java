/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.ui;

import org.osaf.commons.struts.OSAFStrutsConstants;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Action for account self-management
 */
public class AccountAction extends CosmoAction {
    private static final String MSG_ERROR_EMAIL_EXISTS =
        "Account.Form.EmailExists";
    private static final String MSG_ERROR_USERNAME_EXISTS =
        "Account.Form.UsernameExists";
    private static final String MSG_CONFIRM_UPDATE = "Account.Form.Updated";
    private static final Log log = LogFactory.getLog(AccountAction.class);

    /**
     * The request parameter that contains the email address
     * identifying a user.
     */
    public static final String PARAM_EMAIL = "email";
    /**
     * The request parameter that contains the username identifying a
     * user.
     */
    public static final String PARAM_USERNAME = "username";
    /**
     * The request attribute in which this action places an
     * identified User: <code>User</code>
     */
    public static final String ATTR_USER = "User";

    private ProvisioningManager mgr;

    /**
     */
    public void setProvisioningManager(ProvisioningManager mgr) {
        this.mgr = mgr;
    }

    /**
     * Retrieves the identified user.
     */
    public ActionForward view(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        UserForm userForm = (UserForm) form;

        User user = getSecurityManager().getSecurityContext().getUser();
        populateForm(userForm, user);

        request.setAttribute(ATTR_USER, user);

        return mapping.findForward(OSAFStrutsConstants.FWD_OK);
    }

    /**
     * Signs up the specified user.
     */
    public ActionForward signup(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        UserForm userForm = (UserForm) form;
        User formUser = new User();
        populateUser(formUser, userForm);

        // assign the user role to the new account
        Role userRole = mgr.getRoleByName(CosmoSecurityManager.ROLE_USER);
        formUser.addRole(userRole);

        try {
            if (log.isDebugEnabled()) {
                log.debug("signing up user " + formUser.getUsername());
            }
            User user = mgr.saveUser(formUser);

            request.setAttribute(ATTR_USER, user);
        } catch (DataIntegrityViolationException e) {
            handleIntegrityViolation(request, e);
            return mapping.findForward(OSAFStrutsConstants.FWD_FAILURE);
        }

        // log in the user automatically
        CosmoSecurityContext securityContext =
            getSecurityManager().
            establishSecurityContext(userForm.getUsername(),
                                     userForm.getPassword());

        return mapping.findForward(OSAFStrutsConstants.FWD_SUCCESS);
    }
    
    /**
     * Updates the specified user.
     */
    public ActionForward update(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        UserForm userForm = (UserForm) form;

        try {
            User formUser = getSecurityManager().getSecurityContext().getUser();
            populateUser(formUser, userForm);
            if (log.isDebugEnabled()) {
                log.debug("updating user " + formUser.getUsername());
            }
            User user = mgr.updateUser(formUser);

            request.setAttribute(ATTR_USER, user);
            saveConfirmationMessage(request, MSG_CONFIRM_UPDATE);
        } catch (DataIntegrityViolationException e) {
            handleIntegrityViolation(request, e);
            return mapping.findForward(OSAFStrutsConstants.FWD_FAILURE);
        }

        return mapping.findForward(OSAFStrutsConstants.FWD_SUCCESS);
    }

    private void populateUser(User user, UserForm form) {
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        // password is required for signup but not for update. on
        // update, don't blank out the saved password if the user is
        // only changing username and/or email address.
        if (form.getPassword() != null && ! form.getPassword().equals("")) {
            user.setPassword(form.getPassword());
        }
    }

    private void populateForm(UserForm form, User user) {
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        // never set password into the form
    }

    // would be great if this exception told us which constraint was
    // violated.. we have to work it out for ourself, breaking
    // encapsulation
    private void handleIntegrityViolation(HttpServletRequest request,
                                          DataIntegrityViolationException e) {
        if (e.getCause() instanceof SQLException) {
            if (e.getCause().getMessage().toLowerCase().
                startsWith("unique constraint violation: email")) {
                saveErrorMessage(request, MSG_ERROR_EMAIL_EXISTS,
                                 PARAM_EMAIL);
                return;
            } else if (e.getCause().getMessage().toLowerCase().
                       startsWith("unique constraint violation: username")) {
                saveErrorMessage(request, MSG_ERROR_USERNAME_EXISTS,
                                 PARAM_USERNAME);
                return;
            }
        }
        throw e;
    }
}
