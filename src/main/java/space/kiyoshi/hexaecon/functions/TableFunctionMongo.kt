/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

@file:Suppress("unused", "SameParameterValue", "UnnecessaryVariable")

package space.kiyoshi.hexaecon.functions

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
import space.kiyoshi.hexaecon.utils.DataManager

object TableFunctionMongo {
    private val dataeconomyvalue = DataManager.main().getString("DataBase.DataEconomyName")!!
    private val mongohost = DataManager.main().getString("MongoDB.Host")!!
    private val mongoClient: MongoClient = MongoClients.create(mongohost)

    fun insertValueIntoDocument(databaseName: String, collectionName: String, field: String, value: Any) {
        val connectionString = ConnectionString(mongohost)
        val mongoClient: MongoClient = MongoClients.create(connectionString)
        val database = mongoClient.getDatabase(databaseName)
        val collection = database.getCollection(collectionName)
        val document = Document(field, value)
        collection.insertOne(document)
    }


    fun createCollection(name: String) {
        val collectionName = name
        val database = mongoClient.getDatabase("hexaecon")
        val collection = database.getCollection(collectionName)
        if (!collectionExists(mongoClient, collectionName)) {
            database.createCollection(collectionName)
        }
        if (collection.countDocuments() == 0L) {
            val defaultValue = 0L
            val defaultDocument = Document(dataeconomyvalue, defaultValue)
            collection.insertOne(defaultDocument)
        }
    }


    fun createCollectionAmount(name: String, value: Long) {
        val collectionName = name
        val database = mongoClient.getDatabase("hexaecon")
        val collection = database.getCollection(collectionName)
        if (!collectionExists(mongoClient, collectionName)) {
            database.createCollection(collectionName)
        }
        val document = Document(dataeconomyvalue, value)
        collection.insertOne(document)
    }


    fun dropCollection(name: String) {
        val collectionName = name
        val database = mongoClient.getDatabase("hexaecon")
        if (collectionExists(mongoClient, collectionName)) {
            database.getCollection(collectionName).drop()
        }
    }

    fun selectAllFromCollectionAsString(collectionName: String): List<String> {
        val database = mongoClient.getDatabase("hexaecon")
        val collection = database.getCollection(collectionName)
        val results = mutableListOf<String>()
        try {
            val cursor = collection.find()
            for (document in cursor) {
                val coinsValue = document.getLong("coins")
                results.add("$coinsValue")
            }
            cursor.cursor().close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    private fun collectionExists(database: MongoClient, collectionName: String): Boolean {
        val collectionNames = database.getDatabase("hexaecon").listCollectionNames().toList()
        return collectionNames.contains(collectionName)
    }


}