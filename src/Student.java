public class Student {
        private int studentID;
        private String studentName;
        private String emailID;
        private String studentNumber;

        // Constructor with parameters
        public Student(int studentID, String studentName, String emailID, String studentNumber) {
                this.studentID = studentID;
                this.studentName = studentName;
                this.emailID = emailID;
                this.studentNumber = studentNumber;
        }

        // Getter and Setter methods for studentID
        public int getStudentID() {
                return studentID;
        }

        public void setStudentID(int studentID) {
                this.studentID = studentID;
        }

        // Getter and Setter methods for studentName
        public String getStudentName() {
                return studentName;
        }

        public void setStudentName(String studentName) {
                this.studentName = studentName;
        }

        // Getter and Setter methods for emailID
        public String getEmailID() {
                return emailID;
        }

        public void setEmailID(String emailID) {
                this.emailID = emailID;
        }

        // Getter and Setter methods for studentNumber
        public String getStudentNumber() {
                return studentNumber;
        }

        public void setStudentNumber(String studentNumber) {
                this.studentNumber = studentNumber;
        }

        // Special function to get the full student information
        public String getFullStudent() {
                return "Student ID: " + studentID +
                        "\nStudent Name: " + studentName +
                        "\nEmail ID: " + emailID +
                        "\nStudent Number: " + studentNumber;
        }

        // Override toString method
        @Override
        public String toString() {
                return "Student ID: " + studentID +
                        "\nStudent Name: " + studentName +
                        "\nEmail ID: " + emailID +
                        "\nStudent Number: " + studentNumber;
        }
}
