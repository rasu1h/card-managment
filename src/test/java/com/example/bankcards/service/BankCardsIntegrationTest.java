package com.example.bankcards.service;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.requests.RegisterAdminRequest;
import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.response.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankCardsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${app.admin.registration.code:ADMIN_SECRET_2024}")
    private String adminCode;

    private static String adminToken;
    private static String userToken;
    private static Long userId;
    private static Long cardId1;
    private static Long cardId2;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
        // Reset static tokens - they become invalid after DB cleanup
        adminToken = null;
        userToken = null;
        userId = null;
        cardId1 = null;
        cardId2 = null;
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @Order(1)
    @DisplayName("1. Регистрация администратора с правильным кодом")
    void testRegisterAdmin_Success() throws Exception {
        RegisterAdminRequest request = RegisterAdminRequest.builder()
                .username("admin")
                .password("admin123")
                .phoneNumber("+77001234567")
                .adminCode(adminCode)
                .build();

        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Администратор успешно зарегистрирован"));

        // Verify admin was created
        assertThat(userRepository.findByUsername("admin")).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("2. Регистрация администратора с неправильным кодом - должна провалиться")
    void testRegisterAdmin_InvalidCode() throws Exception {
        RegisterAdminRequest request = RegisterAdminRequest.builder()
                .username("fake_admin")
                .password("admin123")
                .phoneNumber("+77009876543")
                .adminCode("WRONG_CODE")
                .build();

        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify admin was NOT created
        assertThat(userRepository.findByUsername("fake_admin")).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("3. Регистрация обычного пользователя")
    void testRegisterUser_Success() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .username("john_doe")
                .password("password123")
                .phoneNumber("+77005555555")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Пользователь успешно зарегистрирован"));

        // Verify user was created
        assertThat(userRepository.findByUsername("john_doe")).isPresent();
    }

    @Test
    @Order(4)
    @DisplayName("4. Регистрация с дубликатом имени пользователя - должна провалиться")
    void testRegisterUser_DuplicateUsername() throws Exception {
        // First registration
        AuthRequest request1 = AuthRequest.builder()
                .username("duplicate_user")
                .password("password123")
                .phoneNumber("+77006666666")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Second registration with same username
        AuthRequest request2 = AuthRequest.builder()
                .username("duplicate_user")
                .password("password456")
                .phoneNumber("+77007777777")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @Order(5)
    @DisplayName("5. Вход администратора")
    void testLoginAdmin_Success() throws Exception {
        // Register admin first
        RegisterAdminRequest registerRequest = RegisterAdminRequest.builder()
                .username("admin_login")
                .password("admin123")
                .phoneNumber("+77008888888")
                .adminCode(adminCode)
                .build();

        mockMvc.perform(post("/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        AuthRequest loginRequest = AuthRequest.builder()
                .username("admin_login")
                .password("admin123")
                .phoneNumber("+77008888888")
                .build();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        assertThat(response.token()).isNotNull();
        adminToken = response.token();
    }

    @Test
    @Order(6)
    @DisplayName("6. Вход пользователя")
    void testLoginUser_Success() throws Exception {
        // Register user first
        AuthRequest registerRequest = AuthRequest.builder()
                .username("user_login")
                .password("password123")
                .phoneNumber("+77009999999")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        AuthRequest loginRequest = AuthRequest.builder()
                .username("user_login")
                .password("password123")
                .phoneNumber("+77009999999")
                .build();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        assertThat(response.token()).isNotNull();
        userToken = response.token();

        // Get userId for later tests
        userId = userRepository.findByUsername("user_login").get().getId();
    }

    @Test
    @Order(7)
    @DisplayName("7. Вход с неправильными данными - должен провалиться")
    void testLogin_InvalidCredentials() throws Exception {
        AuthRequest loginRequest = AuthRequest.builder()
                .username("nonexistent")
                .password("wrongpassword")
                .phoneNumber("+77001111111")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ADMIN CARD OPERATIONS ====================

    @Test
    @Order(8)
    @DisplayName("8. Админ создает карту для пользователя")
    void testAdminCreateCard_Success() throws Exception {
        // Setup: register admin and user, get tokens
        setupAdminAndUser();

        MvcResult result = mockMvc.perform(post("/api/v1/cards/admin/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ownerId", userId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maskedCardNumber").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        SuccessResponse<?> response = objectMapper.readValue(responseBody, SuccessResponse.class);

        // Extract cardId for later tests
        cardId1 = objectMapper.convertValue(
                ((java.util.LinkedHashMap) response.getData()).get("id"),
                Long.class
        );

        assertThat(cardId1).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("9. Админ создает вторую карту для пользователя")
    void testAdminCreateSecondCard_Success() throws Exception {
        setupAdminAndUser();

        MvcResult result = mockMvc.perform(post("/api/v1/cards/admin/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ownerId", userId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        SuccessResponse<?> response = objectMapper.readValue(responseBody, SuccessResponse.class);

        cardId2 = objectMapper.convertValue(
                ((java.util.LinkedHashMap) response.getData()).get("id"),
                Long.class
        );

        assertThat(cardId2).isNotNull();
        assertThat(cardId2).isNotEqualTo(cardId1);
    }

    @Test
    @Order(10)
    @DisplayName("10. Пользователь НЕ может создать карту (только админ)")
    void testUserCannotCreateCard() throws Exception {
        setupAdminAndUser();

        mockMvc.perform(post("/api/v1/cards/admin/create")
                        .header("Authorization", "Bearer " + userToken)
                        .param("ownerId", userId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    @DisplayName("11. Админ пополняет баланс карты")
    void testAdminTopUpCard_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(post("/api/v1/cards/admin/" + cardId1 + "/top-up")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("amount", "10000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Баланс пополнен"));

        // Verify balance was updated
        BigDecimal balance = cardRepository.findById(cardId1).get().getBalance();
        assertThat(balance).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @Order(12)
    @DisplayName("12. Админ блокирует карту")
    void testAdminBlockCard_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(post("/api/v1/cards/admin/" + cardId1 + "/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("reason", "Подозрительная активность"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));

        // Verify card was blocked
        CardStatus status = cardRepository.findById(cardId1).get().getStatus();
        assertThat(status).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @Order(13)
    @DisplayName("13. Админ активирует заблокированную карту")
    void testAdminActivateCard_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        // First block the card
        mockMvc.perform(post("/api/v1/cards/admin/" + cardId1 + "/block")
                .header("Authorization", "Bearer " + adminToken));

        // Then activate it
        mockMvc.perform(post("/api/v1/cards/admin/" + cardId1 + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify card was activated
        CardStatus status = cardRepository.findById(cardId1).get().getStatus();
        assertThat(status).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    @Order(14)
    @DisplayName("14. Админ удаляет карту")
    void testAdminDeleteCard_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(delete("/api/v1/cards/admin/" + cardId1)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify card status is DELETED
        CardStatus status = cardRepository.findById(cardId1).get().getStatus();
        assertThat(status).isEqualTo(CardStatus.DELETED);
    }

    @Test
    @Order(15)
    @DisplayName("15. Админ просматривает все карты с пагинацией")
    void testAdminGetAllCards_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(get("/api/v1/cards/admin/all")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    // ==================== USER CARD OPERATIONS ====================

    @Test
    @Order(16)
    @DisplayName("16. Пользователь просматривает свои карты")
    void testUserGetMyCards_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(get("/api/v1/cards/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @Order(17)
    @DisplayName("17. Пользователь проверяет баланс карты")
    void testUserGetCardBalance_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "5000.00");

        mockMvc.perform(get("/api/v1/cards/my/" + cardId1 + "/balance")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(cardId1))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("KZT"));
    }

    @Test
    @Order(18)
    @DisplayName("18. Пользователь блокирует свою карту")
    void testUserBlockOwnCard_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        mockMvc.perform(post("/api/v1/cards/my/" + cardId1 + "/block")
                        .header("Authorization", "Bearer " + userToken)
                        .param("reason", "Потерял карту"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));
    }

    @Test
    @Order(19)
    @DisplayName("19. Пользователь ищет карты по последним 4 цифрам")
    void testUserSearchCards_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();

        String lastFour = cardRepository.findById(cardId1).get().getLastFour();

        mockMvc.perform(get("/api/v1/cards/my/search")
                        .header("Authorization", "Bearer " + userToken)
                        .param("lastFour", lastFour)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].lastFour").value(lastFour));
    }

    // ==================== TRANSACTION TESTS ====================

    @Test
    @Order(20)
    @DisplayName("20. Пользователь переводит деньги между своими картами")
    void testUserTransferBetweenOwnCards_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "10000.00");

        TransferRequest transferRequest = TransferRequest.builder()
                .fromCardId(cardId1)
                .toCardId(cardId2)
                .amount(new BigDecimal("3000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(3000.00))
                .andExpect(jsonPath("$.data.transactionType").value("TRANSFER_BETWEEN_OWN_CARDS"));

        // Verify balances
        BigDecimal fromBalance = cardRepository.findById(cardId1).get().getBalance();
        BigDecimal toBalance = cardRepository.findById(cardId2).get().getBalance();

        assertThat(fromBalance).isEqualByComparingTo(new BigDecimal("7000.00"));
        assertThat(toBalance).isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    @Test
    @Order(21)
    @DisplayName("21. Перевод на ту же карту - должен провалиться")
    void testTransferToSameCard_Fails() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "10000.00");

        TransferRequest transferRequest = TransferRequest.builder()
                .fromCardId(cardId1)
                .toCardId(cardId1)
                .amount(new BigDecimal("1000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(22)
    @DisplayName("22. Перевод с недостаточным балансом - должен провалиться")
    void testTransferInsufficientFunds_Fails() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "1000.00");

        TransferRequest transferRequest = TransferRequest.builder()
                .fromCardId(cardId1)
                .toCardId(cardId2)
                .amount(new BigDecimal("5000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    @DisplayName("23. Перевод с заблокированной карты - должен провалиться")
    void testTransferFromBlockedCard_Fails() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "10000.00");

        // Block the card
        mockMvc.perform(post("/api/v1/cards/admin/" + cardId1 + "/block")
                .header("Authorization", "Bearer " + adminToken));

        TransferRequest transferRequest = TransferRequest.builder()
                .fromCardId(cardId1)
                .toCardId(cardId2)
                .amount(new BigDecimal("1000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    @DisplayName("24. Пользователь просматривает историю транзакций")
    void testUserGetTransactionHistory_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "10000.00");

        // Make a transfer first
        TransferRequest transferRequest = TransferRequest.builder()
                .fromCardId(cardId1)
                .toCardId(cardId2)
                .amount(new BigDecimal("2000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)));

        // Get transaction history
        mockMvc.perform(get("/api/v1/transactions/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(2000.00));
    }

    @Test
    @Order(25)
    @DisplayName("25. Множественные переводы и проверка истории")
    void testMultipleTransfersAndHistory_Success() throws Exception {
        setupAdminAndUser();
        createCardsForUser();
        topUpCard(cardId1, "20000.00");

        // Make 3 transfers
        for (int i = 1; i <= 3; i++) {
            TransferRequest transferRequest = TransferRequest.builder()
                    .fromCardId(cardId1)
                    .toCardId(cardId2)
                    .amount(new BigDecimal("1000.00"))
                    .build();

            mockMvc.perform(post("/api/v1/transactions/transfer")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transferRequest)));
        }

        // Verify transaction history
        mockMvc.perform(get("/api/v1/transactions/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));

        // Verify final balances
        BigDecimal fromBalance = cardRepository.findById(cardId1).get().getBalance();
        BigDecimal toBalance = cardRepository.findById(cardId2).get().getBalance();

        assertThat(fromBalance).isEqualByComparingTo(new BigDecimal("17000.00"));
        assertThat(toBalance).isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    // ==================== AUTHORIZATION TESTS ====================

    @Test
    @Order(26)
    @DisplayName("26. Доступ без токена - должен провалиться")
    void testAccessWithoutToken_Fails() throws Exception {
        mockMvc.perform(get("/api/v1/cards/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(27)
    @DisplayName("27. Доступ с неправильным токеном - должен провалиться")
    void testAccessWithInvalidToken_Fails() throws Exception {
        mockMvc.perform(get("/api/v1/cards/my")
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(28)
    @DisplayName("28. Пользователь пытается получить доступ к админским функциям - должен провалиться")
    void testUserAccessAdminEndpoint_Fails() throws Exception {
        setupAdminAndUser();

        mockMvc.perform(get("/api/v1/cards/admin/all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ==================== HELPER METHODS ====================

    private void setupAdminAndUser() throws Exception {
        if (adminToken == null || userToken == null) {
            // Register and login admin
            RegisterAdminRequest adminRequest = RegisterAdminRequest.builder()
                    .username("test_admin")
                    .password("admin123")
                    .phoneNumber("+77000000001")
                    .adminCode(adminCode)
                    .build();

            mockMvc.perform(post("/auth/register/admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminRequest)))
                    .andExpect(status().isCreated());

            AuthRequest adminLogin = AuthRequest.builder()
                    .username("test_admin")
                    .password("admin123")
                    .phoneNumber("+77000000001")
                    .build();

            MvcResult adminLoginResult = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adminLogin)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse adminAuthResponse = objectMapper.readValue(
                    adminLoginResult.getResponse().getContentAsString(),
                    AuthResponse.class
            );
            adminToken = adminAuthResponse.token();

            // Register and login user
            AuthRequest userRequest = AuthRequest.builder()
                    .username("test_user")
                    .password("password123")
                    .phoneNumber("+77000000002")
                    .build();

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)));

            AuthRequest userLogin = AuthRequest.builder()
                    .username("test_user")
                    .password("password123")
                    .phoneNumber("+77000000002")
                    .build();

            MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLogin)))
                    .andReturn();

            AuthResponse userAuthResponse = objectMapper.readValue(
                    userLoginResult.getResponse().getContentAsString(),
                    AuthResponse.class
            );
            userToken = userAuthResponse.token();
            userId = userRepository.findByUsername("test_user").get().getId();
        }
    }

    private void createCardsForUser() throws Exception {
        if (cardId1 == null || cardId2 == null) {
            // Create first card
            MvcResult result1 = mockMvc.perform(post("/api/v1/cards/admin/create")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("ownerId", userId.toString()))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseBody1 = result1.getResponse().getContentAsString();
            SuccessResponse<?> response1 = objectMapper.readValue(responseBody1, SuccessResponse.class);
            Object data1 = response1.getData();
            assertThat(data1).as("Admin create card response must contain data").isNotNull();
            cardId1 = objectMapper.convertValue(
                    ((java.util.LinkedHashMap) data1).get("id"),
                    Long.class
            );

            // Create second card
            MvcResult result2 = mockMvc.perform(post("/api/v1/cards/admin/create")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("ownerId", userId.toString()))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseBody2 = result2.getResponse().getContentAsString();
            SuccessResponse<?> response2 = objectMapper.readValue(responseBody2, SuccessResponse.class);
            Object data2 = response2.getData();
            assertThat(data2).as("Admin create card response must contain data").isNotNull();
            cardId2 = objectMapper.convertValue(
                    ((java.util.LinkedHashMap) data2).get("id"),
                    Long.class
            );
        }
    }

    private void topUpCard(Long cardId, String amount) throws Exception {
        mockMvc.perform(post("/api/v1/cards/admin/" + cardId + "/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .param("amount", amount));
    }
}