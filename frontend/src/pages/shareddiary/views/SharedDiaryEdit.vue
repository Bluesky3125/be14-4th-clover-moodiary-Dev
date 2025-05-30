<template>
  <div class="diary-page">
    <transition name="page-flip" mode="out-in">
      <div v-if="loaded" key="edit" class="write-wrapper">

        <h2 class="today-title">✍️ {{ todayString }} 일기 수정</h2>

        <form class="write-form">
          <div class="title-section">
            <input v-model="title" type="text" placeholder="제목을 입력하세요" required class="title-input" />
            <div class="title-buttons">
              <button type="button" class="upload-btn" @click="showStickerModal = true">🧸 스티커 추가</button>
              <button type="button" class="upload-btn" @click="triggerFileInput">📷 사진 추가</button>
              <input type="file" ref="fileInput" accept="image/*" @change="handlePhotoUpload" hidden />
            </div>
          </div>

          <div class="textarea-wrapper">
            <textarea
              v-model="content"
              placeholder="오늘의 이야기를 써주세요"
              required
              class="notebook-textarea"
            ></textarea>

            <div class="sticker-layer">
              <div
                v-for="(sticker, i) in stickers"
                :key="i"
                class="sticker-wrapper"
                :style="{
                  left: sticker.x + 'px',
                  top: sticker.y + 'px',
                  width: sticker.width + 'px',
                  height: sticker.height + 'px',
                  zIndex: i
                }"
              >
                <img
                  :src="sticker.url"
                  draggable="false"
                  class="sticker"
                  :class="{ selected: selectedIndex === i }"
                  @mousedown="(e) => startDrag(i, e)"
                  @click.stop="selectSticker(i)"
                />
                <div
                  v-if="selectedIndex === i"
                  class="resize-handle"
                  @mousedown.stop="startResize(i, $event)"
                ></div>
              </div>
            </div>
          </div>

          <div class="sticker-toolbar">
            <button type="button" class="submit-btn" @click="confirmUpdate">수정 완료</button>
            <button type="button" class="submit-btn" @click="goBack">취소</button>
          </div>
        </form>

        <!-- 스티커 선택 모델 -->
        <div v-if="showStickerModal" class="sticker-modal">
          <div class="sticker-modal-inner">
            <div class="sticker-option" v-for="src in stickerOptions" :key="src">
              <img :src="src" @click="addSticker(src); showStickerModal = false" />
            </div>
            <button @click="showStickerModal = false" class="close-btn">닫기</button>
          </div>
        </div>

      </div>
    </transition>
  </div>
