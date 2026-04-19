import java.time.LocalDate;

public class ProductSale {
    private int saleId;
    private Reservation reservation;
    private String productName;
    private String category;
    private int quantity;
    private double unitPrice;
    private LocalDate saleDate;

    public ProductSale(int saleId, Reservation reservation, String productName,
                       String category, int quantity, double unitPrice, LocalDate saleDate) {
        this.saleId = saleId;
        this.reservation = reservation;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.saleDate = saleDate;
    }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public double getTotalSale() { return quantity * unitPrice; }
}