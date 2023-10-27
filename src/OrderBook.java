import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class OrderBook {

    private int orderID;
    private int studentID;
    private String orderDate;
    private String returnDate;

    private ShippingAddress shippingAddress;
    private CreditCard creditCard;
    private List<OrderLineBook> lines;

    public OrderBook() {
        lines = new ArrayList<>();
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public List<OrderLineBook> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineBook> lines) {
        this.lines = lines;
    }

    public void addLine(OrderLineBook line) {
        lines.add(line);
    }

    public void removeLine(OrderLineBook line) {
        lines.remove(line);
    }
}
