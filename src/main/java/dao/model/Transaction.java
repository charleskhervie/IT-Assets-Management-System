package dao.model;

public class Transaction {
    private int transactionId;
    private int unitId;
    private int empId;
    private String transactionType;
    private String transactionDate;

    public Transaction(int transactionId, int unitId, int empId, String transactionType, String transactionDate) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.empId = empId;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
}
