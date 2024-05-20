package csu.RouteGuideBackend.config;

import csu.RouteGuideBackend.domain.member.entity.Member;
import csu.RouteGuideBackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public PrincipalDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(username);
        return new PrincipalDetails(memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패")));
    }
}
