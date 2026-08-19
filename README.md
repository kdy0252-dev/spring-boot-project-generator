# Spring Boot Project Generator

공통 Gradle convention과 품질 검사를 포함한 독립 Spring 프로젝트를 생성한다.

## Preset

| 유형 | 기본 구성 |
|---|---|
| `server` | Spring Web, SpringDoc, Security, Data JPA, Modulith |
| `agent` | Spring Web, SpringDoc, Embabel/OpenAI, Modulith core |
| `batch` | Spring Batch, Web, SpringDoc, JPA/JDBC/jOOQ, Liquibase, Quartz, Redis |

모든 유형에 Gradle wrapper, `build-logic`, Checkstyle, Error Prone,
OpenRewrite, jMolecules/ArchUnit, JaCoCo와 아키텍처 테스트가 생성된다.
실제 애플리케이션 코드의 Line Coverage가 70% 미만이면 빌드가 실패한다.

## 사용법

생성할 위치에서 다음과 같이 실행한다.

```bash
./create-project.sh my-service server
./create-project.sh my-agent agent
./create-project.sh my-batch batch
```

인자를 생략하면 프로젝트명과 유형을 대화형으로 입력받는다.

```bash
./create-project.sh
```

기존 디렉터리는 덮어쓰지 않는다. 생성 후 다음 명령으로 검증한다.

```bash
cd my-service
./gradlew :app:build
```
