package com.appsdeveloper.web.rest;

import com.appsdeveloper.domain.Speaker;
import com.appsdeveloper.repository.SpeakerRepository;
import com.appsdeveloper.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.appsdeveloper.domain.Speaker}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class SpeakerResource {

    private final Logger log = LoggerFactory.getLogger(SpeakerResource.class);

    private static final String ENTITY_NAME = "speaker";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SpeakerRepository speakerRepository;

    public SpeakerResource(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    /**
     * {@code POST  /speakers} : Create a new speaker.
     *
     * @param speaker the speaker to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new speaker, or with status {@code 400 (Bad Request)} if the speaker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/speakers")
    public Mono<ResponseEntity<Speaker>> createSpeaker(@Valid @RequestBody Speaker speaker) throws URISyntaxException {
        log.debug("REST request to save Speaker : {}", speaker);
        if (speaker.getId() != null) {
            throw new BadRequestAlertException("A new speaker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return speakerRepository
            .save(speaker)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/speakers/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /speakers/:id} : Updates an existing speaker.
     *
     * @param id the id of the speaker to save.
     * @param speaker the speaker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated speaker,
     * or with status {@code 400 (Bad Request)} if the speaker is not valid,
     * or with status {@code 500 (Internal Server Error)} if the speaker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/speakers/{id}")
    public Mono<ResponseEntity<Speaker>> updateSpeaker(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Speaker speaker
    ) throws URISyntaxException {
        log.debug("REST request to update Speaker : {}, {}", id, speaker);
        if (speaker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, speaker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return speakerRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return speakerRepository
                        .save(speaker)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            result ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString())
                                    )
                                    .body(result)
                        );
                }
            );
    }

    /**
     * {@code PATCH  /speakers/:id} : Partial updates given fields of an existing speaker, field will ignore if it is null
     *
     * @param id the id of the speaker to save.
     * @param speaker the speaker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated speaker,
     * or with status {@code 400 (Bad Request)} if the speaker is not valid,
     * or with status {@code 404 (Not Found)} if the speaker is not found,
     * or with status {@code 500 (Internal Server Error)} if the speaker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/speakers/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<Speaker>> partialUpdateSpeaker(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Speaker speaker
    ) throws URISyntaxException {
        log.debug("REST request to partial update Speaker partially : {}, {}", id, speaker);
        if (speaker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, speaker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return speakerRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<Speaker> result = speakerRepository
                        .findById(speaker.getId())
                        .map(
                            existingSpeaker -> {
                                if (speaker.getFirstName() != null) {
                                    existingSpeaker.setFirstName(speaker.getFirstName());
                                }
                                if (speaker.getLastName() != null) {
                                    existingSpeaker.setLastName(speaker.getLastName());
                                }
                                if (speaker.getEmail() != null) {
                                    existingSpeaker.setEmail(speaker.getEmail());
                                }
                                if (speaker.getTwitter() != null) {
                                    existingSpeaker.setTwitter(speaker.getTwitter());
                                }
                                if (speaker.getBio() != null) {
                                    existingSpeaker.setBio(speaker.getBio());
                                }

                                return existingSpeaker;
                            }
                        )
                        .flatMap(speakerRepository::save);

                    return result
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            res ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString())
                                    )
                                    .body(res)
                        );
                }
            );
    }

    /**
     * {@code GET  /speakers} : get all the speakers.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of speakers in body.
     */
    @GetMapping("/speakers")
    public Mono<List<Speaker>> getAllSpeakers(@RequestParam(required = false, defaultValue = "false") boolean eagerload) {
        log.debug("REST request to get all Speakers");
        return speakerRepository.findAllWithEagerRelationships().collectList();
    }

    /**
     * {@code GET  /speakers} : get all the speakers as a stream.
     * @return the {@link Flux} of speakers.
     */
    @GetMapping(value = "/speakers", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Speaker> getAllSpeakersAsStream() {
        log.debug("REST request to get all Speakers as a stream");
        return speakerRepository.findAll();
    }

    /**
     * {@code GET  /speakers/:id} : get the "id" speaker.
     *
     * @param id the id of the speaker to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the speaker, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/speakers/{id}")
    public Mono<ResponseEntity<Speaker>> getSpeaker(@PathVariable Long id) {
        log.debug("REST request to get Speaker : {}", id);
        Mono<Speaker> speaker = speakerRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(speaker);
    }

    /**
     * {@code DELETE  /speakers/:id} : delete the "id" speaker.
     *
     * @param id the id of the speaker to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/speakers/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteSpeaker(@PathVariable Long id) {
        log.debug("REST request to delete Speaker : {}", id);
        return speakerRepository
            .deleteById(id)
            .map(
                result ->
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
            );
    }
}