</template>
  
  <script setup>
  import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import axios from 'axios'
  import { useAuthStore } from '@/stores/auth'
  
  const route = useRoute()
  const router = useRouter()
  
  const roomId = route.params.roomId
  const diaryId = route.params.diaryId
  
  
  const authStore = useAuthStore()
  const loginUserId = computed(() => authStore.user?.id)
  
  const title = ref('')
  const content = ref('')
  const stickers = ref([])
  const selectedIndex = ref(null)
  const showStickerModal = ref(false)
  const fileInput = ref(null)
  const loaded = ref(false)
  
  const stickerOptions = [
  '/src/assets/stickers/heart.png',
  '/src/assets/stickers/star.png',
  '/src/assets/stickers/rabbit.png',
  '/src/assets/stickers/smpet.png',
  '/src/assets/stickers/img1_sticker_3.png',
  '/src/assets/stickers/img1_sticker_4.png',
  '/src/assets/stickers/img1_sticker_5.png',
  '/src/assets/stickers/img1_sticker_6.png',
  '/src/assets/stickers/img1_sticker_7.png',
  '/src/assets/stickers/img1_sticker_9.png',
  '/src/assets/stickers/img1_sticker_10.png',
  '/src/assets/stickers/img1_sticker_14.png',
  '/src/assets/stickers/img1_sticker_16.png',
  '/src/assets/stickers/img1_sticker_17.png',
  '/src/assets/stickers/img1_sticker_22.png',
  '/src/assets/stickers/img1_sticker_23.png',
  '/src/assets/stickers/img1_sticker_24.png',
  '/src/assets/stickers/img1_sticker_27.png',
  '/src/assets/stickers/img1_sticker_28.png',
  '/src/assets/stickers/img1_sticker_29.png',
  '/src/assets/stickers/img1_sticker_30.png',
  '/src/assets/stickers/img1_sticker_31.png',
  '/src/assets/stickers/img1_sticker_32.png',
  '/src/assets/stickers/img1_sticker_33.png',
  '/src/assets/stickers/img1_sticker_34.png',
  '/src/assets/stickers/img1_sticker_35.png',
  '/src/assets/stickers/img1_sticker_36.png',
  '/src/assets/stickers/img1_sticker_38.png',
  '/src/assets/stickers/img1_sticker_41.png',
  '/src/assets/stickers/img1_sticker_42.png',
  '/src/assets/stickers/img1_sticker_45.png',
  '/src/assets/stickers/img1_sticker_57.png',
  '/src/assets/stickers/img1_sticker_81.png',
  '/src/assets/stickers/img1_sticker_83.png',
  '/src/assets/stickers/img1_sticker_85.png',
  '/src/assets/stickers/img1_sticker_86.png',
  '/src/assets/stickers/img1_sticker_87.png',
  '/src/assets/stickers/img2_sticker_3.png',
  '/src/assets/stickers/img2_sticker_4.png',
  '/src/assets/stickers/img2_sticker_19.png',
  '/src/assets/stickers/img2_sticker_20.png',
  '/src/assets/stickers/img2_sticker_21.png',
  '/src/assets/stickers/img2_sticker_38.png',
  '/src/assets/stickers/img2_sticker_51.png',
  '/src/assets/stickers/img2_sticker_52.png',
  '/src/assets/stickers/img2_sticker_53.png',
  '/src/assets/stickers/img2_sticker_60.png',
  '/src/assets/stickers/img2_sticker_62.png',
  '/src/assets/stickers/img2_sticker_63.png',
  '/src/assets/stickers/img2_sticker_64.png',
  '/src/assets/stickers/img2_sticker_69.png',
  '/src/assets/stickers/img2_sticker_70.png',
  '/src/assets/stickers/img3_sticker_0.png',
  '/src/assets/stickers/img3_sticker_2.png',
  '/src/assets/stickers/img3_sticker_5.png',
  '/src/assets/stickers/img3_sticker_11.png',
  '/src/assets/stickers/img3_sticker_13.png',
  '/src/assets/stickers/img3_sticker_14.png',
  '/src/assets/stickers/img3_sticker_17.png',
  '/src/assets/stickers/img3_sticker_18.png',
  '/src/assets/stickers/img3_sticker_22.png',
  '/src/assets/stickers/img3_sticker_23.png',
  '/src/assets/stickers/img3_sticker_24.png',
  '/src/assets/stickers/img3_sticker_25.png',
  '/src/assets/stickers/img3_sticker_27.png',
  '/src/assets/stickers/img3_sticker_30.png',
  '/src/assets/stickers/img3_sticker_31.png',
  '/src/assets/stickers/img3_sticker_33.png',
  '/src/assets/stickers/img3_sticker_34.png',
  '/src/assets/stickers/img3_sticker_35.png',
  '/src/assets/stickers/img3_sticker_37.png',
  '/src/assets/stickers/img3_sticker_40.png',
  '/src/assets/stickers/img3_sticker_41.png',
  '/src/assets/stickers/img3_sticker_43.png',
  '/src/assets/stickers/img3_sticker_45.png',
  '/src/assets/stickers/img3_sticker_48.png',
  '/src/assets/stickers/img3_sticker_49.png',
  '/src/assets/stickers/img3_sticker_50.png',
  '/src/assets/stickers/img3_sticker_51.png',
  '/src/assets/stickers/img3_sticker_52.png',
  '/src/assets/stickers/img3_sticker_53.png',
  '/src/assets/stickers/img3_sticker_54.png',
  '/src/assets/stickers/img4_sticker_1.png',
  '/src/assets/stickers/img4_sticker_2.png',
  '/src/assets/stickers/img4_sticker_3.png',
  '/src/assets/stickers/img4_sticker_5.png',
  '/src/assets/stickers/img4_sticker_6.png',
  '/src/assets/stickers/img4_sticker_8.png',
  '/src/assets/stickers/img4_sticker_9.png',
  '/src/assets/stickers/img4_sticker_10.png',
  '/src/assets/stickers/img4_sticker_11.png',
  '/src/assets/stickers/img4_sticker_13.png',
  '/src/assets/stickers/img4_sticker_14.png',
  '/src/assets/stickers/img4_sticker_15.png',
  '/src/assets/stickers/img4_sticker_17.png',
  '/src/assets/stickers/img4_sticker_18.png',
  '/src/assets/stickers/img4_sticker_19.png',
  '/src/assets/stickers/img4_sticker_21.png',
  '/src/assets/stickers/img4_sticker_22.png',
  '/src/assets/stickers/img4_sticker_23.png',
  '/src/assets/stickers/img4_sticker_24.png',
  '/src/assets/stickers/img4_sticker_25.png',
  '/src/assets/stickers/img4_sticker_26.png',
  '/src/assets/stickers/img4_sticker_27.png',
  '/src/assets/stickers/img4_sticker_28.png',
  '/src/assets/stickers/img4_sticker_29.png',
  '/src/assets/stickers/img4_sticker_30.png',
  '/src/assets/stickers/img4_sticker_31.png',
  '/src/assets/stickers/img4_sticker_32.png',
  '/src/assets/stickers/img4_sticker_33.png',
  '/src/assets/stickers/img4_sticker_34.png',
  '/src/assets/stickers/img4_sticker_35.png',
  '/src/assets/stickers/img4_sticker_36.png',
  '/src/assets/stickers/img4_sticker_37.png',
  '/src/assets/stickers/img4_sticker_38.png',
  '/src/assets/stickers/img4_sticker_39.png',
  '/src/assets/stickers/img4_sticker_40.png',
  '/src/assets/stickers/img4_sticker_41.png',
  '/src/assets/stickers/img4_sticker_42.png'
  ]
  
  const todayString = computed(() => {
    const today = new Date()
    const days = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
    return `${today.getFullYear()}년 ${today.getMonth() + 1}월 ${today.getDate()}일 ${days[today.getDay()]}`
  })

  const handleKeyDown = (e) => {
    if (e.key === 'Delete' && selectedIndex.value !== null) {
        stickers.value.splice(selectedIndex.value, 1)
        selectedIndex.value = null
    }
  }
    
  // 기존 일기 데이터 불러오기
  onMounted(async () => {
  try {
    const res = await axios.get(`/shareddiary/${diaryId}`)
    const data = res.data

    title.value = data.title
    content.value = data.content
    if (data.styleLayer) {
      stickers.value = JSON.parse(data.styleLayer)
    }

    loaded.value = true
    window.addEventListener('keydown', handleKeyDown)
  } catch (e) {
    console.error('일기 불러오기 실패', e)
    alert('일기를 불러올 수 없습니다.')
    router.push({ name: 'SharedDiaryList', params: { roomId } })
  }
})
  
  const triggerFileInput = () => {
    fileInput.value?.click()
  }
  
  const addSticker = (url) => {
    stickers.value.push({ url, x: 100, y: 100, width: 80, height: 80, type: 'sticker' })
  }
  
  const handlePhotoUpload = (e) => {
    const file = e.target.files[0]
    if (!file) return
  
    const reader = new FileReader()
    reader.onload = () => {
      stickers.value.push({
        url: reader.result,
        x: 100,
        y: 100,
        width: 140,
        height: 140,
        type: 'photo'
      })
    }
    reader.readAsDataURL(file)
  }
  
  let dragging = ref(null)
  let resizing = ref(null)
  
  const startDrag = (index, event) => {
    event.preventDefault()
    dragging.value = { index, startX: event.clientX, startY: event.clientY, origX: stickers.value[index].x, origY: stickers.value[index].y }
    document.addEventListener('mousemove', onDrag)
    document.addEventListener('mouseup', stopDrag)
  }
  
  const onDrag = (event) => {
    if (!dragging.value) return
    const { index, startX, startY, origX, origY } = dragging.value
    const deltaX = event.clientX - startX
    const deltaY = event.clientY - startY
  
    const wrapper = document.querySelector('.textarea-wrapper')
    const maxX = wrapper.offsetWidth - stickers.value[index].width
    const maxY = wrapper.offsetHeight - stickers.value[index].height
  
    stickers.value[index].x = Math.min(Math.max(0, origX + deltaX), maxX)
    stickers.value[index].y = Math.min(Math.max(0, origY + deltaY), maxY)
  }
  
  const stopDrag = () => {
    dragging.value = null
    resizing.value = null
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mousemove', onResize)
    document.removeEventListener('mouseup', stopDrag)
  }
  
  const selectSticker = (index) => {
    if (selectedIndex.value !== index) {
      const target = stickers.value.splice(index, 1)[0]
      stickers.value.push(target)
      selectedIndex.value = stickers.value.length - 1
    }
  }
  
  const startResize = (index, event) => {
    event.preventDefault()
    resizing.value = { index, startX: event.clientX, startY: event.clientY, origWidth: stickers.value[index].width, origHeight: stickers.value[index].height }
    document.addEventListener('mousemove', onResize)
    document.addEventListener('mouseup', stopDrag)
  }
  
  const onResize = (event) => {
    if (!resizing.value) return
    const { index, startX, startY, origWidth, origHeight } = resizing.value
    const deltaX = event.clientX - startX
    const deltaY = event.clientY - startY
  
    stickers.value[index].width = Math.max(30, origWidth + deltaX)
    stickers.value[index].height = Math.max(30, origHeight + deltaY)
  }
  
  const confirmUpdate = async () => {
  if (!title.value || !content.value) {
    alert('제목과 내용을 모두 입력해주세요.')
    return
  }

  try {
    // ✅ UpdateSharedDiaryRequest 구조에 맞춰 JSON 객체 생성
    const requestDto = {
      diaryId: diaryId,
      title: title.value,
      content: content.value,
      styleLayer: JSON.stringify(stickers.value)
    }

    // ✅ FormData에 JSON blob으로 묶어서 "data" 키로 추가
    const formData = new FormData()
    formData.append(
      'data',
      new Blob([JSON.stringify(requestDto)], { type: 'application/json' })
    )

    // 📌 선택적으로 이미지도 추가하고 싶다면 여기에 파일 추가
    // if (fileInput.value?.files[0]) {
    //   formData.append('image', fileInput.value.files[0])
    // }

    // ✅ axios 요청
    await axios.put('/shareddiary/update', formData)

    alert('수정 완료!')
    router.push({ name: 'SharedDiaryDetail', params: { roomId, diaryId } })
  } catch (error) {
    console.error('수정 실패', error)
    alert('수정 실패')
  }
}
  const goBack = () => {
    router.back()
  }

  onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleKeyDown)
    })
  </script>
  
  <style scoped>
  /* ✅ 작성폼 스타일과 완전 일치 */
  @import url('https://fonts.googleapis.com/css2?family=Nanum+Pen+Script&display=swap');
  
  .diary-page { perspective: 1500px; }
  .page-flip-enter-active, .page-flip-leave-active { transition: transform 0.6s ease; transform-style: preserve-3d; }
  .page-flip-enter-from { transform: rotateY(-90deg); }
  .page-flip-leave-to { transform: rotateY(90deg); }
  
  .write-wrapper {
    max-width: 850px;
    margin: 4rem auto;
    padding: 3rem;
    background-color: #fffce6;
    border-radius: 20px;
    border: 3px dashed #d9c7aa;
    font-family: 'Ownglyph PDH', sans-serif;
    font-size: 18px;
    color: #535353;
    position: relative;
    animation: inkFadeIn 1.2s ease;
  }

