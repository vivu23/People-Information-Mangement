package springboot.repositories;

import mvc.model.User;

public interface UserRepository extends org.   springframework.data.jpa.repository.JpaRepository<mvc.model.User, Long> {
    User findByuserLogin(String userLogin);
}