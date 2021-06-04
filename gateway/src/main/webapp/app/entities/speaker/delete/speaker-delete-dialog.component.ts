import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ISpeaker } from '../speaker.model';
import { SpeakerService } from '../service/speaker.service';

@Component({
  templateUrl: './speaker-delete-dialog.component.html',
})
export class SpeakerDeleteDialogComponent {
  speaker?: ISpeaker;

  constructor(protected speakerService: SpeakerService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.speakerService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
