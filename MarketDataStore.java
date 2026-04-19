
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SINGLETON DESIGN PATTERN
 * Single instance holding all application data (Users, Stalls, Reservations, ProductSales)
 */
public class MarketDataStore {

    private static MarketDataStore instance;

    private List<User> users;
    private List<Stall> stalls;
    private List<Reservation> reservations;
    private List<ProductSale> productSales;

    private int userIdCounter = 10;
    private int stallIdCounter = 10;
    private int reservationIdCounter = 10;
    private int saleIdCounter = 10;

    private MarketDataStore() {
        users = new ArrayList<>();
        stalls = new ArrayList<>();
        reservations = new ArrayList<>();
        productSales = new ArrayList<>();
        loadDummyData();
    }

    public static MarketDataStore getInstance() {
        if (instance == null) {
            instance = new MarketDataStore();
        }
        return instance;
    }

    private void loadDummyData() {
        // --- Users ---
        users.add(new User(1, "admin", "admin123", "Maria Santos", "admin@market.ph", "09171000001", User.UserRole.ADMIN));
        users.add(new User(2, "manager1", "manager123", "Jose Reyes", "jose@market.ph", "09171000002", User.UserRole.MARKET_MANAGER));
        users.add(new User(3, "manager2", "manager123", "Ana Cruz", "ana@market.ph", "09171000003", User.UserRole.MARKET_MANAGER));
        users.add(new User(4, "farmer1", "farmer123", "Pedro Dela Cruz", "pedro@farm.ph", "09171000004", User.UserRole.FARMER));
        users.add(new User(5, "farmer2", "farmer123", "Luisa Gomez", "luisa@farm.ph", "09171000005", User.UserRole.FARMER));
        users.add(new User(6, "farmer3", "farmer123", "Ramon Torres", "ramon@farm.ph", "09171000006", User.UserRole.FARMER));
        users.add(new User(7, "farmer4", "farmer123", "Elena Villanueva", "elena@farm.ph", "09171000007", User.UserRole.FARMER));
        users.add(new User(8, "farmer5", "farmer123", "Antonio Bautista", "antonio@farm.ph", "09171000008", User.UserRole.FARMER));

        // --- Stalls ---
        stalls.add(new Stall(1, "A-01", "Section A - North Wing", "Vegetables", 250.00, "Premium corner stall for fresh vegetables"));
        stalls.add(new Stall(2, "A-02", "Section A - North Wing", "Vegetables", 200.00, "Mid-row stall for root crops"));
        stalls.add(new Stall(3, "A-03", "Section A - North Wing", "Fruits", 300.00, "Large end stall for tropical fruits"));
        stalls.add(new Stall(4, "B-01", "Section B - South Wing", "Fruits", 250.00, "Corner stall for citrus and berries"));
        stalls.add(new Stall(5, "B-02", "Section B - South Wing", "Herbs", 150.00, "Compact stall for medicinal herbs"));
        stalls.add(new Stall(6, "B-03", "Section B - South Wing", "Herbs", 150.00, "Compact stall for spices and herbs"));
        stalls.add(new Stall(7, "C-01", "Section C - East Wing", "Organic", 350.00, "Premium organic produce stall"));
        stalls.add(new Stall(8, "C-02", "Section C - East Wing", "Organic", 320.00, "Certified organic products stall"));
        stalls.add(new Stall(9, "D-01", "Section D - West Wing", "Grains", 200.00, "Bulk grains and legumes stall"));
        stalls.add(new Stall(10, "D-02", "Section D - West Wing", "Grains", 200.00, "Rice and corn specialty stall"));

        stalls.get(2).setStatus(Stall.StallStatus.UNDER_MAINTENANCE);

        // --- Reservations ---
        User farmer1 = getUserById(4);
        User farmer2 = getUserById(5);
        User farmer3 = getUserById(6);
        User farmer4 = getUserById(7);
        User farmer5 = getUserById(8);

        Stall s1 = getStallById(1);
        Stall s2 = getStallById(2);
        Stall s4 = getStallById(4);
        Stall s5 = getStallById(5);
        Stall s7 = getStallById(7);
        Stall s9 = getStallById(9);

        Reservation r1 = new Reservation(1, farmer1, s1,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 7), "Selling fresh tomatoes and peppers");
        r1.setStatus(Reservation.ReservationStatus.APPROVED);
        r1.setCreatedAt(LocalDate.of(2026, 3, 25));
        s1.setStatus(Stall.StallStatus.RESERVED);
        reservations.add(r1);

