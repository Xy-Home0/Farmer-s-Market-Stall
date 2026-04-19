
import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER DESIGN PATTERN - Subject/Publisher
 * Manages observers and notifies them of market events.
 */
public class MarketEventManager {

    private static MarketEventManager instance;
    private List<MarketObserver> observers = new ArrayList<>();

    private MarketEventManager() {}

    public static MarketEventManager getInstance() {
        if (instance == null) {
            instance = new MarketEventManager();
        }
        return instance;
    }

    public void subscribe(MarketObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(MarketObserver observer) {
        observers.remove(observer);
    }

    public void notifyReservationUpdated(String message) {
        for (MarketObserver observer : observers) {
            observer.onReservationUpdated(message);
        }
    }

    public void notifyStallStatusChanged(String message) {
        for (MarketObserver observer : observers) {
            observer.onStallStatusChanged(message);
        }
    }

    public void notifyUserUpdated(String message) {
        for (MarketObserver observer : observers) {
            observer.onUserUpdated(message);
        }
    }
}