package ai.llm.cook.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private String fullName;

    @OneToMany(mappedBy = "user")
    private List<Recipe> recipes;

    private boolean enabled = true;
}