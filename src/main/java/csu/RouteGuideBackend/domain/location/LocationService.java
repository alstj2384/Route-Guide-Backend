package csu.RouteGuideBackend.domain.location;

import csu.RouteGuideBackend.domain.member.entity.Member;
import csu.RouteGuideBackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void update(int id, double lat, double lon){
        Member find = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));

        if (find.getLocation() == null){
            Location now = Location.builder()
                    .lat(lat)
                    .lon(lon)
                    .member(find)
                    .updatedAt(LocalDateTime.now())
                    .build();
            Location save = locationRepository.save(now);
            find.setLocation(save);
        } else {
            Location now = find.getLocation();
            now.setUpdatedAt(LocalDateTime.now());
            now.setLat(lat);
            now.setLon(lon);
        }


        // id = 회원 id
    }

}
