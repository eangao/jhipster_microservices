import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { ISpeaker, Speaker } from '../speaker.model';
import { SpeakerService } from '../service/speaker.service';
import { ISession } from 'app/entities/session/session.model';
import { SessionService } from 'app/entities/session/service/session.service';

@Component({
  selector: 'jhi-speaker-update',
  templateUrl: './speaker-update.component.html',
})
export class SpeakerUpdateComponent implements OnInit {
  isSaving = false;

  sessionsSharedCollection: ISession[] = [];

  editForm = this.fb.group({
    id: [],
    firstName: [null, [Validators.required]],
    lastName: [null, [Validators.required]],
    email: [null, [Validators.required]],
    twitter: [null, [Validators.required]],
    bio: [null, [Validators.required]],
    sessions: [],
  });

  constructor(
    protected speakerService: SpeakerService,
    protected sessionService: SessionService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ speaker }) => {
      this.updateForm(speaker);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const speaker = this.createFromForm();
    if (speaker.id !== undefined) {
      this.subscribeToSaveResponse(this.speakerService.update(speaker));
    } else {
      this.subscribeToSaveResponse(this.speakerService.create(speaker));
    }
  }

  trackSessionById(index: number, item: ISession): number {
    return item.id!;
  }

  getSelectedSession(option: ISession, selectedVals?: ISession[]): ISession {
    if (selectedVals) {
      for (const selectedVal of selectedVals) {
        if (option.id === selectedVal.id) {
          return selectedVal;
        }
      }
    }
    return option;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISpeaker>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(speaker: ISpeaker): void {
    this.editForm.patchValue({
      id: speaker.id,
      firstName: speaker.firstName,
      lastName: speaker.lastName,
      email: speaker.email,
      twitter: speaker.twitter,
      bio: speaker.bio,
      sessions: speaker.sessions,
    });

    this.sessionsSharedCollection = this.sessionService.addSessionToCollectionIfMissing(
      this.sessionsSharedCollection,
      ...(speaker.sessions ?? [])
    );
  }

  protected loadRelationshipsOptions(): void {
    this.sessionService
      .query()
      .pipe(map((res: HttpResponse<ISession[]>) => res.body ?? []))
      .pipe(
        map((sessions: ISession[]) =>
          this.sessionService.addSessionToCollectionIfMissing(sessions, ...(this.editForm.get('sessions')!.value ?? []))
        )
      )
      .subscribe((sessions: ISession[]) => (this.sessionsSharedCollection = sessions));
  }

  protected createFromForm(): ISpeaker {
    return {
      ...new Speaker(),
      id: this.editForm.get(['id'])!.value,
      firstName: this.editForm.get(['firstName'])!.value,
      lastName: this.editForm.get(['lastName'])!.value,
      email: this.editForm.get(['email'])!.value,
      twitter: this.editForm.get(['twitter'])!.value,
      bio: this.editForm.get(['bio'])!.value,
      sessions: this.editForm.get(['sessions'])!.value,
    };
  }
}
