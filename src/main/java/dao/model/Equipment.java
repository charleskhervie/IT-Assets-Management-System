package dao.model;

public class Equipment {
    private int equipmentId;
    private String equipmentName;
    private String brand;
    private String model;
    private String specifications;
    private int categoryId;

    public Equipment(int equipmentId, String equipmentName, String brand, String model, String specifications, int categoryId) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.brand = brand;
        this.model = model;
        this.specifications = specifications;
        this.categoryId = categoryId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
