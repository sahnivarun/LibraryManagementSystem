import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;

import static util.PortAddresses.MAIN_SERVER_PORT;

public class RemoteDataAdapter implements DataAccess {
    private int orderCount;
    private Gson gson = new Gson();
    private Socket s = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;

    @Override
    public void connect() {
        System.out.println("connect");
        try {
            s = new Socket("localhost", MAIN_SERVER_PORT);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (Exception ex) {
            System.out.println("connect Ex:" + ex.toString());
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean saveProduct(Product product) {
        connect(); // Establish the connection before sending data
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_PRODUCT_REQUEST;
        req.body = gson.toJson(product);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Product saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the product on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the product on the server.");
        } finally {
            disconnect(); // Close the connection after sending/receiving data
        }

        return false;
    }

    @Override
    public boolean saveOrder(Order order) {
        connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_ORDER_REQUEST;
        req.body = gson.toJson(order);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Order saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the order on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the order on the server.");
        } finally {
            disconnect();
        }

        return false;
    }

    @Override
    public boolean saveShippingAddress(ShippingAddress address) {
        connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_SHIPPING_ADDRESS_REQUEST;
        req.body = gson.toJson(address);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Shipping address saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the shipping address on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the shipping address on the server.");
        } finally {
            disconnect();
        }

        return false;
    }

    @Override
    public boolean saveCreditCard(CreditCard card) {
        connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_CREDIT_CARD_REQUEST;
        req.body = gson.toJson(card);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Credit card saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the credit card on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the credit card on the server.");
        } finally {
            disconnect();
        }

        return false;
    }

    @Override
    public boolean saveReceipt(Receipt receipt) {
        connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_RECEIPT_REQUEST;
        req.body = gson.toJson(receipt);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Receipt saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the receipt on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the receipt on the server.");
        } finally {
            disconnect();
        }

        return false;
    }

    @Override
    public User loadUser(String username, String password) {
        System.out.println("loadUser()");
        connect();
        System.out.println("Setting up RequstModel");
        RequestModel req = new RequestModel();
        req.code = RequestModel.LOAD_USER_REQUEST;

        // Create a User object with the provided username and password
        User user = new User(username, password);
        System.out.println("User : " + user.toString());

        req.body = gson.toJson(user);

        String json = gson.toJson(req);
        System.out.println("json: " + json);
        try {
            System.out.println("Inside try");
            dos.writeUTF(json);
            System.out.println("dosWrite Complete ");
            String received = dis.readUTF();  // No data received
            System.out.println("Received: " + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            System.out.println("res: " + res.toString() + "  code: " + res.code + " body: "+ res.body);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return null;
            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
                System.out.println("The Server could not find a user with that username and password!");
                return null;
            } else {
                User loadedUser = gson.fromJson(res.body, User.class);
                System.out.println("Receiving a User object");
                System.out.println("Username = " + loadedUser.getUsername());
                return loadedUser;
            }

        } catch (Exception ex) {
            System.out.println("exception: " + ex.toString() );
            ex.printStackTrace();
        }
            disconnect();


        return null;
    }

    @Override
    public Product loadProduct(int productID) {
        connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.LOAD_PRODUCT_REQUEST;
        req.body = String.valueOf(productID);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            System.out.println("Server response:" + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return null;
            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
                System.out.println("The Server could not find a product with that ID!");
                return null;
            } else {
                Product model = gson.fromJson(res.body, Product.class);
                System.out.println("Receiving a ProductModel object");
                System.out.println("ProductID = " + model.getProductID());
                System.out.println("Product name = " + model.getName());
                return model;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            disconnect();
        }

        return null;
    }

    @Override
    public int getOrderCount() {
        connect();

        // Create a request to fetch the order count
        RequestModel req = new RequestModel();
        req.code = RequestModel.GET_ORDER_COUNT_REQUEST;

        try {
            String json = gson.toJson(req);
            dos.writeUTF(json);

            // Receive the response from the server
            String received = dis.readUTF();
            System.out.println("Server response: " + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return -1; // Return an error value
            } else {
                int orderCount = Integer.parseInt(res.body); // Fetch and return the order count
                System.out.println("Order count received from the server: " + orderCount);
                return orderCount;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            disconnect(); // Assuming this method closes the connection to the server
        }

        return -1; // Return an error value if there's an issue
    }

}
