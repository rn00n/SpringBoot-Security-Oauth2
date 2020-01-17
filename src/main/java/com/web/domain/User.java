package com.web.domain;

import com.web.domain.enums.SocialType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table
public class User implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    public Long getIdx(){
        return idx;
    }

    @Column
    private String name;
    public String getName(){
        return name;
    }

    @Column
    private String password;
    public String getPassword(){
        return password;
    }

    @Column
    private String email;
    public String getEmail(){
        return email;
    }

    @Column
    private String principal;
    public String getPrincipal(){
        return principal;
    }

    @Column
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    public SocialType getSocialType(){
        return socialType;
    }

    @Column
    private LocalDateTime createdDate;
    public LocalDateTime getCreatedDate(){
        return createdDate;
    }

    @Column
    private LocalDateTime updatedDate;
    public LocalDateTime getUpdatedDate(){
        return updatedDate;
    }


//    lombok이 작동을 안한다
//    @Builder
    public User(String name, String password, String email, String principal, SocialType socialType, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.principal = principal;
        this.socialType = socialType;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
    static class Builder{
        private String name;
        private String password;
        private String email;
        private String principal;
        @Enumerated(EnumType.STRING)
        private SocialType socialType;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public Builder withName(String name){
            this.name = name;
            return this;
        }
        public Builder withPassword(String password){
            this.password = password;
            return this;
        }
        public Builder withEmail(String email){
            this.email = email;
            return this;
        }
        public Builder withPrincipal(String principal){
            this.principal = principal;
            return this;
        }
        public Builder withSocialType(SocialType socialType){
            this.socialType  = socialType;
            return this;
        }
        public Builder withCreatedDate(LocalDateTime createdDate){
            this.createdDate = createdDate;
            return this;
        }
        public Builder withUpdatedDate(LocalDateTime updatedDate){
            this.updatedDate = updatedDate;
            return this;
        }
        public User build(){
            if(name==null || password==null || email==null || principal==null ||
                    socialType==null || createdDate==null || updatedDate==null) {
                throw new IllegalStateException("Cannot이다이다");
            }
            return new User(name, password, email, principal, socialType, createdDate, updatedDate);
        }
    }
}
/* 로그인과 관련하여 인증 및 권한이 추가되므로 User 테이블에 컬럼 추가
*  OAuth2인증으로 제공받는 키 값인 principal
*  어떤 소셜 미디어로 인증 받았는지 구분하는 socialType 컬럼 추가*/