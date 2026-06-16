package com.br.lvs_group.label_cat.config;

import com.br.lvs_group.label_cat.entities.TypeOfLabel;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.entities.UserFunction;
import com.br.lvs_group.label_cat.repositories.TypeOfLabelRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TypeOfLabelRepository typeOfLabelRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("root@labelcat.com").isEmpty()) {
            User root = new User();
            root.setName("Root");
            root.setEmail("root@labelcat.com");
            root.setPassword(passwordEncoder.encode("root123"));
            root.setFunction(UserFunction.ADMIN);
            userRepository.save(root);

            System.out.println(">>> Root user created: root@labelcat.com / root123");
        }

        seedTypeOfLabel("Peça", "Componente para montagem");
        seedTypeOfLabel("Fixadores", "Parafusos, porcas e arruelas");
        seedTypeOfLabel("Prateleira", "Localização de armazenamento");
        seedTypeOfLabel("Jaula", "Contêiner para transporte");
    }

    private void seedTypeOfLabel(String name, String description) {
        if (!typeOfLabelRepository.existsByName(name)) {
            TypeOfLabel type = new TypeOfLabel();
            type.setName(name);
            type.setDescription(description);
            typeOfLabelRepository.save(type);

            System.out.println(">>> TypeOfLabel created: " + name);
        }
    }
}
