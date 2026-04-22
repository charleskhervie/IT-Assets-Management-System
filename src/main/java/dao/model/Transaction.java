package dao.model;

public class Transaction {
    private int transactionId;
    private int unitId;
    private int empId;
    private int processedBy;
    private String transactionType;
    private String transactionDate;
    private String returnDate;
    private String status;
    private String remarks;

    public Transaction(int transactionId, int unitId, int empId, int processedBy,
                       String transactionType, String transactionDate,
                       String returnDate, String status, String remarks) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.empId = empId;
        this.processedBy = processedBy;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.returnDate = returnDate;
        this.status = status;
        this.remarks = remarks;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }
    public int getProcessedBy() { return processedBy; }
    public void setProcessedBy(int processedBy) { this.processedBy = processedBy; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}