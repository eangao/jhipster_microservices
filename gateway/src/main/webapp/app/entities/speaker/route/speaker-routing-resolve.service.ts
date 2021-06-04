import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISpeaker, Speaker } from '../speaker.model';
import { SpeakerService } from '../service/speaker.service';

@Injectable({ providedIn: 'root' })
export class SpeakerRoutingResolveService implements Resolve<ISpeaker> {
  constructor(protected service: SpeakerService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ISpeaker> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((speaker: HttpResponse<Speaker>) => {
          if (speaker.body) {
            return of(speaker.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Speaker());
  }
}
