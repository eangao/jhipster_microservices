jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { ISpeaker, Speaker } from '../speaker.model';
import { SpeakerService } from '../service/speaker.service';

import { SpeakerRoutingResolveService } from './speaker-routing-resolve.service';

describe('Service Tests', () => {
  describe('Speaker routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: SpeakerRoutingResolveService;
    let service: SpeakerService;
    let resultSpeaker: ISpeaker | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(SpeakerRoutingResolveService);
      service = TestBed.inject(SpeakerService);
      resultSpeaker = undefined;
    });

    describe('resolve', () => {
      it('should return ISpeaker returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSpeaker = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSpeaker).toEqual({ id: 123 });
      });

      it('should return new ISpeaker if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSpeaker = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultSpeaker).toEqual(new Speaker());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSpeaker = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSpeaker).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
