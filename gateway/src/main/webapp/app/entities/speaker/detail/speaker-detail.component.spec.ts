import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { SpeakerDetailComponent } from './speaker-detail.component';

describe('Component Tests', () => {
  describe('Speaker Management Detail Component', () => {
    let comp: SpeakerDetailComponent;
    let fixture: ComponentFixture<SpeakerDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [SpeakerDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ speaker: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(SpeakerDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(SpeakerDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load speaker on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.speaker).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
