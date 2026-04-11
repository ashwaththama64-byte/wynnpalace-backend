package com.game.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.game.platform.entity.BankCard;

public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    List<BankCard> findByUserId(Long userId);

}