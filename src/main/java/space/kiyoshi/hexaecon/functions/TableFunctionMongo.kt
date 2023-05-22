@file:Suppress("unused", "SameParameterValue", "UnnecessaryVariable")

package space.kiyoshi.hexaecon.functions

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
import space.kiyoshi.hexaecon.utils.GetConfig

object TableFunctionMongo {
    private val dataeconomyvalue = GetConfig.main().getString("DataBase.DataEconomyName")!!
    private val mongohost = GetConfig.main().getString("MongoDB.Host")!!
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

        // Create a collection if it doesn't exist
        if (!collectionExists(mongoClient, collectionName)) {
            database.createCollection(collectionName)
        }

        // Insert the default value if the collection is empty
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

        // Create a collection if it doesn't exist
        if (!collectionExists(mongoClient, collectionName)) {
            database.createCollection(collectionName)
        }

        // Insert the specified value into the collection
        val document = Document(dataeconomyvalue, value)
        collection.insertOne(document)
    }


    fun dropCollection(name: String) {
        val collectionName = name

        val database = mongoClient.getDatabase("hexaecon")

        // Drop the collection if it exists
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