package com.example.demo.service;

import com.example.demo.dto.request.PostRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.dto.response.PostResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Image;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    /**
     * 게시물 생성
     */
    @Transactional
    public Long createPost(PostRequestDto dto, List<MultipartFile> images) throws IOException {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 1. Post 엔티티 생성 (아직 저장하지 않음)
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .viewCount(0L)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .modifiedAt(new Timestamp(System.currentTimeMillis()))
                .user(user)
                .build();

        // 2. 이미지 S3에 업로드 및 Image 엔티티 생성
        if (images != null && !images.isEmpty()) {
            List<Image> imageList = new ArrayList<>();
            for (MultipartFile imageFile : images) {
                String imageUrl = s3Uploader.upload(imageFile, "images");
                Image image = Image.builder()
                        .imageUrl(imageUrl)
                        .post(post) // 연관관계 설정
                        .build();
                imageList.add(image);
            }
            // Post 엔티티에 이미지 리스트를 설정합니다.
            // CascadeType.ALL 때문에 Post 저장 시 Image도 함께 저장됩니다.
            post.setImages(imageList);
        }

        // 3. Post 엔티티를 저장 (연관된 Image 엔티티들도 함께 저장됨)
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    /**
     * 게시물 상세 조회
     */
    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 이미지 URL들을 리스트로 변환
        List<String> imageUrls = post.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        List<CommentResponseDto> commentDtos = post.getComments().stream()
                .map(comment -> CommentResponseDto.builder()
                        .userId(comment.getUser().getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .authorNickname(comment.getUser().getNickname())
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
                .imageUrls(imageUrls)
                .comments(commentDtos)
                .likeCount((long) post.getLikes().size())
                .build();
    }

    /**
     * 유저별 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getUserPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return postRepository.findByUser(user).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시물 전체 조회
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostsAll() {
        return postRepository.findAllWithLikes().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 피드 게시판 조회
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostsFeed() {
        return postRepository.findByCategory(Category.FEED).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 팁 게시판 조회
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostsTip() {
        return postRepository.findByCategory(Category.TIP).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 제목으로 검색
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostsByTitle(String title) {
        return postRepository.findByTitleContaining(title).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }
  
    public List<PostSummaryDto> searchPosts(String title, Category category) {
        // category 값이 null인지 아닌지에 따라 다른 Repository 메소드를 호출합니다.
        if (category != null) {
            // 카테고리가 지정된 경우: 해당 카테고리 내에서 제목으로 검색
            return postRepository.findByTitleContainingAndCategory(title, category).stream()
                    .map(this::convertToSummaryDto)
                    .collect(Collectors.toList());
        } else {
            // 카테고리가 지정되지 않은 경우: 전체 게시판에서 제목으로 검색
            return postRepository.findByTitleContaining(title).stream()
                    .map(this::convertToSummaryDto)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Post 엔티티를 PostSummaryDto로 변환하는 헬퍼 메서드
     */
    private PostSummaryDto convertToSummaryDto(Post post) {
        // 첫 번째 이미지를 썸네일로 사용, 이미지가 없으면 null
        String thumbnailUrl = post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl();

        return PostSummaryDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname(post.getUser().getNickname())
                .thumbnailUrl(thumbnailUrl) // 썸네일 추가
                .likeCount((long) post.getLikes().size())
                .build();
    }

    /**
     * Post 엔티티를 PostSummaryDto로 변환하는 헬퍼 메서드
     */
    private PostSummaryDto convertToSummaryDto(Post post) {
        // 첫 번째 이미지를 썸네일로 사용, 이미지가 없으면 null
        String thumbnailUrl = post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl();

        return PostSummaryDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .authorNickname(post.getUser().getNickname())
                .thumbnailUrl(thumbnailUrl) // 썸네일 추가
                .likeCount((long) post.getLikes().size())
                .build();
    }

}

