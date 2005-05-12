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

import org.osaf.commons.struts.OSAFMappingDispatchAction;
import org.osaf.commons.struts.OSAFStrutsConstants;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
 * Action for managing users.
 */
public class UserAction extends OSAFMappingDispatchAction {
    private static final String MSG_ERROR_EXISTS = "User.Form.Exists";
    private static final String MSG_CONFIRM_CREATE = "User.Form.Created";
    private static final String MSG_CONFIRM_UPDATE = "User.Form.Updated";
    private static final String MSG_CONFIRM_REMOVE = "User.Form.Removed";
    private static final Log log = LogFactory.getLog(UserAction.class);

    /**
     * The request parameter that contains the username identifying a
     * user.
     */
    public static final String PARAM_USERNAME = "username";
    /**
     * The request parameter that contains the id identifying a
     * user.
     */
    public static final String PARAM_ID = "id";
    /**
     * The request attribute in which this action places an
     * identified User: <code>User</code>
     */
    public static final String ATTR_USER = "User";
    /**
     * The request attribute in which this action places a List of
     * Users: <code>Users</code>
     */
    public static final String ATTR_USERS = "Users";
    /**
     * The request attribute in which this action places a sorted
     * List of the Roles to which a user can be assigned.
     */
    public static final String ATTR_ROLES = "Roles";

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

        // the User may have previously been set by a request
        // attribute. if not, look to see if the form has id info. if
        // not, we're viewing the user for the first time.
        User user = (User) request.getAttribute(ATTR_USER);
        if (user == null) {
            if (userForm.getId() != null && ! userForm.getId().equals("")) {
                user = mgr.getUser(userForm.getId());
            }
            else {
                String username = request.getParameter(PARAM_USERNAME);
                if (log.isDebugEnabled()) {
                    log.debug("viewing user " + username);
                }
                user = mgr.getUserByUsername(username);
                populateUpdateForm(userForm, user);
            }
        }

        request.setAttribute(ATTR_USER, user);
        request.setAttribute(ATTR_ROLES, getSortedRoles());

        addTitleParam(request, user.getUsername());

        return mapping.findForward(OSAFStrutsConstants.FWD_OK);
    }

    /**
     * Creates the specified user.
     */
    public ActionForward create(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        UserForm userForm = (UserForm) form;
        User formUser = new User();
        populateUser(formUser, userForm);

        try {
            if (log.isDebugEnabled()) {
                log.debug("creating user " + formUser.getUsername());
            }
            User user = mgr.saveUser(formUser);

            request.setAttribute(ATTR_USER, user);
            saveConfirmationMessage(request, MSG_CONFIRM_CREATE);
        } catch (DataIntegrityViolationException e) {
            saveErrorMessage(request, MSG_ERROR_EXISTS, userForm.getUsername());
            return mapping.findForward(OSAFStrutsConstants.FWD_FAILURE);
        }

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

        if (isCancelled(request)) {
            userForm.reset(mapping, request);
            return mapping.findForward(OSAFStrutsConstants.FWD_CANCEL);
        }

        try {
            User formUser =  mgr.getUser(userForm.getId());
            populateUser(formUser, userForm);
            if (log.isDebugEnabled()) {
                log.debug("updating user " + formUser.getUsername());
            }
            User user = mgr.updateUser(formUser);

            request.setAttribute(ATTR_USER, user);
            saveConfirmationMessage(request, MSG_CONFIRM_UPDATE);
        } catch (DataIntegrityViolationException e) {
            saveErrorMessage(request, MSG_ERROR_EXISTS, userForm.getUsername());
            return mapping.findForward(OSAFStrutsConstants.FWD_FAILURE);
        }

        return mapping.findForward(OSAFStrutsConstants.FWD_SUCCESS);
    }

    /**
     * Removes the identified user.
     */
    public ActionForward remove(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        String id = request.getParameter(PARAM_ID);

        if (id != null) {
            if (log.isDebugEnabled()) {
                log.debug("removing user " + id);
            }
            mgr.removeUser(id);

            saveConfirmationMessage(request, MSG_CONFIRM_REMOVE);
        }

        return mapping.findForward(OSAFStrutsConstants.FWD_SUCCESS);
    }

    /**
     * Retrieves a list of all users.
     */
    public ActionForward list(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        UserForm userForm = (UserForm) form;

        // if the form has not yet been submitted, default the role
        // selection
        if (request.getParameter(OSAFStrutsConstants.PARAM_BUTTON_CREATE) ==
            null) {
            Role userRole = mgr.getRoleByName(CosmoSecurityManager.ROLE_USER);
            String[] roleIds = { userRole.getId().toString() };
            userForm.setRole(roleIds);
        }

        request.setAttribute(ATTR_USERS, getSortedUsers());
        request.setAttribute(ATTR_ROLES, getSortedRoles());

        return mapping.findForward(OSAFStrutsConstants.FWD_OK);
    }

    private List getSortedUsers() {
        if (log.isDebugEnabled()) {
            log.debug("listing users");
        }
        List users = mgr.getUsers();
        Collections.sort(users);

        return users;
    }

    private List getSortedRoles() {
        if (log.isDebugEnabled()) {
            log.debug("listing roles");
        }
        List roles = mgr.getRoles();
        Collections.sort(roles);
        return roles;
    }

    private void populateUser(User user, UserForm form) {
        if (user.getId() == null) {
            // this is a creation action, where username is modifiable
            user.setUsername(form.getUsername());
        }
        user.setEmail(form.getEmail());
        if (form.getPassword() != null && ! form.getPassword().equals("")) {
            user.setPassword(form.getPassword());
        }
        String[] roleIds = form.getRole();
        HashMap idx = new HashMap();
        for (int i=0; i<roleIds.length; i++) {
            Role role = mgr.getRole(roleIds[i]);
            idx.put(roleIds[i], role);
            user.addRole(role);
        }
        for (Iterator i=user.getRoles().iterator(); i.hasNext();) {
            Role role = (Role) i.next();
            if (idx.get(role.getId().toString()) == null) {
                i.remove();
            }
        }
    }

    private void populateUpdateForm(UserForm form, User user) {
        form.setId(user.getId());
        form.setEmail(user.getEmail());
        // never set password in the form
        Role[] roles = (Role[]) user.getRoles().toArray(new Role[0]);
        form.setRole(mapRolesToRoleIds(roles));
    }

    private String[] mapRolesToRoleIds(Role[] roles) {
        String[] roleIds = new String[roles.length];
        for (int i=0; i<roles.length; i++) {
            roleIds[i] = roles[i].getId().toString();
        }
        return roleIds;
    }
}
