package com.preview.preview.module.post.application;


import com.preview.preview.core.exception.CustomException;
import com.preview.preview.core.exception.ErrorCode;
import com.preview.preview.module.category.domain.Category;
import com.preview.preview.module.category.domain.CategoryRepository;
import com.preview.preview.module.job.domain.Job;
import com.preview.preview.module.mentor.domain.MentorRepository;
import com.preview.preview.module.post.application.dto.*;
import com.preview.preview.module.post.domain.Post;
import com.preview.preview.module.post.domain.PostLikeRepository;
import com.preview.preview.module.post.domain.PostRepository;
import com.preview.preview.module.user.domain.User;
import com.preview.preview.module.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostRepository postsRepository;
    private final UserRepository userRepository;

    private final MentorRepository mentorRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Transactional
    public PostCreateResponseDto save(long kakaoID, PostCreateRequestDto requestDto) {

        User user = userRepository.findByKakaoId(kakaoID).orElseThrow(() -> {
            throw new CustomException(ErrorCode.NOT_EXISTED_USER_ID);
        });

        if (user.getMentor() == null){
            throw new CustomException(ErrorCode.NOT_EQUAL_USER_RESOURCE);
        }

        if (user.getMentor().getMentorJobList().size() == 0){
            throw new CustomException(ErrorCode.NOT_EXISTED_MENTOR_JOB);
        }

        Category category = categoryRepository.findCategoryByName(user.getMentor().getMentorJobList().get(0)).orElseThrow(() -> {
            throw new CustomException(ErrorCode.NOT_EXISTED_CATEGORY_ID);
        });

        Post post = Post.builder()
                .category(category)
                .user(user)
                .content(requestDto.getContents())
                .title(requestDto.getTitle())
                .build();

        return PostCreateResponseDto.from(postsRepository.save(post));
    }

    @Transactional
    public PostUpdateResponseDto update(long kakaoId, PostsUpdateRequestDto requestDto) {
        Post post = postsRepository.findById(requestDto.getPostId()).filter(getPost -> getPost.getDeletedDate() == null).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_POST_ID));
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));

        // kakao id, post에 있는 user id 비교 다를 시 예외처리
        if (post.getUser().getId() != user.getId()) {
            throw new CustomException(ErrorCode.NOT_EQUAL_USER_RESOURCE);
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContents());

        return PostUpdateResponseDto.from(postsRepository.save(post));
    }

    @Transactional
    public PostGetResponseDto findById(Long kakaoId, Long postId) {
        userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));
        Post post = postsRepository.findById(postId).filter(getPost -> getPost.getDeletedDate() == null).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_POST_ID));
        return PostGetResponseDto.from(post);
    }

    @Transactional
    public List<PostsGetByCategoryResponseDto> findPostsByCategoryName(long kakaoId, String name, Pageable pageable) {
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));
        List<Post> posts = postsRepository.findPostByCategoryNameAndDeletedDateIsNull(name, pageable).toList();
        boolean like;

        List<PostsGetByCategoryResponseDto> list = new ArrayList<>();

        for (Post s : posts) {
            like = postLikeRepository.existsPostLikeByUserIdAndPostId(user.getId(), s.getId());
            list.add(PostsGetByCategoryResponseDto.from(s, like));
        }

        return list;
    }

    @Transactional
    public List<PostsGetByCategoryResponseDto> search(long kakaoId, String keyword, String category , Pageable pageable){
        List<Post> posts = postsRepository.findPostByCategoryNameAndDeletedDateIsNullAndContentContainingOrTitleContaining(category, keyword, keyword, pageable).toList();
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));
        List<PostsGetByCategoryResponseDto> list = new ArrayList<>();

        boolean like;

        for (Post s : posts) {
            like = postLikeRepository.existsPostLikeByUserIdAndPostId(user.getId(), s.getId());
            list.add(PostsGetByCategoryResponseDto.from(s, like));
        }
        return list;
    }

    @Transactional
    public List<PostsGetByCategoryResponseDto> findRecommendedPostsByCategoryName(long kakaoId, String name, Pageable pageable) {
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));

        List<Post> posts = postsRepository.findPostByCategoryNameAndDeletedDateIsNull(name, pageable).stream().filter(
                post ->
                user.isExistedJob(post.getCategory().getName()) == true).collect(Collectors.toList());
        boolean like;

        List<PostsGetByCategoryResponseDto> list = new ArrayList<>();

        for (Post s : posts) {
            like = postLikeRepository.existsPostLikeByUserIdAndPostId(user.getId(), s.getId());
            list.add(PostsGetByCategoryResponseDto.from(s, like));
        }

        return list;
    }

    @Transactional
    public List<PostGetAtHomeResponseDto> findPostsByNewMentor(Pageable pageable){
        List<Post> posts = postsRepository.findPostByDeletedDateIsNull(pageable).toList();

        List<PostGetAtHomeResponseDto> list = new ArrayList<>();

        for (Post post:posts){
            list.add(PostGetAtHomeResponseDto.from(post));
        }

        return list;
    }

    @Transactional
    public List<PostGetAtHomeResponseDto> findPostsByRecommendMentor(long kakaoId, Pageable pageable){
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(()->new CustomException(ErrorCode.NOT_EXISTED_USER_ID));

        List<Post> posts = postsRepository.findPostByCategoryNameAndDeletedDateIsNull(user.getLikedJobsInProfile().get(0), pageable).toList();

        List<PostGetAtHomeResponseDto> list = new ArrayList<>();

        for (Post post:posts){
            list.add(PostGetAtHomeResponseDto.from(post));
        }

        return list;
    }

    @Transactional
    public PostDeleteResponseDto delete(long kakaoId, long postId) {

        Post post = postsRepository.findById(postId).filter(getPost -> getPost.getDeletedDate() == null).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_POST_ID));
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));

        // kakao id, post에 있는 user id 비교 다를 시 예외처리
        if (post.getUser().getId() != user.getId()) {
            throw new CustomException(ErrorCode.NOT_EQUAL_USER_RESOURCE);
        }
        post.deletePost();
        postsRepository.save(post);
        return PostDeleteResponseDto.from(post);
    }

    @Transactional
    public List<PostsGetByMentorIdResponseDto> getPostsBykakaoId(long kakaoId){
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(()-> new CustomException(ErrorCode.NOT_EXISTED_USER_ID));

        List<Post> posts = postsRepository.findPostByUserId(user.getId()).stream().filter(post -> post.getDeletedDate() == null).collect(Collectors.toList());
        List<PostsGetByMentorIdResponseDto> list = new ArrayList<>();
        for (Post post: posts){
            list.add(PostsGetByMentorIdResponseDto.from(post));
        }
        return list;
    }

    @Transactional
    public PostGetByMentorResponseDto getPostByMentor(long postId){
        Post post = postsRepository.findById(postId).filter(post1 -> post1.getDeletedDate() == null).orElseThrow(()->new CustomException(ErrorCode.NOT_EXISTED_POST_ID));
        return PostGetByMentorResponseDto.from(post);
    }

}
