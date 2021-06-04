import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ISpeaker, Speaker } from '../speaker.model';

import { SpeakerService } from './speaker.service';

describe('Service Tests', () => {
  describe('Speaker Service', () => {
    let service: SpeakerService;
    let httpMock: HttpTestingController;
    let elemDefault: ISpeaker;
    let expectedResult: ISpeaker | ISpeaker[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      service = TestBed.inject(SpeakerService);
      httpMock = TestBed.inject(HttpTestingController);

      elemDefault = {
        id: 0,
        firstName: 'AAAAAAA',
        lastName: 'AAAAAAA',
        email: 'AAAAAAA',
        twitter: 'AAAAAAA',
        bio: 'AAAAAAA',
      };
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Speaker', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.create(new Speaker()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Speaker', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            firstName: 'BBBBBB',
            lastName: 'BBBBBB',
            email: 'BBBBBB',
            twitter: 'BBBBBB',
            bio: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should partial update a Speaker', () => {
        const patchObject = Object.assign(
          {
            lastName: 'BBBBBB',
            twitter: 'BBBBBB',
          },
          new Speaker()
        );

        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = Object.assign({}, returnedFromService);

        service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PATCH' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Speaker', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            firstName: 'BBBBBB',
            lastName: 'BBBBBB',
            email: 'BBBBBB',
            twitter: 'BBBBBB',
            bio: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Speaker', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addSpeakerToCollectionIfMissing', () => {
        it('should add a Speaker to an empty array', () => {
          const speaker: ISpeaker = { id: 123 };
          expectedResult = service.addSpeakerToCollectionIfMissing([], speaker);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(speaker);
        });

        it('should not add a Speaker to an array that contains it', () => {
          const speaker: ISpeaker = { id: 123 };
          const speakerCollection: ISpeaker[] = [
            {
              ...speaker,
            },
            { id: 456 },
          ];
          expectedResult = service.addSpeakerToCollectionIfMissing(speakerCollection, speaker);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a Speaker to an array that doesn't contain it", () => {
          const speaker: ISpeaker = { id: 123 };
          const speakerCollection: ISpeaker[] = [{ id: 456 }];
          expectedResult = service.addSpeakerToCollectionIfMissing(speakerCollection, speaker);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(speaker);
        });

        it('should add only unique Speaker to an array', () => {
          const speakerArray: ISpeaker[] = [{ id: 123 }, { id: 456 }, { id: 12590 }];
          const speakerCollection: ISpeaker[] = [{ id: 123 }];
          expectedResult = service.addSpeakerToCollectionIfMissing(speakerCollection, ...speakerArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const speaker: ISpeaker = { id: 123 };
          const speaker2: ISpeaker = { id: 456 };
          expectedResult = service.addSpeakerToCollectionIfMissing([], speaker, speaker2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(speaker);
          expect(expectedResult).toContain(speaker2);
        });

        it('should accept null and undefined values', () => {
          const speaker: ISpeaker = { id: 123 };
          expectedResult = service.addSpeakerToCollectionIfMissing([], null, speaker, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(speaker);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
