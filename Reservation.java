import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private User farmer;
    private Stall stall;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReservationStatus status;
    private double totalAmount;
    private String notes;
    private LocalDate createdAt;

    public enum ReservationStatus {
        PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED
    }

    public Reservation(int reservationId, User farmer, Stall stall,
                       LocalDate startDate, LocalDate endDate, String notes) {
        this.reservationId = reservationId;
        this.farmer = farmer;
        this.stall = stall;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDate.now();
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        this.totalAmount = days * stall.getDailyRate();
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }
    public Stall getStall() { return stall; }
    public void setStall(Stall stall) { this.stall = stall; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        calculateTotalAmount();
    }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        calculateTotalAmount();
    }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " - " + stall.getStallNumber()
                + " by " + farmer.getFullName();
    }
}