import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISpeaker, getSpeakerIdentifier } from '../speaker.model';

export type EntityResponseType = HttpResponse<ISpeaker>;
export type EntityArrayResponseType = HttpResponse<ISpeaker[]>;

@Injectable({ providedIn: 'root' })
export class SpeakerService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/speakers');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(speaker: ISpeaker): Observable<EntityResponseType> {
    return this.http.post<ISpeaker>(this.resourceUrl, speaker, { observe: 'response' });
  }

  update(speaker: ISpeaker): Observable<EntityResponseType> {
    return this.http.put<ISpeaker>(`${this.resourceUrl}/${getSpeakerIdentifier(speaker) as number}`, speaker, { observe: 'response' });
  }

  partialUpdate(speaker: ISpeaker): Observable<EntityResponseType> {
    return this.http.patch<ISpeaker>(`${this.resourceUrl}/${getSpeakerIdentifier(speaker) as number}`, speaker, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ISpeaker>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISpeaker[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addSpeakerToCollectionIfMissing(speakerCollection: ISpeaker[], ...speakersToCheck: (ISpeaker | null | undefined)[]): ISpeaker[] {
    const speakers: ISpeaker[] = speakersToCheck.filter(isPresent);
    if (speakers.length > 0) {
      const speakerCollectionIdentifiers = speakerCollection.map(speakerItem => getSpeakerIdentifier(speakerItem)!);
      const speakersToAdd = speakers.filter(speakerItem => {
        const speakerIdentifier = getSpeakerIdentifier(speakerItem);
        if (speakerIdentifier == null || speakerCollectionIdentifiers.includes(speakerIdentifier)) {
          return false;
        }
        speakerCollectionIdentifiers.push(speakerIdentifier);
        return true;
      });
      return [...speakersToAdd, ...speakerCollection];
    }
    return speakerCollection;
  }
}
