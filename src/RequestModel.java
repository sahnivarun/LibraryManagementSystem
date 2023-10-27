public class RequestModel {
    public static final int EXIT_REQUEST = 0;
    public static final int CUSTOMER_REQUEST = 1;
    public static final int ORDER_REQUEST = 2;
    public static final int SAVE_ORDER_BOOK_REQUEST= 22;
    public static final int LOAD_ORDER_REQUEST = 222;
    public static final int UPDATE_ORDER_REQUEST = 2222;
    public static final int DELETE_ORDER_REQUEST = 22222;
    public static final int LOAD_BOOK_REQUEST = 3;
    public static final int SAVE_BOOK_REQUEST = 33;
    public static final int DELETE_BOOK_REQUEST = 333;
    public static final int USER_REQUEST = 4;
    public static final int LOAD_USER_REQUEST = 44;
    public static final int SAVE_USER_REQUEST = 444;
    public static final int UPDATE_USER_REQUEST = 4444;
    public static final int DELETE_USER_REQUEST = 44444;
    public static final int SAVE_STUDENT_REQUEST = 5;
    public static final int UPDATE_STUDENT_REQUEST = 55;
    public static final int LOAD_STUDENT_REQUEST = 555;
    public static final int DELETE_STUDENT_REQUEST = 5555;
    public static final int SAVE_RECEIPT_REQUEST = 7;
    public static final int UPDATE_RECEIPT_REQUEST = 77;
    public static final int LOAD_RECEIPT_REQUEST = 777;
    public static final int DELETE_RECEIPT_REQUEST = 7777;
    public static final int GET_ORDER_COUNT_REQUEST = 8;

    public int code;
    public String body;
}
