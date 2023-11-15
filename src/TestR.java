import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import org.json.JSONObject;

public class TestR {

    public static void main(String[] args) {
        // Establish a connection to the Redis database
        try (Jedis jedis = new Jedis("redis://default:varunsahni@redis-17090.c262.us-east-1-3.ec2.cloud.redislabs.com:17090")) {

            String usernameFromUI = "admin";
            String passwordFromUI = "password";

            String productIdFromUI = "1";
            // Display product information
            displayProductInfo(jedis, productIdFromUI);

            // Verify user from Redis
            boolean isUserVerified = verifyUserFromRedis(jedis, usernameFromUI, passwordFromUI);

            // Print verification result
            if (isUserVerified) {
                System.out.println("User verified!");
            } else {
                System.out.println("User not verified.");
            }
        } catch (JedisDataException e) {
            System.out.println("Error accessing Redis data!");
            e.printStackTrace();
        }
    }

    public static void displayProductInfo(Jedis jedis, String productId) {
        // Form the key for the product data in Redis
        String key = "ProductID";

        // Check if the key exists in Redis
        if (jedis.exists(key)) {
            try {
                // Get the type of the value stored in the key
                String keyType = jedis.type(key);
                System.out.println("Type of value in key " + key + ": " + keyType);

                // Check if the value is a JSON document
                if ("string".equals(keyType)) {
                    System.out.println("Inside string equals keyType");
                    // Get the JSON data for the current product from the key
                    String productData = jedis.get(key);
                    System.out.println("Type of productData: " + productData.getClass().getSimpleName());
                    System.out.println("ProductData: " + productData);

                    // Parse JSON data using Jedis
                    JSONObject productsJson = new JSONObject(productData);

                    // Check if the product ID exists in the JSON
                    if (productsJson.has(productId)) {
                        JSONObject productJson = productsJson.getJSONObject(productId);

                        // Display product information
                        System.out.println("Product ID: " + productId);
                        System.out.println("Name: " + productJson.getString("name"));
                        System.out.println("Price: " + productJson.getDouble("price"));
                        System.out.println("Quantity: " + productJson.getInt("quantity"));
                    } else {
                        System.out.println("Product ID not found.");
                    }
                } else {
                    System.out.println("Value in key " + key + " is not a string.");
                }
            } catch (Exception e) {
                System.out.println("Error processing JSON data: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Key not found in Redis.");
        }
    }

    public static boolean verifyUserFromRedis(Jedis jedis, String username, String password) {
        // Form the key for the user data in Redis (assuming the data is in the "User1" hash key)
        String key = "User:1";

        // Check if the key exists in Redis
        if (jedis.exists(key)) {
            // Get the JSON data for the current user from the hash fields
            String userNameFromRedis = jedis.hget(key, "UserName");
            String passwordFromRedis = jedis.hget(key, "Password");

            // Check if the username and password match
            return userNameFromRedis.equals(username) && passwordFromRedis.equals(password);
        }

        return false;
    }



}