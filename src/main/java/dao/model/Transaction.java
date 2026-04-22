package dao.model;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private int unitId;
    private int borrowedBy;
    private int processedBy;
    private LocalDateTime borrowedDate;
    private LocalDateTime returnDate;
    private String status;
    private String remarks;

    public Transaction(int transactionId, int unitId, int borrowedBy, int processedBy,
                        LocalDateTime borrowedDate, LocalDateTime returnDate, String status, String remarks) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.borrowedBy = borrowedBy;
        this.processedBy = processedBy;
        this.borrowedDate = borrowedDate;
        this.returnDate = returnDate;
        this.status = status;
        this.remarks = remarks;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public int getBorrower() { return borrowedBy; }
    public void setBorrower(int borrowedBy) { this.borrowedBy = borrowedBy; }
    public int getProcessedBy() { return processedBy; }
    public void setProcessedBy(int processedBy) { this.processedBy = processedBy; }
    public LocalDateTime getBorrowedDate() { return borrowedDate; }
    public void setTransactionDate(LocalDateTime borrowedDate) { this.borrowedDate = borrowedDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}