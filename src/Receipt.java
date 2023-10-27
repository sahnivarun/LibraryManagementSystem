import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private int studentId;
    private String dateTime;

    private String student;
    private String books;

    public String getBooks() {
        return books;
    }

    public void setBooks(String books) {
        this.books = books;
    }

    public Receipt() {
    }

    public Receipt(int receiptNumber, int orderId, int studentId, String dateTime, String student, String books) {
        this.receiptNumber = receiptNumber;
        this.orderId = orderId;
        this.studentId = studentId;
        this.dateTime = dateTime;
        this.student = student;
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

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
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
                ", studentDetails='" + student + '\'' +
                '}';
    }
}
