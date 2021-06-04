jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { SpeakerService } from '../service/speaker.service';
import { ISpeaker, Speaker } from '../speaker.model';
import { ISession } from 'app/entities/session/session.model';
import { SessionService } from 'app/entities/session/service/session.service';

import { SpeakerUpdateComponent } from './speaker-update.component';

describe('Component Tests', () => {
  describe('Speaker Management Update Component', () => {
    let comp: SpeakerUpdateComponent;
    let fixture: ComponentFixture<SpeakerUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let speakerService: SpeakerService;
    let sessionService: SessionService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [SpeakerUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(SpeakerUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(SpeakerUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      speakerService = TestBed.inject(SpeakerService);
      sessionService = TestBed.inject(SessionService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should call Session query and add missing value', () => {
        const speaker: ISpeaker = { id: 456 };
        const sessions: ISession[] = [{ id: 84790 }];
        speaker.sessions = sessions;

        const sessionCollection: ISession[] = [{ id: 24165 }];
        spyOn(sessionService, 'query').and.returnValue(of(new HttpResponse({ body: sessionCollection })));
        const additionalSessions = [...sessions];
        const expectedCollection: ISession[] = [...additionalSessions, ...sessionCollection];
        spyOn(sessionService, 'addSessionToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ speaker });
        comp.ngOnInit();

        expect(sessionService.query).toHaveBeenCalled();
        expect(sessionService.addSessionToCollectionIfMissing).toHaveBeenCalledWith(sessionCollection, ...additionalSessions);
        expect(comp.sessionsSharedCollection).toEqual(expectedCollection);
      });

      it('Should update editForm', () => {
        const speaker: ISpeaker = { id: 456 };
        const sessions: ISession = { id: 82443 };
        speaker.sessions = [sessions];

        activatedRoute.data = of({ speaker });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(speaker));
        expect(comp.sessionsSharedCollection).toContain(sessions);
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const speaker = { id: 123 };
        spyOn(speakerService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ speaker });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: speaker }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(speakerService.update).toHaveBeenCalledWith(speaker);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const speaker = new Speaker();
        spyOn(speakerService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ speaker });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: speaker }));
        saveSubject.complete();

        // THEN
        expect(speakerService.create).toHaveBeenCalledWith(speaker);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const speaker = { id: 123 };
        spyOn(speakerService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ speaker });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(speakerService.update).toHaveBeenCalledWith(speaker);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });

    describe('Tracking relationships identifiers', () => {
      describe('trackSessionById', () => {
        it('Should return tracked Session primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackSessionById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });
    });

    describe('Getting selected relationships', () => {
      describe('getSelectedSession', () => {
        it('Should return option if no Session is selected', () => {
          const option = { id: 123 };
          const result = comp.getSelectedSession(option);
          expect(result === option).toEqual(true);
        });

        it('Should return selected Session for according option', () => {
          const option = { id: 123 };
          const selected = { id: 123 };
          const selected2 = { id: 456 };
          const result = comp.getSelectedSession(option, [selected2, selected]);
          expect(result === selected).toEqual(true);
          expect(result === selected2).toEqual(false);
          expect(result === option).toEqual(false);
        });

        it('Should return option if this Session is not selected', () => {
          const option = { id: 123 };
          const selected = { id: 456 };
          const result = comp.getSelectedSession(option, [selected]);
          expect(result === option).toEqual(true);
          expect(result === selected).toEqual(false);
        });
      });
    });
  });
});
