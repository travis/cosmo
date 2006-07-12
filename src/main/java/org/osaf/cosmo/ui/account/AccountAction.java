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
package org.osaf.cosmo.ui.account;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.UserService;
// XXX: we really should have a separate AccountForm rather than
// reusing UserForm, but since we're moving away from struts soon
// anyway, it hardly matters
import org.osaf.cosmo.ui.CosmoAction;
import org.osaf.cosmo.ui.UIConstants;
import org.osaf.cosmo.ui.admin.user.UserForm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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

    private UserService userService;

    /**
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
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

        return mapping.findForward(UIConstants.FWD_OK);
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

        try {
            if (log.isDebugEnabled()) {
                log.debug("signing up user " + formUser.getUsername());
            }
            User user = userService.createUser(formUser);
            request.setAttribute(ATTR_USER, user);

            // set the new security context
            getSecurityManager().
                initiateSecurityContext(userForm.getUsername(),
                                        userForm.getPassword());
        } catch (DuplicateEmailException e) {
            saveErrorMessage(request, MSG_ERROR_EMAIL_EXISTS, PARAM_EMAIL);
            return mapping.findForward(UIConstants.FWD_FAILURE);
        } catch (DuplicateUsernameException e) {
            saveErrorMessage(request, MSG_ERROR_USERNAME_EXISTS,
                             PARAM_USERNAME);
            return mapping.findForward(UIConstants.FWD_FAILURE);
        }

        return mapping.findForward(UIConstants.FWD_SUCCESS);
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
            User user = userService.updateUser(formUser);

            // if the password was changed, update the
            // security context with the new password
            if (userForm.getPassword() != null &&
                ! userForm.getPassword().equals("")) {
                getSecurityManager().
                    initiateSecurityContext(formUser.getUsername(),
                                            userForm.getPassword());
            }

            request.setAttribute(ATTR_USER, user);
            saveConfirmationMessage(request, MSG_CONFIRM_UPDATE);
        } catch (DuplicateEmailException e) {
            saveErrorMessage(request, MSG_ERROR_EMAIL_EXISTS, PARAM_EMAIL);
            return mapping.findForward(UIConstants.FWD_FAILURE);
        } catch (DuplicateUsernameException e) {
            saveErrorMessage(request, MSG_ERROR_USERNAME_EXISTS,
                             PARAM_USERNAME);
            return mapping.findForward(UIConstants.FWD_FAILURE);
        }

        return mapping.findForward(UIConstants.FWD_SUCCESS);
    }

    private void populateUser(User user, UserForm form) {
        // user does not get to change his username once it's set
        if (user.getUsername() == null) {
            user.setUsername(form.getUsername());
        }
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        // password is required for signup but not for update. on
        // update, don't blank out the saved password if the user is
        // only changing username and/or email address.
        if (form.getPassword() != null && ! form.getPassword().equals("")) {
            user.setPassword(form.getPassword());
        }
    }

    private void populateForm(UserForm form, User user) {
        form.setUsername(user.getUsername());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        // never set password into the form
    }
}
