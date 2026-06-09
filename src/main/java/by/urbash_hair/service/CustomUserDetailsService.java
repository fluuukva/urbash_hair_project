package by.urbash_hair.service;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.entity.Client;
import by.urbash_hair.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DATA_OFFICER = "DATA_OFFICER";

    private final ClientRepository clientRepository;
    private final HashUtils hashUtils;

    @Value("${admin.phone}")
    private String adminPhone;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        String phoneHash = hashUtils.hashPhone(phone);
        Client client = clientRepository.findByPhoneHash(phoneHash)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с телефоном " + phone + " не найден"));
        return buildUserDetails(client);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с ID " + id + " не найден"));
        return buildUserDetails(client);
    }

    private UserDetails buildUserDetails(Client client) {
        String role = determineRole(client);
        return new User(
                String.valueOf(client.getId()),
                "N/A",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    public String determineRole(Client client) {
        String phoneFromDb = client.getPhone();          // расшифрованный номер
        System.err.println("=== ADMIN CHECK ===");
        System.err.println("Config admin phone: " + adminPhone);
        System.err.println("Client phone from DB: " + phoneFromDb);
        System.err.println("Equal? " + (phoneFromDb != null && phoneFromDb.equals(adminPhone)));
        System.err.println("==================");

        if (phoneFromDb != null && phoneFromDb.equals(adminPhone)) {
            return ROLE_ADMIN;
        }
        return ROLE_CLIENT;
    }
}
