package com.appsdeveloper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.appsdeveloper.IntegrationTest;
import com.appsdeveloper.domain.Speaker;
import com.appsdeveloper.repository.SpeakerRepository;
import com.appsdeveloper.service.EntityManager;
import java.time.Duration;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link SpeakerResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class SpeakerResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_TWITTER = "AAAAAAAAAA";
    private static final String UPDATED_TWITTER = "BBBBBBBBBB";

    private static final String DEFAULT_BIO = "AAAAAAAAAA";
    private static final String UPDATED_BIO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/speakers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SpeakerRepository speakerRepository;

    @Mock
    private SpeakerRepository speakerRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Speaker speaker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Speaker createEntity(EntityManager em) {
        Speaker speaker = new Speaker()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .email(DEFAULT_EMAIL)
            .twitter(DEFAULT_TWITTER)
            .bio(DEFAULT_BIO);
        return speaker;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Speaker createUpdatedEntity(EntityManager em) {
        Speaker speaker = new Speaker()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .twitter(UPDATED_TWITTER)
            .bio(UPDATED_BIO);
        return speaker;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_speaker__sessions").block();
            em.deleteAll(Speaker.class).block();
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
        speaker = createEntity(em);
    }

    @Test
    void createSpeaker() throws Exception {
        int databaseSizeBeforeCreate = speakerRepository.findAll().collectList().block().size();
        // Create the Speaker
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeCreate + 1);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(DEFAULT_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(DEFAULT_BIO);
    }

    @Test
    void createSpeakerWithExistingId() throws Exception {
        // Create the Speaker with an existing ID
        speaker.setId(1L);

        int databaseSizeBeforeCreate = speakerRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkFirstNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().collectList().block().size();
        // set the field null
        speaker.setFirstName(null);

        // Create the Speaker, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkLastNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().collectList().block().size();
        // set the field null
        speaker.setLastName(null);

        // Create the Speaker, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().collectList().block().size();
        // set the field null
        speaker.setEmail(null);

        // Create the Speaker, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkTwitterIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().collectList().block().size();
        // set the field null
        speaker.setTwitter(null);

        // Create the Speaker, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkBioIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().collectList().block().size();
        // set the field null
        speaker.setBio(null);

        // Create the Speaker, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllSpeakersAsStream() {
        // Initialize the database
        speakerRepository.save(speaker).block();

        List<Speaker> speakerList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Speaker.class)
            .getResponseBody()
            .filter(speaker::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(speakerList).isNotNull();
        assertThat(speakerList).hasSize(1);
        Speaker testSpeaker = speakerList.get(0);
        assertThat(testSpeaker.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(DEFAULT_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(DEFAULT_BIO);
    }

    @Test
    void getAllSpeakers() {
        // Initialize the database
        speakerRepository.save(speaker).block();

        // Get all the speakerList
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
            .value(hasItem(speaker.getId().intValue()))
            .jsonPath("$.[*].firstName")
            .value(hasItem(DEFAULT_FIRST_NAME))
            .jsonPath("$.[*].lastName")
            .value(hasItem(DEFAULT_LAST_NAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].twitter")
            .value(hasItem(DEFAULT_TWITTER))
            .jsonPath("$.[*].bio")
            .value(hasItem(DEFAULT_BIO));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSpeakersWithEagerRelationshipsIsEnabled() {
        when(speakerRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(speakerRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSpeakersWithEagerRelationshipsIsNotEnabled() {
        when(speakerRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(speakerRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getSpeaker() {
        // Initialize the database
        speakerRepository.save(speaker).block();

        // Get the speaker
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, speaker.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(speaker.getId().intValue()))
            .jsonPath("$.firstName")
            .value(is(DEFAULT_FIRST_NAME))
            .jsonPath("$.lastName")
            .value(is(DEFAULT_LAST_NAME))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.twitter")
            .value(is(DEFAULT_TWITTER))
            .jsonPath("$.bio")
            .value(is(DEFAULT_BIO));
    }

    @Test
    void getNonExistingSpeaker() {
        // Get the speaker
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewSpeaker() throws Exception {
        // Initialize the database
        speakerRepository.save(speaker).block();

        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();

        // Update the speaker
        Speaker updatedSpeaker = speakerRepository.findById(speaker.getId()).block();
        updatedSpeaker
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .twitter(UPDATED_TWITTER)
            .bio(UPDATED_BIO);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSpeaker.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedSpeaker))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(UPDATED_BIO);
    }

    @Test
    void putNonExistingSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, speaker.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSpeakerWithPatch() throws Exception {
        // Initialize the database
        speakerRepository.save(speaker).block();

        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();

        // Update the speaker using partial update
        Speaker partialUpdatedSpeaker = new Speaker();
        partialUpdatedSpeaker.setId(speaker.getId());

        partialUpdatedSpeaker.lastName(UPDATED_LAST_NAME).twitter(UPDATED_TWITTER);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSpeaker.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSpeaker))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(DEFAULT_BIO);
    }

    @Test
    void fullUpdateSpeakerWithPatch() throws Exception {
        // Initialize the database
        speakerRepository.save(speaker).block();

        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();

        // Update the speaker using partial update
        Speaker partialUpdatedSpeaker = new Speaker();
        partialUpdatedSpeaker.setId(speaker.getId());

        partialUpdatedSpeaker
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .twitter(UPDATED_TWITTER)
            .bio(UPDATED_BIO);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSpeaker.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSpeaker))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(UPDATED_BIO);
    }

    @Test
    void patchNonExistingSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, speaker.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().collectList().block().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(speaker))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSpeaker() {
        // Initialize the database
        speakerRepository.save(speaker).block();

        int databaseSizeBeforeDelete = speakerRepository.findAll().collectList().block().size();

        // Delete the speaker
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, speaker.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Speaker> speakerList = speakerRepository.findAll().collectList().block();
        assertThat(speakerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
