package app.ccb.services;

import app.ccb.domain.dtos.EmployeeImportDto;
import app.ccb.domain.entities.Branch;
import app.ccb.domain.entities.Client;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.BranchRepository;
import app.ccb.repositories.EmployeeRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final static String EMPLOYEES_JSON_FILE_PATH = "C:\\Users\\Guest Lector\\Desktop\\ColonialCouncilBank\\src\\main\\resources\\files\\json\\employees.json";

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BranchRepository branchRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() != 0;
    }

    @Override
    public String readEmployeesJsonFile() throws IOException {
        return this.fileUtil.readFile(EMPLOYEES_JSON_FILE_PATH);
    }

    @Override
    public String importEmployees(String employees) {
        StringBuilder importResult = new StringBuilder();
        EmployeeImportDto[] employeeImportDtos = this.gson.fromJson(employees, EmployeeImportDto[].class);

        for (EmployeeImportDto employeeImportDto : employeeImportDtos) {
            if (!this.validationUtil.isValid(employeeImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Branch branchEntity = this.branchRepository.findByName(employeeImportDto.getBranchName()).orElse(null);

            if (branchEntity == null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Employee employeeEntity = this.modelMapper.map(employeeImportDto, Employee.class);
            employeeEntity.setFirstName(employeeImportDto.getFullName().split("\\s+")[0]);
            employeeEntity.setLastName(employeeImportDto.getFullName().split("\\s+")[1]);
            employeeEntity.setStartedOn(LocalDate.parse(employeeImportDto.getStartedOn()));
            employeeEntity.setBranch(branchEntity);

            this.employeeRepository.saveAndFlush(employeeEntity);

            importResult.append(String.format("Successfully imported Employee - %s %s", employeeEntity.getFirstName(), employeeEntity.getLastName())).append(System.lineSeparator());
        }

        return importResult.toString().trim();
    }

    @Override
    public String exportTopEmployees() {
        List<Employee> employeeEntities = this.employeeRepository.extractTopEmployees();

        StringBuilder exportResult = new StringBuilder();
        for (Employee employeeEntity : employeeEntities) {
            exportResult.append("Full Name: ").append(String.format("%s %s", employeeEntity.getFirstName(), employeeEntity.getLastName())).append(System.lineSeparator());
            exportResult.append("Salary: ").append(String.format("%.2f", employeeEntity.getSalary())).append(System.lineSeparator());
            exportResult.append("Started On: ").append(String.valueOf(employeeEntity.getStartedOn())).append(System.lineSeparator());
            exportResult.append("Clients:").append(System.lineSeparator());

            for (Client clientEntity : employeeEntity.getClients()) {
                exportResult.append(String.format(" %s", clientEntity.getFullName())).append(System.lineSeparator());
            }

            exportResult.append(System.lineSeparator());
        }

        return exportResult.toString().trim();
    }
}
