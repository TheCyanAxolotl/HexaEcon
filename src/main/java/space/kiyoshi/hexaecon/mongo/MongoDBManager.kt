@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package space.kiyoshi.hexaecon.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

class MongoDBManager(connectionString: String, databaseName: String) {
    private val mongoClient: MongoClient = MongoClients.create(connectionString)
    private val database = mongoClient.getDatabase(databaseName)

    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    fun insertDocument(collectionName: String, document: Document) {
        val collection = getCollection(collectionName)
        collection.insertOne(document)
    }

    fun updateDocument(collectionName: String, filter: Document, update: Document) {
        val collection = getCollection(collectionName)
        collection.updateOne(filter, update)
    }

    fun deleteDocument(collectionName: String, filter: Document) {
        val collection = getCollection(collectionName)
        collection.deleteOne(filter)
    }

    fun close() {
        mongoClient.close()
    }
}