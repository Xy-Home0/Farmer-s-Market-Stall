import javax.swing.*;

/**
 * Farmers Market Stall Reservation System
 *
 * OOP Design Patterns Used:
 * 1. SINGLETON  - MarketDataStore: ensures a single data store instance app-wide
 * 2. FACTORY    - UserFactory: creates User objects based on their role
 * 3. OBSERVER   - MarketEventManager (Subject) + AdminDashboard/ManagerDashboard/FarmerDashboard (Observers):
 *                 UI panels subscribe and react to reservation/stall/user change events
 *
 * Class Relationships:
 * - COMPOSITION: Reservation CONTAINS User (farmer) and Stall - cannot exist without them
 * - AGGREGATION: MarketDataStore aggregates Lists of Users, Stalls, Reservations, ProductSales
 * - ASSOCIATION: ReservationService USES/DEPENDS ON MarketDataStore and MarketEventManager
 * - ProductSale is associated with Reservation (tracks sales per reserved stall)
 *
 * User Roles: Admin, Farmer, Market Manager
 * CRUD: Full CRUD for Users, Stalls, Reservations, ProductSales
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UITheme.applyLookAndFeel();
            new LoginFrame().setVisible(true);
        });
    }
}