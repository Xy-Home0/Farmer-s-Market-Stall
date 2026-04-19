
/**
 * FACTORY DESIGN PATTERN
 * Creates User objects based on their role type.
 */
public class UserFactory {

    public static User createUser(int userId, String username, String password,
                                  String fullName, String email, String phone,
                                  String roleStr) {
        User.UserRole role;
        switch (roleStr.toUpperCase().replace(" ", "_")) {
            case "ADMIN":
                role = User.UserRole.ADMIN;
                break;
            case "MARKET_MANAGER":
            case "MANAGER":
                role = User.UserRole.MARKET_MANAGER;
                break;
            case "FARMER":
            default:
                role = User.UserRole.FARMER;
                break;
        }
        return new User(userId, username, password, fullName, email, phone, role);
    }

    public static User createAdmin(int userId, String username, String password,
                                   String fullName, String email, String phone) {
        return new User(userId, username, password, fullName, email, phone, User.UserRole.ADMIN);
    }

    public static User createFarmer(int userId, String username, String password,
                                    String fullName, String email, String phone) {
        return new User(userId, username, password, fullName, email, phone, User.UserRole.FARMER);
    }

    public static User createMarketManager(int userId, String username, String password,
                                            String fullName, String email, String phone) {
        return new User(userId, username, password, fullName, email, phone, User.UserRole.MARKET_MANAGER);
    }
}