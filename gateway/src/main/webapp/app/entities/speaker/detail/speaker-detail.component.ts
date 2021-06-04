import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISpeaker } from '../speaker.model';

@Component({
  selector: 'jhi-speaker-detail',
  templateUrl: './speaker-detail.component.html',
})
export class SpeakerDetailComponent implements OnInit {
  speaker: ISpeaker | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ speaker }) => {
      this.speaker = speaker;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
