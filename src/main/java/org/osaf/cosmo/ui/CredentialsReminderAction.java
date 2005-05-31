package org.osaf.cosmo.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.BeanValidatorForm;

import org.osaf.commons.struts.OSAFStrutsConstants;

/**
 * A {@link CosmoAction} that generates email reminders for forgotten
 * usernames and passwords.
 */
public class CredentialsReminderAction extends CosmoAction {
    private static final Log log =
        LogFactory.getLog(CredentialsReminderAction.class);

    private static final String FORM_EMAIL = "email";
    private static final String FORM_BUTTON_USERNAME = "username";
    private static final String FORM_BUTTON_PASSWORD = "password";
    private static final String MSG_CONFIRM_USERNAME =
        "Forgot.Confirm.Username";
    private static final String MSG_CONFIRM_PASSWORD =
        "Forgot.Confirm.Password";

    /**
     * Looks up the user for the entered email address and:
     *
     * <ul>
     * <li> If the username button was clicked, sends a reminder email
     * containing the username to the user's email address
     * <li> If the password button was clicked, reset's the user's
     * password and sends a confirmation email containing the new
     * password to the user's email address
     * </ul>
     *
     * @see OSAFStrutsConstants#FWD_OK
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        BeanValidatorForm forgotForm = (BeanValidatorForm) form;
        String email = (String) forgotForm.get(FORM_EMAIL);

        log.debug("email address: " + email);

        if (wasUsernameButtonClicked(forgotForm)) {
            saveConfirmationMessage(request, MSG_CONFIRM_USERNAME);
        }
        if (wasPasswordButtonClicked(forgotForm)) {
            saveConfirmationMessage(request, MSG_CONFIRM_PASSWORD);
        }

        return mapping.findForward(OSAFStrutsConstants.FWD_OK);
    }

    public boolean wasUsernameButtonClicked(BeanValidatorForm form) {
        return form.get(FORM_BUTTON_USERNAME) != null;
    }
    public boolean wasPasswordButtonClicked(BeanValidatorForm form) {
        return form.get(FORM_BUTTON_PASSWORD) != null;
    }
}
