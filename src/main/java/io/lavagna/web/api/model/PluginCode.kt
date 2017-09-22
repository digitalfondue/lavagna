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
package io.lavagna.web.api.model

/**
 * Plugin configuration model
 *
 * @param name Integration name
 * @param code Javascript code
 * @param properties Map of key-value properties
 * @param projects List of projects that this plugin is enabled for
 * @param metadata Integration metadata. Contains description and configuration for the properties
 *
 * Properties are defined in the metadata parameter. A configuration object is contained, where each property has a label and type attached to it.
 */
class PluginCode(var name: String, var code: String?, var properties: Map<String, String>?, var projects: List<String> ?, var metadata: Map<String, Any>?)
