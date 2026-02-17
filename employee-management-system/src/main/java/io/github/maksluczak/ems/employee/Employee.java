package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private String profileImageUrl;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
