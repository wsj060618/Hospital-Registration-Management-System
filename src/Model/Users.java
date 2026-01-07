package Model;
import Dao.UserDao;
public class Users {
    private String username;
    private String password;
    private String identity;

    public Users() {
    }

    public Users(String username, String password, String identity) {
        this.username = username;
        this.password = password;
        this.identity = identity;
    }
    // 获取用户名
    public String getUsername() {
        return username;
    }
    // 获取身份
    public String getIdentity() {
        return identity;
    }
    // 获取密码
    private String getPassword() {
        return password;
    }
    // 设置用户名
    public void setUsername(String username) {
        this.username = username;
    }
    // 设置密码
    public void setPassword(String password) {
        this.password = password;
    }
    // 设置身份
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String accessGetPassword(Object caller) {
        // 仅允许 UserDao 类调用
        if (caller instanceof UserDao) {
            return getPassword();
        } else {
            throw new SecurityException("权限不足！仅 UserDao 可访问");
        }
    }
}
