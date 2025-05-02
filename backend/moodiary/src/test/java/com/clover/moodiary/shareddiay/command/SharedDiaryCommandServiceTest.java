package com.clover.moodiary.shareddiay.command;

import com.clover.moodiary.shareddiary.command.dto.CreateSharedDiaryRequest;
import com.clover.moodiary.shareddiary.command.dto.DeleteSharedDiaryRequest;
import com.clover.moodiary.shareddiary.command.dto.UpdateSharedDiaryReponse;
import com.clover.moodiary.shareddiary.command.dto.UpdateSharedDiaryRequest;
import com.clover.moodiary.shareddiary.command.entity.SharedDiary;
import com.clover.moodiary.shareddiary.command.repository.SharedDiaryRepository;
import com.clover.moodiary.shareddiary.command.service.SharedDiaryCommandService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@SpringBootTest
public class SharedDiaryCommandServiceTest {

    @Autowired
    private SharedDiaryCommandService sharedDiaryCommandService;

    @Autowired
    private SharedDiaryRepository sharedDiaryRepository;

    @ParameterizedTest
    @DisplayName("공유 일기 작성 테스트")
    @ValueSource(ints = {1})
    public void createSharedDiaryTest(int userId) throws IOException {
        Integer roomId = 1;
        String title = "오늘의 일기";
        String content = "따뜻한 커피와 함께 시작한 하루.";
        String styleLayer = "[{\"type\":\"sticker\",\"src\":\"heart.png\",\"x\":100,\"y\":120}]";

        CreateSharedDiaryRequest request = new CreateSharedDiaryRequest(
                roomId,
                userId,
                title,
                content,
                styleLayer
        );

        MultipartFile image = null;

        Integer diaryId = sharedDiaryCommandService.createDiary(request, image).getId();

        SharedDiary diary = sharedDiaryRepository.findById(diaryId).orElse(null);
        Assertions.assertNotNull(diary);
        Assertions.assertEquals(userId, diary.getUserId());
        Assertions.assertEquals(title, diary.getTitle());
        Assertions.assertEquals(content, diary.getContent());
        Assertions.assertEquals(styleLayer, diary.getStyleLayer());
        Assertions.assertEquals(roomId, diary.getSharedDiaryRoomId());
    }

    @ParameterizedTest
    @DisplayName("공유 일기 수정 테스트")
    @ValueSource(ints = {10})
    public void updateSharedDiaryTest(int diaryId) throws IOException {
        int userId = 1;

        UpdateSharedDiaryRequest request = new UpdateSharedDiaryRequest(
                diaryId,
                userId,
                "수정된 일기 제목",
                "오늘은 정말 잊지 못할 하루였다. 커피 향이 마음을 가득 채웠지.",
                "[{\"type\":\"sticker\",\"src\":\"star.png\",\"x\":150,\"y\":200}]"
        );

        MultipartFile image = null;

        UpdateSharedDiaryReponse response = sharedDiaryCommandService.updateDiary(request, image);

        SharedDiary diary = sharedDiaryRepository.findById(response.getDiaryId()).orElse(null);
        Assertions.assertNotNull(diary);
        Assertions.assertEquals(request.getTitle(), diary.getTitle());
        Assertions.assertEquals(request.getContent(), diary.getContent());
        Assertions.assertEquals(request.getStyleLayer(), diary.getStyleLayer());
        Assertions.assertEquals(diaryId, response.getDiaryId());
    }

    @ParameterizedTest
    @DisplayName("공유 일기 삭제 테스트")
    @ValueSource(ints = {10})
    public void deleteSharedDiaryTest(int diaryId) {
        int userId = 1;

        DeleteSharedDiaryRequest request = new DeleteSharedDiaryRequest(diaryId, userId);

        sharedDiaryCommandService.deleteDiary(request);

        SharedDiary deletedDiary = sharedDiaryRepository.findById(diaryId).orElse(null);
        Assertions.assertNotNull(deletedDiary);
        Assertions.assertEquals("Y", deletedDiary.getIsDeleted());
    }
}
