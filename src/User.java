import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int userID;
    private String username;
    private String password;
    private String fullName;
    private boolean isManager;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public static User fromJson(String json) {
        try {
            // Assuming your JSON data is represented as a JSONObject
            JSONObject jsonObject = new JSONObject(json);

            User user = new User();
            user.setUserID(jsonObject.getInt("UserID"));
            user.setUsername(jsonObject.getString("UserName"));
            user.setPassword(jsonObject.getString("Password"));
            user.setFullName(jsonObject.getString("DisplayName"));
            // Set other fields as needed

            return user;
        } catch (JSONException e) {
            // Handle JSON parsing exceptions
            e.printStackTrace();
            return null;
        }
    }

}
