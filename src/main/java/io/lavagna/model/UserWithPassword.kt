package io.lavagna.model

import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper


open class UserWithPassword(@ConstructorAnnotationRowMapper.Column("USER_NAME") val username: String,
                            @ConstructorAnnotationRowMapper.Column("USER_PASSWORD") val password: String) {
}
