import java.util.List;
import java.util.stream.Collectors;

public class StallService {

    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private MarketEventManager eventManager = MarketEventManager.getInstance();

    public void addStall(Stall stall) {
        dataStore.addStall(stall);
        eventManager.notifyStallStatusChanged("New stall added: " + stall.getStallNumber());
    }

    public void updateStall(Stall stall) {
        dataStore.updateStall(stall);
        eventManager.notifyStallStatusChanged("Stall updated: " + stall.getStallNumber());
    }

    public void deleteStall(int stallId) {
        dataStore.deleteStall(stallId);
        eventManager.notifyStallStatusChanged("Stall deleted (ID: " + stallId + ")");
    }

    public List<Stall> getAllStalls() {
        return dataStore.getStalls();
    }

    public List<Stall> getAvailableStalls() {
        return dataStore.getStalls().stream()
                .filter(s -> s.getStatus() == Stall.StallStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public Stall getStallById(int stallId) {
        return dataStore.getStalls().stream()
                .filter(s -> s.getStallId() == stallId)
                .findFirst().orElse(null);
    }
}