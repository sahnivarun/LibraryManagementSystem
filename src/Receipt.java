import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int userId;
    private long dateTime;
    private double totalCost;
    private String shippingAddress;
    private String creditCardNumber;

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int userId, long dateTime, double totalCost, String shippingAddress, String creditCardNumber) {
        this.receiptNumber = receiptNumber;
        this.orderId = orderId;
        this.userId = userId;
        this.dateTime = dateTime;
        this.totalCost = totalCost;
        this.shippingAddress = shippingAddress;
        this.creditCardNumber = creditCardNumber;
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

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
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

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    @Override
    public String toString() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date(dateTime));

        return "Receipt{" +
                "receiptNumber=" + receiptNumber +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", timestamp=" + formattedDate +
                ", totalCost=" + totalCost +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                '}';
    }
}
