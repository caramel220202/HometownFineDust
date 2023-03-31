# HometownFineDust
  * 우리동네 공기질을 측정치를 확인 할 수 있는 App
  
## 배운것들
* FusedLocationProviderClient 사용하여 현 위치정보를 가져오는 방법

* Coroutine

  * Dispatcher

    * Dispatcher.Main  : UI와 상호작용하는 작업을 실행하기 위해서만 사용됨

    * Dispatcher.IO  : 디스크 또는 네트워크 작업을 실행하기 위해 사용됨

    * Dispatcher.Default  : CPU를 많이 사용하는 작업을 기본 스레드 외부에서 사용하도록 최적화됨, 정렬작업이나 JSON 파싱에 작업사용

  * Launch
    * Job을 반환함
    
  * Async
    * Defered<T> 반환함
    
* KakaoAPI
* 정부공공 API
  * 여러 데이터를 통해 응용 가능 할 수 있겠다 

