package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.UserState;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.dto.PrincipalDetails;
import com.thred.datingapp.report.repository.ReportHistoryRepository;
import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository          userRepository;
    private final ReportHistoryRepository reportHistoryRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username).orElseThrow(() -> {
            log.error("[loadUserByUsername] 존재하지 않은 회원 ===> userName: {}", username);
            return new CustomException(UserErrorCode.USER_NOT_FOUND);
        });
        if (UserState.SUSPENDED.equals(user.getUserState())) {

            int totalRemainingDays = reportHistoryRepository.findSuspendedDateByReportedUserId(user.getId())
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToInt(date -> (int) ChronoUnit.DAYS.between(LocalDate.now(), date))
                    .filter(days -> days > 0)
                    .sum();

            if (totalRemainingDays <= 0) {
                log.info("[loadUserByUsername] Suspended -> Active로 변경 ===>userName: {}, totalRemainingDays: {}", username, totalRemainingDays);
                user.updateUserState(UserState.ACTIVE);
            } else {
                log.error("[loadUserByUsername] 관리자로 인한 정지된 사용자 ===>userName: {}, totalRemainingDays: {}", username, totalRemainingDays);
                throw new CustomException(UserErrorCode.SUSPENDED_USER);
            }
        }
        // 탈퇴 유예 기간 내에 로그인한 경우, 회원 상태를 ACTIVE로 복구하고 탈퇴 요청 취소
        user.cancelWithdraw();
        return new PrincipalDetails(user);
    }
}
