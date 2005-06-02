package org.osaf.cosmo.ui;

import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.struts.validator.BeanValidatorForm;

import org.osaf.commons.struts.OSAFStrutsConstants;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.User;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.javamail.MimeMessageHelper;

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
    private static final String MSG_USERNAME_REMINDER_SUBJECT =
        "Email.UsernameReminder.Subject";
    private static final String MSG_USERNAME_REMINDER_TEXT =
        "Email.UsernameReminder.Text";
    private static final String MSG_PASSWORD_RESET_SUBJECT =
        "Email.PasswordReset.Subject";
    private static final String MSG_PASSWORD_RESET_TEXT =
        "Email.PasswordReset.Text";

    private JavaMailSender mailSender;
    private ProvisioningManager mgr;

    /**
     */
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     */
    public void setProvisioningManager(ProvisioningManager mgr) {
        this.mgr = mgr;
    }

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
        User user = mgr.getUserByEmail(email);

        if (wasUsernameButtonClicked(forgotForm)) {
            sendUsernameReminderMessage(request, user);
            saveConfirmationMessage(request, MSG_CONFIRM_USERNAME);
        }
        if (wasPasswordButtonClicked(forgotForm)) {
            String newPassword = generatePassword();
            user.setPassword(newPassword);
            mgr.updateUser(user);
            sendPasswordResetMessage(request, user, newPassword);
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

    private String generatePassword() {
        // XXX
        return "deadbeef";
    }

    private void sendUsernameReminderMessage(final HttpServletRequest request,
                                             final User user) {
        mailSender.send(new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage)
                    throws MessagingException {
                    MessageResources resources = getResources(request);
                    Locale locale = getLocale(request);

                    String subject =
                        resources.getMessage(locale,
                                             MSG_USERNAME_REMINDER_SUBJECT);
                    String text =
                        resources.getMessage(locale,
                                             MSG_USERNAME_REMINDER_TEXT,
                                             user.getUsername());

                    MimeMessageHelper message =
                        new MimeMessageHelper(mimeMessage);
                    // XXX serverAdmin config property
                    message.setFrom("root@localhost");
                    message.setTo(user.getEmail());
                    message.setSubject(subject);
                    message.setText(text);
                }
            });
    }

    private void sendPasswordResetMessage(final HttpServletRequest request,
                                          final User user,
                                          final String newPassword) {
        mailSender.send(new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage)
                    throws MessagingException {
                    MessageResources resources = getResources(request);
                    Locale locale = getLocale(request);

                    String subject =
                        resources.getMessage(locale,
                                             MSG_PASSWORD_RESET_SUBJECT);
                    String text =
                        resources.getMessage(locale,
                                             MSG_PASSWORD_RESET_TEXT,
                                             newPassword);

                    MimeMessageHelper message =
                        new MimeMessageHelper(mimeMessage);
                    // XXX serverAdmin config property
                    message.setFrom("root@localhost");
                    message.setTo(user.getEmail());
                    message.setSubject(subject);
                    message.setText(text);
                }
            });
    }
}
