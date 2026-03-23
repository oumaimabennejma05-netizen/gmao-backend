package com.gmao.config;

import com.gmao.entity.Machine;
import com.gmao.entity.MachineData;
import com.gmao.entity.Task;
import com.gmao.entity.User;
import com.gmao.enums.MachineStatus;
import com.gmao.enums.Role;
import com.gmao.enums.TaskPriority;
import com.gmao.enums.TaskStatus;
import com.gmao.repository.MachineDataRepository;
import com.gmao.repository.MachineRepository;
import com.gmao.repository.TaskRepository;
import com.gmao.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                     MachineRepository machineRepository,
                                     TaskRepository taskRepository,
                                     MachineDataRepository machineDataRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                logger.info("Database already initialized. Skipping data seeding.");
                return;
            }

            logger.info("Initializing sample data...");

            // Create users
            User admin = new User("Admin GMAO", "admin@gmao.com", passwordEncoder.encode("Admin@123"), Role.ADMIN);
            admin.setAddress("123 Industrial Zone, Tunis");
            admin.setIdNumber("ADM001");

            User responsable = new User("Mohamed Ali", "responsable@gmao.com", passwordEncoder.encode("Resp@123"), Role.RESPONSABLE);
            responsable.setAddress("456 Factory St, Sfax");
            responsable.setIdNumber("RSP001");

            User tech1 = new User("Karim Sassi", "tech1@gmao.com", passwordEncoder.encode("Tech@123"), Role.TECHNICIAN);
            tech1.setAddress("789 Workshop Ave, Sousse");
            tech1.setIdNumber("TCH001");

            User tech2 = new User("Amira Ben Salah", "tech2@gmao.com", passwordEncoder.encode("Tech@123"), Role.TECHNICIAN);
            tech2.setAddress("321 Maintenance Rd, Monastir");
            tech2.setIdNumber("TCH002");

            userRepository.saveAll(List.of(admin, responsable, tech1, tech2));

            // Create machines
            Machine cnc = new Machine("CNC Machine Alpha", "FANUC-30i", "Workshop A", MachineStatus.OPERATIONAL);
            cnc.setMaintenanceDate(LocalDate.now().plusMonths(2));

            Machine lathe = new Machine("Lathe Machine Beta", "DMG-CLX500", "Workshop B", MachineStatus.OPERATIONAL);
            lathe.setMaintenanceDate(LocalDate.now().plusMonths(1));

            Machine press = new Machine("Hydraulic Press Gamma", "ENERPAC-P142", "Press Room", MachineStatus.MAINTENANCE);
            press.setMaintenanceDate(LocalDate.now().minusDays(5));

            Machine compressor = new Machine("Air Compressor Delta", "ATLAS-GA55", "Utility Room", MachineStatus.OPERATIONAL);
            compressor.setMaintenanceDate(LocalDate.now().plusMonths(3));

            Machine conveyor = new Machine("Conveyor Belt Epsilon", "INTRALOX-T500", "Assembly Line", MachineStatus.BROKEN);
            conveyor.setMaintenanceDate(LocalDate.now());

            machineRepository.saveAll(List.of(cnc, lathe, press, compressor, conveyor));

            // Create machine telemetry data
            // CNC - getting hot
            machineDataRepository.save(new MachineData(cnc, 87.5, 3.2, 950.0));
            machineDataRepository.save(new MachineData(cnc, 85.0, 3.0, 948.0));

            // Lathe - normal
            machineDataRepository.save(new MachineData(lathe, 55.0, 2.1, 320.0));
            machineDataRepository.save(new MachineData(lathe, 53.0, 2.0, 318.0));

            // Press - critical vibration
            machineDataRepository.save(new MachineData(press, 65.0, 8.9, 1100.0));
            machineDataRepository.save(new MachineData(press, 68.0, 9.1, 1098.0));

            // Compressor - warning
            machineDataRepository.save(new MachineData(compressor, 72.0, 4.8, 820.0));

            // Conveyor - critical
            machineDataRepository.save(new MachineData(conveyor, 95.0, 7.5, 1350.0));

            // Create tasks
            Task task1 = new Task();
            task1.setTitle("CNC Temperature Inspection");
            task1.setDescription("Inspect and clean cooling system on CNC Machine Alpha. Temperature trending above warning threshold.");
            task1.setMachine(cnc);
            task1.setTechnician(tech1);
            task1.setPriority(TaskPriority.HIGH);
            task1.setStatus(TaskStatus.IN_PROGRESS);
            task1.setDueDate(LocalDate.now().plusDays(2));

            Task task2 = new Task();
            task2.setTitle("Hydraulic Press Emergency Maintenance");
            task2.setDescription("Critical vibration detected. Inspect bearings, mounting, and hydraulic seals.");
            task2.setMachine(press);
            task2.setTechnician(tech2);
            task2.setPriority(TaskPriority.CRITICAL);
            task2.setStatus(TaskStatus.PENDING);
            task2.setDueDate(LocalDate.now());

            Task task3 = new Task();
            task3.setTitle("Conveyor Belt Repair");
            task3.setDescription("Conveyor belt has stopped. Diagnose fault and replace damaged components.");
            task3.setMachine(conveyor);
            task3.setTechnician(tech1);
            task3.setPriority(TaskPriority.CRITICAL);
            task3.setStatus(TaskStatus.PENDING);
            task3.setDueDate(LocalDate.now());

            Task task4 = new Task();
            task4.setTitle("Lathe Monthly Lubrication");
            task4.setDescription("Perform monthly lubrication routine on DMG CLX500 lathe machine.");
            task4.setMachine(lathe);
            task4.setTechnician(tech2);
            task4.setPriority(TaskPriority.LOW);
            task4.setStatus(TaskStatus.COMPLETED);
            task4.setDueDate(LocalDate.now().minusDays(3));

            Task task5 = new Task();
            task5.setTitle("Compressor Filter Replacement");
            task5.setDescription("Replace air filters on Atlas GA55 compressor. Runtime approaching maintenance threshold.");
            task5.setMachine(compressor);
            task5.setTechnician(tech1);
            task5.setPriority(TaskPriority.MEDIUM);
            task5.setStatus(TaskStatus.PENDING);
            task5.setDueDate(LocalDate.now().plusDays(7));

            taskRepository.saveAll(List.of(task1, task2, task3, task4, task5));

            logger.info("Sample data initialized successfully!");
            logger.info("Admin credentials - Email: admin@gmao.com | Password: Admin@123");
            logger.info("Responsable credentials - Email: responsable@gmao.com | Password: Resp@123");
            logger.info("Technician 1 credentials - Email: tech1@gmao.com | Password: Tech@123");
        };
    }
}
