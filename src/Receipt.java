import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int studentId;
    private String dateTime;
    private double totalCost;
    private String shippingAddress;

    public String getBooks() {
        return books;
    }

    public void setBooks(String books) {
        this.books = books;
    }

    private String books;

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int userId, String dateTime, double totalCost, String shippingAddress, String books) {
        this.receiptNumber = receiptNumber;
        this.orderId = orderId;
        this.studentId = userId;
        this.dateTime = dateTime;
        this.totalCost = totalCost;
        this.shippingAddress = shippingAddress;
        this.books= books;
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
        return studentId;
    }

    public void setUserId(int studentId) {
        this.studentId = studentId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
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

    @Override
    public String toString() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date(dateTime));

        return "Receipt{" +
                "receiptNumber=" + receiptNumber +
                ", orderId=" + orderId +
                ", studentID=" + studentId +
                ", timestamp=" + formattedDate +
                ", totalCost=" + totalCost +
                ", shippingAddress='" + shippingAddress + '\'' +
                '}';
    }
}