        Reservation r2 = new Reservation(2, farmer2, s4,
                LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 12), "Seasonal fruits: mango and pineapple");
        r2.setStatus(Reservation.ReservationStatus.APPROVED);
        r2.setCreatedAt(LocalDate.of(2026, 3, 28));
        s4.setStatus(Stall.StallStatus.RESERVED);
        reservations.add(r2);

        Reservation r3 = new Reservation(3, farmer3, s7,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 20), "Organic mixed vegetables");
        r3.setStatus(Reservation.ReservationStatus.PENDING);
        r3.setCreatedAt(LocalDate.of(2026, 4, 2));
        reservations.add(r3);

        Reservation r4 = new Reservation(4, farmer4, s5,
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 22), "Herbal plants and teas");
        r4.setStatus(Reservation.ReservationStatus.COMPLETED);
        r4.setCreatedAt(LocalDate.of(2026, 3, 10));
        reservations.add(r4);

        Reservation r5 = new Reservation(5, farmer5, s2,
                LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 30), "Camote and cassava");
        r5.setStatus(Reservation.ReservationStatus.COMPLETED);
        r5.setCreatedAt(LocalDate.of(2026, 3, 15));
        reservations.add(r5);

        Reservation r6 = new Reservation(6, farmer1, s9,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10), "Rice and monggo beans");
        r6.setStatus(Reservation.ReservationStatus.COMPLETED);
        r6.setCreatedAt(LocalDate.of(2026, 2, 22));
        reservations.add(r6);

        Reservation r7 = new Reservation(7, farmer2, s1,
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 17), "Ripe mangoes");
        r7.setStatus(Reservation.ReservationStatus.COMPLETED);
        r7.setCreatedAt(LocalDate.of(2026, 2, 5));
        reservations.add(r7);

        Reservation r8 = new Reservation(8, farmer3, s4,
                LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 27), "Watermelons and papayas");
        r8.setStatus(Reservation.ReservationStatus.REJECTED);
        r8.setCreatedAt(LocalDate.of(2026, 2, 15));
        reservations.add(r8);

        Reservation r9 = new Reservation(9, farmer1, s7,
                LocalDate.of(2026, 4, 25), LocalDate.of(2026, 5, 5), "Organic tomatoes and lettuce");
        r9.setStatus(Reservation.ReservationStatus.PENDING);
        r9.setCreatedAt(LocalDate.of(2026, 4, 18));
        reservations.add(r9);

        // --- Product Sales ---
        productSales.add(new ProductSale(1, r1, "Tomatoes", "Vegetables", 50, 35.00, LocalDate.of(2026, 4, 1)));
        productSales.add(new ProductSale(2, r1, "Bell Peppers", "Vegetables", 30, 80.00, LocalDate.of(2026, 4, 2)));
        productSales.add(new ProductSale(3, r1, "Red Onions", "Vegetables", 20, 60.00, LocalDate.of(2026, 4, 3)));
        productSales.add(new ProductSale(4, r2, "Mangoes", "Fruits", 100, 45.00, LocalDate.of(2026, 4, 5)));
        productSales.add(new ProductSale(5, r2, "Pineapple", "Fruits", 40, 55.00, LocalDate.of(2026, 4, 6)));
        productSales.add(new ProductSale(6, r4, "Basil", "Herbs", 25, 30.00, LocalDate.of(2026, 3, 15)));
        productSales.add(new ProductSale(7, r4, "Peppermint", "Herbs", 20, 40.00, LocalDate.of(2026, 3, 16)));
        productSales.add(new ProductSale(8, r5, "Camote", "Vegetables", 60, 28.00, LocalDate.of(2026, 3, 20)));
        productSales.add(new ProductSale(9, r5, "Cassava", "Vegetables", 40, 22.00, LocalDate.of(2026, 3, 21)));
        productSales.add(new ProductSale(10, r6, "Rice (per kilo)", "Grains", 200, 55.00, LocalDate.of(2026, 3, 1)));
        productSales.add(new ProductSale(11, r6, "Monggo Beans", "Grains", 80, 95.00, LocalDate.of(2026, 3, 2)));
        productSales.add(new ProductSale(12, r7, "Ripe Mangoes", "Fruits", 150, 50.00, LocalDate.of(2026, 2, 10)));
    }

    private User getUserById(int id) {
        return users.stream().filter(u -> u.getUserId() == id).findFirst().orElse(null);
    }

    private Stall getStallById(int id) {
        return stalls.stream().filter(s -> s.getStallId() == id).findFirst().orElse(null);
    }

    // ---- Users CRUD ----
    public List<User> getUsers() { return users; }

    public void addUser(User user) {
        user.setUserId(++userIdCounter);
        users.add(user);
    }

    public void updateUser(User updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId() == updated.getUserId()) {
                users.set(i, updated);
                return;
            }
        }
    }

    public void deleteUser(int userId) {
        users.removeIf(u -> u.getUserId() == userId);
    }

    public User authenticateUser(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password) && u.isActive())
                .findFirst().orElse(null);
    }

    // ---- Stalls CRUD ----
    public List<Stall> getStalls() { return stalls; }

    public void addStall(Stall stall) {
        stall.setStallId(++stallIdCounter);
        stalls.add(stall);
    }

    public void updateStall(Stall updated) {
        for (int i = 0; i < stalls.size(); i++) {
            if (stalls.get(i).getStallId() == updated.getStallId()) {
                stalls.set(i, updated);
                return;
            }
        }
    }

    public void deleteStall(int stallId) {
        stalls.removeIf(s -> s.getStallId() == stallId);
    }

    // ---- Reservations CRUD ----
    public List<Reservation> getReservations() { return reservations; }

    public void addReservation(Reservation reservation) {
        reservation.setReservationId(++reservationIdCounter);
        reservations.add(reservation);
    }

    public void updateReservation(Reservation updated) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationId() == updated.getReservationId()) {
                reservations.set(i, updated);
                return;
            }
        }
    }

    public void deleteReservation(int reservationId) {
        reservations.removeIf(r -> r.getReservationId() == reservationId);
    }

    // ---- Product Sales ----
    public List<ProductSale> getProductSales() { return productSales; }

    public void addProductSale(ProductSale sale) {
        sale.setSaleId(++saleIdCounter);
        productSales.add(sale);
    }

    public int getNextUserId() { return userIdCounter + 1; }
    public int getNextStallId() { return stallIdCounter + 1; }
    public int getNextReservationId() { return reservationIdCounter + 1; }
    public int getNextSaleId() { return saleIdCounter + 1; }
}