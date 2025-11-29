package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Users
import com.musicapp.models.User
import org.jetbrains.exposed.sql.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*

object UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = UUID.fromString(row[Users.id]),
        username = row[Users.username],
        password = row[Users.password],
        role = row[Users.role]
    )

    // Crea un nuevo usuario con contraseña hasheada
    suspend fun createUser(username: String, password: String, role: String = "USER"): User? = dbQuery {
        try {
            val userId = UUID.randomUUID().toString()
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12))

            Users.insert {
                it[id] = userId
                it[Users.username] = username
                it[Users.password] = hashedPassword
                it[Users.role] = role
            }

            User(
                id = UUID.fromString(userId),
                username = username,
                password = hashedPassword,
                role = role
            )
        } catch (e: Exception) {
            println("Error creando usuario: ${e.message}")
            null
        }
    }

   // Busca un usuario por su nombre de usuario
    suspend fun findByUsername(username: String): User? = dbQuery {
        Users.select { Users.username eq username }
            .map { resultRowToUser(it) }
            .singleOrNull()
    }

    //Busca un usuario por su ID
    suspend fun findById(id: UUID): User? = dbQuery {
        Users.select { Users.id eq id.toString() }
            .map { resultRowToUser(it) }
            .singleOrNull()
    }

    // Valida las credenciales del usuario
    suspend fun validateCredentials(username: String, password: String): User? {
        val user = findByUsername(username) ?: return null

        return if (BCrypt.checkpw(password, user.password)) {
            user
        } else {
            null
        }
    }

    // Obtiene todos los usuarios
    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll()
            .map { resultRowToUser(it) }
    }

    // Elimina un usuario por su ID
    suspend fun deleteUser(id: UUID): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id.toString() } > 0
    }

    // Actualiza el rol de un usuario
    suspend fun updateUserRole(id: UUID, newRole: String): Boolean = dbQuery {
        Users.update({ Users.id eq id.toString() }) {
            it[role] = newRole
        } > 0
    }
}