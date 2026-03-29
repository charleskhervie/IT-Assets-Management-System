package dao.model;

import java.time.LocalDateTime;

public class Unit {
    private int unitId;
    private int equipmentId;
    private String serialNumber;
    private String status;
    private int addedBy;
    private LocalDateTime createdAt;

    public Unit(int unitId, int equipmentId, String serialNumber, String status, int addedBy, LocalDateTime createdAt) {
        this.unitId = unitId;
        this.equipmentId = equipmentId;
        this.serialNumber = serialNumber;
        this.status = status;
        this.addedBy = addedBy;
        this.createdAt = createdAt;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(int addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
