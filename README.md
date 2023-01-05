# lolsearcher-data-batch

> [LolSearcher App](https://github.com/kyo705/LolSearcher#lolsearcher)
> 으로부터 발생한 Match 데이터들을 가져와 게임 캐릭터별 픽률, 벤률, 승률과 아이템 별 승률, 상대 캐릭터별 승률 등의 데이터로 가공하는 애플리케이션

## 프로젝트 깃 브런치

> - **main** — 실제 메인 브런치(완성본)
> - **develop** — 다음 버전을 위한 개발 브런치(테스트용)

## 프로젝트 커밋 메시지 카테고리

> - [INITIAL] — repository를 생성하고 최초에 파일을 업로드 할 때
> - [ADD] — 신규 파일 추가
> - [UPDATE] — 코드 변경이 일어날때
> - [REFACTOR] — 코드를 리팩토링 했을때
> - [FIX] — 잘못된 링크 정보 변경, 필요한 모듈 추가 및 삭제
> - [REMOVE] — 파일 제거
> - [STYLE] — 디자인 관련 변경사항

## 프로젝트 내 적용 기술
> - Back-End
    >   - 언어 : Java
>   - 프레임 워크 : SpringBoot, Spring Batch
>   - 빌드 관리 툴 : Gradle
>   - 스케줄러 : Spring Quartz
> - DevOps
    >   - DBMS : MariaDB

## 기능 요구사항
>  ### 배치 기능
> 1. Reader : 하룻동안 발생한 Match 데이터에서 챔피언, 게임 타입 별 픽률, 벤률, 승률을 가져옴(집계 함수로)
> 2. Writer : reader로 가져온 통계 데이터를 Statistic 테이블들에 업데이트함
> 3. 배치 처리 시작 전 파라미터 유효성 검증 로직 작성
> 4. 배치 처리 중 read한 데이터의 유효성 검증(ex. 전체 게임 횟수 = 이긴 횟수 + 진 횟수) 및 유효하지 않다면 배치 중단
> ### 스케줄 기능
> 1. 하루에 한 번 배치 처리가 실행되도록 설계