package io.github.maksluczak.ems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Modifying
    @Transactional
    @Query("update Employee e set e.profileImageId = ?1 where e.id = ?2")
    void updateProfileImageId(String profileImageId, Integer id);
}
