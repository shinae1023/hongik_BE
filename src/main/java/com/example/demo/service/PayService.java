package com.example.demo.service;

import com.example.demo.dto.response.BankAccountResponseDto;
import com.example.demo.dto.response.ExchangeResponseDto;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayService {
    private final UserRepository  userRepository;

    //충전하기
    @Transactional
    public String chargeCoin(Long userId, int money){
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("유저를 찾을 수 없습니다."));
        user.updateCoin(money);
        return  money + " 코인 충전을 완료했습니다.";
    }

    //환전하기
    @Transactional
    public ExchangeResponseDto exchangeCoin(Long userId, int money){
        User user= userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("유저를 찾을 수 없습니다."));
        user.updateCoin(-money);

        String bank = user.getBank();
        String accountNumber = user.getAccountNumber();

        // 응답 DTO 생성
        ExchangeResponseDto responseDto = new ExchangeResponseDto();
        responseDto.setMessage(money + " 코인 환전을 완료했습니다.");
        responseDto.setBank(bank);
        responseDto.setAccountNumber(accountNumber);

        return responseDto;
    }

    //코인 조회
    @Transactional(readOnly = true)
    public int getMyCoin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        return user.getCoin();
    }
}
