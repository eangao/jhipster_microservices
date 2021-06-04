import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { SpeakerComponent } from '../list/speaker.component';
import { SpeakerDetailComponent } from '../detail/speaker-detail.component';
import { SpeakerUpdateComponent } from '../update/speaker-update.component';
import { SpeakerRoutingResolveService } from './speaker-routing-resolve.service';

const speakerRoute: Routes = [
  {
    path: '',
    component: SpeakerComponent,
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SpeakerDetailComponent,
    resolve: {
      speaker: SpeakerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SpeakerUpdateComponent,
    resolve: {
      speaker: SpeakerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SpeakerUpdateComponent,
    resolve: {
      speaker: SpeakerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(speakerRoute)],
  exports: [RouterModule],
})
export class SpeakerRoutingModule {}
