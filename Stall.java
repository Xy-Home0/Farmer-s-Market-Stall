public class Stall {
    private int stallId;
    private String stallNumber;
    private String location;
    private String category;
    private double dailyRate;
    private StallStatus status;
    private String description;

    public enum StallStatus {
        AVAILABLE, RESERVED, UNDER_MAINTENANCE
    }

    public Stall(int stallId, String stallNumber, String location,
                 String category, double dailyRate, String description) {
        this.stallId = stallId;
        this.stallNumber = stallNumber;
        this.location = location;
        this.category = category;
        this.dailyRate = dailyRate;
        this.description = description;
        this.status = StallStatus.AVAILABLE;
    }

    public int getStallId() { return stallId; }
    public void setStallId(int stallId) { this.stallId = stallId; }
    public String getStallNumber() { return stallNumber; }
    public void setStallNumber(String stallNumber) { this.stallNumber = stallNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public StallStatus getStatus() { return status; }
    public void setStatus(StallStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return stallNumber + " - " + category + " (" + location + ")";
    }
}