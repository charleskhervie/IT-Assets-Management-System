package dao.model;

import java.time.LocalDateTime;
/**
 * Represents a unit of equipment in the IT asset inventory.
 * A unit is a physical instance of an equipment type, identified by a serial number.
 */

public class Unit {
    /**
     * Constructs a new Unit with the given attributes.
     *
     * @param unitId      the unique identifier of the unit
     * @param equipmentId the ID of the equipment type this unit belongs to
     * @param serialNumber the unique serial number of this unit
     * @param status      the current status (Available, Checked-Out, Maintenance)
     * @param addedBy     the emp_id of the employee who added this unit
     * @param createdAt   the timestamp when this unit was added
     * @param assignedTo  the emp_id of the employee currently assigned this unit, or null
     */
    private int unitId;
    private int equipmentId;
    private String serialNumber;
    private String status;
    private int addedBy;
    private LocalDateTime createdAt;
    private Integer assignedTo;
    // display fields
    private String equipmentName;
    private String categoryName;
    private String addedByName;
    private String assignedToName;

    public Unit(int unitId, int equipmentId, String serialNumber, String status,
                int addedBy, LocalDateTime createdAt, Integer assignedTo) {
        this.unitId = unitId;
        this.equipmentId = equipmentId;
        this.serialNumber = serialNumber;
        this.status = status;
        this.addedBy = addedBy;
        this.createdAt = createdAt;
        this.assignedTo = assignedTo;
    }

    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAddedBy() { return addedBy; }
    public void setAddedBy(int addedBy) { this.addedBy = addedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Integer assignedTo) { this.assignedTo = assignedTo; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getAddedByName() { return addedByName; }
    public void setAddedByName(String addedByName) { this.addedByName = addedByName; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
}
