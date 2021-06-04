jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { SessionService } from '../service/session.service';
import { ISession, Session } from '../session.model';

import { SessionUpdateComponent } from './session-update.component';

describe('Component Tests', () => {
  describe('Session Management Update Component', () => {
    let comp: SessionUpdateComponent;
    let fixture: ComponentFixture<SessionUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let sessionService: SessionService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [SessionUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(SessionUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(SessionUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      sessionService = TestBed.inject(SessionService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const session: ISession = { id: 456 };

        activatedRoute.data = of({ session });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(session));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const session = { id: 123 };
        spyOn(sessionService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ session });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: session }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(sessionService.update).toHaveBeenCalledWith(session);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const session = new Session();
        spyOn(sessionService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ session });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: session }));
        saveSubject.complete();

        // THEN
        expect(sessionService.create).toHaveBeenCalledWith(session);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const session = { id: 123 };
        spyOn(sessionService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ session });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(sessionService.update).toHaveBeenCalledWith(session);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
