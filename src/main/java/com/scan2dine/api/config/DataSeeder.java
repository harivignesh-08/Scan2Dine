package com.scan2dine.api.config;

import com.scan2dine.api.entity.*;
import com.scan2dine.api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final MealRepository mealRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CollegeRepository collegeRepository,
                      UserRepository userRepository,
                      HostelRepository hostelRepository,
                      RoomRepository roomRepository,
                      StudentRepository studentRepository,
                      MealRepository mealRepository,
                      PasswordEncoder passwordEncoder) {
        this.collegeRepository = collegeRepository;
        this.userRepository = userRepository;
        this.hostelRepository = hostelRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.mealRepository = mealRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            System.out.println("====== SCAN2DINE: STARTING DATABASE SEEDING ======");
            
            // 1. Seed Super Admin (Global Context)
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                System.out.println("SEEDING: Creating Super Admin user account (superadmin / superadmin123)...");
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
                superAdmin.setEmail("superadmin@scan2dine.com");
                superAdmin.setRole("SUPER_ADMIN");
                superAdmin.setActive(true);
                userRepository.save(superAdmin);
            } else {
                System.out.println("SEEDING: Super Admin user already exists.");
            }

            // 2. Seed Sample College (Tenant)
            if (collegeRepository.findByCollegeCode("ECE101").isEmpty()) {
                System.out.println("SEEDING: Creating default College ECE101 and its associated admins, wardens, hostels, and students...");
                College college = new College();
                college.setCollegeName("Exemplar College of Engineering");
                college.setCollegeCode("ECE101");
                college.setLogo("/logo.png");
                college.setThemeColor("#3b82f6"); // Blue theme
                college.setEmail("info@exemplar.edu");
                college.setPhone("+91 44 27478000");
                college.setErpName("Campus7");
                college.setErpBaseUrl("https://api.campus7.exemplar.edu/v1");
                college.setErpApiKey("key_exemplar_8dfb1e9c2");
                college.setSubscriptionPlan("BASIC");
                college.setSubscriptionStartDate(LocalDateTime.now());
                college.setSubscriptionEndDate(LocalDateTime.now().plusYears(1)); // 1 year validity
                college.setStatus("APPROVED");

                College savedCollege = collegeRepository.save(college);

                // Set current tenant context to seed tenant records
                TenantContext.setCurrentTenant(savedCollege.getId());

                // 3. Seed College Admin for this college
                User collegeAdmin = new User();
                collegeAdmin.setCollegeId(savedCollege.getId());
                collegeAdmin.setUsername("collegeadmin");
                collegeAdmin.setPassword(passwordEncoder.encode("collegeadmin123"));
                collegeAdmin.setEmail("admin@exemplar.edu");
                collegeAdmin.setRole("COLLEGE_ADMIN");
                collegeAdmin.setActive(true);
                userRepository.save(collegeAdmin);

                // 4. Seed Warden for this college
                User warden = new User();
                warden.setCollegeId(savedCollege.getId());
                warden.setUsername("warden");
                warden.setPassword(passwordEncoder.encode("warden123"));
                warden.setEmail("warden@exemplar.edu");
                warden.setRole("WARDEN");
                warden.setActive(true);
                userRepository.save(warden);

                // 5. Seed Hostels
                Hostel nelsonHostel = new Hostel();
                nelsonHostel.setCollegeId(savedCollege.getId());
                nelsonHostel.setName("Nelson Mandela Men's Hostel");
                nelsonHostel.setCapacity(120);
                Hostel savedNelson = hostelRepository.save(nelsonHostel);

                Hostel teresaHostel = new Hostel();
                teresaHostel.setCollegeId(savedCollege.getId());
                teresaHostel.setName("Mother Teresa Women's Hostel");
                teresaHostel.setCapacity(120);
                Hostel savedTeresa = hostelRepository.save(teresaHostel);

                // 6. Seed Rooms
                Room room101 = new Room();
                room101.setCollegeId(savedCollege.getId());
                room101.setHostel(savedNelson);
                room101.setRoomNumber("101");
                room101.setCapacity(4);
                roomRepository.save(room101);

                Room room102 = new Room();
                room102.setCollegeId(savedCollege.getId());
                room102.setHostel(savedNelson);
                room102.setRoomNumber("102");
                room102.setCapacity(4);
                roomRepository.save(room102);

                Room room201 = new Room();
                room201.setCollegeId(savedCollege.getId());
                room201.setHostel(savedTeresa);
                room201.setRoomNumber("201");
                room201.setCapacity(4);
                roomRepository.save(room201);

                // 7. Seed Meals settings
                Meal breakfast = new Meal();
                breakfast.setCollegeId(savedCollege.getId());
                breakfast.setMealName("Breakfast");
                breakfast.setStartTime(LocalTime.of(7, 0));
                breakfast.setEndTime(LocalTime.of(9, 30));
                mealRepository.save(breakfast);

                Meal lunch = new Meal();
                lunch.setCollegeId(savedCollege.getId());
                lunch.setMealName("Lunch");
                lunch.setStartTime(LocalTime.of(12, 0));
                lunch.setEndTime(LocalTime.of(14, 30));
                mealRepository.save(lunch);

                Meal dinner = new Meal();
                dinner.setCollegeId(savedCollege.getId());
                dinner.setMealName("Dinner");
                dinner.setStartTime(LocalTime.of(19, 30));
                dinner.setEndTime(LocalTime.of(22, 0));
                mealRepository.save(dinner);

                // 8. Seed Students
                Student student1 = new Student();
                student1.setCollegeId(savedCollege.getId());
                student1.setName("Rahul Sharma");
                student1.setRollNumber("20ECE01");
                student1.setDepartment("Computer Science");
                student1.setYear(3);
                student1.setPhone("+91 9840123456");
                student1.setHostel(savedNelson);
                student1.setRoom(room101);
                student1.setBarcode("9876543210");
                student1.setStatus("ACTIVE");
                studentRepository.save(student1);

                Student student2 = new Student();
                student2.setCollegeId(savedCollege.getId());
                student2.setName("Priya Patel");
                student2.setRollNumber("20ECE02");
                student2.setDepartment("Information Technology");
                student2.setYear(3);
                student2.setPhone("+91 9840123457");
                student2.setHostel(savedTeresa);
                student2.setRoom(room201);
                student2.setBarcode("9876543211");
                student2.setStatus("ACTIVE");
                studentRepository.save(student2);

                // Clear tenant context
                TenantContext.clear();
            } else {
                System.out.println("SEEDING: Default College ECE101 already exists.");
            }
            
            System.out.println("====== SCAN2DINE: DATABASE SEEDING SUCCESSFUL ======");
        } catch (Exception e) {
            System.err.println("!!! SCAN2DINE: DATABASE SEEDING FAILED !!!");
            e.printStackTrace();
        }
    }
}
