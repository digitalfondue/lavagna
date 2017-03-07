/**
 * This file is part of lavagna.

 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see //www.gnu.org/licenses/>.
 */
package io.lavagna.model

import io.lavagna.common.Json
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertiesLoaderUtils
import java.util.*

class ProjectMailTicketConfigData(val inboundProtocol: String,
                                  val inboundServer: String,
                                  val inboundPort: Int,
                                  val inboundUser: String,
                                  val inboundPassword: String,
                                  val inboundInboxFolder: String?,
                                  val inboundProperties: String?,
                                  val outboundServer: String,
                                  val outboundPort: Int,
                                  val outboundProtocol: String,
                                  val outboundUser: String,
                                  val outboundPassword: String,
                                  val outboundAddress: String,
                                  val outboundProperties: String?) {

    override fun toString(): String {
        return Json.GSON.toJson(this)
    }

    fun generateOutboundProperties(): Properties {
        val properties = Properties()

        if(outboundProperties != null) {
            properties.putAll(PropertiesLoaderUtils.loadProperties(EncodedResource(ByteArrayResource(
                outboundProperties.toByteArray(charset("UTF-8"))), "UTF-8")))
        }

        return properties
    }

    fun generateInboundProperties(): Properties {
        val properties = Properties()

        if(inboundProperties != null) {
            properties.putAll(PropertiesLoaderUtils.loadProperties(EncodedResource(ByteArrayResource(
                inboundProperties.toByteArray(charset("UTF-8"))), "UTF-8")))
        }

        return properties
    }
}


