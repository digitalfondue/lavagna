/**
 * This file is part of lavagna.
 *
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.service;

import io.lavagna.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class MailTicketService {

    private static final Logger LOG = LogManager.getLogger();

    private final MailTicketRepository mailTicketRepository;
    private final CardService cardService;
    private final CardDataService cardDataService;
    private final UserRepository userRepository;

    private final static String EMAIL_PROVIDER = "ticket-email";

    public MailTicketService(MailTicketRepository mailTicketRepository,
                             CardService cardService,
                             CardDataService cardDataService,
                             UserRepository userRepository) {
        this.mailTicketRepository = mailTicketRepository;
        this.cardService = cardService;
        this.cardDataService = cardDataService;
        this.userRepository = userRepository;
    }

    public void checkNew() {
        List<ProjectMailTicketConfig> entries = mailTicketRepository.findAll();

        for(ProjectMailTicketConfig entry: entries) {
            MailReceiver receiver = entry.getConfig().getInboundProtocol().startsWith("pop3") ?
                getPop3MailReceiver(entry.getConfig(), entry.getProperties()) :
                getImapMailReceiver(entry.getConfig(), entry.getProperties());

            try {
                Object[] messages = receiver.receive();
                LOG.info("found {} messages", messages.length);
                for(int i = 0; i < messages.length; i++) {
                    MimeMessage message = (MimeMessage) messages[i];

                    String deliveredTo = message.getHeader("Delivered-To", "");

                    for(ProjectMailTicket ticketConfig: entry.getEntries()) {
                        if(entry.getConfig().getFrom().equals(deliveredTo)) {
                            Address[] froms = message.getFrom();
                            String from = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
                            try {
                                createCard(message.getSubject(), getTextFromMessage(message), ticketConfig.getColumnId(), from);
                            } catch (IOException|MessagingException e) {
                                LOG.error("failed to parse message content", e);
                            }
                        }
                    }
                }
            } catch (MessagingException e) {
                LOG.error("could not retrieve messages for ticket mail config id: {}", entry.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    private void createCard(String name, String description, int columnId, String username) {
        if(!userRepository.userExists(EMAIL_PROVIDER, username)) {
            userRepository.createUser(EMAIL_PROVIDER, username, username, null, true);
        }
        User user = userRepository.findUserByName(EMAIL_PROVIDER, username);

        Card card = cardService.createCardFromTop(name, columnId, new Date(), user);
        cardDataService.updateDescription(card.getId(), description, new Date(), user.getId());
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
        MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    private MailReceiver getPop3MailReceiver(ProjectMailTicketConfigData config, Properties properties) {
        String sanitizedUsername = sanitizeUsername(config.getInboundUser());

        String url = "pop3://"
            + sanitizedUsername + ":" + config.getInboundPassword() + "@"
            + config.getInboundServer()
            + "/INBOX";

        Pop3MailReceiver receiver = new Pop3MailReceiver(url);

        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.pop3.port", Integer.toString(config.getInboundPort()));
        if(config.getInboundProtocol().equals("pop3s")) {
            mailProperties.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            mailProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
            mailProperties.setProperty("mail.pop3.socketFactory.port", Integer.toString(config.getInboundPort()));
        }
        mailProperties.setProperty("mail.debug", "true");
        mailProperties.putAll(properties);
        receiver.setJavaMailProperties(mailProperties);

        return receiver;
    }

    private MailReceiver getImapMailReceiver(ProjectMailTicketConfigData config, Properties properties) {
        return null;
    }

    private String sanitizeUsername(String username) {
        return username != null ? username.replace("@","%40") : null;
    }
}
