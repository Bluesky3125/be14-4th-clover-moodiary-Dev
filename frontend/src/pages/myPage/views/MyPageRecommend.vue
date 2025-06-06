<template>
    <div class="my-page-inner">
        <h1>추천 설정</h1>
        <div class="search-box">
            <input type="text" v-model="keyword"/>
            <div>
                <button @click="fetchItems" style="margin:10px;">검색</button>
                <button @click="initUserPref" style="margin:10px;">초기화</button>
            </div>
        </div>
        <div>
            <!-- 목록을 2열 그리드로 표시 -->
            <div class="item-grid">
                <div v-for="item in pagedItems" :key="item.id" class="item-card">
                    <label>
                        <input type="checkbox" :value="item.id" v-model="selectedItems" />
                        {{ item.name }}
                    </label>
                </div>
                <!-- 빈 칸 (자리를 채우기 위해) -->
                <div
                    v-for="n in emptySlots"
                    :key="'empty-' + n"
                    class="item-card empty"
                ></div>
            </div>

            <!-- 페이지네이션 -->
            <div class="pagination">
                <button @click="prevPage" :disabled="currentPage === 1">이전</button>
                <span>페이지 {{ currentPage }} / {{ totalPages }}</span>
                <button @click="nextPage" :disabled="currentPage === totalPages">다음</button>
            </div>
        </div>
        <br>
        <!-- 선택된 항목 전송 -->
        <button style="width: 100px; align-self: center;" @click="submitSelection">추천 제외하기</button>
    </div>
</template>

<script setup>
import {onMounted, ref, computed} from 'vue'
import { useAuthStore } from '@/stores/auth.js';
import axios from 'axios';

// 설명. 링크로 들어오는 경우에 대비해 로그인 여부 받아오기
const store = useAuthStore();
// 상태 정의
const items = ref([])
const selectedItems = ref([])
const keyword = ref('');
const currentPage = ref(1)             // 현재 페이지
const itemsPerPage = 16                // 2열 x 8행 = 16개

const pagedItems = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    return items.value.slice(start, start + itemsPerPage)
})

// 부족한 항목 수만큼 빈 슬롯 계산
const emptySlots = computed(() => {
    const diff = itemsPerPage - pagedItems.value.length
    return diff > 0 ? diff : 0
})

const totalPages = computed(() => {
    return Math.ceil(items.value.length / itemsPerPage)
})

const nextPage = () => {
    if (currentPage.value < totalPages.value) currentPage.value++
}
const prevPage = () => {
    if (currentPage.value > 1) currentPage.value--
}

// 백엔드에서 목록 가져오기
const fetchItems = async () => {
    try {
        const response = await axios.get(`/action/search?keyword=${keyword.value}`)
        items.value = response.data;
    } catch (error) {
        console.error('목록 불러오기 실패:', error)
    }
}

const initUserPref = async () => {
    try {
        const response = await axios.post(`/action/weight/init`, {
            headers: {
                'Authorization': `Bearer ${store.token}`,
            }
        });
        if (response.status === 200) {
            alert('성공적으로 초기화되었습니다.')
        } else {
            alert('초기화 실패')
        }
    } catch (error) {
        console.error('초기화 요청 중 오류 발생')
    }
}

// 선택 항목 백엔드로 전송
const submitSelection = async () => {
    console.log(JSON.stringify(selectedItems.value))
    try {
        const response = await axios.post(`/action/exclude`, JSON.stringify(selectedItems.value), {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${store.token}`
            }
        })

        if (response.status === 200) {
            alert('성공적으로 전송되었습니다.')
        } else {
            alert('전송 실패')
        }
    } catch (error) {
        console.error('전송 중 오류 발생:', error)
    }
}

// 페이지 로드시 데이터 로딩
onMounted(fetchItems)
</script>

<style scoped>
    .item-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr); /* 2열 */
        gap: 10px;
        margin-top: 20px;
        flex-grow: 1;
    }

    .item-card {
        border: 1px solid #ccc;
        border-radius: 6px;
        padding: 10px;
        background-color: #f9f9f9;
        height: 50px;
    }

    .item-card.empty {
        background-color: transparent;
        border: none;
    }

    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12px;
        margin-top: 20px;
    }

    .my-page-inner {
        display: flex;
        flex-direction: column;
        padding: 20px 10%;
    }

    .search-box {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .search-box input {
        padding: 12px 24px;
        font-size: 16px;
        border: 2px solid #ccc;
        border-radius: 10px;
        width: 80%;
        font-family: var(--font-omyu);
    }
</style>