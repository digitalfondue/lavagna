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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jsoup.Jsoup;
import org.springframework.integration.mail.ImapMailReceiver;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MailTicketService {

    private static final Logger LOG = LogManager.getLogger();

    private final MailTicketRepository mailTicketRepository;
    private final CardService cardService;
    private final CardDataService cardDataService;
    private final UserRepository userRepository;
    private final EventEmitter eventEmitter;
    private final BoardRepository boardRepository;

    private final static String EMAIL_PROVIDER = "ticket-email";
    private final static String DEFAULT_INBOX = "inbox";
    private final static Pattern CARD_SHORT_NAME = Pattern.compile(".?(?<shortname>[A-Z0-9]+)\\-(?<sequence>[0-9]+).?");

    public MailTicketService(MailTicketRepository mailTicketRepository,
                             CardService cardService,
                             CardDataService cardDataService,
                             UserRepository userRepository,
                             EventEmitter eventEmitter,
                             BoardRepository boardRepository) {
        this.mailTicketRepository = mailTicketRepository;
        this.cardService = cardService;
        this.cardDataService = cardDataService;
        this.userRepository = userRepository;
        this.eventEmitter = eventEmitter;
        this.boardRepository = boardRepository;
    }

    public List<ProjectMailTicketConfig> findAllByProject(final int projectId) {
        return mailTicketRepository.findAllByProject(projectId);
    }

    public ProjectMailTicketConfig findConfig(final int id) {
        return mailTicketRepository.findConfig(id);
    }

    public ProjectMailTicket findTicket(final int id) {
        return mailTicketRepository.findTicket(id);
    }

    @Transactional(readOnly = false)
    public ProjectMailTicketConfig addConfig(final String name, final int projectId, final ProjectMailTicketConfigData config, final String subject, final String body) {
        mailTicketRepository.addConfig(name, projectId, config, subject, body);

        return mailTicketRepository.findLastCreatedConfig();
    }

    @Transactional(readOnly = false)
    public int updateConfig(final int id, final String name, final boolean enabled, final ProjectMailTicketConfigData config, final String subject, final String body, final int projectId) {
        return mailTicketRepository.updateConfig(id, name, enabled, config, subject, body, projectId);
    }

    @Transactional(readOnly = false)
    public int deleteConfig(final int id, final int projectId) {
        return mailTicketRepository.deleteConfig(id, projectId);
    }

    @Transactional(readOnly = false)
    public ProjectMailTicket addTicket(final String name, final String alias, final boolean useAlias, final int columnId, final int configId, final String metadata) {
        mailTicketRepository.addTicket(name, alias, useAlias, columnId, configId, metadata);

        return  mailTicketRepository.findLastCreatedTicket();
    }

    @Transactional(readOnly = false)
    public int updateTicket(final int id, final String name, final boolean enabled, final String alias, final boolean useAlias, final int columnId, final int configId, final String metadata) {
        return mailTicketRepository.updateTicket(id, name, enabled, alias, useAlias, columnId, configId, metadata);
    }

    @Transactional(readOnly = false)
    public int deleteTicket(final int id) {
        return mailTicketRepository.deleteTicket(id);
    }

    public void checkNew() {
        List<ProjectMailTicketConfig> entries = mailTicketRepository.findAll();

        for(ProjectMailTicketConfig entry: entries) {
            if(entry.getEntries().size() == 0 || !entry.getEnabled()) {
                continue;
            }

            MailReceiver receiver = entry.getConfig().getInboundProtocol().startsWith("pop3") ?
                getPop3MailReceiver(entry.getConfig()) :
                getImapMailReceiver(entry.getConfig());

            try {
                Date updateLastChecked = entry.getLastChecked();

                Object[] messages = receiver.receive();
                LOG.debug("found {} messages", messages.length);

                for(int i = 0; i < messages.length; i++) {
                    MimeMessage message = (MimeMessage) messages[i];
                    if(!message.getReceivedDate().after(entry.getLastChecked())) {
                        continue;
                    } else {
                        updateLastChecked = message.getReceivedDate().after(updateLastChecked) ?
                            message.getReceivedDate() :
                            updateLastChecked;
                    }

                    String deliveredTo = getDeliveredTo(message);

                    for(ProjectMailTicket ticketConfig: entry.getEntries()) {
                        if(ticketConfig.getEnabled() && ticketConfig.getAlias().equals(deliveredTo)) {
                            String from = getFrom(message);
                            String name = getName(message);
                            Matcher m = CARD_SHORT_NAME.matcher(message.getSubject());

                            if(!m.find() ||
                                (m.find() && !cardService.existCardWith(m.group("shortname"), Integer.parseInt(m.group("sequence"))))) {
                                try {
                                    ImmutablePair<Card, User> cardAndUser = createCard(message.getSubject(), getTextFromMessage(message), from, ticketConfig.getColumnId());

                                    notify(cardAndUser.getLeft(), entry, ticketConfig, cardAndUser.getRight(), from, name);
                                } catch (IOException|MessagingException e) {
                                    LOG.error("failed to parse message body", e);
                                }
                            }
                        }
                    }
                }

                mailTicketRepository.updateLastChecked(entry.getId(), updateLastChecked);
            } catch (MessagingException e) {
                LOG.error("could not retrieve messages for ticket mail config id: {}", entry.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    private ImmutablePair<Card, User> createCard(String name, String description, String username, int columnId) {
        if(!userRepository.userExists(EMAIL_PROVIDER, username)) {
            userRepository.createUser(EMAIL_PROVIDER, username, null, null, null, true);
        }
        User user = userRepository.findUserByName(EMAIL_PROVIDER, username);

        Card card = cardService.createCardFromTop(name, columnId, new Date(), user);
        cardDataService.updateDescription(card.getId(), description, new Date(), user.getId());

        return new ImmutablePair<>(card, user);
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

    private MailReceiver getPop3MailReceiver(ProjectMailTicketConfigData config) {
        String sanitizedUsername = sanitizeUsername(config.getInboundUser());
        String inboxFolder = getInboxFolder(config);

        String url = "pop3://"
            + sanitizedUsername + ":" + config.getInboundPassword() + "@"
            + config.getInboundServer()
            + "/" + inboxFolder.toUpperCase();

        Pop3MailReceiver receiver = new Pop3MailReceiver(url);

        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.pop3.port", Integer.toString(config.getInboundPort()));
        if(config.getInboundProtocol().equals("pop3s")) {
            mailProperties.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            mailProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
            mailProperties.setProperty("mail.pop3.socketFactory.port", Integer.toString(config.getInboundPort()));
        }
        mailProperties.putAll(config.generateInboundProperties());
        receiver.setJavaMailProperties(mailProperties);

        return receiver;
    }

    private MailReceiver getImapMailReceiver(ProjectMailTicketConfigData config) {
        String sanitizedUsername = sanitizeUsername(config.getInboundUser());
        String inboxFolder = getInboxFolder(config);

        String url = config.getInboundProtocol() + "://"
            + sanitizedUsername + ":" + config.getInboundPassword() + "@"
            + config.getInboundServer() + ":" + config.getInboundPort()
            + "/" + inboxFolder.toLowerCase();

        ImapMailReceiver receiver = new ImapMailReceiver(url);
        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);

        Properties mailProperties = new Properties();
        if(config.getInboundProtocol().equals("imaps")) {
            mailProperties.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            mailProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
        }
        mailProperties.setProperty("mail.store.protocol", config.getInboundProtocol());
        mailProperties.putAll(config.generateInboundProperties());
        receiver.setJavaMailProperties(mailProperties);

        receiver.afterPropertiesSet();

        return receiver;
    }

    private String getInboxFolder(ProjectMailTicketConfigData config) {
        return config.getInboundInboxFolder() != null && config.getInboundInboxFolder().trim().length() > 0 ?
            config.getInboundInboxFolder() :
            DEFAULT_INBOX;
    }

    private String sanitizeUsername(String username) {
        return username != null ? username.replace("@","%40") : null;
    }

    private String getFrom(MimeMessage message) throws MessagingException {
        Address[] froms = message.getFrom();
        return froms == null ? null : ((InternetAddress) froms[0]).getAddress();
    }

    private String getName(MimeMessage message) throws MessagingException {
        Address[] froms = message.getFrom();
        return froms == null ? null : ((InternetAddress) froms[0]).getPersonal();
    }

    private String getDeliveredTo(MimeMessage message) throws MessagingException {
        return message.getHeader("Delivered-To", "");
    }

    private void notify(Card createdCard, ProjectMailTicketConfig config, ProjectMailTicket ticketConfig, User user, String to, String name) {
        ProjectAndBoard projectAndBoard = boardRepository.findProjectAndBoardByColumnId(createdCard.getColumnId());

        eventEmitter.emitCreateCard(projectAndBoard.getProject().getShortName(), projectAndBoard.getBoard()
            .getShortName(), createdCard.getColumnId(), createdCard, user);

        sendEmail(to, name, createdCard, projectAndBoard.getBoard(), config, ticketConfig);
    }

    private void sendEmail(String to, String name, Card createdCard, Board board, ProjectMailTicketConfig config, ProjectMailTicket ticketConfig) {
        String cardId = board.getShortName() + "-" + createdCard.getSequence();
        String subject = config.getSubject().replaceAll("\\{\\{card}}", cardId);
        String body = config.getBody().replaceAll("\\{\\{card}}", cardId).replaceAll("\\{\\{name}}", name != null ? name : to);

        Parser parser = Parser.builder().build();
        Node document = parser.parse(body);
        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        TextContentRenderer textRendered = TextContentRenderer.builder().build();

        String htmlText = htmlRenderer.render(document);
        String plainText = textRendered.render(document);


        ProjectMailTicketConfigData configData = config.getConfig();
        MailConfig mailConfig = new MailConfig(configData.getOutboundServer(),
            configData.getOutboundPort(),
            configData.getOutboundProtocol(),
            configData.getOutboundUser(),
            configData.getOutboundPassword(),
            ticketConfig.getSendByAlias() ? ticketConfig.getAlias() : configData.getOutboundAddress(),
            configData.getOutboundProperties());

        mailConfig.send(to, subject, plainText, htmlText);
    }
}
