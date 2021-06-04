package com.appsdeveloper.web.rest;

import static com.appsdeveloper.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.appsdeveloper.IntegrationTest;
import com.appsdeveloper.domain.Session;
import com.appsdeveloper.repository.SessionRepository;
import com.appsdeveloper.service.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link SessionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class SessionResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_START_DATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_DATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END_DATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_DATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/sessions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Session session;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Session createEntity(EntityManager em) {
        Session session = new Session()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .startDateTime(DEFAULT_START_DATE_TIME)
            .endDateTime(DEFAULT_END_DATE_TIME);
        return session;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Session createUpdatedEntity(EntityManager em) {
        Session session = new Session()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .startDateTime(UPDATED_START_DATE_TIME)
            .endDateTime(UPDATED_END_DATE_TIME);
        return session;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Session.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        session = createEntity(em);
    }

    @Test
    void createSession() throws Exception {
        int databaseSizeBeforeCreate = sessionRepository.findAll().collectList().block().size();
        // Create the Session
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeCreate + 1);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testSession.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testSession.getStartDateTime()).isEqualTo(DEFAULT_START_DATE_TIME);
        assertThat(testSession.getEndDateTime()).isEqualTo(DEFAULT_END_DATE_TIME);
    }

    @Test
    void createSessionWithExistingId() throws Exception {
        // Create the Session with an existing ID
        session.setId(1L);

        int databaseSizeBeforeCreate = sessionRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = sessionRepository.findAll().collectList().block().size();
        // set the field null
        session.setTitle(null);

        // Create the Session, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkStartDateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = sessionRepository.findAll().collectList().block().size();
        // set the field null
        session.setStartDateTime(null);

        // Create the Session, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkEndDateTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = sessionRepository.findAll().collectList().block().size();
        // set the field null
        session.setEndDateTime(null);

        // Create the Session, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllSessionsAsStream() {
        // Initialize the database
        sessionRepository.save(session).block();

        List<Session> sessionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Session.class)
            .getResponseBody()
            .filter(session::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(sessionList).isNotNull();
        assertThat(sessionList).hasSize(1);
        Session testSession = sessionList.get(0);
        assertThat(testSession.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testSession.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testSession.getStartDateTime()).isEqualTo(DEFAULT_START_DATE_TIME);
        assertThat(testSession.getEndDateTime()).isEqualTo(DEFAULT_END_DATE_TIME);
    }

    @Test
    void getAllSessions() {
        // Initialize the database
        sessionRepository.save(session).block();

        // Get all the sessionList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(session.getId().intValue()))
            .jsonPath("$.[*].title")
            .value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.[*].startDateTime")
            .value(hasItem(sameInstant(DEFAULT_START_DATE_TIME)))
            .jsonPath("$.[*].endDateTime")
            .value(hasItem(sameInstant(DEFAULT_END_DATE_TIME)));
    }

    @Test
    void getSession() {
        // Initialize the database
        sessionRepository.save(session).block();

        // Get the session
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, session.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(session.getId().intValue()))
            .jsonPath("$.title")
            .value(is(DEFAULT_TITLE))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.startDateTime")
            .value(is(sameInstant(DEFAULT_START_DATE_TIME)))
            .jsonPath("$.endDateTime")
            .value(is(sameInstant(DEFAULT_END_DATE_TIME)));
    }

    @Test
    void getNonExistingSession() {
        // Get the session
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewSession() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();

        // Update the session
        Session updatedSession = sessionRepository.findById(session.getId()).block();
        updatedSession
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .startDateTime(UPDATED_START_DATE_TIME)
            .endDateTime(UPDATED_END_DATE_TIME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSession.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testSession.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testSession.getStartDateTime()).isEqualTo(UPDATED_START_DATE_TIME);
        assertThat(testSession.getEndDateTime()).isEqualTo(UPDATED_END_DATE_TIME);
    }

    @Test
    void putNonExistingSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, session.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSessionWithPatch() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();

        // Update the session using partial update
        Session partialUpdatedSession = new Session();
        partialUpdatedSession.setId(session.getId());

        partialUpdatedSession.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testSession.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testSession.getStartDateTime()).isEqualTo(DEFAULT_START_DATE_TIME);
        assertThat(testSession.getEndDateTime()).isEqualTo(DEFAULT_END_DATE_TIME);
    }

    @Test
    void fullUpdateSessionWithPatch() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();

        // Update the session using partial update
        Session partialUpdatedSession = new Session();
        partialUpdatedSession.setId(session.getId());

        partialUpdatedSession
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .startDateTime(UPDATED_START_DATE_TIME)
            .endDateTime(UPDATED_END_DATE_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testSession.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testSession.getStartDateTime()).isEqualTo(UPDATED_START_DATE_TIME);
        assertThat(testSession.getEndDateTime()).isEqualTo(UPDATED_END_DATE_TIME);
    }

    @Test
    void patchNonExistingSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, session.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        session.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSession() {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeDelete = sessionRepository.findAll().collectList().block().size();

        // Delete the session
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, session.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
