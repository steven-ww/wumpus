# Reactive Commentator Progress
## Session scope
Started implementation from `reactive_commentator.md` with focus on server implementation order item 1 in `../Wumpus-server`:
- add commentary DTOs and endpoint
- add fallback commentator path
- add tests
- keep `/api/prompt` compatibility endpoint

## Completed in this session
Implemented new commentary API slice in `../Wumpus-server`:
- Added `POST /api/commentary` resource:
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryResource.java`
- Added commentary request/response contracts:
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryRequest.java`
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryResponse.java`
- Added commentary service + sanitization + fallback behavior:
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryService.java`
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryGateway.java`
  - `src/main/java/com/stevenww/wumpus/commentary/FallbackCommentaryGateway.java`
- Added tests:
  - `src/test/java/com/stevenww/wumpus/commentary/CommentaryServiceTest.java`
  - `src/test/java/com/stevenww/wumpus/commentary/CommentaryResourceTest.java`
- Added LangChain OpenAI-compatible commentary components:
  - `src/main/java/com/stevenww/wumpus/commentary/WumpusCommentatorAiService.java`
  - `src/main/java/com/stevenww/wumpus/commentary/LangChainCommentaryGateway.java`
  - `src/main/java/com/stevenww/wumpus/commentary/CommentaryGatewayProducer.java`
- Updated server configuration/dependencies for OpenRouter-compatible calls:
  - `../Wumpus-server/pom.xml` (`quarkus-langchain4j-openai`)
  - `../Wumpus-server/src/main/resources/application.properties`
- Added unit test for LangChain gateway serialization/delegation:
  - `../Wumpus-server/src/test/java/com/stevenww/wumpus/commentary/LangChainCommentaryGatewayTest.java`
- Updated docs for new endpoint and compatibility behavior:
  - `README.md`

Extended resilience + deployment work in `../Wumpus-server`:
- Added Quarkus fault-tolerance extension:
  - `../Wumpus-server/pom.xml` (`quarkus-smallrye-fault-tolerance`)
- Applied annotation-based resilience controls:
  - `CommentaryResource` now has `@RateLimit(1 request / second)` with fallback path
  - `CommentaryService` now has `@Timeout(4500ms)` with deterministic fallback method
- Added rate-limit behavior coverage:
  - `../Wumpus-server/src/test/java/com/stevenww/wumpus/commentary/CommentaryResourceTest.java`
- Updated deploy workflow to pass runtime LLM env vars + API key into the container:
  - `../Wumpus-server/.github/workflows/build.yml`
- Updated server README for fault-tolerance behavior and CI/CD vars/secrets:
  - `../Wumpus-server/README.md`

Implemented first client-side commentary integration in this repo:
- Added commentary client layer:
  - `src/main/java/za/co/sww/game/wumpus/commentary/CommentarySnapshot.java`
  - `src/main/java/za/co/sww/game/wumpus/commentary/CommentaryClient.java`
  - `src/main/java/za/co/sww/game/wumpus/commentary/NoopCommentaryClient.java`
  - `src/main/java/za/co/sww/game/wumpus/commentary/HttpCommentaryClient.java`
  - `src/main/java/za/co/sww/game/wumpus/commentary/CommentaryDispatcher.java`
- Integrated commentary dispatch into gameplay flow:
  - `src/main/java/za/co/sww/game/wumpus/service/HuntService.java`
  - Builds snapshots after accepted move/shoot actions.
  - Tracks `movesTaken` and recent action summaries (ring buffer of 3).
  - Supports `WUMPUS_COMMENTARY_URL`, `WUMPUS_COMMENTARY_TIMEOUT_MS`, and optional
    `WUMPUS_COMMENTARY_INCLUDE_HIDDEN_STATE`.
- Added structured action outcomes to gameplay actions:
  - `PlayerMovement.movePlayer(...)` now returns `MoveResult`.
  - `HazardChecker.checkForHazards(...)` now returns `HazardOutcome`.
  - `ShootAction.shootArrow(...)` now returns `ShootResult`.
- Added client commentary tests:
  - `src/test/java/za/co/sww/game/wumpus/commentary/NoopCommentaryClientTest.java`
  - `src/test/java/za/co/sww/game/wumpus/commentary/CommentaryDispatcherTest.java`
- Added client JSON dependency:
  - `build.gradle.kts` (`jackson-databind`)

## Validation status
Validation history:
- Initial run failed due shell using Java 21 (`release version 25 not supported`).
- Re-ran using Java 25 SDK explicitly:
  - `JAVA_HOME=/Users/eework/.sdkman/candidates/java/25.0.3-tem PATH=/Users/eework/.sdkman/candidates/java/25.0.3-tem/bin:$PATH /Users/eework/IdeaProjects/other/Wumpus-server/mvnw -B -f /Users/eework/IdeaProjects/other/Wumpus-server/pom.xml clean verify`
  - Found and fixed one test issue in `CommentaryServiceTest` (`List.of(..., null, ...)` -> `Arrays.asList(..., null, ...)`).
  - Final result: **BUILD SUCCESS** with tests passing (`10 run, 0 failed, 0 errors`).
- Added LangChain OpenAI-compatible integration and re-ran validation:
  - `JAVA_HOME=/Users/eework/.sdkman/candidates/java/25.0.3-tem PATH=/Users/eework/.sdkman/candidates/java/25.0.3-tem/bin:$PATH /Users/eework/IdeaProjects/other/Wumpus-server/mvnw -B -f /Users/eework/IdeaProjects/other/Wumpus-server/pom.xml clean verify`
  - Final result after step 2 changes: **BUILD SUCCESS** (`11 run, 0 failed, 0 errors`).
- Re-ran server validation after resilience + workflow updates:
  - `JAVA_HOME=/Users/eework/.sdkman/candidates/java/25.0.3-tem PATH=/Users/eework/.sdkman/candidates/java/25.0.3-tem/bin:$PATH /Users/eework/IdeaProjects/other/Wumpus-server/mvnw -B -f /Users/eework/IdeaProjects/other/Wumpus-server/pom.xml clean verify`
  - Final result: **BUILD SUCCESS** (`12 run, 0 failed, 0 errors`).
- Ran client validation after commentary integration:
  - `JAVA_HOME=/Users/eework/.sdkman/candidates/java/25.0.3-tem PATH=/Users/eework/.sdkman/candidates/java/25.0.3-tem/bin:$PATH /Users/eework/IdeaProjects/other/wumpus/gradlew -p /Users/eework/IdeaProjects/other/wumpus test`
  - `JAVA_HOME=/Users/eework/.sdkman/candidates/java/25.0.3-tem PATH=/Users/eework/.sdkman/candidates/java/25.0.3-tem/bin:$PATH /Users/eework/IdeaProjects/other/wumpus/gradlew -p /Users/eework/IdeaProjects/other/wumpus build`
  - Final result: **BUILD SUCCESS** for both commands.

## Remaining work (next stage)
Near-term next tasks from the plan:
1. Expand client snapshot quality with richer, explicit action metadata (e.g. nominated arrow path and target intent).
2. Add focused tests for snapshot content on specific outcomes (bat relocation, pit death, wumpus bump, shot self/hit/miss).
3. Decide whether to keep or retire `/api/prompt` compatibility endpoint once external callers are migrated.

## Notes for resume
- `/api/prompt` remains in place unchanged for compatibility during migration.
- Commentary now supports provider switching:
  - `WUMPUS_LLM_PROVIDER=fallback` (default) => deterministic quips (`fallback=true`)
  - `WUMPUS_LLM_PROVIDER=openai` => LangChain/OpenRouter path
- Resume from `../Wumpus-server` first, then return to client refactors in this repo.
