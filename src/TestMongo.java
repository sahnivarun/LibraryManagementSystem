import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;


public class TestMongo{
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://varunsahni:varunsahni1@store.95blgys.mongodb.net/?retryWrites=true&w=majority";

        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();

        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionString)).serverApi(serverApi).build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("Store");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

                // Specify the collection
                MongoCollection<Document> ordersCollection = database.getCollection("Orders");

                // Find the document with the specified order ID
                Document query = new Document("orderID", 1);
                MongoCursor<Document> cursor = ordersCollection.find(query).iterator();

                try {
                    while (cursor.hasNext()) {
                        Document orderDocument = cursor.next();
                        // Print details of the document
                        System.out.println("Order ID: " + orderDocument.getInteger("orderID"));
                        System.out.println("Order Date: " + orderDocument.getString("orderDate"));
                        System.out.println("Product ID: " + orderDocument.getInteger("productID"));
                        System.out.println("Total Cost: " + orderDocument.getDouble("totalCost"));
                        System.out.println("Total Tax: " + orderDocument.getDouble("totalTax"));
                    }
                } finally {
                    cursor.close();
                }
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
