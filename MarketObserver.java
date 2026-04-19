
public interface MarketObserver {
    void onReservationUpdated(String message);
    void onStallStatusChanged(String message);
    void onUserUpdated(String message);
}