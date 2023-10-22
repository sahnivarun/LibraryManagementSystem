import java.util.Date;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int userId;
    private long timestamp;
    private double totalCost;
    private String shippingAddress;
    private String creditCardNumber;

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int userId, long timestamp, double totalCost, String shippingAddress) {
        this.receiptNumber = receiptNumber;
        this.orderId = orderId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.totalCost = totalCost;
        this.shippingAddress = shippingAddress;
    }

    // Getters and setters for all fields

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(int receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptNumber=" + receiptNumber +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                ", totalCost=" + totalCost +
                ", shippingAddress='" + shippingAddress + '\'' +
                '}';
    }
}

