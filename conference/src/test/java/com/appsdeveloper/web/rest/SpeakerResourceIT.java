package com.appsdeveloper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsdeveloper.IntegrationTest;
import com.appsdeveloper.domain.Speaker;
import com.appsdeveloper.repository.SpeakerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SpeakerResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
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
    private MockMvc restSpeakerMockMvc;

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

    @BeforeEach
    public void initTest() {
        speaker = createEntity(em);
    }

    @Test
    @Transactional
    void createSpeaker() throws Exception {
        int databaseSizeBeforeCreate = speakerRepository.findAll().size();
        // Create the Speaker
        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isCreated());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeCreate + 1);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(DEFAULT_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(DEFAULT_BIO);
    }

    @Test
    @Transactional
    void createSpeakerWithExistingId() throws Exception {
        // Create the Speaker with an existing ID
        speaker.setId(1L);

        int databaseSizeBeforeCreate = speakerRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFirstNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().size();
        // set the field null
        speaker.setFirstName(null);

        // Create the Speaker, which fails.

        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().size();
        // set the field null
        speaker.setLastName(null);

        // Create the Speaker, which fails.

        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().size();
        // set the field null
        speaker.setEmail(null);

        // Create the Speaker, which fails.

        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTwitterIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().size();
        // set the field null
        speaker.setTwitter(null);

        // Create the Speaker, which fails.

        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBioIsRequired() throws Exception {
        int databaseSizeBeforeTest = speakerRepository.findAll().size();
        // set the field null
        speaker.setBio(null);

        // Create the Speaker, which fails.

        restSpeakerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isBadRequest());

        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSpeakers() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        // Get all the speakerList
        restSpeakerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(speaker.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].twitter").value(hasItem(DEFAULT_TWITTER)))
            .andExpect(jsonPath("$.[*].bio").value(hasItem(DEFAULT_BIO)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSpeakersWithEagerRelationshipsIsEnabled() throws Exception {
        when(speakerRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSpeakerMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(speakerRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSpeakersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(speakerRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSpeakerMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(speakerRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getSpeaker() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        // Get the speaker
        restSpeakerMockMvc
            .perform(get(ENTITY_API_URL_ID, speaker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(speaker.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.twitter").value(DEFAULT_TWITTER))
            .andExpect(jsonPath("$.bio").value(DEFAULT_BIO));
    }

    @Test
    @Transactional
    void getNonExistingSpeaker() throws Exception {
        // Get the speaker
        restSpeakerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewSpeaker() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();

        // Update the speaker
        Speaker updatedSpeaker = speakerRepository.findById(speaker.getId()).get();
        // Disconnect from session so that the updates on updatedSpeaker are not directly saved in db
        em.detach(updatedSpeaker);
        updatedSpeaker
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .twitter(UPDATED_TWITTER)
            .bio(UPDATED_BIO);

        restSpeakerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSpeaker.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedSpeaker))
            )
            .andExpect(status().isOk());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(UPDATED_BIO);
    }

    @Test
    @Transactional
    void putNonExistingSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, speaker.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(speaker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(speaker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSpeakerWithPatch() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();

        // Update the speaker using partial update
        Speaker partialUpdatedSpeaker = new Speaker();
        partialUpdatedSpeaker.setId(speaker.getId());

        partialUpdatedSpeaker.lastName(UPDATED_LAST_NAME).twitter(UPDATED_TWITTER);

        restSpeakerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSpeaker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSpeaker))
            )
            .andExpect(status().isOk());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(DEFAULT_BIO);
    }

    @Test
    @Transactional
    void fullUpdateSpeakerWithPatch() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();

        // Update the speaker using partial update
        Speaker partialUpdatedSpeaker = new Speaker();
        partialUpdatedSpeaker.setId(speaker.getId());

        partialUpdatedSpeaker
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .twitter(UPDATED_TWITTER)
            .bio(UPDATED_BIO);

        restSpeakerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSpeaker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSpeaker))
            )
            .andExpect(status().isOk());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
        Speaker testSpeaker = speakerList.get(speakerList.size() - 1);
        assertThat(testSpeaker.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testSpeaker.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testSpeaker.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testSpeaker.getTwitter()).isEqualTo(UPDATED_TWITTER);
        assertThat(testSpeaker.getBio()).isEqualTo(UPDATED_BIO);
    }

    @Test
    @Transactional
    void patchNonExistingSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, speaker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(speaker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(speaker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSpeaker() throws Exception {
        int databaseSizeBeforeUpdate = speakerRepository.findAll().size();
        speaker.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSpeakerMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(speaker)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Speaker in the database
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSpeaker() throws Exception {
        // Initialize the database
        speakerRepository.saveAndFlush(speaker);

        int databaseSizeBeforeDelete = speakerRepository.findAll().size();

        // Delete the speaker
        restSpeakerMockMvc
            .perform(delete(ENTITY_API_URL_ID, speaker.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Speaker> speakerList = speakerRepository.findAll();
        assertThat(speakerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
