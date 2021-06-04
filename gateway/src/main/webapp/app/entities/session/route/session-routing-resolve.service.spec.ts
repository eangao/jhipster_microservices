jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { ISession, Session } from '../session.model';
import { SessionService } from '../service/session.service';

import { SessionRoutingResolveService } from './session-routing-resolve.service';

describe('Service Tests', () => {
  describe('Session routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: SessionRoutingResolveService;
    let service: SessionService;
    let resultSession: ISession | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(SessionRoutingResolveService);
      service = TestBed.inject(SessionService);
      resultSession = undefined;
    });

    describe('resolve', () => {
      it('should return ISession returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSession = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSession).toEqual({ id: 123 });
      });

      it('should return new ISession if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSession = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultSession).toEqual(new Session());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSession = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSession).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
