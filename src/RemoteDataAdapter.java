import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;

public class RemoteDataAdapter implements DataAccess {
    private Gson gson = new Gson();
    private Socket s = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;

    @Override
    public void connect() {
        try {
            s = new Socket("localhost", 5056);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (IOException ex) {
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
}
