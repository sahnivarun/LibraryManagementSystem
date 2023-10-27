public class Book {

    private int bookID;
    private String bookName;
    private String authorName;
    private double quantity;
    private String status;

    public Book(){

    }
    public Book(int bookID, String bookName, String authorName, double quantity, String status) {
        this.bookID = bookID;
        this.bookName = bookName;
        this.authorName = authorName;
        this.quantity = quantity;
        this.status = status;

    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
