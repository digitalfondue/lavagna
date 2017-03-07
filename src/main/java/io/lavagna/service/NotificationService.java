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

import com.samskivert.mustache.Escapers;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import com.samskivert.mustache.Template.Fragment;
import io.lavagna.model.*;
import io.lavagna.query.NotificationQuery;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Handle the whole email notification process.
 */
@Service
@Transactional(readOnly = false)
public class NotificationService {

    private static final Logger LOG = LogManager.getLogger();

    private final ConfigurationRepository configurationRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CardDataRepository cardDataRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    private final MessageSource messageSource;

    private final NamedParameterJdbcTemplate jdbc;
    private final NotificationQuery queries;

    private final Template emailTextTemplate;
    private final Template emailHtmlTemplate;


    public NotificationService(ConfigurationRepository configurationRepository, UserRepository userRepository,
        CardDataRepository cardDataRepository, CardRepository cardRepository,
        BoardColumnRepository boardColumnRepository, MessageSource messageSource, NamedParameterJdbcTemplate jdbc,
        NotificationQuery queries) {
        this.configurationRepository = configurationRepository;
        this.userRepository = userRepository;
        this.cardDataRepository = cardDataRepository;
        this.cardRepository = cardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.messageSource = messageSource;
        this.jdbc = jdbc;
        this.queries = queries;

        com.samskivert.mustache.Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false).defaultValue("");
        try {
            emailTextTemplate = compiler.compile(new InputStreamReader(
                new ClassPathResource("/io/lavagna/notification/email.txt")
                    .getInputStream(), StandardCharsets.UTF_8));
            emailHtmlTemplate = compiler
                .compile(new InputStreamReader(new ClassPathResource(
                    "/io/lavagna/notification/email.html")
                    .getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Return a list of user id to notify.
     *
     * @param upTo
     * @return
     */
    public Set<Integer> check(Date upTo) {

        final List<Integer> userWithChanges = new ArrayList<>();
        List<SqlParameterSource> res = jdbc.query(queries.countNewForUsersId(), new RowMapper<SqlParameterSource>() {

            @Override
            public SqlParameterSource mapRow(ResultSet rs, int rowNum) throws SQLException {
                int userId = rs.getInt("USER_ID");
                userWithChanges.add(userId);
                return new MapSqlParameterSource("count", rs.getInt("COUNT_EVENT_ID")).addValue("userId", userId);
            }
        });

        if (!res.isEmpty()) {
            jdbc.batchUpdate(queries.updateCount(), res.toArray(new SqlParameterSource[res.size()]));
        }
        queries.updateCheckDate(upTo);

        // select users that have pending notifications that were not present in this check round
        MapSqlParameterSource userWithChangesParam = new MapSqlParameterSource("userWithChanges", userWithChanges);
        //
        List<Integer> usersToNotify = jdbc.queryForList(queries.usersToNotify() + " "
            + (userWithChanges.isEmpty() ? "" : queries.notIn()), userWithChangesParam, Integer.class);
        //
        jdbc.update(queries.reset() + " " + (userWithChanges.isEmpty() ? "" : queries.notIn()), userWithChangesParam);
        //
        return new TreeSet<>(usersToNotify);
    }

    private List<String> composeCardSection(List<Event> events, EventsContext context) {
        //

        List<String> res = new ArrayList<>();
        for (Event e : events) {
            if (EnumUtils.isValidEnum(SupportedEventType.class, e.getEvent().toString())) {
                ImmutablePair<String, String[]> message = SupportedEventType.valueOf(e.getEvent().toString())
                    .toKeyAndParam(e, context, cardDataRepository);
                res.add(messageSource.getMessage(message.getKey(), message.getValue(), Locale.ENGLISH));
            }
        }
        return res;
    }

    private ImmutableTriple<String, String, String> composeEmailForUser(EventsContext context)
        throws MustacheException, IOException {

        List<Map<String, Object>> cardsModel = new ArrayList<>();

        StringBuilder subject = new StringBuilder();
        for (Entry<Integer, List<Event>> kv : context.events.entrySet()) {

            Map<String, Object> cardModel = new HashMap<>();

            CardFull cf = context.cards.get(kv.getKey());
            StringBuilder cardName = new StringBuilder(cf.getBoardShortName()).append("-").append(cf.getSequence())
                .append(" ").append(cf.getName());

            cardModel.put("cardFull", cf);
            cardModel.put("cardName", cardName.toString());
            cardModel.put("cardEvents", composeCardSection(kv.getValue(), context));

            subject.append(cf.getBoardShortName()).append("-").append(cf.getSequence()).append(", ");

            cardsModel.add(cardModel);
        }

        Map<String, Object> tmplModel = new HashMap<>();
        String baseApplicationUrl = StringUtils
            .appendIfMissing(configurationRepository.getValue(Key.BASE_APPLICATION_URL), "/");
        tmplModel.put("cards", cardsModel);
        tmplModel.put("baseApplicationUrl", baseApplicationUrl);
        tmplModel.put("htmlEscape", new Mustache.Lambda() {
            @Override
            public void execute(Fragment frag, Writer out) throws IOException {
                out.write(Escapers.HTML.escape(frag.execute()));
            }
        });

        String text = emailTextTemplate.execute(tmplModel);
        String html = emailHtmlTemplate.execute(tmplModel);

        return ImmutableTriple.of(subject.substring(0, subject.length() - ", ".length()), text, html);
    }

    /**
     * Send email (if all the conditions are met) to the user.
     *
     * @param userId
     * @param upTo
     * @param emailEnabled
     * @param mailConfig
     */
    public void notifyUser(int userId, Date upTo, boolean emailEnabled, MailConfig mailConfig) {
        Date lastSent = queries.lastEmailSent(userId);

        User user = userRepository.findById(userId);

        Date fromDate = ObjectUtils.firstNonNull(lastSent, DateUtils.addDays(upTo, -1));

        List<Event> events = user.getSkipOwnNotifications() ?
            queries.eventsForUserWithoutHisOwns(userId, fromDate, upTo) : queries.eventsForUser(userId, fromDate, upTo);

        if (!events.isEmpty() && mailConfig != null && mailConfig.getMinimalConfigurationPresent() && emailEnabled
            && user.canSendEmail()) {
            try {
                sendEmailToUser(user, events, mailConfig);
            } catch (MustacheException | IOException | MailException e) {
                LOG.warn("Error while sending an email to user with id " + user.getId(), e);
            }
        }

        //
        queries.updateSentEmailDate(upTo, userId);
    }

    private void sendEmailToUser(User user, List<Event> events, MailConfig mailConfig) throws MustacheException,
        IOException {

        Set<Integer> userIds = new HashSet<>();
        userIds.add(user.getId());
        Set<Integer> cardIds = new HashSet<>();
        Set<Integer> cardDataIds = new HashSet<>();
        Set<Integer> columnIds = new HashSet<>();

        for (Event e : events) {
            cardIds.add(e.getCardId());
            userIds.add(e.getUserId());

            addIfNotNull(userIds, e.getValueUser());
            addIfNotNull(cardIds, e.getValueCard());

            addIfNotNull(cardDataIds, e.getDataId());
            addIfNotNull(cardDataIds, e.getPreviousDataId());

            addIfNotNull(columnIds, e.getColumnId());
            addIfNotNull(columnIds, e.getPreviousColumnId());
        }

        final ImmutableTriple<String, String, String> subjectAndText = composeEmailForUser(new EventsContext(events,
            userRepository.findByIds(userIds), cardRepository.findAllByIds(cardIds),
            cardDataRepository.findDataByIds(cardDataIds), boardColumnRepository.findByIds(columnIds)));

        mailConfig.send(user.getEmail(),"Lavagna: " + subjectAndText.getLeft(),
            subjectAndText.getMiddle(), subjectAndText.getRight());
    }

    private static <T> void addIfNotNull(Set<T> s, T v) {
        if (v != null) {
            s.add(v);
        }
    }
}
