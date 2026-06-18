# ExamFlow

ExamFlow는 한국어 사용자를 위한 오프라인 시험 공부 계획 앱입니다. 사용자가 매주 반복되는 고정 일정과 시험 정보를 입력하면, 앱 내부 알고리즘이 남은 공부 가능 시간에 맞춰 30분 단위 학습 계획을 자동으로 생성합니다.

## 주요 기능

- 주간 고정 일정 및 공부 가능 시간 등록, 수정, 삭제
- 시험 과목, 시험일, 우선순위 등록
- 과목별 시험 범위와 페이지 수 관리
- 균형형, 집중형, 시험 임박형, 여유형 생성 방식 지원
- 캘린더에서 날짜별 학습 계획 확인
- 홈 화면에서 오늘 공부와 다가오는 시험 확인
- Room Database 기반 오프라인 저장
- 클립보드 기반 데이터 백업 및 복원

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room Database
- MVVM Architecture
- Kizitonwose Calendar Compose

## 아키텍처

ExamFlow는 단순하고 안정적인 MVVM 구조를 사용합니다.

- `data/local`: Room Entity, DAO, Database, TypeConverter
- `data/repository`: 데이터 저장소 및 백업/복원 로직
- `domain/scheduler`: UI와 분리된 시간표 생성 알고리즘
- `domain/model`: 도메인 enum 및 모델
- `ui/screens`: Compose 화면
- `ui/navigation`: 하단 탭 및 Navigation Compose 라우팅
- `ui/theme`: 미니멀 흑백 Material 3 테마

## 시간표 생성 알고리즘

시간표 생성은 앱 내부 알고리즘만 사용하며 인터넷, AI, 외부 API에 의존하지 않습니다.

1. 사용자가 입력한 공부 가능 시간만 학습 블록 후보로 사용합니다.
2. 모든 학습 세션은 1시간 단위로 생성합니다.
3. 시험 우선순위, 시험까지 남은 날짜, 전체 페이지 수를 기반으로 과목별 점수를 계산합니다.
4. 전체 공부 시간의 80%는 범위 학습, 20%는 복습으로 배정합니다.
5. 복습은 시험일에 가까운 시점에 배치되도록 우선 처리합니다.
6. 생성된 계획은 Room Database에 저장됩니다.

### 우선순위 공식

```text
ExamScore = PriorityWeight × ExamDateWeight × TotalPages
```

- 우선순위 1~5는 각각 1.0, 1.2, 1.5, 1.8, 2.0 가중치를 사용합니다.
- 시험일까지 0~3일은 2.0, 4~7일은 1.7, 8~14일은 1.5, 15~30일은 1.2, 30일 초과는 1.0 가중치를 사용합니다.

## 생성 방식

- 균형형: 여러 과목을 자연스럽게 섞어 공부합니다.
- 집중형: 과목 전환을 줄이고 같은 과목을 연속 배치합니다.
- 시험 임박형: 7일 이내 시험의 날짜 가중치를 더 높입니다.
- 여유형: 하루 최대 공부 시간을 3시간으로 제한합니다.

## 실행 방법

1. Android Studio에서 이 폴더를 엽니다.
2. Gradle Sync가 완료될 때까지 기다립니다.
3. 에뮬레이터 또는 실제 기기를 선택합니다.
4. Run 버튼을 눌러 실행합니다.

## Github 업로드

이 프로젝트는 Android Studio 프로젝트 구조와 `.gitignore`를 포함하고 있어 바로 GitHub 저장소에 업로드할 수 있습니다.

```bash
git init
git add .
git commit -m "Initial ExamFlow app"
```

## 향후 개선 사항

- 날짜 선택용 Material Date Picker 추가
- 백업 파일 내보내기와 가져오기
- 완료한 공부 항목 체크 상태 저장
- 위젯 또는 알림 기능
- 학습량 통계 화면
- 알고리즘 개선
