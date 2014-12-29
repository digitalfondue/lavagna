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
package io.lavagna.model;

import java.io.IOException;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * See https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html for additional parameters.
 */
@Getter
public class MailConfig {

	private static final Logger LOG = LogManager.getLogger();

	private final String host;
	private final Integer port;
	private final String protocol;
	private final String username;
	private final String password;
	private final String from;
	private final String properties;

	public MailConfig(String host, int port, String protocol, String username, String password, String from,
			String properties) {
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		this.username = username;
		this.password = password;
		this.from = from;
		this.properties = properties;
	}

	public boolean isMinimalConfigurationPresent() {
		return StringUtils.isNotBlank(host) && port != null && StringUtils.isNotBlank(protocol)
				&& StringUtils.isNotBlank(from);
	}

	public void send(final String to, final String subject, final String text) {
		send(to, subject, text, null);
	}

	public void send(final String to, final String subject, final String text, final String html) {

		toMailSender().send(new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = html == null ? new MimeMessageHelper(mimeMessage, "UTF-8")
						: new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setSubject(subject);
				message.setFrom(getFrom());
				message.setTo(to);
				if (html == null) {
					message.setText(text, false);
				} else {
					message.setText(text, html);
				}
			}
		});
	}

	private JavaMailSender toMailSender() {
		JavaMailSenderImpl r = new JavaMailSenderImpl();
		r.setDefaultEncoding("UTF-8");
		r.setHost(host);
		r.setPort(port);
		r.setProtocol(protocol);
		r.setUsername(username);
		r.setPassword(password);
		if (properties != null) {
			try {
				Properties prop = PropertiesLoaderUtils.loadProperties(new EncodedResource(new ByteArrayResource(
						properties.getBytes("UTF-8")), "UTF-8"));
				r.setJavaMailProperties(prop);
			} catch (IOException e) {
				LOG.warn("error while setting the mail sender properties", e);
			}
		}
		return r;
	}
}
