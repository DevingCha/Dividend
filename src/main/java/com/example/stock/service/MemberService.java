package com.example.stock.service;

import com.example.stock.exception.impl.AlreadyExistUserException;
import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import com.example.stock.persist.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = memberRepository.existsByUsername(member.getUsername());

        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    public MemberEntity authenticate(Auth.SignIn signIn) {
        MemberEntity member = memberRepository.findByUsername(signIn.getUsername())
                .orElseThrow(() -> new RuntimeException("아이디 혹은 비밀번호가 일치하지 않습니다."));

        if (!this.passwordEncoder.matches(signIn.getPassword(), member.getPassword())) {
            throw new RuntimeException("아이디 혹은 비밀번호가 일치하지 않습니다.");
        }

        return member;
    }
}
