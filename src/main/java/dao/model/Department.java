package dao.model;

public class Department {
    private int departmentId;
    private String departmentName;
    private String location;

    public Department(int departmentId, String departmentName, String location){
        this.departmentId= departmentId;
        this.departmentName = departmentName;
        this.location = location;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public int getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
}
