package cesur.accesodatos.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * File-based Data Access Object (FileDAO) component.
 * Handles file operations for data access. It simplifies reading, writing, updating,
 * and deleting data in files, making sure you're good to go with minimal setup. Designed
 * to be straightforward and user-friendly, it's all about getting the job done without the hassle.
 * Just remember to check your file paths and connections before you start.
 *
 * {@link IDAO} for data operations.
 * {@link FileHandlerInterface} for file database management.
 * {@link Menu} for user related interactions.
 *
 * @author Marc Albert Seguí Olmos
 */

public class FileDAO implements IDAO, Menu, FileHandlerInterface{
    // Terminal outputs and colors
    /**
     * BLACK_FONT -> Static and final {@link String} variable that stores ASCII code for black font color.
     */
    static final String BLACK_FONT = "\u001B[30m";
    /**
     * GREEN_FONT -> Static and final {@link String} variable that stores ASCII code for green font color.
     */
    static final String GREEN_FONT = "\u001B[32m";
    /**
     * WHITE_BG -> Static and final {@link String} variable that stores ASCII code for white background color.
     */
    static final String WHITE_BG = "\u001B[47m";
    /**
     * RESET -> Static and final {@link String} variable that stores ASCII code to reset terminal colors.
     */
    static final String RESET = "\u001B[0m";
    /**
     * USER_INPUT -> Static and final {@link String} variable that stores a simple prompt for the user when he has to introduce any data.
     */
    static final String USER_INPUT = String.format("%s%s>%s ", BLACK_FONT, WHITE_BG, RESET);

    public BufferedReader reader; // Instance variable
    /**
     * Flag indicating if a file connection has been established.
     * Used to ensure operations don't proceed without proper file access setup.
     */
    private boolean connectionFlag = false;

    /**
     * Flag to control the execution flow of the application, typically used to keep the application running or initiate a graceful shutdown.
     */
    private boolean executionFlag = true;
    /**
     * Path to the "empresa.txt" file within the project's resources directory, used as the data storage for the application.
     */
    static String path = "src/main/resources/empresa.txt"; // Path of the file
    /**
     * Scanner used for capturing user input from the terminal.
     */
    private final Scanner scanner = new Scanner(System.in);
    /**
     * isr -> {@link InputStreamReader} variable that will allow the user to insert data through terminal.
     *
     */
    private final InputStreamReader isr = new InputStreamReader(System.in);


