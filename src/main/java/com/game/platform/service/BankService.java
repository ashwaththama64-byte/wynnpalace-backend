package com.game.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.game.platform.dto.AddCardRequest;
import com.game.platform.entity.BankCard;
import com.game.platform.entity.User;
import com.game.platform.repository.BankCardRepository;
import com.game.platform.repository.UserRepository;

@Service
public class BankService {

    private final BankCardRepository cardRepo;
    private final UserRepository userRepo;

    public BankService(BankCardRepository cardRepo, UserRepository userRepo) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
    }

    public void addCard(String username, AddCardRequest req) {

        User user = userRepo.findByUsername(username).orElseThrow();

        BankCard card = new BankCard();
        card.setUserId(user.getId());
        card.setBankName(req.bankName);
        card.setAccountNumber(req.accountNumber);
        card.setIfsc(req.ifsc);
        card.setHolderName(req.holderName);

        cardRepo.save(card);
    }

    public List<BankCard> getCards(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return cardRepo.findByUserId(user.getId());
    }
}