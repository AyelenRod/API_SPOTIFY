package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Users
import com.musicapp.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import java.util.*

object UserRepository {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        username = row[Users.username],
        password = row[Users.password],
        role = row[Users.role]
    )

    suspend fun createUser(username: String, password: String, role: String = "USER"): User? = dbQuery {
        try {
            val newId = UUID.randomUUID()
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))

            Users.insert {
                it[id] = newId
                it[Users.username] = username
                it[Users.password] = hashedPassword
                it[Users.role] = role
            }

            User(
                id = newId,
                username = username,
                password = hashedPassword,
                role = role
            )
        } catch (e: Exception) {
            println("Error creando usuario: ${e.message}")
            null
        }
    }

    suspend fun findByUsername(username: String): User? = dbQuery {
        Users.select { Users.username eq username }
            .map { resultRowToUser(it) }
            .singleOrNull()
    }

    suspend fun findById(id: UUID): User? = dbQuery {
        Users.select { Users.id eq id }
            .map { resultRowToUser(it) }
            .singleOrNull()
    }

    suspend fun validateCredentials(username: String, password: String): User? {
        val user = findByUsername(username) ?: return null
        return if (BCrypt.checkpw(password, user.password)) user else null
    }

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { resultRowToUser(it) }
    }

    suspend fun deleteUser(id: UUID): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    suspend fun updateUserRole(id: UUID, newRole: String): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[role] = newRole
        } > 0
    }
}