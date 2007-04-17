/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.service.account;

import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class EmailPasswordRecoverer extends PasswordRecoverer {
    private JavaMailSender mailSender;
    private MessageSource messageSource;
    
    private static final String MSG_PASSWORD_RECOVERY_TEXT = 
        "Email.PasswordReset.Text";
    private static final String MSG_PASSWORD_RECOVERY_SUBJECT = 
        "Email.PasswordReset.Subject";
    private static final String MSG_PASSWORD_RECOVERY_HANDLE = 
        "Email.PasswordReset.FromHandle";
    
    private static final Log log = LogFactory.getLog(EmailPasswordRecoverer.class);
    
    public void sendRecovery(final PasswordRecovery passwordRecovery,
                             final PasswordRecoveryMessageContext context) {
        // Create final variable so it is available in message preparator below 
        final User user = passwordRecovery.getUser();

        mailSender.send(new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage)
            throws MessagingException {
                
                Locale locale = context.getLocale();

                User sender = context.getSender();
                String fromAddr = sender.getEmail();
                String fromHandle =
                    messageSource.getMessage(MSG_PASSWORD_RECOVERY_HANDLE,
                            new Object[] {},
                            locale);

                String subject =
                    messageSource.getMessage(MSG_PASSWORD_RECOVERY_SUBJECT,
                        new Object[] {user.getUsername()},
                        locale);

                String text =
                    messageSource.getMessage(MSG_PASSWORD_RECOVERY_TEXT,
                        new Object[] {user.getUsername(),
                                      context.getHostname(),
                                      context.getRecoveryLink()},
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
        log.info("Password recovery link sent to " + user.getEmail() +
                 " with key " + passwordRecovery.getKey());

    }
    
    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
