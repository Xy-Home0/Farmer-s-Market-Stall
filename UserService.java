import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private MarketEventManager eventManager = MarketEventManager.getInstance();

    public User login(String username, String password) {
        return dataStore.authenticateUser(username, password);
    }

    public String addUser(String username, String password, String fullName,
                          String email, String phone, String roleStr) {
        boolean exists = dataStore.getUsers().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            return "Username already exists.";
        }
        User user = UserFactory.createUser(0, username, password, fullName, email, phone, roleStr);
        dataStore.addUser(user);
        eventManager.notifyUserUpdated("New user registered: " + fullName);
        return "SUCCESS";
    }

    public void updateUser(User user) {
        dataStore.updateUser(user);
        eventManager.notifyUserUpdated("User updated: " + user.getFullName());
    }

    public void deleteUser(int userId) {
        dataStore.deleteUser(userId);
        eventManager.notifyUserUpdated("User deleted (ID: " + userId + ")");
    }

    public List<User> getAllUsers() {
        return dataStore.getUsers();
    }

    public List<User> getFarmers() {
        return dataStore.getUsers().stream()
                .filter(u -> u.getRole() == User.UserRole.FARMER)
                .collect(Collectors.toList());
    }

    public List<User> getManagers() {
        return dataStore.getUsers().stream()
                .filter(u -> u.getRole() == User.UserRole.MARKET_MANAGER)
                .collect(Collectors.toList());
    }
}