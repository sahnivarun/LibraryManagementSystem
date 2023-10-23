import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int userId;
    private String dateTime;
    private double totalCost;
    private String shippingAddress;
    private String creditCardNumber;

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int userId, String dateTime, double totalCost, String shippingAddress, String creditCardNumber) {
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

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        if (creditCardNumber.length() >= 4) {
            // Extract the last 4 digits
            String last4Digits = creditCardNumber.substring(creditCardNumber.length() - 4);

            // Create a string with asterisks of the same length as the original number
            StringBuilder maskedCreditCard = new StringBuilder();
            for (int i = 0; i < creditCardNumber.length() - 4; i++) {
                maskedCreditCard.append('*');
            }

            // Append the last 4 digits to the masked string
            maskedCreditCard.append(last4Digits);

            this.creditCardNumber = maskedCreditCard.toString();
        } else {
            // If the credit card number is less than 4 digits, keep it as is
            this.creditCardNumber = creditCardNumber;
        }
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
