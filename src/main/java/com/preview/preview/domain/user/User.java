package com.preview.preview.domain.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.preview.preview.domain.BaseTimeEntity;
import com.preview.preview.domain.authority.Authority;
import com.preview.preview.domain.enterprise.Enterprise;
import com.preview.preview.domain.job.Job;
import com.preview.preview.domain.like.PostLike;
import com.preview.preview.domain.post.Post;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Setter
    @Column(nullable = false, name = "nickname")
    private String nickname; // 닉네임

    @Column(nullable = true, name = "email")
    private String email; // 이메일

    @Column(nullable = false, name = "kakao_id")
    private Long kakaoId;

    @Setter
    @Column(name = "password")
    private String password; // 비밀번호

    @JsonIgnore
    @Column(name = "activated")
    private boolean activated;

    @Setter
    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;

    @Setter
    @ManyToMany
    @JoinTable(
            name = "user_job",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "job_name", referencedColumnName = "job_name")}
    )
    private Set<Job> likedJobs;

    @Setter
    @ManyToMany
    @JoinTable(
            name = "user_enterprise",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "enterprise_name", referencedColumnName = "enterprise_name")}
    )
    Set<Enterprise> likedEnterprises;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<Post> posts = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<PostLike> postLikes = new ArrayList<>();

    public Long deleteUser(){
        deletedDate = LocalDateTime.now();
        return id;
    }
}
