package com.larsknoke.db

import org.ktorm.database.Database

object DatabaseConnection {
    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/notes_app",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "root"
    )
}

