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
package org.osaf.cosmo.service.account;


import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.User;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class EmailAccountActivator extends AbstractCosmoAccountActivator {
    private static final Log log =
        LogFactory.getLog(EmailAccountActivator.class);

    private IdentifierGenerator idGenerator;
    private JavaMailSender mailSender;
    private ResourceBundleMessageSource messageSource;

    private static final String MSG_ACTIVATION_SUBJECT =
        "Email.Activation.Subject";
    private static final String MSG_ACTIVATION_HANDLE =
        "Email.Activation.FromHandle";
    private static final String MSG_ACTIVATION_TEXT =
        "Email.Activation.Text";


    public String generateActivationToken() {

        return this.getIdGenerator().nextIdentifier().toString();
    }

    public void sendActivationMessage(final User user,
            final ActivationContext activationContext) {

        mailSender.send(new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage)
            throws MessagingException {

                Locale locale = activationContext.getLocale();

                User sender = activationContext.getSender();
                String fromAddr = sender.getEmail();
                String fromHandle =
                    messageSource.getMessage(MSG_ACTIVATION_HANDLE,
                            new Object[] {},
                            locale);

                String subject =
                    messageSource.getMessage(MSG_ACTIVATION_SUBJECT,
                        new Object[] {user.getUsername()},
                        locale);

                String text =
                    messageSource.getMessage(MSG_ACTIVATION_TEXT,
                        new Object[] {user.getUsername(),
                                      activationContext.getHostname(),
                                      activationContext.getActivationLink(
                                              user.getActivationId())},
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
        log.info("Account activation link sent to " + user.getEmail());

    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        // TODO Auto-generated method stub

    }

    public IdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdentifierGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
