import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {

    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private MarketEventManager eventManager = MarketEventManager.getInstance();

    public String createReservation(User farmer, Stall stall, LocalDate startDate,
                                    LocalDate endDate, String notes) {
        if (stall.getStatus() == Stall.StallStatus.RESERVED) {
            return "Stall is already reserved.";
        }
        if (stall.getStatus() == Stall.StallStatus.UNDER_MAINTENANCE) {
            return "Stall is under maintenance.";
        }
        if (endDate.isBefore(startDate)) {
            return "End date cannot be before start date.";
        }
        if (startDate.isBefore(LocalDate.now())) {
            return "Start date cannot be in the past.";
        }

        boolean conflict = dataStore.getReservations().stream().anyMatch(r ->
                r.getStall().getStallId() == stall.getStallId()
                        && (r.getStatus() == Reservation.ReservationStatus.APPROVED || r.getStatus() == Reservation.ReservationStatus.PENDING)
                        && !(endDate.isBefore(r.getStartDate()) || startDate.isAfter(r.getEndDate()))
        );
        if (conflict) {
            return "Stall has a conflicting reservation for the selected dates.";
        }

        Reservation reservation = new Reservation(0, farmer, stall, startDate, endDate, notes);
        dataStore.addReservation(reservation);
        eventManager.notifyReservationUpdated("New reservation submitted by " + farmer.getFullName());
        return "SUCCESS";
    }

    public String approveReservation(int reservationId) {
        for (Reservation r : dataStore.getReservations()) {
            if (r.getReservationId() == reservationId) {
                r.setStatus(Reservation.ReservationStatus.APPROVED);
                r.getStall().setStatus(Stall.StallStatus.RESERVED);
                eventManager.notifyReservationUpdated("Reservation #" + reservationId + " approved.");
                eventManager.notifyStallStatusChanged("Stall " + r.getStall().getStallNumber() + " is now Reserved.");
                return "SUCCESS";
            }
        }
        return "Reservation not found.";
    }

    public String rejectReservation(int reservationId) {
        for (Reservation r : dataStore.getReservations()) {
            if (r.getReservationId() == reservationId) {
                r.setStatus(Reservation.ReservationStatus.REJECTED);
                eventManager.notifyReservationUpdated("Reservation #" + reservationId + " rejected.");
                return "SUCCESS";
            }
        }
        return "Reservation not found.";
    }

    public String cancelReservation(int reservationId) {
        for (Reservation r : dataStore.getReservations()) {
            if (r.getReservationId() == reservationId) {
                if (r.getStatus() == Reservation.ReservationStatus.COMPLETED) {
                    return "Cannot cancel a completed reservation.";
                }
                r.setStatus(Reservation.ReservationStatus.CANCELLED);
                if (r.getStall().getStatus() == Stall.StallStatus.RESERVED) {
                    r.getStall().setStatus(Stall.StallStatus.AVAILABLE);
                }
                eventManager.notifyReservationUpdated("Reservation #" + reservationId + " cancelled.");
                eventManager.notifyStallStatusChanged("Stall " + r.getStall().getStallNumber() + " is now Available.");
                return "SUCCESS";
            }
        }
        return "Reservation not found.";
    }

    public String completeReservation(int reservationId) {
        for (Reservation r : dataStore.getReservations()) {
            if (r.getReservationId() == reservationId) {
                r.setStatus(Reservation.ReservationStatus.COMPLETED);
                if (r.getStall().getStatus() == Stall.StallStatus.RESERVED) {
                    r.getStall().setStatus(Stall.StallStatus.AVAILABLE);
                }
                eventManager.notifyReservationUpdated("Reservation #" + reservationId + " marked as completed.");
                return "SUCCESS";
            }
        }
        return "Reservation not found.";
    }

    public List<Reservation> getReservationsByFarmer(int farmerId) {
        return dataStore.getReservations().stream()
                .filter(r -> r.getFarmer().getUserId() == farmerId)
                .collect(Collectors.toList());
    }

    public List<Reservation> getReservationsByStall(int stallId) {
        return dataStore.getReservations().stream()
                .filter(r -> r.getStall().getStallId() == stallId)
                .collect(Collectors.toList());
    }

    public List<Reservation> getPendingReservations() {
        return dataStore.getReservations().stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING)
                .collect(Collectors.toList());
    }
}