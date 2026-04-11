package com.game.platform.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.game.platform.dto.UserResponse;
import com.game.platform.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
   
    boolean existsByUserCode(String userCode);
    List<User> findByUsernameContainingIgnoreCase(String keyword);
    
    @Query("""
    		SELECT u FROM User u 
    		WHERE 
    		LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		OR LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		""")
    		List<User> searchByUsernameOrUserCode(@Param("keyword") String keyword);
}