package com.example.demo.service;

import com.example.demo.dto.request.PayFarmRequestDto;
import com.example.demo.dto.response.BankAccountResponseDto;
import com.example.demo.dto.response.ExchangeResponseDto;
import com.example.demo.dto.response.PayFarmResponseDto;
import com.example.demo.entity.Farm;
import com.example.demo.entity.FarmImage;
import com.example.demo.entity.User;
import com.example.demo.exception.InsufficientCoinException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayService {
    private final UserRepository  userRepository;
    private final FarmRepository farmRepository;

    //결제하기
    @Transactional
    public PayFarmResponseDto pay(PayFarmRequestDto dto, Long userId) { // originalPrice 파라미터 추가

        // 1. 유저와 텃밭 정보 조회
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Farm farm = farmRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

        User provider = userRepository.findById(dto.getOwner().getUserId()).orElseThrow(UserNotFoundException::new);

        // 2. 텃밭이 이미 분양되었는지 확인
        if (!farm.isAvailable()) { // Farm 엔티티에 isAvailable() 메서드가 있다고 가정
            throw new IllegalStateException("이미 분양된 텃밭입니다.");
        }

        // 3. 할인 금액(ecoScore) 조회
        int ecoScoreDiscount = dto.getEcoScoreUse(); // User 엔티티에 getEcoScore()가 있다고 가정

        // 4. 최종 결제 금액 계산 (가격보다 ecoScore가 많으면 0원)
        int finalPrice = Math.max(0, farm.getPrice() - ecoScoreDiscount);

        // 5. 실제로 사용될 ecoScore 계산 (원가를 초과하여 차감되지 않도록 함)
        int ecoScoreToUse = Math.min(farm.getPrice(), ecoScoreDiscount);

        // 6. 유저의 코인이 최종 결제 금액보다 많은지 확인
        if (user.getCoin() < finalPrice) { // User 엔티티에 getCoin()이 있다고 가정
            throw new InsufficientCoinException("코인이 부족합니다.");
        }

        // 7. 코인 및 ecoScore 차감
        user.updateCoin(-finalPrice); // 결제 금액만큼 코인 차감
        user.updateEcoScore(-ecoScoreToUse); // 사용된 ecoScore만큼 점수 차감
        provider.updateCoin(dto.getPrice());

        // 8. 텃밭 상태를 '분양 불가'로 변경
        farm.setAvailable(false);
        farm.updateBorrowerId(userId);
        user.updateEcoScore(100);

        return createPayFarmResponse(farm,user);
    }

    private PayFarmResponseDto createPayFarmResponse(Farm farm, User user) {
        PayFarmResponseDto.FarmDto farmDto = PayFarmResponseDto.FarmDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .size(farm.getSize())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .theme(farm.getTheme())
                .description(farm.getDescription())
                .imageUrls(farm.getImages().stream().map(FarmImage::getImageUrl).collect(Collectors.toList()))
                .owner(PayFarmResponseDto.UserDto.builder()
                        .userId(farm.getUser().getUserId())
                        .nickname(farm.getUser().getNickname())
                        .build())
                .borrowerId(farm.getBorrowerId())
                .createdAt(farm.getCreatedAt())
                .updatedTime(farm.getUpdateTime())
                .isAvailable(farm.isAvailable())
                .build();

        return PayFarmResponseDto.builder()
                .farm(farmDto)
                .EcoScore(String.valueOf(user.getEcoScore())) // 남은 ecoScore
                .coin(String.valueOf(user.getCoin()))       // 남은 coin
                .build();
    }

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
