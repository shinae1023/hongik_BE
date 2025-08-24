package com.example.demo.service;

import com.example.demo.dto.request.PostRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.dto.response.PostResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Image;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 💡 클래스 레벨에 읽기 전용 트랜잭션을 기본으로 설정
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final S3Uploader s3Uploader;

    /**
     * 게시물 생성
     */
    @Transactional // 💡 쓰기 작업이므로 readOnly=false 적용
    public Long createPost(PostRequestDto dto, List<MultipartFile> images) throws IOException {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .viewCount(0L)
                .user(user)
                .createdAt(createdAt)
                .build();

        if (images != null && !images.isEmpty()) {
            List<Image> imageList = new ArrayList<>();
            for (MultipartFile imageFile : images) {
                String imageUrl = s3Uploader.upload(imageFile, "images");
                imageList.add(Image.builder().imageUrl(imageUrl).post(post).build());
            }
            post.setImages(new HashSet<>(imageList));
        }

        Post savedPost = postRepository.save(post);
        user.updateEcoScore(10);
        return savedPost.getId();
    }

    /**
     * 게시물 상세 조회
     */
    public PostResponseDto getPost(Long postId, Long userId) { // ✅ 현재 사용자 ID를 파라미터로 받음
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // ✅ 현재 사용자가 이 게시물에 좋아요를 눌렀는지 확인
        boolean isLiked = (userId != null) && likeRepository.findByUser_UserIdAndPostId(userId, postId).isPresent();

        List<String> imageUrls = post.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        List<CommentResponseDto> commentDtos = post.getComments().stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId())
                        .userId(comment.getUser().getUserId())
                        .postId(comment.getPost().getId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .authorNickname(comment.getUser().getNickname())
                        .profileImage(comment.getUser().getProfileImage())
                        .build())
                .collect(Collectors.toList());

        return PostResponseDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .authorNickname(post.getUser().getNickname())
                .profileImage(post.getUser().getProfileImage())
                .imageUrls(imageUrls)
                .comments(commentDtos)
                .likeCount((long) post.getLikes().size())
                .isLiked(isLiked) // ✅ 계산된 isLiked 값 추가
                .build();
    }

    /**
     * 게시물 목록을 DTO로 변환하는 통합 헬퍼 메소드 (N+1 해결 및 코드 중복 제거)
     */
    private List<PostSummaryDto> mapPostsToSummaryDtos(List<Post> posts, Long userId) {
        // ✅ 비로그인 사용자는 빈 Set을 사용, 로그인 사용자는 좋아요한 게시물 ID 목록 조회 (쿼리 1번)
        Set<Long> likedPostIds = (userId == null)
                ? Collections.emptySet()
                : likeRepository.findByUser_UserId(userId).stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        return posts.stream()
                .map(post -> convertToSummaryDto(post, likedPostIds))
                .collect(Collectors.toList());
    }

    /**
     * 유저별 게시물 조회
     */
    public List<PostSummaryDto> getUserPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        List<Post> posts = postRepository.findByUser(user);
        return mapPostsToSummaryDtos(posts, userId); // ✅ 통합 헬퍼 메소드 사용
    }

    /**
     * 게시물 전체 조회
     */
    public List<PostSummaryDto> getPostsAll(Long userId) {
        List<Post> posts = postRepository.findAllWithLikes();
        return mapPostsToSummaryDtos(posts, userId); // ✅ 통합 헬퍼 메소드 사용
    }

    /**
     * 피드 게시판 조회
     */
    public List<PostSummaryDto> getPostsFeed(Long userId) {
        List<Post> posts = postRepository.findByCategory(Category.FEED);
        return mapPostsToSummaryDtos(posts, userId); // ✅ 통합 헬퍼 메소드 사용
    }

    /**
     * 팁 게시판 조회
     */
    public List<PostSummaryDto> getPostsTip(Long userId) {
        List<Post> posts = postRepository.findByCategory(Category.TIP);
        return mapPostsToSummaryDtos(posts, userId); // ✅ 통합 헬퍼 메소드 사용
    }

    /**
     * 게시글 검색 (제목 또는 제목+카테고리)
     */
    public List<PostSummaryDto> searchPosts(String title, Category category, Long userId) {
        List<Post> posts;
        if (category != null) {
            posts = postRepository.findByTitleContainingAndCategory(title, category);
        } else {
            posts = postRepository.findByTitleContaining(title);
        }
        return mapPostsToSummaryDtos(posts, userId); // ✅ 통합 헬퍼 메소드 사용
    }

    /**
     * 게시글 삭제
     */
    @Transactional // 💡 쓰기 작업이므로 readOnly=false 적용
    public String deletePost(Long userId, long postId) {
        // ✅ findByUser_UserIdAndId 보다는 작성자 검증 로직을 서비스 계층에서 명시적으로 하는 것이 더 명확함
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new SecurityException("게시물을 삭제할 권한이 없습니다.");
        }
        postRepository.delete(post);
        return "게시물이 삭제되었습니다.";
    }

    /**
     * Post 엔티티를 PostSummaryDto로 변환하는 헬퍼 메서드
     */
    private PostSummaryDto convertToSummaryDto(Post post, Set<Long> likedPostIds) {
        String thumbnailUrl = post.getImages().stream()
                .findFirst()
                .map(Image::getImageUrl)
                .orElse(null);

        return PostSummaryDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname(post.getUser().getNickname())
                .profileImage(post.getUser().getProfileImage())
                .content(post.getContent())
                .thumbnailUrl(thumbnailUrl)
                .likeCount((long) post.getLikes().size())
                .createdAt(post.getCreatedAt())
                .isLiked(likedPostIds.contains(post.getId()))
                .build();
    }
}