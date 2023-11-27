import java.util.Date;
import java.text.SimpleDateFormat;

public class Receipt {
    private int receiptNumber;
    private int orderId;
    private String dateTime;
    private String student;
    private int studentID;
    private String books;

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = Integer.parseInt(studentID);
    }

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
                "Order Number=" + receiptNumber +
                ", timestamp=" + formattedDate +
                ", studentDetails='" + student + '\'' +
                '}';
    }
}
