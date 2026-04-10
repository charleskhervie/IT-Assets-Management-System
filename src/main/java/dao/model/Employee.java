package dao.model;

public class Employee {
    private int empId;
    private int departmentId;
    private String userName;
    private String password;
    private String role;
    private String fullName;

    public Employee(int userId, int department_id, String departmentId, String userName, String password, String role, String fullName){
        this.empId = userId;
        this.departmentId = department_id;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


}