package dao.model;
<<<<<<< HEAD
<<<<<<< HEAD

=======
>>>>>>> 6b4fa582ee676dfd47e14ca9ca68de9158bbc419
=======

>>>>>>> origin/Charles/equipment-units
public class Employee {
    private int empId;
    private int departmentId;
    private String username;
    private String password;
    private String role;
    private String fullName;

<<<<<<< HEAD
<<<<<<< HEAD
    public Employee(int userId, int department_id, String departmentId, String userName, String password, String role, String fullName){
        this.empId = userId;
        this.departmentId = department_id;
=======
    public Employee(int empID, int departmentId, String userName, String password, String role, String fullName){
        this.empId = empID;
        this.departmentId = departmentId;
>>>>>>> 6b4fa582ee676dfd47e14ca9ca68de9158bbc419
        this.userName = userName;
=======
    public Employee(int empId, int departmentId, String username, String password, String role, String fullName) {
        this.empId = empId;
        this.departmentId = departmentId;
        this.username = username;
>>>>>>> origin/Charles/equipment-units
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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