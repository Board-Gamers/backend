package com.a404.boardgamers.Review.Service;

import com.a404.boardgamers.Game.Domain.Entity.Game;
import com.a404.boardgamers.Game.Domain.Repository.GameRepository;
import com.a404.boardgamers.Review.DTO.ReviewDTO;
import com.a404.boardgamers.Review.Domain.Entity.Review;
import com.a404.boardgamers.Review.Domain.Repository.ReviewRepository;
import com.a404.boardgamers.User.Domain.Entity.User;
import com.a404.boardgamers.User.Domain.Repository.UserRepository;
import com.a404.boardgamers.Util.Response;
import com.a404.boardgamers.Util.TimestampToDateString;
import com.a404.boardgamers.Util.TokenToId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public ResponseEntity findGameReviews(int gameId, int page, int pageSize) {
        Optional<Game> game = gameRepository.findGameById(gameId);
        if (!game.isPresent()) {
            return Response.newResult(HttpStatus.NO_CONTENT, "일치하는 게임이 존재하지 않습니다.", null);
        }
        long totalItemCount = reviewRepository.countReviewsByGameId(gameId);

        HashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("totalPageItemCnt", totalItemCount);
        linkedHashMap.put("totalPage", ((totalItemCount - 1) / pageSize) + 1);
        linkedHashMap.put("nowPage", page);
        linkedHashMap.put("nowPageSize", pageSize);

        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        List<Review> reviewList = reviewRepository.findReviewsByGameId(gameId, pageRequest);

        System.out.println(gameId + " " + game.get().getName() + " > " + reviewList.size());
        ArrayList<ReviewDTO.ReviewDetailResponse> arr = new ArrayList<>();

        for (Review item : reviewList) {
            String nameKor = game.get().getNameKor() == null ? "" : game.get().getNameKor();
            System.out.println(item.getComment());
            arr.add(ReviewDTO.ReviewDetailResponse.builder()
                    .id(item.getId())
                    .userId(item.getUserId())
                    .gameId(gameId)
                    .gameName(game.get().getName())
                    .gameNameKor(nameKor)
                    .userNickname(item.getUserNickname())
                    .comment(item.getComment())
                    .rating(item.getRating())
                    .createdAt(TimestampToDateString.convertDate(item.getCreatedAt()))
                    .build());
        }
        linkedHashMap.put("reviews", arr);

        return Response.newResult(HttpStatus.OK, gameId + "번 ", linkedHashMap);
    }

    @Transactional
    public ResponseEntity addGameReview(@RequestBody ReviewDTO.ReviewInsertRequest reviewInsertRequest, HttpServletRequest httpServletRequest) {
        String userId = TokenToId.getId(httpServletRequest);
        if (userId == null) {
            return Response.newResult(HttpStatus.UNAUTHORIZED, "로그인 후 이용해주세요.", null);
        }
        Optional<User> user = userRepository.findUserByLoginId(userId);
        if (!user.isPresent()) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "유효하지 않은 접근입니다.", null);
        }
        // 해당 게임에 리뷰 남긴 적이 있으면 처리하기
        Optional<Review> optionalReview = reviewRepository.findReviewByGameIdAndUserId(reviewInsertRequest.getGameId(), user.get().getId());
        if (optionalReview.isPresent()) {
            return updateGameReview(ReviewDTO.ReviewUpdateRequest.builder()
                    .id(optionalReview.get().getId())
                    .comment(reviewInsertRequest.getComment())
                    .rating(reviewInsertRequest.getRating())
                    .build(), httpServletRequest);
        }
        Optional<Game> optionalGame = gameRepository.findGameById(reviewInsertRequest.getGameId());
        if (!optionalGame.isPresent()) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "존재하지 않는 게임에 대해 평점을 남기실 수 없습니다.", null);
        }
        Review review = Review.builder()
                .userId(user.get().getId())
                .userNickname(user.get().getNickname())
                .gameName(optionalGame.get().getName())
                .rating(reviewInsertRequest.getRating())
                .gameId(reviewInsertRequest.getGameId())
                .comment(reviewInsertRequest.getComment())
                .build();
        reviewRepository.save(review);

        return Response.newResult(HttpStatus.OK, "평가를 남겼습니다.", review);
    }

    @Transactional
    public ResponseEntity updateGameReview(@RequestBody ReviewDTO.ReviewUpdateRequest updateRequest, HttpServletRequest httpServletRequest) {
        String userId = TokenToId.getId(httpServletRequest);
        if (userId == null) {
            return Response.newResult(HttpStatus.UNAUTHORIZED, "로그인 후 사용해주세요.", null);
        }
        Optional<Review> optReview = reviewRepository.findById(updateRequest.getId());
        if (!optReview.isPresent()) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "일치하는 리뷰가 없습니다.", null);
        }
        if (!userId.equals(optReview.get().getUserNickname())) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "직접 작성한 평가만 수정할 수 있습니다.", null);
        }
        optReview.get().updateReview(updateRequest.getRating(), updateRequest.getComment());
        return Response.newResult(HttpStatus.OK, "리뷰를 수정하였습니다.", optReview.get());
    }

    @Transactional
    public ResponseEntity deleteGameReview(int id, HttpServletRequest httpServletRequest) {
        String userId = TokenToId.getId(httpServletRequest);
        if (userId == null) {
            return Response.newResult(HttpStatus.UNAUTHORIZED, "로그인 후 사용해주세요.", null);
        }
//        Optional<User> optUser = userRepository.findUserById(userId);
        Optional<Review> optReview = reviewRepository.findById(id);
        if (!optReview.isPresent()) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "존재하지 않는 리뷰입니다.", null);
        }
        if (!userId.equals(optReview.get().getUserNickname())) {
            return Response.newResult(HttpStatus.BAD_REQUEST, "직접 작성한 평가만 수정할 수 있습니다.", null);
        }
        reviewRepository.delete(optReview.get());
        return Response.newResult(HttpStatus.OK, "리뷰가 삭제되었습니다.", null);
    }
}