    /**
     * Retrieves all employees from the data storage file.
     * This method reads the "empresa.txt" file line by line, looking for entries that start with "employee". Each found entry is parsed into an Employee object and added to a list of employees. This list is then returned.
     *
     * The method handles any IOExceptions that might occur during file reading, printing the stack trace to standard error if an exception is caught.
     *
     * @return A list of Employee objects representing all employees found in the data storage file. If no employees are found or an error occurs, an empty list is returned.
     */
    @Override
    public List<Employee> findAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("employee")) {
                    String[] parts = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
                    Employee emp = new Employee(Integer.parseInt(parts[0]), parts[1], parts[2], Integer.parseInt(parts[3]));
                    employees.add(emp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * Searches for and retrieves an employee by their unique identifier from the data storage file.
     * This method iterates through the "empresa.txt" file, identifying lines that represent employee data.
     * It parses each employee entry and checks if the employee's ID matches the specified {@code id}. If a match is found,
     * it constructs and returns an Employee object representing that employee.
     *
     * If the {@code id} provided is not an instance of {@link Integer}, or if no employee with the specified ID is found,
     * the method returns {@code null}. IOExceptions encountered during file reading are caught and handled by printing
     * the stack trace, but do not stop the method from returning a value.
     *
     * @param id The unique identifier of the employee to search for. Expected to be of type {@link Integer}.
     * @return An {@link Employee} object representing the employee with the specified ID, or {@code null} if no such employee is found or if an error occurs.
     */
    @Override
    public Employee findEmployeeById(Object id) {
        if (!(id instanceof Integer)) {
            return null;
        }
        Integer searchId = (Integer) id;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("employee")) {
                    String[] parts = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
                    Integer employeeId = Integer.parseInt(parts[0]);
                    if (employeeId == searchId) {
                        return new Employee(employeeId, parts[1], parts[2], Integer.parseInt(parts[3]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new employee to the data storage file.
     * This method formats the provided {@link Employee} object into a string representation following the
     * format: "employee(id,name,position,departmentId)" and appends it to the end of the "empresa.txt" file.
     * This allows for the persistent storage of the new employee's information.
     *
     * The method utilizes a {@link PrintWriter} wrapped in a {@link BufferedWriter} and a {@link FileWriter}
     * configured to append to the file, ensuring that existing data is not overwritten.
     *
     * IOExceptions are caught and handled by printing the stack trace, but the method itself does not return any value.
     *
     * @param employee The {@link Employee} object containing the information of the employee to be added to the file.
     */
    @Override
    public void addEmployee(Employee employee) {
        try (FileWriter fw = new FileWriter(path, true); // Open the file with append
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // Line format: employee(id,surname,job,department_id)
            String employeeData = String.format("employee(%d,%s,%s,%d)",
                    employee.getEmpno(),
                    employee.getName(),
                    employee.getPosition(),
                    employee.getDepno());
            out.println(employeeData); // Write the new employee info in the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an employee's details in the storage file based on the provided ID.
     * Validates the ID, then prompts for and accepts new values for the employee's surname, job, and department ID.
     * Updates both the in-memory representation and the persistent data in "empresa.txt".
     *
     * If the input ID is not an integer, the employee cannot be found, or input values are invalid, the operation will fail.
     * IOExceptions during file update are caught and logged, potentially causing the method to return null.
     *
     * @param id The ID of the employee to be updated, expected to be an Integer.
     * @return The updated Employee object if successful, or null if an error occurs.
     */
    @Override
    public Employee updateEmployee(Object id) {
        if (!(id instanceof Integer)) {
            System.out.println("ID no válido");
            return null;
        }

        int empId = (Integer) id;
        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Empleado no encontrado.");
            return null;
        }

        System.out.println("Actualizar empleado con ID: " + empId);
        System.out.print("Apellido (actual: " + employee.getName() + "): ");
        String surname = scanner.nextLine();
        if (surname.isEmpty()) throw new IllegalArgumentException("El apellido no puede estar vacío");

        System.out.print("Trabajo (actual: " + employee.getPosition() + "): ");
        String job = scanner.nextLine();
        if (job.isEmpty()) throw new IllegalArgumentException("El trabajo no puede estar vacío");

        System.out.print("ID del Departamento (actual: " + employee.getDepno() + "): ");
        String departmentId = scanner.nextLine();
        if (departmentId.isEmpty()) throw new IllegalArgumentException("El ID del departamento no puede estar vacío");

        // Update the used object with the new values
        employee.setName(surname);
        employee.setPosition(job);
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8));

            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).startsWith("employee(" + empId + ",")) {
                    String newLine = String.format("employee(%d,%s,%s,%d)", empId, surname, job, Integer.parseInt(departmentId));
                    fileContent.set(i, newLine);
                    break;
                }
            }

            Files.write(Paths.get(path), fileContent, StandardCharsets.UTF_8);
            System.out.println("El empleado ha sido actualizado correctamente en el archivo.");
        } catch (IOException e) {
            System.err.println("Ocurrió un error al escribir en el archivo: " + e.getMessage());
            return null;
        }

        return employee;
    }

    /**
     * Removes an employee from the data storage file based on the provided ID. This method first
     * identifies the employee to be deleted by iterating through all employees. If found, the
     * employee is removed, and the file is updated to reflect this change.
     *
     * @param id The unique identifier of the employee to delete.
     * @return The Employee object that was deleted, or null if the ID is not valid, the employee
     * is not found, or an error occurs.
     */
    @Override
    public Employee deleteEmployee(Object id) {
        if (!(id instanceof Integer)) {
            System.out.println("ID no válido");
            return null;
        }

        int empId = (Integer) id;
        List<Employee> employees = findAllEmployees();
        Employee employeeToRemove = null;

        // Find the employee to delete
        for (Employee emp : employees) {
            if (emp.getEmpno() == empId) {
                employeeToRemove = emp;
                break;
            }
        }

        if (employeeToRemove == null) {
            System.out.println("Empleado no encontrado.");
            return null;
        }

        // Remove the employee from the list
        employees.remove(employeeToRemove);

        // Overwrite the file with the updated list
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            for (Employee emp : employees) {
                String employeeData = String.format("employee(%d,%s,%s,%d)",
                        emp.getEmpno(),
                        emp.getName(),
                        emp.getPosition(),
                        emp.getDepno());
                out.println(employeeData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("Se ha eliminado el empleado.");
        return employeeToRemove;
    }

    /**
     * Retrieves all departments from the data storage file. Parses each line that starts with
     * "department" into a Department object and collects them into a list.
     *
     * @return A list of all departments found in the file.
     * Returns an empty list if no departments are found or an IOException occurs.
     */
    @Override
    public List<Department> findAllDepartments() {
        List<Department> departments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("department")) {
                    String[] parts = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
                    Department dept = new Department(Integer.parseInt(parts[0]), parts[1], parts[2]);
                    departments.add(dept);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return departments;
    }

    /**
     * Searches for and returns a department by its ID from the data storage file.
     * Parses lines starting with "department", comparing the ID with the provided search ID.
     * If a match is found, constructs and returns a Department object.
     *
     * @param id The unique identifier for the department to find. Should be of type {@link Integer}.
     * @return A Department object matching the given ID, or null if the ID is not an {@link Integer},
     * the department is not found, or an IOException occurs.
     */
    @Override
    public Department findDepartmentById(Object id) {
        // Check if the provided ID is an instance of Integer to ensure type safety.
        if (!(id instanceof Integer)) {
            return null; // Alternatively, handle the error appropriately.
        }

        int searchId = (Integer) id; // Cast the ID to an Integer.
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) { // Open the file for reading.
            String line;
            while ((line = reader.readLine()) != null) { // Read the file line by line.
                if (line.startsWith("department")) { // Look for lines that represent departments.
                    // Extract the department details enclosed in parentheses and split by comma.
                    String[] parts = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
                    int departmentId = Integer.parseInt(parts[0]); // Parse the department ID.
                    if (departmentId == searchId) { // Check if the department ID matches the search ID.
                        // Construct and return a Department object with the extracted details.
                        return new Department(departmentId, parts[1], parts[2]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new department to the data storage file.
     * This method formats the provided {@link Department} object into a string
     * and appends it to the end of the "empresa.txt" file. The format used for the department
     * data is "department(id,name,city)", ensuring consistency with the file's data structure.
     * If an IOException occurs during the file writing process, the stack trace is printed.
     *
     * @param department The {@link Department} object containing the information of the department to be added.
     */
    @Override
    public void addDepartment(Department department) {
        try (FileWriter fw = new FileWriter(path, true); // Opens the file in append mode to ensure existing content is not overwritten
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // Line format: department(id,name,city)
            String departmentData = String.format("department(%d,%s,%s)",
                    department.getDepno(),
                    department.getName(),
                    department.getLocation());
            out.println(departmentData); // Writes the new department information into the file
        } catch (IOException e) {
            e.printStackTrace(); // Handles IOException by printing the stack trace
        }
    }

    /**
     * Updates the information for an existing department based on the provided ID.
     * If the ID is valid and the department is found, it prompts for new values for the department's name and city.
     * These new values are then updated in the department object and the "empresa.txt" file to reflect the changes.
     *
     * @param id The unique identifier for the department to be updated, expected to be of type {@link Integer}.
     * @return The updated {@link Department} object if the operation is successful; null otherwise.
     */
    public Department updateDepartment(Object id) {
        if (!(id instanceof Integer)) {
            System.out.println("Invalid ID");
            return null; // Validates that the ID is an Integer
        }

        int deptId = (Integer) id;
        Department department = findDepartmentById(deptId); // Attempts to find the department by ID
        if (department == null) {
            System.out.println("Department not found."); // Indicates if the department was not found
            return null;
        }

        System.out.println("Updating department with ID: " + deptId); // Begins the update process
        System.out.print("Name (current: " + department.getName() + "): "); // Prompts for a new name
        String name = scanner.nextLine();
        if (name.isEmpty()) throw new IllegalArgumentException("The name cannot be empty"); // Ensures the new name is not empty

        System.out.print("City (current: " + department.getLocation() + "): "); // Prompts for a new city
        String city = scanner.nextLine();
        if (city.isEmpty()) throw new IllegalArgumentException("The city cannot be empty"); // Ensures the new city is not empty

        department.setName(name); // Updates the department's name
        department.setLocation(city); // Updates the department's city

        // Implements the logic to write the updated Department object back to the file
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8));

            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).startsWith("department(" + deptId + ",")) {
                    String newLine = String.format("department(%d,%s,%s)", deptId, name, city); // Formats the updated department data
                    fileContent.set(i, newLine); // Replaces the old department data with the updated data in the file content list
                    break;
                }
            }

            Files.write(Paths.get(path), fileContent, StandardCharsets.UTF_8); // Writes the updated file content back to the file
            System.out.println("Department has been successfully updated in the file."); // Confirms the department update
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage()); // Handles possible IOException
            return null;
        }

        return department; // Returns the updated department object
    }

    /**
     * Deletes a department from the data storage file based on the provided ID.
     * It first finds the department to be deleted. If found, the department is removed from
     * the list of all departments, and the file is updated to reflect this change.
     *
     * @param id The unique identifier of the department to be deleted, expected to be an {@link Integer}.
     * @return The Department object that was removed, or null if the ID is invalid, the department is not found, or an error occurs.
     */
    @Override
    public Department deleteDepartment(Object id) {
        // Validate the ID is an instance of Integer, return null if not
        if (!(id instanceof Integer)) {
            System.out.println("The provided ID is not valid.");
            return null;
        }

        int deptId = (Integer) id;
        // Fetch the list of all departments
        List<Department> departments = findAllDepartments();
        Department departmentToRemove = null;

        // Loop through the list to find the department with the given ID
        for (Department dept : departments) {
            if (dept.getDepno() == deptId) {
                departmentToRemove = dept;
                break; // Department found, break the loop
            }
        }

        // If no department is found, print a message and return null
        if (departmentToRemove == null) {
            System.out.println("Department not found.");
            return null;
        }

        // Remove the found department from the list
        departments.remove(departmentToRemove);

        // Try to overwrite the file with the updated list of departments
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            for (Department dept : departments) {
                // Format each department's data and write it to the file
                String departmentData = String.format("department(%d,%s,%s)",
                        dept.getDepno(),
                        dept.getName(),
                        dept.getLocation());
                out.println(departmentData);
            }
        } catch (IOException e) {
            // Handle possible IOException by printing the stack trace
            e.printStackTrace();
            return null;
        }

        // Return the removed department
        return departmentToRemove;
    }

    /**
     * Retrieves a list of employees who belong to a specific department.
     * Validates the department ID before proceeding to ensure it's an integer. Then, it iterates through all employees,
     * adding those to a list whose department ID matches the provided department ID.
     *
     * @param idDept The unique identifier of the department whose employees are to be found. Expected to be of type {@link Integer}.
     * @return A list of {@link Employee} objects who are associated with the specified department ID. Returns null if the provided ID is invalid.
     */
    @Override
    public List<Employee> findEmployeesByDept(Object idDept) {
        // Validate that the provided department ID is an Integer
        if (!(idDept instanceof Integer)) {
            System.out.println("Invalid department ID");
            return null;
        }

        int deptId = (Integer) idDept;
        // Fetch all employees
        List<Employee> allEmployees = findAllEmployees();
        // Prepare a list to hold employees belonging to the specified department
        List<Employee> employeesByDept = new ArrayList<>();

        // Iterate through all employees to find those who belong to the specified department
        for (Employee emp : allEmployees) {
            if (emp.getDepno() == deptId) {
                // If an employee belongs to the department, add them to the list
                employeesByDept.add(emp);
            }
        }
        // Return the list of employees found for the specified department
        return employeesByDept;
    }


    @Override
    public boolean checkFileExists() {
        try {
            // Check if the file exists using the predefined path variable
            connectionFlag = Files.exists(Paths.get(path));

            if (connectionFlag) {
                System.out.println("The database file empresa.txt exists.");
            } else {
                System.out.println("The file does not exist, connectionFlag is set to false.");
            }
            return connectionFlag; // Return the state of connectionFlag
        } catch (Exception e) { // Catch a general exception
            System.err.println("ERROR: An error occurred: " + e.getMessage());
            return false; // Return false if an exception is caught
        }
    }
    @Override
    public void closeConnection() {
        try {
            if (reader != null) {
                reader.close(); // Close the BufferedReader or other input stream
                System.out.printf("%s- Database connection closed -%s\n", GREEN_FONT, RESET);
            }
        } catch (IOException e) {
            System.err.println("ERROR: An error occurred while closing file resources: " + e.getMessage());
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeMenu() {
        BufferedReader reader = new BufferedReader(this.isr); // At this point the Stream is still opened -> At finally block I'll close it
        try {
            while (this.executionFlag) {
                System.out.printf("%s%s- WELCOME TO THE COMPANY -%s\n", "\u001B[46m", BLACK_FONT, RESET);
                System.out.println("Select an option:" + "\n\t1) List all Employees" + "\n\t2) Find Employee by its ID" + "\n\t3) Add new Employee" + "\n\t4) Update Employee" + "\n\t5) Delete Employee" + "\n\t6) List all Departments" + "\n\t7) Find Department by its ID" + "\n\t8) Add new Department" + "\n\t9) Update Department" + "\n\t10) Delete Department" + "\n\t11) Find Employees by Department" + "\n\t0) Exit program");
                System.out.print(USER_INPUT);
                String optStr = reader.readLine(); // Read user input and check its value for bad inputs
                if (optStr.isEmpty()) {
                    System.err.println("ERROR: Please indicate the option number");
                    continue;
                } else if (!optStr.matches("\\d{1,2}")) {
                    System.err.println("ERROR: Please provide a valid input for option! The input must be an Integer value");
                    continue;
                }
                int opt = Integer.parseInt(optStr);
                switch (opt) { // Execute corresponding method for user input
                    case 1 -> executeFindAllEmployees();
                    case 2 -> executeFindEmployeeByID();
                    case 3 -> executeAddEmployee();
                    case 4 -> executeUpdateEmployee();
                    case 5 -> executeDeleteEmployee();
                    case 6 -> executeFindAllDepartments();
                    case 7 -> executeFindDepartmentByID();
                    case 8 -> executeAddDepartment();
                    case 9 -> executeUpdateDepartment();
                    case 10 -> executeDeleteDepartment();
                    case 11 -> executeFindEmployeesByDept();
                    case 0 -> this.executionFlag = false;
                    default -> System.err.println("Please provide a valid option");
                }
            }
        } catch (IOException ioe) {
            System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
        } finally {
            try {
                reader.close(); // Close reader
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error on reader close reported: " + ioe.getMessage());
            }
            closeConnection(); // Close connection method
        }
        System.out.printf("%s%s- SEE YOU SOON -%s\n", "\u001B[46m", BLACK_FONT, RESET); // Program execution end
    }

    // Implementation from Menu interface
    @Override
    public void executeFindAllEmployees() {
        if (this.connectionFlag) {
            String row = "+" + "-".repeat(7) + "+" + "-".repeat(16) + "+" + "-".repeat(16) + "+" + "-".repeat(7) + "+";
            List<Employee> employees = this.findAllEmployees(); // Get the Employees list
            if (employees != null) { // Check if the returned list is not null
                System.out.println(row);
                System.out.printf("| %-5s | %-14s | %-14s | %-5s |\n", "EMPNO", "NOMBRE", "PUESTO", "DEPNO");
                System.out.println(row);
                for (Employee e : employees) {
                    System.out.printf("| %-5s | %-14s | %-14s | %-5s |\n", e.getEmpno(), e.getName(), e.getPosition(), e.getDepno());
                }
                System.out.println(row);
            } else {
                System.out.println("There are currently no Employees stored");
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeFindEmployeeByID() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Employee's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Employee ID. Employee's ID are Integer values");
                    return;
                }
                Employee returnEmp = this.findEmployeeById(Integer.parseInt(input)); // Get the Employee object by querying it by the ID
                if (returnEmp != null) {
                    System.out.println("Employee's information:");
                    System.out.println(returnEmp.toString());
                } else { // There is no Employee with the indicated ID
                    System.out.println("There is no Employee with EMPNO " + input);
                }
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeAddEmployee() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try { // Ask for all required information to create a new Employee
                System.out.println("Insert new Employee's ID:");
                System.out.print(USER_INPUT);
                String id = reader.readLine();
                if (!id.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Employee ID. Employee's ID are Integer values");
                    return;
                } else if (findEmployeeById(Integer.parseInt(id)) != null) { // There is already an Employee with that ID
                    System.err.println("ERROR: There is already an Employee with the same ID");
                    return;
                }
                System.out.println("Insert new Employee's NAME:");
                System.out.print(USER_INPUT);
                String name = reader.readLine();
                if (name.isEmpty()) { // Check for empty input
                    System.err.println("ERROR: You can't leave the information empty");
                    return;
                }
                System.out.println("Insert new Employee's ROLE:");
                System.out.print(USER_INPUT);
                String role = reader.readLine();
                if (role.isEmpty()) { // Check for empty input
                    System.err.println("ERROR: You can't leave the information empty");
                    return;
                }
                System.out.println("Insert new Employee's DEPNO:");
                System.out.print(USER_INPUT);
                String depno = reader.readLine();
                if (!depno.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Departments' ID are Integer values");
                    return;
                } else if (findDepartmentById(Integer.parseInt(depno)) == null) { // There is no Department with introduced DEPNO
                    System.err.println("ERROR: There is no Department with DEPNO " + depno);
                    return;
                }
                // Everything is good to execute the method
                Employee newEmployee = new Employee(Integer.parseInt(id), name, role, Integer.parseInt(depno)); // Create Employee object
                this.addEmployee(newEmployee);
                System.out.printf("%sNew Employee added successfully!%s\n", GREEN_FONT, RESET);
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeUpdateEmployee() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Employee's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Employee ID. Employee's ID are Integer values");
                    return;
                }
                Employee returnEmp = this.findEmployeeById(Integer.parseInt(input));
                if (returnEmp == null) { // Check if there is an Employee with the indicated ID
                    System.out.println("There is no Employee with EMPNO " + input);
                    return;
                }
                // Execute IDAO method
                Employee updated = updateEmployee(Integer.parseInt(input));
                System.out.println(updated.toString());
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeDeleteEmployee() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Employee's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Employee ID. Employee's ID are Integer values");
                    return;
                }
                Employee returnEmp = this.findEmployeeById(Integer.parseInt(input));
                if (returnEmp == null) { // Check if there is an Employee with the indicated ID
                    System.out.println("There is no Employee with EMPNO " + input);
                    return;
                }
                // Execute IDAO method
                Employee deleted = deleteEmployee(Integer.parseInt(input));
                System.out.println(deleted.toString());
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeFindAllDepartments() {
        if (this.connectionFlag) {
            String row = "+" + "-".repeat(7) + "+" + "-".repeat(20) + "+" + "-".repeat(16) + "+";
            List<Department> departments = this.findAllDepartments();
            if (departments != null) { // Check if the returned list is null or empty
                System.out.println(row);
                System.out.printf("| %-5s | %-18s | %-14s |\n", "DEPNO", "NOMBRE", "UBICACION");
                System.out.println(row);
                for (Department d : departments) {
                    System.out.printf("| %-5s | %-18s | %-14s |\n", d.getDepno(), d.getName(), d.getLocation());
                }
                System.out.println(row);
            } else {
                System.out.println("There are currently no Department stored");
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeFindDepartmentByID() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Department's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Department's ID are Integer values");
                    return;
                }
                Department returnDept = this.findDepartmentById(Integer.parseInt(input));
                if (returnDept != null) { // Check if the returning Department is null
                    System.out.println("Department's information:");
                    System.out.println(returnDept.toString());
                } else { // There is no Employee with the indicated ID
                    System.out.println("There is no Department with DEPNO " + input);
                }
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeAddDepartment() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert new Department's ID:");
                System.out.print(USER_INPUT);
                String depno = reader.readLine();
                if (!depno.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Department's ID are Integer values");
                    return;
                } else if (findDepartmentById(Integer.parseInt(depno)) != null) { // There is already an Employee with that ID
                    System.err.println("ERROR: There is already an Department with the same ID");
                    return;
                }
                System.out.println("Insert new Department's NAME:");
                System.out.print(USER_INPUT);
                String name = reader.readLine();
                if (name.isEmpty()) { // Check for empty input
                    System.err.println("ERROR: You can't leave the information empty");
                    return;
                }
                System.out.println("Insert new Department's LOCATION:");
                System.out.print(USER_INPUT);
                String location = reader.readLine();
                if (location.isEmpty()) { // Check for empty input
                    System.err.println("ERROR: You can't leave the information empty");
                    return;
                }
                // Everything is good to execute the method
                Department newDepartment = new Department(Integer.parseInt(depno), name, location); // Create Employee object
                this.addDepartment(newDepartment);
                System.out.printf("%sNew Department added successfully!%s\n", GREEN_FONT, RESET);
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeUpdateDepartment() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Department's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Department's ID are Integer values");
                    return;
                }
                Department returnDept = this.findDepartmentById(Integer.parseInt(input));
                if (returnDept == null) { // Check if there is an Employee with the indicated ID
                    System.out.println("There is no Department with DEPNO " + input);
                    return;
                }
                // Execute IDAO method
                Department updated = updateDepartment(Integer.parseInt(input));
                System.out.println(updated.toString());
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeDeleteDepartment() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Department's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Department's ID are Integer values");
                    return;
                }
                Department returnDept = this.findDepartmentById(Integer.parseInt(input));
                if (returnDept == null) { // Check if there is an Employee with the indicated ID
                    System.out.println("There is no Department with DEPNO " + input);
                    return;
                }
                // Execute IDAO method
                Department deleted = deleteDepartment(Integer.parseInt(input));
                System.out.println(deleted.toString());
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }

    // Implementation from Menu interface
    @Override
    public void executeFindEmployeesByDept() {
        if (this.connectionFlag) {
            BufferedReader reader = new BufferedReader(this.isr); // To read user input
            try {
                System.out.println("Insert Department's ID:");
                System.out.print(USER_INPUT);
                String input = reader.readLine();
                if (!input.matches("\\d+")) { // Check if the output is not numeric
                    System.err.println("ERROR: Please provide a valid Department ID. Department's ID are Integer values");
                    return;
                }
                Department returnDept = this.findDepartmentById(Integer.parseInt(input));
                if (returnDept == null) { // Check if there is an Employee with the indicated ID
                    System.out.println("There is no Department with DEPNO " + input);
                    return;
                }
                // Execute IDAO method
                ArrayList<Employee> departmentEmployees = (ArrayList<Employee>) findEmployeesByDept(Integer.parseInt(input));
                String row = "+" + "-".repeat(7) + "+" + "-".repeat(16) + "+" + "-".repeat(16) + "+";
                if (departmentEmployees == null || departmentEmployees.isEmpty()) { // No Employees in Department case
                    System.out.println("There are currently no Employees in the Department");
                } else {
                    System.out.println(row);
                    System.out.printf("| %-5s | %-14s | %-14s |\n", "EMPNO", "NOMBRE", "PUESTO");
                    System.out.println(row);
                    for (Employee e : departmentEmployees) {
                        System.out.printf("| %-5s | %-14s | %-14s |\n", e.getEmpno(), e.getName(), e.getPosition());
                    }
                    System.out.println(row);
                }
            } catch (IOException ioe) {
                System.err.println("ERROR: IOException error reported: " + ioe.getMessage());
            }
        } else {
            System.err.println("ERROR: You must first try to connect to the database with the method .connectDB()");
        }
    }
}