@keyframes inkFadeIn {
  from { filter: blur(3px); opacity: 0; transform: scale(1.02); }
  to { filter: none; opacity: 1; transform: scale(1); }
}

.write-form { display: flex; flex-direction: column; gap: 1.5rem; }
.title-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.title-input {
  flex: 1;
  font-family: 'Ownglyph PDH', sans-serif;
  font-size: 18px;
  font-weight: 400;
  color: #535353;
  border: none;
  border-bottom: 2px dashed #c5b496;
  background-color: #fffce6;
  padding: 0.8rem 0.5rem;
  outline: none;
}

.title-input:focus {
  outline: none;
  border-bottom: 2px dashed #c5b496;
}

.title-buttons {
  display: flex;
  gap: 0.5rem;
}

.upload-btn {
  background-color: #f5eccc;
  border-radius: 10px;
  padding: 0.5rem 0.8rem;
  cursor: pointer;
  white-space: nowrap;
  font-size: 14px;
  min-width: 90px;
  color: #7a5c3d;
  font-family: 'Ownglyph PDH', sans-serif;
  font-weight: 400;
}

.textarea-wrapper { position: relative; border: 1px solid #d9c7aa; border-radius: 10px; background-color: #fffce6; overflow: auto; box-shadow: inset 0 0 8px rgba(0,0,0,0.08); }

.notebook-textarea {
  font-family: 'Ownglyph PDH', sans-serif; font-size: 18px; font-weight: 400; color: #535353;
  line-height: 34px; padding: 12px 18px; min-height: 500px; width: 100%;
  background: repeating-linear-gradient(to bottom, #fffce6 0px, #fffce6 33px, #d9c7aa 34px);
  background-size: 100% 34px; background-position-y: 12px; box-sizing: border-box; border: none; resize: vertical; outline: none;
}

.sticker-layer { position: absolute; top: 0; left: 0; pointer-events: none; width: 100%; height: 100%; }
.sticker-wrapper { position: absolute; pointer-events: auto; }
.sticker { width: 100%; height: 100%; user-select: none; touch-action: none; cursor: move; }
.resize-handle {
  width: 12px; height: 12px;
  background-color: #6f9d6b;
  border-radius: 50%;
  position: absolute;
  right: -6px; bottom: -6px;
  cursor: nwse-resize;
  pointer-events: auto;
}

.sticker.selected { outline: 2px dashed #f06292; }
.sticker-toolbar { 
  display: flex; 
  gap: 1rem; 
  margin-top: 1.5rem; 
  justify-content: center;
  width: 100%;
}
.submit-btn {
  all: unset;
  width: 122px;
  height: 46px;
  background-color: #E9D2AF;
  color: #535353;
  border-radius: 10px;
  font-family: 'Ownglyph PDH', sans-serif;
  font-size: 18px;
  font-weight: 400;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  align-self: flex-end;
}
.submit-btn:hover {
  background-color: #d1b07a;
}

.sticker-modal { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background-color: #fffce6; border: 2px solid #d9c7aa; border-radius: 12px; padding: 2rem; z-index: 100; }
.sticker-modal-inner { display: flex; flex-wrap: wrap; gap: 1rem; justify-content: center; align-items: center; }
.sticker-option img { width: 60px; height: 60px; cursor: pointer; }
.sticker-option img:hover { transform: scale(1.1); }
.close-btn { margin-top: 1rem; background-color: #c9a36b; color: white; border-radius: 8px; padding: 0.5rem 1.2rem; }

.confirm-btn {
  background-color: #EFEFED !important;
}

.confirm-btn.confirmed {
  background-color: #E9D2AF !important;
}
</style>
  