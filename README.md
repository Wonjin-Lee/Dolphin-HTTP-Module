## Dolphin HTTP Module 소개

Apache Components의 HttpClient를 활용하여 만든 HTTP 클라이언트입니다.

* HTTP, HTTPS 통신 지원
* GET, POST 메서드 지원
* TLS Version 설정 가능
* Supported Protocols 설정 가능
* Supported Cipher Suites 설정 가능
* KeyStore(Certificate) 로딩 기능 지원
* Connection Pool 관련 설정 가능
* log4j를 이용한 로깅 지원
* One Line Log 출력

## 라이브러리

* Apache Components - HttpClient (4.5.2)

## 사용법

### 1. Build
 * jar 파일로 빌드한 후, 모듈을 사용할 프로젝트에 라이브러리를 추가해 주면 된다.
 * Dolphin에서 사용하고 있는 의존성을 모두 포함한 상태로 빌드하기 위해 아래의 Goals를 이용한다.
     - Maven Goals : clean assembly:assembly

### 2. 모듈 사용 예시

```java
import com.wonjin.dolphin.http.HTTPManager;
import com.wonjin.dolphin.http.protocol.ProtocolVersion;
import com.wonjin.dolphin.http.protocol.Protocol;
import java.io.InputStream;
import java.security.KeyStore;

public class Example {
    public static void main(String[] args) {
    
        // 인스턴스 생성
        HTTPManager httpManager = new HTTPManager();
        
        /*
            오버로딩된 setProtocol 메서드를 사용하여 아래 항목을 유동적으로 설정할 수 있다.
            
            1. 통신 방법 : HTTP 또는 HTTPS (Default는 HTTPS)
               - Protocol Enum 클래스 사용
                 - Protocol.HTTP
                 - Protocol.HTTPS
            
            2. TLS 버전 : ProtocolVersion Enum 클래스에 있는 열거 상수를 인자로 넣어 설정 (Default는 TLSv1.2)
               - ProtocolVersion.TLS_1 : TLSv1.0
               - ProtocolVersion.TLS_1_1 : TLSv1.1
               - ProtocolVersion.TLS_1_2 : TLSv1.2
               
            3. SupportedProtocols : 서버 측에 어떤 TLS 버전으로 통신이 가능한지를 알려주기 위한 값 (Default는 {TLSv1.1, TLSv1.2})
            
            4. SupportedCipherSuites : 서버 측에 어떤 Cipher Suite을 사용하여 통신이 가능한지를 알려주기 위한 값 (Default는 null)
               - 이 값을 설정하지 않을 경우(Default : null), 2번 항목인 TLS 버전 설정 값을 기준으로 SupportedCipherSuites가 구성된다.
        */
        // 1. 통신 방법 설정
        httpManager.setProtocol(Protocol.HTTPS);
        
        // 2. 통신 방법, TLS 버전 설정
        httpManager.setProtocol(Protocol.HTTPS, ProtocolVersion.TLS_1_2);
        
        // 3. 통신 방법, TLS 버전, SupportedProtocols 설정
        httpManager.setProtocol(Protocol.HTTPS, ProtocolVersion.TLS_1_2, new String[] {"TLSv1.1", "TLSv1.2"})
        
        // 4. 통신 방법, TLS 버전, SupportedProtocols, SupportedCipherSuites 설정
        httpManager.setProtocol(Protocol.HTTPS, ProtocolVersion.TLS_1_2, new String[] {"TLSv1.1", "TLSv1.2"}, new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA256"});
        
        // URL 설정
        httpManager.setUrl("https://wonjin.com");
        
        // Connection Timeout 설정 (Default는 5000)
        httpManager.setConnectionTimeout(5000);
        
        // Connection Pool로부터 꺼내올 때의 Timeout 설정 (Default는 5000)
        httpManager.setConnectionRequestTimeout(5000);
        
        // Read Timeout 설정 (Default는 5000)
        httpManager.setSocketTimeout(5000);
       
        // KeyStore 설정
        String filePath = "/home/service/payment/WEB-INF/cert/certifcate.p12";
        String password = "1234567890";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = this.getClass.getResoureAsStream(filePath)) {
            keyStore.load(keyStoreStream, password.toCharArray());
        }

        httpManager.setKeyStore(keyStore, password);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        
        // Request Header 설정
        httpManager.setHeader(headerMap);

        // Charset 설정 (Default는 UTF-8)
        httpManager.setCharset("UTF-8");
        
        // Request Parameter 설정
        String parameterString;
        parameterString = "name=wonjin&age=28&address=seoul"; // KVP
        parameterString = "<xml><name>wonjin</name><age>28</age><address>seoul</address></xml>"; // XML

        httpManager.setParameter(parameterString);
        
        String response;
        // GET 방식으로 요청 (응답 엔티티 본문을 문자열로 반환)
        response = httpManager.get();
        
        // POST 방식으로 요청 (응답 엔티티 본문을 문자열로 반환)
        response = httpManager.post();
    }
}
```

### Connection Pool 관련 설정 예시
```java
import com.wonjin.dolphin.http.HTTPManager;

public class Example {
    public static void main(String[] args) {
    
        // 인스턴스 생성
        HTTPManager httpManager = new HTTPManager();
        
        // 각 Host 당 Connection Pool에 생성 가능한 Connection 수 (Default는 50)
        httpManager.setMaxConnectionsPerRoute(50)
        
        // Connection Pool의 수용 가능한 최대 사이즈 (Default는 100)
        httpManager.setMaxConnectionsTotal(100)
    }
}
```

### 3. Log4j - logger 설정
```xml
<logger name="org.apache.commons.httpclient" additivity="true">
	<level value="DEBUG" />
        <appender-ref ref="HTTP_LOG" />
</logger>
	
<logger name="httpclient.wire" additivity="true">
	<level value="DEBUG" />
	<appender-ref ref="HTTP_LOG" />
</logger>	
```

## Release Note
* 1.0.0 - Deploy First Version
* 1.1.0 - 모듈성을 높이기 위해 요청 파라미터를 문자열 형태로 세팅하도록 변경
* 1.2.0 - KeyStore(Certificate) 로드 기능, 재시도 횟수 설정 기능 추가
* 1.2.1 - 내부 로직 리팩토링
