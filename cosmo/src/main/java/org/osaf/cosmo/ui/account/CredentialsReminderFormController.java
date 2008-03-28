/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.ui.UIConstants;

import org.springframework.web.servlet.mvc.SimpleFormController;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * A {@link CosmoAction} that generates email reminders for forgotten
 * usernames and passwords.
 */
public class CredentialsReminderFormController extends SimpleFormController {
    private static final Log log =
        LogFactory.getLog(CredentialsReminderFormController.class);

    private static final String FORM_EMAIL = "email";
    private static final String FORM_BUTTON_USERNAME = "username";
    private static final String FORM_BUTTON_PASSWORD = "password";
    private static final String MSG_ERROR_EMAIL_NOT_FOUND =
        "Forgot.Error.EmailNotFound";
    private static final String MSG_CONFIRM_USERNAME =
        "Forgot.Confirm.Username";
    private static final String MSG_CONFIRM_PASSWORD =
        "Forgot.Confirm.Password";
    private static final String MSG_USERNAME_REMINDER_FROMHANDLE =
        "Email.UsernameReminder.FromHandle";
    private static final String MSG_USERNAME_REMINDER_SUBJECT =
        "Email.UsernameReminder.Subject";
    private static final String MSG_USERNAME_REMINDER_TEXT =
        "Email.UsernameReminder.Text";
    private static final String MSG_PASSWORD_RESET_FROMHANDLE =
        "Email.PasswordReset.FromHandle";
    private static final String MSG_PASSWORD_RESET_SUBJECT =
        "Email.PasswordReset.Subject";
    private static final String MSG_PASSWORD_RESET_TEXT =
        "Email.PasswordReset.Text";
    
    private ResourceBundleMessageSource messageSource;

    private JavaMailSender mailSender;
    private UserService userService;

    /**
     */
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
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
     * @see UIConstants#FWD_OK
     *
    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {

        throws Exception {
        BeanValidatorForm forgotForm = (BeanValidatorForm) form;
        String email = (String) forgotForm.get(FORM_EMAIL);
        User user = null;
        try {
            user = userService.getUserByEmail(email);
        } catch (DataRetrievalFailureException e) {
            saveErrorMessage(request, MSG_ERROR_EMAIL_NOT_FOUND);
            return mapping.findForward(UIConstants.FWD_FAILURE);
        }

        if (wasUsernameButtonClicked(forgotForm)) {
            sendUsernameReminderMessage(request, response, user);
            saveConfirmationMessage(request, MSG_CONFIRM_USERNAME);
        }
        if (wasPasswordButtonClicked(forgotForm)) {
            String newPassword = userService.generatePassword();
            user.setPassword(newPassword);
            userService.updateUser(user);
            sendPasswordResetMessage(request, response, user, newPassword);
            saveConfirmationMessage(request, MSG_CONFIRM_PASSWORD);
        }

        return mapping.findForward(UIConstants.FWD_OK);
    }

    public boolean wasUsernameButtonClicked(BeanValidatorForm form) {
        return form.get(FORM_BUTTON_USERNAME) != null;
    }

    public boolean wasPasswordButtonClicked(BeanValidatorForm form) {
        return form.get(FORM_BUTTON_PASSWORD) != null;
    }

    private void sendUsernameReminderMessage(final HttpServletRequest request,
                                             final HttpServletResponse response,
                                             final User user) {
        mailSender.send(new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage)
                    throws MessagingException {
                    MessageResources resources = getResources(request);
                    Locale locale = getLocale(request);

                    User rootUser = userService.getUser(User.USERNAME_OVERLORD);
                    String fromAddr = (String) getServlet().getServletContext().
                        getAttribute(CosmoConstants.SC_ATTR_SERVER_ADMIN);
                    String fromHandle =
                        resources.getMessage(locale,
                                             MSG_USERNAME_REMINDER_FROMHANDLE);
                    String subject =
                        resources.getMessage(locale,
                                             MSG_USERNAME_REMINDER_SUBJECT);
                    String text =
                        resources.getMessage(locale,
                                             MSG_USERNAME_REMINDER_TEXT,
                                             user.getUsername(),
                                             getContextRelativeURL(request,
                                                                   "/"),
                                             rootUser.getEmail());

                    MimeMessageHelper message =
                        new MimeMessageHelper(mimeMessage);
                    message.setFrom("\"" + fromHandle + "\" <" + fromAddr +
                                    ">");
                    message.setTo(user.getEmail());
                    message.setSubject(subject);
                    message.setText(text);
                }
            });
    }
*/
    private void sendPasswordResetMessage(final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final User user,
                                          final String newPassword) {
        mailSender.send(new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage)
                    throws MessagingException {
                    Locale locale = request.getLocale();

                    User rootUser = userService.getUser(User.USERNAME_OVERLORD);
                    String fromAddr = rootUser.getEmail();
                    String fromHandle =
                        messageSource.getMessage(
                                             MSG_PASSWORD_RESET_FROMHANDLE, new Object[]{}, locale);
                    
                    String subject =
                        messageSource.getMessage(MSG_PASSWORD_RESET_SUBJECT, new Object[]{},locale);
                    String text =
                        messageSource.getMessage(MSG_PASSWORD_RESET_TEXT,
                                new Object[] {newPassword,
                                              request.getContextPath(),
                                              rootUser.getEmail()},
                                locale);
                    MimeMessageHelper message =
                        new MimeMessageHelper(mimeMessage);
                    message.setFrom("\"" + fromHandle + "\" <" + fromAddr +
                                    ">");
                    message.setTo(user.getEmail());
                    message.setSubject(subject);
                    message.setText(text);
                }
            });
    }
}
