/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

package space.kiyoshi.hexaecon.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

class MongoDBManager(connectionString: String, databaseName: String) {
    private val mongoClient: MongoClient = MongoClients.create(connectionString)

    fun close() {
        mongoClient.close()
    }
}