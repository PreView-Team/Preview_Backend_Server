package com.preview.preview.module.post.domain;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.preview.preview.module.category.domain.Category;
import com.preview.preview.module.review.domain.Review;
import com.preview.preview.module.user.domain.User;
import com.preview.preview.util.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "post")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Setter
    @Column(nullable = false, name = "title")
    private String title;

    @Setter
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_to_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_to_user"))
    private User user;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<Review> reviews = new ArrayList<>();

    @Formula("(SELECT count(1) FROM review r WHERE r.post_id = post_id)")
    private int reviewCnt;


    public Long deletePost(){
        deletedDate = LocalDateTime.now();
        return id;
    }

    public double getGrade(){
        int size = reviews.size();
        double num = 0;

        if (size == 0){
            return 0;
        }

        for (Review s:reviews){
            num += s.getGrade();
        }

        num = num / reviews.size();
        return Math.round(num);
    }
}
