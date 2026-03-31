package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.dto.request.SubmitScoreRequest;
import com.hdtpt.pentachat.games.dto.response.GameSessionResponse;
import com.hdtpt.pentachat.games.dto.response.LeaderboardEntryResponse;
import com.hdtpt.pentachat.games.dto.response.ObstacleDefinitionResponse;
import com.hdtpt.pentachat.games.dto.response.TargetDefinitionResponse;
import com.hdtpt.pentachat.games.model.Game;
import com.hdtpt.pentachat.games.model.GameSession;
import com.hdtpt.pentachat.games.model.GameSessionStatus;
import com.hdtpt.pentachat.games.repository.GameRepository;
import com.hdtpt.pentachat.games.repository.GameSessionRepository;
import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private static final int GEM_THRESHOLD_SCORE = 1000;
    private static final String COSMIC_ASSET_ROOT = "/game-assets/Cosmic Sentinel";
    private static final String DEFAULT_SHIP_ASSET_URL = COSMIC_ASSET_ROOT + "/ship/player-ship.png";

    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final GamePointWalletService gamePointWalletService;

    public GameService(
            GameRepository gameRepository,
            GameSessionRepository gameSessionRepository,
            UserRepository userRepository,
            WalletService walletService,
            GamePointWalletService gamePointWalletService) {
        this.gameRepository = gameRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.gamePointWalletService = gamePointWalletService;
    }

    public List<Game> getGameList() {
        ensureDefaultGames();
        return gameRepository.findAll();
    }

    public GameSessionResponse startGameSession(Long gameId, Long userId) {
        ensureDefaultGames();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game khong ton tai"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khong ton tai"));

        GameSession session = gameSessionRepository.save(GameSession.builder()
                .gameId(gameId)
                .userId(userId)
                .username(user.getUsername())
                .score(0)
                .livesRemaining(3)
                .enemiesDestroyed(0)
                .obstaclesHit(0)
                .gemsAwarded(0)
                .rewardGranted(false)
                .status(GameSessionStatus.ACTIVE)
                .build());

        return switch (resolveGameMode(game.getName())) {
            case "cosmic" -> buildCosmicSessionResponse(game, session);
            case "poker" -> buildPokerSessionResponse(game, session);
            case "caro" -> buildCaroSessionResponse(game, session);
            default -> buildComingSoonSessionResponse(game, session);
        };
    }

    public GameSession submitScore(Long gameId, Long sessionId, Long userId, SubmitScoreRequest request) {
        GameSession session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay session game"));

        if (!Objects.equals(session.getGameId(), gameId)) {
            throw new RuntimeException("Session khong thuoc game nay");
        }

        if (session.getStatus() == GameSessionStatus.FINISHED) {
            return session;
        }

        session.setScore(request.getScore());
        session.setLivesRemaining(request.getLivesRemaining());
        session.setEnemiesDestroyed(request.getEnemiesDestroyed());
        session.setObstaclesHit(request.getObstaclesHit());

        if (Boolean.TRUE.equals(request.getGameOver()) || request.getLivesRemaining() <= 0) {
            session.setStatus(GameSessionStatus.FINISHED);
            session.setFinishedAt(LocalDateTime.now());
            if (!Boolean.TRUE.equals(session.getRewardGranted())) {
                Game game = gameRepository.findById(gameId)
                        .orElseThrow(() -> new RuntimeException("Game khong ton tai"));
                String mode = resolveGameMode(game.getName());
                if ("caro".equals(mode)) {
                    session.setGemsAwarded(0);
                    if (session.getScore() > 0) {
                        gamePointWalletService.addPoints(userId, session.getScore());
                    }
                    session.setRewardGranted(true);
                } else {
                    int gemsAwarded = session.getScore() / GEM_THRESHOLD_SCORE;
                    session.setGemsAwarded(gemsAwarded);
                    session.setRewardGranted(true);
                    if (gemsAwarded > 0) {
                        walletService.awardGems(userId, gemsAwarded);
                    }
                }
            }
        }

        return gameSessionRepository.save(session);
    }

    public List<LeaderboardEntryResponse> getLeaderboard(Long gameId) {
        ensureDefaultGames();

        List<GameSession> sessions = gameSessionRepository
                .findTop10ByGameIdAndStatusOrderByScoreDescCreatedAtAsc(gameId, GameSessionStatus.FINISHED);

        return IntStream.range(0, sessions.size())
                .mapToObj(index -> {
                    GameSession session = sessions.get(index);
                    return LeaderboardEntryResponse.builder()
                            .rank(index + 1)
                            .userId(session.getUserId())
                            .username(session.getUsername())
                            .score(session.getScore())
                            .gemsAwarded(session.getGemsAwarded())
                            .enemiesDestroyed(session.getEnemiesDestroyed())
                            .finishedAt(session.getFinishedAt() != null ? session.getFinishedAt() : session.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    private void ensureDefaultGames() {
        ensureGameExists(
                "Cosmic Sentinel",
                "Dieu khien phi thuyen bang chuot, ban mob ngoai hanh tinh va ne thien thach de giu 3 mang song.",
                "");
        ensureGameExists(
                "Poker Royale",
                "Ban poker toi da 4 cho, co the them bot vao ban va choi tung van de tranh top diem.",
                "");
        ensureGameExists(
                "Co Caro (X/O)",
                "Dau tri 5 nuoc thang, vua chat vua danh co.",
                "");
        ensureGameExists(
                "Ran San Moi",
                "Dieu khien ran an moi, cang an cang dai.",
                "");
        ensureGameExists(
                "Dao Vang",
                "Tha ngam keo vang, kim cuong va tui bi an.",
                "");
    }

    private void ensureGameExists(String name, String description, String imageUrl) {
        if (gameRepository.findByNameIgnoreCase(name).isPresent()) {
            return;
        }

        gameRepository.save(new Game(null, name, description, imageUrl));
    }

    private List<TargetDefinitionResponse> buildTargetDefinitions() {
        return List.of(
                TargetDefinitionResponse.builder()
                        .code("mob1")
                        .name("mob1")
                        .points(25)
                        .health(1)
                        .spawnWeight(60)
                        .sizeMultiplier(1.0)
                        .iconUrl(COSMIC_ASSET_ROOT + "/mobs/mob1.png")
                        .build(),
                TargetDefinitionResponse.builder()
                        .code("mob2")
                        .name("mob2")
                        .points(60)
                        .health(3)
                        .spawnWeight(28)
                        .sizeMultiplier(1.3)
                        .iconUrl(COSMIC_ASSET_ROOT + "/mobs/mob2.png")
                        .build(),
                TargetDefinitionResponse.builder()
                        .code("mob3")
                        .name("mob3")
                        .points(120)
                        .health(5)
                        .spawnWeight(12)
                        .sizeMultiplier(1.7)
                        .iconUrl(COSMIC_ASSET_ROOT + "/mobs/mob3.png")
                        .build());
    }

    private List<ObstacleDefinitionResponse> buildObstacleDefinitions() {
        return List.of(
                ObstacleDefinitionResponse.builder()
                        .code("asteroid")
                        .name("Thien thach")
                        .damage(1)
                        .iconUrl(COSMIC_ASSET_ROOT + "/obstacles/asteroid.png")
                        .imageUrl("")
                        .build(),
                ObstacleDefinitionResponse.builder()
                        .code("planet")
                        .name("Hanh tinh lang thang")
                        .damage(1)
                        .iconUrl(COSMIC_ASSET_ROOT + "/obstacles/planet.png")
                        .imageUrl("")
                        .build());
    }

    private String resolveGameMode(String gameName) {
        String normalized = gameName == null ? "" : gameName.toLowerCase();
        if (normalized.contains("cosmic")) {
            return "cosmic";
        }
        if (normalized.contains("poker")) {
            return "poker";
        }
        if (normalized.contains("caro")) {
            return "caro";
        }
        return "coming_soon";
    }

    private GameSessionResponse buildCosmicSessionResponse(Game game, GameSession session) {
        return GameSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .mode("cosmic")
                .initialLives(3)
                .arenaWidth(900)
                .arenaHeight(560)
                .shipAssetUrl(DEFAULT_SHIP_ASSET_URL)
                .bulletIntervalMs(180)
                .targetSpawnIntervalMs(700)
                .obstacleSpawnIntervalMs(1350)
                .gemThresholdScore(GEM_THRESHOLD_SCORE)
                .minPlayers(1)
                .maxPlayers(1)
                .maxBots(0)
                .botsSupported(false)
                .statusMessage("Cosmic Sentinel san sang cat canh.")
                .targets(buildTargetDefinitions())
                .obstacles(buildObstacleDefinitions())
                .build();
    }

    private GameSessionResponse buildPokerSessionResponse(Game game, GameSession session) {
        return GameSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .mode("poker")
                .gemThresholdScore(GEM_THRESHOLD_SCORE)
                .minPlayers(2)
                .maxPlayers(4)
                .maxBots(3)
                .botsSupported(true)
                .defaultBotNames(List.of("Nova Bot", "Orion Bot", "Luna Bot"))
                .statusMessage("Ban poker ho tro toi da 4 cho va co the them bot vao ban.")
                .build();
    }

    private GameSessionResponse buildCaroSessionResponse(Game game, GameSession session) {
        return GameSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .mode("caro")
                .boardSize(15)
                .gemThresholdScore(GEM_THRESHOLD_SCORE)
                .minPlayers(1)
                .maxPlayers(1)
                .maxBots(1)
                .botsSupported(true)
                .statusMessage("Co Caro ho tro bot 3 do kho: de, binh thuong va kho.")
                .build();
    }

    private GameSessionResponse buildComingSoonSessionResponse(Game game, GameSession session) {
        return GameSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .mode("coming_soon")
                .statusMessage("Game nay chua duoc mo tren game center.")
                .gemThresholdScore(GEM_THRESHOLD_SCORE)
                .build();
    }
}
