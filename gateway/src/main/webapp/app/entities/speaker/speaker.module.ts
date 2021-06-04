import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';
import { SpeakerComponent } from './list/speaker.component';
import { SpeakerDetailComponent } from './detail/speaker-detail.component';
import { SpeakerUpdateComponent } from './update/speaker-update.component';
import { SpeakerDeleteDialogComponent } from './delete/speaker-delete-dialog.component';
import { SpeakerRoutingModule } from './route/speaker-routing.module';

@NgModule({
  imports: [SharedModule, SpeakerRoutingModule],
  declarations: [SpeakerComponent, SpeakerDetailComponent, SpeakerUpdateComponent, SpeakerDeleteDialogComponent],
  entryComponents: [SpeakerDeleteDialogComponent],
})
export class SpeakerModule {}
