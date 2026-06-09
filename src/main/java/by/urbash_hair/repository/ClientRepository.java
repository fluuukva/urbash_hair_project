package by.urbash_hair.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import by.urbash_hair.entity.Client;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByPhoneHash(String phoneHash);

    Optional<Client> findByEmailHash(String emailHash);

    Optional<Client> findByTelegramId(String telegramId);

    @Deprecated
    Optional<Client> findByEmail(String email);

    @Deprecated
    Optional<Client> findByPhone(String phone);
}