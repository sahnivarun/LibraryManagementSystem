import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int studentId;
    private String dateTime;

    private String studentDetails;

    public String getBooks() {
        return books;
    }

    public void setBooks(String books) {
        this.books = books;
    }

    private String books;

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int studentId, String dateTime, String studentDetails, String books) {
        this.receiptNumber = receiptNumber;
        this.orderId = orderId;
        this.studentId = studentId;
        this.dateTime = dateTime;
        this.studentDetails = studentDetails;
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

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getStudentDetails() {
        return studentDetails;
    }

    public void setStudentDetails(String studentDetails) {
        this.studentDetails = studentDetails;
    }

    @Override
    public String toString() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(new Date(dateTime));

        return "Receipt{" +
                "receiptNumber=" + receiptNumber +
                ", orderId=" + orderId +
                ", studentID=" + studentId +
                ", timestamp=" + formattedDate +
                ", studentDetails='" + studentDetails + '\'' +
                '}';
    }
}
