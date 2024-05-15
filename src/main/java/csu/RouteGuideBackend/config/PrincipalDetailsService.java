package csu.RouteGuideBackend.config;

import csu.RouteGuideBackend.domain.member.Member;
import csu.RouteGuideBackend.domain.member.MemberRepository;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(username);
        Member memberEntity = memberRepository.findByEmail(username).orElse(null);
        log.info(memberEntity.toString());
        if(memberEntity != null){
            return new PrincipalDetails(memberEntity);
        }
        return null;
    }
}
